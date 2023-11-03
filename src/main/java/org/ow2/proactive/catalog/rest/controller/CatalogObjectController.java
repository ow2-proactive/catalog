/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.catalog.rest.controller;

import static org.ow2.proactive.catalog.dto.AssociationStatus.ALL;
import static org.ow2.proactive.catalog.dto.AssociationStatus.UNPLANNED;
import static org.ow2.proactive.catalog.util.AccessType.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.ow2.proactive.catalog.dto.*;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.ow2.proactive.catalog.util.LinkUtil;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.name.validator.ObjectNameValidator;
import org.ow2.proactive.catalog.util.name.validator.TagsValidator;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@RestController
@Log4j2
@RequestMapping(value = "/buckets")
public class CatalogObjectController {

    private static final String REQUEST_API_QUERY = "/{bucketName}/resources";

    private static final String ANONYMOUS = "anonymous";

    private static final String ACTION = "[Action] ";

    public static final String JOB_PLANNER_LABEL = "job_planner";

    public static final String JOB_PLANNER_ASSOCIATION_STATUS_KEY = "association_status";

    @Autowired
    private GrantRightsService grantRightsService;

    @Autowired
    BucketGrantService bucketGrantService;

    @Autowired
    CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private RawObjectResponseCreator rawObjectResponseCreator;

