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
import org.ow2.proactive.catalog.rest.query.CatalogQueryExpressionBuilder;
import org.ow2.proactive.catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.catalog.rest.query.QueryExpressionContext;
import org.ow2.proactive.catalog.rest.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.UnprocessableEntityException;
import org.ow2.proactive.catalog.rest.service.exception.UnsupportedMediaTypeException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.rest.service.repository.QueryDslWorkflowRevisionRepository;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParser;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserFactory;
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

    /**
     * This value is used to detect whether the full workflow XML payload
     * must be returned to clients or if workflow's metadata only are enough.
     * The value has to be used with query parameter {@code alt}.
     */
    private static final String SUPPORTED_ALT_VALUE = "xml";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CatalogObjectRevisionResourceAssembler workflowRevisionResourceAssembler;

    @Autowired
    private QueryDslWorkflowRevisionRepository queryDslWorkflowRevisionRepository;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Transactional
    public CatalogObjectMetadata createCatalogObjectRevision(String kind, String name, String commitMessage,
            Long bucketId, Optional<Long> catalogObjectId, Optional<String> contentType, byte[] rawObject) {
        try {
            //TODO
            CatalogObjectParser catalogObjectParser = CatalogObjectParserFactory.get().getParser(kind);
            List<KeyValueMetadata> keyValueMetadataListParsed = catalogObjectParser.parse(new ByteArrayInputStream(rawObject));

            return createCatalogObjectRevision(kind,
                                               name,
                                               commitMessage,
                                               bucketId,
                                               catalogObjectId,
                                               contentType,
                                               keyValueMetadataListParsed,
                                               rawObject);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public CatalogObjectMetadata createCatalogObjectRevision(String kind, String name, String commitMessage,
            Long bucketId, Optional<Long> objectId, Optional<String> contentType,
            List<KeyValueMetadata> keyValueMetadataListParsed, byte[] rawObject) {
        Bucket bucket = findBucket(bucketId);

        CatalogObjectRevision catalogObjectRevision;
        String layoutValue = contentType.orElse("");

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
                                                          layoutValue,
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

    public PagedResources listCatalogObjects(Long bucketId, Optional<Long> workflowId, Optional<String> query,
            Pageable pageable, PagedResourcesAssembler assembler) throws QueryExpressionBuilderException {

        findBucket(bucketId);
        Page<CatalogObjectRevision> page;

        if (workflowId.isPresent()) {
            findObjectById(workflowId.get());

            if (query.isPresent() && "".compareTo(query.get().trim()) != 0) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(bucketId,
                                                                                   workflowId.get(),
                                                                                   queryExpressionContext,
                                                                                   pageable);
            } else {
                // it is not required to pass bucket ID since
                // object ID is unique for all buckets
                page = catalogObjectRevisionRepository.getRevisions(workflowId.get(), pageable);
            }
        } else {
            if (query.isPresent()) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(bucketId,
                                                                                          queryExpressionContext,
                                                                                          pageable);
            } else {
                page = catalogObjectRepository.getMostRecentRevisions(bucketId, pageable);
            }
        }

        return assembler.toResource(page, workflowRevisionResourceAssembler);
    }

    private QueryExpressionContext createJpaQueryExpression(String catalogQuery)
            throws QueryExpressionBuilderException {
        CatalogQueryExpressionBuilder builder = new CatalogQueryExpressionBuilder(catalogQuery);

        return builder.build();
    }

    public ResponseEntity<?> getCatalogObject(Long bucketId, Long workflowId, Optional<Long> revisionId,
            Optional<String> alt) {
        findBucket(bucketId);
        findObjectById(workflowId);

        CatalogObjectRevision catalogObjectRevision = getWorkflowRevision(bucketId, workflowId, revisionId);

        if (alt.isPresent()) {
            String altValue = alt.get();

            if (!altValue.equalsIgnoreCase(SUPPORTED_ALT_VALUE)) {
                throw new UnsupportedMediaTypeException("Unsupported media type '" + altValue + "' (only '" +
                                                        SUPPORTED_ALT_VALUE + "' is allowed)");
            }

            byte[] bytes = catalogObjectRevision.getXmlPayload();

            return ResponseEntity.ok()
                                 .contentLength(bytes.length)
                                 .contentType(MediaType.APPLICATION_XML)
                                 .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        }

        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);

        objectMetadata.add(createLink(bucketId, workflowId, catalogObjectRevision));

        return ResponseEntity.ok(objectMetadata);
    }

    public List<CatalogObjectRevision> getWorkflowsRevisions(Long bucketId, List<Long> idList) {
        findBucket(bucketId);
        List<CatalogObjectRevision> workflows = idList.stream()
                                                      .map(workflowId -> getWorkflowRevision(bucketId,
                                                                                             workflowId,
                                                                                             Optional.empty()))
                                                      .collect(Collectors.toList());
        return workflows;
    }

    private CatalogObjectRevision getWorkflowRevision(Long bucketId, Long workflowId, Optional<Long> revisionId) {
        CatalogObjectRevision catalogObjectRevision;

        if (revisionId.isPresent()) {
            catalogObjectRevision = catalogObjectRevisionRepository.getWorkflowRevision(bucketId,
                                                                                        workflowId,
                                                                                        revisionId.get());
        } else {
            catalogObjectRevision = catalogObjectRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
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
     * @param workflowId The id of the CatalogObject containing the CatalogObjectRevision
     * @param revisionId The revision number of the CatalogObject
     * @return The deleted CatalogObjectRevision metadata
     */
    public ResponseEntity<?> delete(Long bucketId, Long workflowId, Optional<Long> revisionId) {
        CatalogObject catalogObject = findObjectById(workflowId);
        CatalogObjectRevision catalogObjectRevision = null;
        if (revisionId.isPresent() && catalogObject.getRevisions().size() > 1) {
            if (revisionId.get() == catalogObject.getLastCommitId()) {
                Iterator iter = catalogObject.getRevisions().iterator();
                catalogObjectRevision = (CatalogObjectRevision) iter.next();
                CatalogObjectRevision newCatalogObjectRevisionReference = (CatalogObjectRevision) iter.next();
                catalogObject.setLastCommitId(newCatalogObjectRevisionReference.getCommitId());
                catalogObjectRevision = catalogObjectRevisionRepository.getWorkflowRevision(bucketId,
                                                                                            workflowId,
                                                                                            catalogObjectRevision.getCommitId());
                catalogObjectRevisionRepository.delete(catalogObjectRevision);
            } else {
                catalogObjectRevision = catalogObjectRevisionRepository.getWorkflowRevision(bucketId,
                                                                                            workflowId,
                                                                                            revisionId.get());
                catalogObjectRevisionRepository.delete(catalogObjectRevision);
            }
        } else {
            catalogObjectRevision = catalogObjectRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
            catalogObjectRepository.delete(catalogObject);
        }
        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(catalogObjectRevision);
        objectMetadata.add(createLink(bucketId, workflowId, catalogObjectRevision));
        return ResponseEntity.ok(objectMetadata);
    }

    public Link createLink(Long bucketId, Long workflowId, CatalogObjectRevision catalogObjectRevision) {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(CatalogObjectRevisionController.class).get(bucketId,
                                                                                                                 workflowId,
                                                                                                                 catalogObjectRevision.getCommitId(),
                                                                                                                 null));

        // alt request parameter name and value is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
