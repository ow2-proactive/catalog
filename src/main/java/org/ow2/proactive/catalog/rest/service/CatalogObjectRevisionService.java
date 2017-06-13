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
import org.ow2.proactive.catalog.rest.entity.BucketEntity;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.rest.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.rest.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.UnprocessableEntityException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
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
@Transactional
public class CatalogObjectRevisionService {

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private CatalogObjectRevisionResourceAssembler catalogObjectRevisionResourceAssembler;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    private static final Logger logger = LoggerFactory.getLogger(CatalogObjectRevisionService.class);

    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String kind, String name,
            String commitMessage, Optional<Long> catalogObjectId, String contentType, byte[] rawObject) {
        try {
            CatalogObjectParserInterface catalogObjectParser = CatalogObjectParserFactory.get().getParser(kind);
            List<KeyValueMetadataEntity> keyValueMetadataListParsed = catalogObjectParser.parse(new ByteArrayInputStream(rawObject));

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

    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String kind, String name,
            String commitMessage, Optional<Long> objectId, String contentType,
            List<KeyValueMetadataEntity> keyValueMetadataListParsed, byte[] rawObject) {
        BucketEntity bucket = findBucket(bucketId);

        CatalogObjectRevisionEntity catalogObjectRevision;

        CatalogObjectEntity catalogObject = null;
        if (objectId.isPresent()) {
            catalogObject = catalogObjectService.findObjectById(objectId.get());
        } else {
            catalogObject = new CatalogObjectEntity(bucket);
        }

        catalogObjectRevision = new CatalogObjectRevisionEntity(kind,
                                                                LocalDateTime.now(),
                                                                name,
                                                                commitMessage,
                                                                bucketId,
                                                                contentType,
                                                                rawObject);

        catalogObjectRevision.addKeyValueList(keyValueMetadataListParsed);

        catalogObjectRevision = catalogObjectRevisionRepository.save(catalogObjectRevision);

        catalogObject.addRevision(catalogObjectRevision);

        catalogObjectService.save(catalogObject);

        return new CatalogObjectMetadata(catalogObjectRevision);
    }

    protected BucketEntity findBucket(Long bucketId) {
        BucketEntity bucket = bucketRepository.findOne(bucketId);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return bucket;
    }

    public PagedResources listCatalogObjects(Long bucketId, Optional<String> kind, Pageable pageable,
            PagedResourcesAssembler assembler) {

        findBucket(bucketId);

        Page<CatalogObjectRevisionEntity> page = catalogObjectService.getMostRecentRevisions(bucketId, pageable, kind);

        return assembler.toResource(page, catalogObjectRevisionResourceAssembler);
    }

    public PagedResources listCatalogObjectRevisions(Long bucketId, Long catalogObjectId, Pageable pageable,
            PagedResourcesAssembler assembler) {

        findBucket(bucketId);
        catalogObjectService.findObjectById(catalogObjectId);

        Page<CatalogObjectRevisionEntity> page = catalogObjectRevisionRepository.getRevisions(catalogObjectId,
                                                                                              pageable);

        return assembler.toResource(page, catalogObjectRevisionResourceAssembler);
    }

    public ResponseEntity<CatalogObjectMetadata> getCatalogObject(Long bucketId, Long objectId,
            Optional<Long> revisionId) {

        findBucket(bucketId);
        catalogObjectService.findObjectById(objectId);

        CatalogObjectRevisionEntity catalogObjectRevision = getCatalogObjectRevision(bucketId, objectId, revisionId);

        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);

        objectMetadata.add(createLink(bucketId, objectId, catalogObjectRevision));

        return ResponseEntity.ok(objectMetadata);
    }

    public ResponseEntity<InputStreamResource> getCatalogObjectRaw(Long bucketId, Long objectId,
            Optional<Long> revisionId) {

        findBucket(bucketId);
        catalogObjectService.findObjectById(objectId);

        CatalogObjectRevisionEntity catalogObjectRevision = getCatalogObjectRevision(bucketId, objectId, revisionId);

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

    public List<CatalogObjectRevisionEntity> getCatalogObjectsRevisions(Long bucketId, List<Long> idList) {
        findBucket(bucketId);
        List<CatalogObjectRevisionEntity> revisions = idList.stream()
                                                            .map(objectId -> getCatalogObjectRevision(bucketId,
                                                                                                      objectId,
                                                                                                      Optional.empty()))
                                                            .collect(Collectors.toList());
        return revisions;
    }

    private CatalogObjectRevisionEntity getCatalogObjectRevision(Long bucketId, Long objectId,
            Optional<Long> revisionId) {
        CatalogObjectRevisionEntity catalogObjectRevision;

        if (revisionId.isPresent()) {
            catalogObjectRevision = catalogObjectRevisionRepository.getCatalogObjectRevision(bucketId,
                                                                                             objectId,
                                                                                             revisionId.get());
        } else {
            catalogObjectRevision = catalogObjectService.getMostRecentCatalogObjectRevision(bucketId, objectId);
        }

        if (catalogObjectRevision == null) {
            throw new RevisionNotFoundException();
        }

        return catalogObjectRevision;
    }

    /**
     * Generic method to delete either a specific CatalogObjectRevisionEntity or a complete CatalogObjectEntity
     * and all of its dependencies.
     * Deleting a previous CatalogObjectRevisionEntity will not impact the current revision of a CatalogObjectEntity.
     * Deleting the current CatalogObjectRevisionEntity will:
     * <ul>
     *     <li>also delete the CatalogObjectEntity if it has only one CatalogObjectRevisionEntity</li>
     *     <li>impact the CatalogObjectEntity by referencing the previous CatalogObjectRevisionEntity if it had more than one CatalogObjectRevisionEntity.</li>
     * </ul>
     * @param bucketId The id of the BucketEntity containing the CatalogObjectEntity
     * @param objectId The id of the CatalogObjectEntity containing the CatalogObjectRevisionEntity
     * @param revisionId The revision number of the CatalogObjectEntity
     * @return The deleted CatalogObjectRevisionEntity metadata
     */
    public ResponseEntity<CatalogObjectMetadata> delete(Long bucketId, Long objectId, Optional<Long> revisionId) {
        CatalogObjectEntity catalogObject = catalogObjectService.findObjectById(objectId);
        CatalogObjectRevisionEntity catalogObjectRevision = null;
        if (revisionId.isPresent() && catalogObject.getRevisions().size() > 1) {
            if (revisionId.get() == catalogObject.getLastCommitId()) {
                Iterator iter = catalogObject.getRevisions().iterator();
                catalogObjectRevision = (CatalogObjectRevisionEntity) iter.next();
                CatalogObjectRevisionEntity newCatalogObjectRevisionReference = (CatalogObjectRevisionEntity) iter.next();
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
            catalogObjectRevision = catalogObjectService.getMostRecentCatalogObjectRevision(bucketId, objectId);
            catalogObjectService.delete(catalogObject);
        }
        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);
        objectMetadata.add(createLink(bucketId, objectId, catalogObjectRevision));
        return ResponseEntity.ok(objectMetadata);
    }

    public Link createLink(Long bucketId, Long objectId, CatalogObjectRevisionEntity catalogObjectRevision) {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(CatalogObjectRevisionController.class).getRaw(bucketId,
                                                                                                                    objectId,
                                                                                                                    catalogObjectRevision.getCommitId()));

        return new Link(controllerLinkBuilder.toString()).withRel("content");
    }

}
