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
package org.ow2.proactive.catalog.rest.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.ow2.proactive.catalog.rest.assembler.CatalogObjectRevisionResourceAssembler;
import org.ow2.proactive.catalog.rest.controller.CatalogObjectRevisionController;
import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.entity.Bucket;
import org.ow2.proactive.catalog.rest.entity.CatalogObject;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.catalog.rest.entity.KeyValueMetadata;
import org.ow2.proactive.catalog.rest.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.UnprocessableEntityException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserFactory;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ActiveEon Team
 */
@Service
public class CatalogObjectRevisionService {

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CatalogObjectRevisionResourceAssembler catalogObjectRevisionResourceAssembler;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    private static final Logger logger = LoggerFactory.getLogger(CatalogObjectRevisionService.class);

    @Transactional
    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String kind, String name,
            String commitMessage, Optional<Long> catalogObjectId, String contentType, byte[] rawObject) {
        try {
            CatalogObjectParserInterface catalogObjectParser = CatalogObjectParserFactory.get().getParser(kind);
            List<KeyValueMetadata> keyValueMetadataListParsed = catalogObjectParser.parse(new ByteArrayInputStream(rawObject));

            return createCatalogObjectRevision(bucketId,
                                               kind,
                                               name,
                                               commitMessage,
                                               catalogObjectId,
                                               contentType,
                                               keyValueMetadataListParsed,
                                               rawObject);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String kind, String name,
            String commitMessage, Optional<Long> objectId, String contentType,
            List<KeyValueMetadata> keyValueMetadataListParsed, byte[] rawObject) {
        Bucket bucket = findBucket(bucketId);

        CatalogObjectRevision catalogObjectRevision;

        CatalogObject catalogObject = null;
        if (objectId.isPresent()) {
            catalogObject = findObjectById(objectId.get());
        } else {
            catalogObject = new CatalogObject(bucket);
        }

        catalogObjectRevision = new CatalogObjectRevision(kind,
                                                          LocalDateTime.now(),
                                                          name,
                                                          commitMessage,
                                                          bucketId,
                                                          contentType,
                                                          rawObject);

        catalogObjectRevision.addKeyValueList(keyValueMetadataListParsed);

        catalogObjectRevision = catalogObjectRevisionRepository.save(catalogObjectRevision);

        catalogObject.addRevision(catalogObjectRevision);

        catalogObjectRepository.save(catalogObject);

        return new CatalogObjectMetadata(catalogObjectRevision);
    }

    protected CatalogObject findObjectById(long objectId) {
        CatalogObject catalogObject = catalogObjectRepository.findOne(objectId);

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException();
        }

        return catalogObject;
    }

    protected Bucket findBucket(Long bucketId) {
        Bucket bucket = bucketRepository.findOne(bucketId);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return bucket;
    }

    public PagedResources listCatalogObjects(Long bucketId, Optional<String> kind, Pageable pageable,
            PagedResourcesAssembler assembler) {

        findBucket(bucketId);

        Page<CatalogObjectRevision> page = catalogObjectRepository.getMostRecentRevisions(bucketId, pageable);

        return assembler.toResource(page, catalogObjectRevisionResourceAssembler);
    }

    public PagedResources listCatalogObjectRevisions(Long bucketId, Long catalogObjectId, Pageable pageable,
            PagedResourcesAssembler assembler) {

        findBucket(bucketId);

        Page<CatalogObjectRevision> page;

        findObjectById(catalogObjectId);

        CatalogObject catalogObject = findObjectById(catalogObjectId);
        page = catalogObjectRevisionRepository.getRevisions(catalogObjectId, pageable);

        return assembler.toResource(page, catalogObjectRevisionResourceAssembler);
    }

    public ResponseEntity<CatalogObjectMetadata> getCatalogObject(Long bucketId, Long objectId,
            Optional<Long> revisionId) {

        findBucket(bucketId);
        CatalogObject catalogObject = findObjectById(objectId);

        CatalogObjectRevision catalogObjectRevision = getCatalogObjectRevision(bucketId, objectId, revisionId);

        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);

        objectMetadata.add(createLink(bucketId, objectId, catalogObjectRevision));

        return ResponseEntity.ok(objectMetadata);
    }

    public ResponseEntity<InputStreamResource> getCatalogObjectRaw(Long bucketId, Long objectId,
            Optional<Long> revisionId) {

        findBucket(bucketId);
        findObjectById(objectId);

        CatalogObjectRevision catalogObjectRevision = getCatalogObjectRevision(bucketId, objectId, revisionId);

        byte[] bytes = catalogObjectRevision.getRawObject();

        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok().contentLength(bytes.length);

        try {
            MediaType mediaType = MediaType.valueOf(catalogObjectRevision.getContentType());
            responseBodyBuilder = responseBodyBuilder.contentType(mediaType);
        } catch (org.springframework.http.InvalidMediaTypeException mimeEx) {
            logger.warn("The wrong content type for object: " + objectId + ", commitId:" + revisionId +
                        ", the contentType: " + catalogObjectRevision.getContentType(), mimeEx);
            mimeEx.printStackTrace();
        }
        return responseBodyBuilder.body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }

    public List<CatalogObjectRevision> getCatalogObjectsRevisions(Long bucketId, List<Long> idList) {
        findBucket(bucketId);
        List<CatalogObjectRevision> revisions = idList.stream()
                                                      .map(objectId -> getCatalogObjectRevision(bucketId,
                                                                                                objectId,
                                                                                                Optional.empty()))
                                                      .collect(Collectors.toList());
        return revisions;
    }

    private CatalogObjectRevision getCatalogObjectRevision(Long bucketId, Long objectId, Optional<Long> revisionId) {
        CatalogObjectRevision catalogObjectRevision;

        if (revisionId.isPresent()) {
            catalogObjectRevision = catalogObjectRevisionRepository.getCatalogObjectRevision(bucketId,
                                                                                             objectId,
                                                                                             revisionId.get());
        } else {
            catalogObjectRevision = catalogObjectRepository.getMostRecentCatalogObjectRevision(bucketId, objectId);
        }

        if (catalogObjectRevision == null) {
            throw new RevisionNotFoundException();
        }

        return catalogObjectRevision;
    }

    /**
     * Generic method to delete either a specific CatalogObjectRevision or a complete CatalogObject
     * and all of its dependencies.
     * Deleting a previous CatalogObjectRevision will not impact the current revision of a CatalogObject.
     * Deleting the current CatalogObjectRevision will:
     * <ul>
     *     <li>also delete the CatalogObject if it has only one CatalogObjectRevision</li>
     *     <li>impact the CatalogObject by referencing the previous CatalogObjectRevision if it had more than one CatalogObjectRevision.</li>
     * </ul>
     * @param bucketId The id of the Bucket containing the CatalogObject
     * @param objectId The id of the CatalogObject containing the CatalogObjectRevision
     * @param revisionId The revision number of the CatalogObject
     * @return The deleted CatalogObjectRevision metadata
     */
    public ResponseEntity<CatalogObjectMetadata> delete(Long bucketId, Long objectId, Optional<Long> revisionId) {
        CatalogObject catalogObject = findObjectById(objectId);
        CatalogObjectRevision catalogObjectRevision = null;
        if (revisionId.isPresent() && catalogObject.getRevisions().size() > 1) {
            if (revisionId.get() == catalogObject.getLastCommitId()) {
                Iterator iter = catalogObject.getRevisions().iterator();
                catalogObjectRevision = (CatalogObjectRevision) iter.next();
                CatalogObjectRevision newCatalogObjectRevisionReference = (CatalogObjectRevision) iter.next();
                catalogObject.setLastCommitId(newCatalogObjectRevisionReference.getCommitId());
                catalogObjectRevision = catalogObjectRevisionRepository.getCatalogObjectRevision(bucketId,
                                                                                                 objectId,
                                                                                                 catalogObjectRevision.getCommitId());
                catalogObjectRevisionRepository.delete(catalogObjectRevision);
            } else {
                catalogObjectRevision = catalogObjectRevisionRepository.getCatalogObjectRevision(bucketId,
                                                                                                 objectId,
                                                                                                 revisionId.get());
                catalogObjectRevisionRepository.delete(catalogObjectRevision);
            }
        } else {
            catalogObjectRevision = catalogObjectRepository.getMostRecentCatalogObjectRevision(bucketId, objectId);
            catalogObjectRepository.delete(catalogObject);
        }
        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);
        objectMetadata.add(createLink(bucketId, objectId, catalogObjectRevision));
        return ResponseEntity.ok(objectMetadata);
    }

    public Link createLink(Long bucketId, Long objectId, CatalogObjectRevision catalogObjectRevision) {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(CatalogObjectRevisionController.class).get(bucketId,
                                                                                                                 objectId,
                                                                                                                 catalogObjectRevision.getCommitId()));

        return new Link(controllerLinkBuilder.toString()).withRel("content");
    }

}
