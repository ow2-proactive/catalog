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
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserResult;
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
    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, Optional<Long> catalogObjectId,
            byte[] catalogObjectJsonContent, Optional<String> layout) {
        try {
            //TODO
            CatalogObjectParser catalogObjectParser = CatalogObjectParserFactory.get().getParser(null);

            return createCatalogObjectRevision(bucketId,
                                               catalogObjectId,
                                               catalogObjectParser.parse(new ByteArrayInputStream(catalogObjectJsonContent)),
                                               layout,
                                               catalogObjectJsonContent);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, Optional<Long> objectId,
            CatalogObjectParserResult catalogObjectParserResult, Optional<String> layout,
            byte[] catalogObjectXmlContent) {
        Bucket bucket = findBucket(bucketId);

        CatalogObject catalogObject = null;
        CatalogObjectRevision catalogObjectRevision;
        String layoutValue = layout.orElse("");

        long revisionNumber = 1;

        if (objectId.isPresent()) {
            catalogObject = findWorkflow(objectId.get());
            revisionNumber = catalogObject.getLastRevisionId() + 1;
        }

        catalogObjectRevision = new CatalogObjectRevision(catalogObjectParserResult.getKind(),
                                                          bucketId,
                                                          revisionNumber,
                                                          catalogObjectParserResult.getJobName(),
                                                          catalogObjectParserResult.getProjectName(),
                                                          LocalDateTime.now(),
                                                          layoutValue,
                                                          catalogObjectXmlContent);

        catalogObjectRevision.addKeyValueList(catalogObjectParserResult.getKeyValueList());

        catalogObjectRevision = catalogObjectRevisionRepository.save(catalogObjectRevision);

        if (!objectId.isPresent()) {
            catalogObject = new CatalogObject(bucket, catalogObjectRevision);
        } else {
            catalogObject.addRevision(catalogObjectRevision);
        }

        catalogObjectRepository.save(catalogObject);

        return new CatalogObjectMetadata(catalogObjectRevision);
    }

    protected CatalogObject findWorkflow(long workflowId) {
        CatalogObject catalogObject = catalogObjectRepository.findOne(workflowId);

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
            findWorkflow(workflowId.get());

            if (query.isPresent() && "".compareTo(query.get().trim()) != 0) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(bucketId,
                                                                                   workflowId.get(),
                                                                                   queryExpressionContext,
                                                                                   pageable);
            } else {
                // it is not required to pass bucket ID since
                // workflow ID is unique for all buckets
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
        findWorkflow(workflowId);

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
        CatalogObject catalogObject = findWorkflow(workflowId);
        CatalogObjectRevision catalogObjectRevision = null;
        if (revisionId.isPresent() && catalogObject.getRevisions().size() > 1) {
            if (revisionId.get() == catalogObject.getLastRevisionId()) {
                Iterator iter = catalogObject.getRevisions().iterator();
                catalogObjectRevision = (CatalogObjectRevision) iter.next();
                CatalogObjectRevision newCatalogObjectRevisionReference = (CatalogObjectRevision) iter.next();
                catalogObject.setLastRevisionId(newCatalogObjectRevisionReference.getRevisionId());
                catalogObjectRevision = catalogObjectRevisionRepository.getWorkflowRevision(bucketId,
                                                                                            workflowId,
                                                                                            catalogObjectRevision.getRevisionId());
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
                                                                                                                 catalogObjectRevision.getRevisionId(),
                                                                                                                 null));

        // alt request parameter name and value is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