    @Autowired
    private JobPlannerService jobPlannerService;

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    private static final long MAXVALUE = Integer.MAX_VALUE;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @Operation(summary = "Creates a new catalog object")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket not found"),
                            @ApiResponse(responseCode = "422", description = "Invalid file content supplied") })
    @RequestMapping(value = REQUEST_API_QUERY, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }, method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogObjectMetadataList create(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "Name of the object or empty when a ZIP archive is uploaded.<br />All objects inside an archive will be stored inside the catalog.", schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @RequestParam(required = false) Optional<String> name,
            @Parameter(description = "Project of the object") @RequestParam(value = "projectName", required = false, defaultValue = "") Optional<String> projectName,
            @Parameter(description = "List of comma separated tags of the object", schema = @Schema(pattern = TagsValidator.TAGS_PATTERN)) @RequestParam(value = "tags", required = false, defaultValue = "") Optional<String> tags,
            @Parameter(description = "Kind of the new object", required = true, schema = @Schema(pattern = KindAndContentTypeValidator.VALID_KIND_NAME_PATTERN)) @RequestParam(value = "kind", required = true) String kind,
            @Parameter(description = "Commit message", required = true) @RequestParam String commitMessage,
            @Parameter(description = "The Content-Type of CatalogRawObject - MIME type", schema = @Schema(pattern = KindAndContentTypeValidator.VALID_KIND_NAME_PATTERN), required = true) @RequestParam String objectContentType,
            @Parameter(description = "The content of CatalogRawObject", required = true) @RequestPart(value = "file") MultipartFile file)
            throws IOException, NotAuthenticatedException, AccessDeniedException {

        // Check Grants
        AuthenticatedUser user = null;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getBucketRights(user, bucketName), write)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        String userName = "";
        if (user != null) {
            userName = user.getName();
            initiator = userName;
        }
        CatalogObjectMetadataList catalogObjectMetadataList;
        if (name.isPresent()) {
            CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucketName,
                                                                                           name.get(),
                                                                                           projectName.orElse(""),
                                                                                           tags.orElse(""),
                                                                                           kind,
                                                                                           commitMessage,
                                                                                           userName,
                                                                                           objectContentType,
                                                                                           file.getBytes(),
                                                                                           FilenameUtils.getExtension(file.getOriginalFilename()));

            catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));
            catalogObject.add(LinkUtil.createRelativeLink(bucketName, catalogObject.getName()));

            catalogObjectMetadataList = new CatalogObjectMetadataList(catalogObject);

        } else {
            List<CatalogObjectMetadata> catalogObjects = catalogObjectService.createCatalogObjects(bucketName,
                                                                                                   projectName.orElse(""),
                                                                                                   tags.orElse(""),
                                                                                                   kind,
                                                                                                   commitMessage,
                                                                                                   userName,
                                                                                                   file.getBytes());

            for (CatalogObjectMetadata catalogObject : catalogObjects) {
                catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));
                catalogObject.add(LinkUtil.createRelativeLink(bucketName, catalogObject.getName()));
            }

            catalogObjectMetadataList = new CatalogObjectMetadataList(catalogObjects);
        }
        if (name.isPresent()) {
            log.info(ACTION + initiator + " created a new catalog object " + name.get() + " inside bucket " +
                     bucketName);
        } else {
            log.info(ACTION + initiator + " created new catalog objects from archive inside bucket " + bucketName);
        }
        return catalogObjectMetadataList;
    }

    @Operation(summary = "Lists all kinds for all objects")
    @RequestMapping(value = "/kinds", method = GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> listKinds() {
        return catalogObjectService.getKinds();
    }

    @Operation(summary = "Lists all Content-Types for all objects")
    @RequestMapping(value = "/content-types", method = GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> listContentTypes() {
        return catalogObjectService.getContentTypes();
    }

    @Operation(summary = "Lists all tags values for all objects stored in the catalog")
    @RequestMapping(value = "/tags", method = GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> listObjectTags() {
        return catalogObjectService.getObjectTags();
    }

    @Operation(summary = "Lists catalog object name references by kind and Content-Type")
    @ApiResponses(value = { @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(value = "/references", method = GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<CatalogObjectNameReference> listCatalogObjectNameReference(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "Filter according to kind", required = false) @RequestParam(value = "kind", required = false) Optional<String> kind,
            @Parameter(description = "Filter according to Content-Type", required = false) @RequestParam(value = "contentType", required = false) Optional<String> contentType) {
        return catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(sessionIdRequired,
                                                                                                 sessionId,
                                                                                                 kind,
                                                                                                 contentType);
    }

    @Operation(summary = "Update a catalog object metadata, like kind, Content-Type, project name and tags")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket, object or revision not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"),
                            @ApiResponse(responseCode = "400", description = "Wrong specified parameters: at least one should be present") })
    @RequestMapping(value = REQUEST_API_QUERY + "/{name:.+}", method = PUT)
    @ResponseStatus(HttpStatus.OK)
    public CatalogObjectMetadata updateObjectMetadata(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name,
            @Parameter(description = "The new kind of an object", schema = @Schema(pattern = KindAndContentTypeValidator.VALID_KIND_NAME_PATTERN)) @RequestParam(value = "kind", required = false) Optional<String> kind,
            @Parameter(description = "The new Content-Type of an object - MIME type", schema = @Schema(pattern = KindAndContentTypeValidator.VALID_KIND_NAME_PATTERN)) @RequestParam(value = "contentType", required = false) Optional<String> contentType,
            @Parameter(description = "The new project name of an object") @RequestParam(value = "projectName", required = false) Optional<String> projectName,
            @Parameter(description = "List of comma separated tags of the object", schema = @Schema(pattern = TagsValidator.TAGS_PATTERN)) @RequestParam(value = "tags", required = false) Optional<String> tags)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        AuthenticatedUser user;
        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), write)) {
                throw new CatalogObjectGrantAccessException(bucketName, name);
            }
            initiator = user.getName();

        } else {
            user = AuthenticatedUser.EMPTY;
        }

        log.info(ACTION + initiator + " updated the metadata of catalog object " + name + " inside bucket " +
                 bucketName);

        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.updateObjectMetadata(bucketName,
                                                                                                name,
                                                                                                kind,
                                                                                                contentType,
                                                                                                projectName,
                                                                                                tags,
                                                                                                user.getName());
        return catalogObjectMetadata;
    }

    @Operation(summary = "Gets a catalog object's metadata by IDs", description = "Note: returns metadata associated to the latest revision of the catalog object.")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket or catalog object not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = REQUEST_API_QUERY + "/{name:.+}", method = GET)
    public CatalogObjectMetadata get(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name)
            throws MalformedURLException, UnsupportedEncodingException, NotAuthenticatedException,
            AccessDeniedException {
        String objectRights = "";
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            // Check Grants
            objectRights = grantRightsService.getCatalogObjectRights(user, bucketName, name);
            if (!AccessTypeHelper.satisfy(objectRights, read)) {
                throw new CatalogObjectGrantAccessException(bucketName, name);
            }
        }

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectMetadata(bucketName, name);
        if (sessionIdRequired) {
            metadata.setRights(objectRights);
        }
        metadata.add(LinkUtil.createLink(bucketName, metadata.getName()));
        metadata.add(LinkUtil.createRelativeLink(bucketName, metadata.getName()));
        if (sessionIdRequired) {
            AssociatedObject associatedObject = jobPlannerService.getAssociatedObject(sessionId, bucketName, name);
            addAssociationStatus(metadata, associatedObject);
        }
        return metadata;
    }

    @Operation(summary = "Gets the raw content of the last revision of a catalog object")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ok"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"),
                            @ApiResponse(responseCode = "404", description = "Bucket, catalog object or catalog object revision not found") })

    @RequestMapping(value = REQUEST_API_QUERY + "/{name:.+}/raw", method = GET, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getRaw(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), read)) {
                throw new CatalogObjectGrantAccessException(bucketName, name);
            }

        }

        CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucketName, name);
        return rawObjectResponseCreator.createRawObjectResponse(rawObject);

    }

    @Operation(summary = "Gets dependencies (dependsOn and calledBy) of a catalog object")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Ok"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied"),
                            @ApiResponse(responseCode = "404", description = "Bucket, catalog object or catalog object revision not found") })

    @RequestMapping(value = REQUEST_API_QUERY + "/{name:.+}/dependencies", method = GET, produces = "application/json")
    public CatalogObjectDependencies getDependencies(
            @Parameter(description = "sessionID", required = false) @RequestHeader(value = "sessionID", required = false) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the existing Object", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), read)) {
                throw new CatalogObjectGrantAccessException(bucketName, name);
            }
        }

        return catalogObjectService.getObjectDependencies(bucketName, name);
    }

    @Operation(summary = "Lists catalog objects metadata", description = "Note: Returns catalog objects metadata associated to the latest revision.")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket not found"),
                            @ApiResponse(responseCode = "206", description = "Missing object"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(value = REQUEST_API_QUERY, method = GET)
    public ResponseEntity<List<CatalogObjectMetadata>> list(
            @Parameter(description = "sessionID") @RequestHeader(value = "sessionID", required = false) String sessionId,
            @PathVariable String bucketName,
            @Parameter(description = "Filter according to kind(s).<br />Multiple kinds can be specified using comma separators") @RequestParam(required = false) Optional<String> kind,
            @Parameter(description = "Filter according to Content-Type.") @RequestParam(required = false) Optional<String> contentType,
            @Parameter(description = "Filter according to Object Name.") @RequestParam(value = "objectName", required = false) Optional<String> objectNameFilter,
            @Parameter(description = "Filter according to Object Tag.") @RequestParam(value = "objectTag", required = false) Optional<String> objectTagFilter,
            @Parameter(description = "Filter according to Job-Planner association status.<br />If enabled, only objects for which a job-planner association exists with the provided status will be returned.<br />Parameter can be ALL, PLANNED, DEACTIVATED, FAILED or UNPLANNED.<br />ALL will filter objects which have an association with any status.<br />UNPLANNED will filter objects without any association.", schema = @Schema(type = "string", allowableValues = { "ALL",
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                "PLANNED", "DEACTIVATED", "FAILED", "UNPLANNED" })) @RequestParam(value = "associationStatus", required = false) Optional<String> associationStatusFilter,
            @Parameter(description = "Include only objects whose project name contains the given string.") @RequestParam(value = "projectName", required = false) Optional<String> projectNameFilter,
            @Parameter(description = "Include only objects whose last commit belong to the given user.") @RequestParam(value = "lastCommitBy", required = false) Optional<String> lastCommitBy,
            @Parameter(description = "Include only objects whose last commit time is greater than the given EPOCH time.") @RequestParam(value = "lastCommitTimeGreater", required = false) Optional<Long> lastCommitTimeGreater,
            @Parameter(description = "Include only objects whose last commit time is less than the given EPOCH time.") @RequestParam(value = "lastCommitTimeLessThan", required = false) Optional<Long> lastCommitTimeLessThan,
            @Parameter(description = "Give a list of name separated by comma to get them in an archive", array = @ArraySchema(schema = @Schema())) @RequestParam(value = "listObjectNamesForArchive", required = false) Optional<List<String>> names,
            @Parameter(description = "Page number", required = false) @RequestParam(defaultValue = "0", value = "pageNo") int pageNo,
            @Parameter(description = "Page size", required = false) @RequestParam(defaultValue = MAXVALUE +
                                                                                                 "", value = "pageSize") int pageSize,
            HttpServletResponse response)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        // Check Grants
        AuthenticatedUser user;
        boolean isPublicBucket = true;
        List<CatalogObjectGrantMetadata> catalogObjectsGrants = new LinkedList<>(); // the user's all grants for the catalog objects in the bucket
        List<BucketGrantMetadata> bucketGrants = new LinkedList<>(); // the user's all grants for the bucket
        String bucketRights = "";
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }
            user = restApiAccessService.getUserFromSessionId(sessionId);
            BucketMetadata bucket = bucketService.getBucketMetadata(bucketName);
            isPublicBucket = GrantHelper.isPublicBucket(bucket.getOwner());
            if (isPublicBucket) {
                bucketRights = admin.name();
            } else {
                bucketGrants = bucketGrantService.getUserBucketGrants(user, bucketName);
                grantRightsService.addGrantsForBucketOwner(user, bucket.getName(), bucket.getOwner(), bucketGrants);
                catalogObjectsGrants = catalogObjectGrantService.getObjectsGrantsInABucket(user, bucketName);
                bucketRights = grantRightsService.getBucketRights(bucketGrants);
            }

            if (!grantRightsService.isBucketAccessible(bucketRights, bucketGrants, catalogObjectsGrants)) {
                throw new BucketGrantAccessException(bucketName);
            }
        }

        //transform empty String into an empty Optional
        kind = kind.filter(s -> !s.isEmpty());
        contentType = contentType.filter(s -> !s.isEmpty());
        objectNameFilter = objectNameFilter.filter(s -> !s.isEmpty());
        objectTagFilter = objectTagFilter.filter(s -> !s.isEmpty());
        projectNameFilter = projectNameFilter.filter(s -> !s.isEmpty());
        lastCommitBy = lastCommitBy.filter(s -> !s.isEmpty());
        if (names.isPresent()) {
            ZipArchiveContent content = catalogObjectService.getCatalogObjectsAsZipArchive(bucketName, names.get());
            return getResponseAsArchive(content, response);
        } else {
            List<CatalogObjectMetadata> metadataList = catalogObjectService.listCatalogObjects(Collections.singletonList(bucketName),
                                                                                               kind,
                                                                                               contentType,
                                                                                               objectNameFilter,
                                                                                               objectTagFilter,
                                                                                               projectNameFilter,
                                                                                               lastCommitBy,
                                                                                               lastCommitTimeGreater,
                                                                                               lastCommitTimeLessThan,
                                                                                               pageNo,
                                                                                               pageSize);

            if (sessionIdRequired && !isPublicBucket) {
                // remove all objects that the user shouldn't have access according to the grants specification.
                grantRightsService.removeInaccessibleObjectsInBucket(metadataList, bucketGrants, catalogObjectsGrants);
            }

            Optional<String> userSpecificBucketRights = GrantHelper.filterFirstUserSpecificGrant(bucketGrants)
                                                                   .map(BucketGrantMetadata::getAccessType);
            for (CatalogObjectMetadata catalogObject : metadataList) {
                catalogObject.add(LinkUtil.createLink(bucketName, catalogObject.getName()));
                catalogObject.add(LinkUtil.createRelativeLink(bucketName, catalogObject.getName()));
                if (sessionIdRequired) {
                    List<CatalogObjectGrantMetadata> objectsGrants = GrantHelper.filterObjectGrants(catalogObjectsGrants,
                                                                                                    catalogObject.getName());
                    catalogObject.setRights(grantRightsService.getCatalogObjectRights(isPublicBucket,
                                                                                      bucketRights,
                                                                                      userSpecificBucketRights,
                                                                                      objectsGrants));
                }
            }
            Optional<AssociatedObjectsByBucket> associatedObjectsByBucketOptional = Optional.empty();
            if (sessionIdRequired) {
                List<AssociatedObjectsByBucket> associatedObjectsByBucketList = jobPlannerService.getAssociatedObjects(sessionId);
                associatedObjectsByBucketOptional = associatedObjectsByBucketList.stream()
                                                                                 .filter(object -> bucketName.equals(object.getBucketName()))
                                                                                 .findFirst();
                if (associatedObjectsByBucketOptional.isPresent()) {
                    AssociatedObjectsByBucket associatedObjects = associatedObjectsByBucketOptional.get();
                    for (CatalogObjectMetadata catalogObject : metadataList) {
                        AssociatedObject associatedObject = associatedObjects.findAssociatedObject(catalogObject.getName());
                        addAssociationStatus(catalogObject, associatedObject);
                    }
                }
            }
            if (sessionIdRequired && associationStatusFilter.isPresent()) {
                if (!associatedObjectsByBucketOptional.isPresent()) {
                    if (!UNPLANNED.equalsIgnoreCase(associationStatusFilter.get())) {
                        return ResponseEntity.ok(Collections.emptyList());
                    }
                    // if UNPLANNED and no objects are associated, we return the full list
                } else {
                    AssociatedObjectsByBucket associatedObjectsByBucket = associatedObjectsByBucketOptional.get();

                    metadataList = metadataList.stream()
                                               .filter(metadata -> isAssociatedInJobPlanner(metadata,
                                                                                            associationStatusFilter.get(),
                                                                                            associatedObjectsByBucket))
                                               .collect(Collectors.toList());
                }
            }
            Collections.sort(metadataList);
            return ResponseEntity.ok(metadataList);
        }
    }

    private void addAssociationStatus(CatalogObjectMetadata catalogObject, AssociatedObject associatedObject) {
        catalogObject.getMetadataList()
                     .add(new Metadata(JOB_PLANNER_ASSOCIATION_STATUS_KEY,
                                       associatedObject.getStatuses().isEmpty() ? UNPLANNED
                                                                                : associatedObject.getStatuses()
                                                                                                  .stream()
                                                                                                  .map(status -> status.name())
                                                                                                  .collect(Collectors.joining(",")),
                                       JOB_PLANNER_LABEL));
    }

    private boolean isAssociatedInJobPlanner(CatalogObjectMetadata objectMetadata, String expectedStatus,
            AssociatedObjectsByBucket associatedObjectsByBucket) {
        switch (expectedStatus) {
            case ALL:
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .anyMatch(associatedObject -> associatedObject.getObjectName()
                                                                                              .equals(objectMetadata.getName()) &&
                                                                              !associatedObject.getStatuses()
                                                                                               .isEmpty());
            case UNPLANNED:
                // object must not have a job-planner association
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .noneMatch(associatedObject -> associatedObject.getObjectName()
                                                                                               .equals(objectMetadata.getName()));
            default:
                AssociationStatus associationStatus = AssociationStatus.convert(expectedStatus);
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .anyMatch(associatedObject -> associatedObject.getObjectName()
                                                                                              .equals(objectMetadata.getName()) &&
                                                                              associatedObject.getStatuses()
                                                                                              .contains(associationStatus));
        }
    }

    @Operation(summary = "Delete a catalog object", description = "Note: delete the entire catalog object as well as its revisions. Returns the deleted CatalogObject's metadata.")
    @ApiResponses(value = { @ApiResponse(responseCode = "404", description = "Bucket or object not found"),
                            @ApiResponse(responseCode = "401", description = "User not authenticated"),
                            @ApiResponse(responseCode = "403", description = "Permission denied") })
    @RequestMapping(value = REQUEST_API_QUERY + "/{name:.+}", method = DELETE)
    public CatalogObjectMetadata delete(
            @Parameter(description = "sessionID", required = true) @RequestHeader(value = "sessionID", required = true) String sessionId,
            @Parameter(description = "The name of the existing Bucket", required = true, schema = @Schema(pattern = BucketNameValidator.VALID_BUCKET_NAME_PATTERN)) @PathVariable String bucketName,
            @Parameter(description = "The name of the Object to delete", required = true, schema = @Schema(pattern = ObjectNameValidator.VALID_OBJECT_NAME_PATTERN)) @PathVariable String name)
            throws UnsupportedEncodingException, NotAuthenticatedException, AccessDeniedException {

        String initiator = ANONYMOUS;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            AuthenticatedUser user = restApiAccessService.getUserFromSessionId(sessionId);
            if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user, bucketName, name), write)) {
                throw new CatalogObjectGrantAccessException(bucketName, name);
            }
            initiator = user.getName();

        }
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.delete(bucketName, name);
        log.info(ACTION + initiator + " deleted the catalog object " + name + " inside bucket " + bucketName);
        return catalogObjectMetadata;
    }

    private ResponseEntity<List<CatalogObjectMetadata>> getResponseAsArchive(ZipArchiveContent zipArchiveContent,
            HttpServletResponse response) {
        HttpStatus status;
        if (zipArchiveContent.isPartial()) {
            status = HttpStatus.PARTIAL_CONTENT;
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        } else {
            status = HttpStatus.OK;
            response.setStatus(HttpServletResponse.SC_OK);
        }

        response.setContentType(ZIP_CONTENT_TYPE);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
        try {
            response.getOutputStream().write(zipArchiveContent.getContent());
            response.getOutputStream().flush();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return new ResponseEntity<>(status);
    }
}
