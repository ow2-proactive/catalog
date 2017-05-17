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
package org.ow2.proactive.workflow_catalog.rest.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.controller.WorkflowRevisionController;
import org.ow2.proactive.workflow_catalog.rest.dto.ObjectMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.entity.CatalogObject;
import org.ow2.proactive.workflow_catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionContext;
import org.ow2.proactive.workflow_catalog.rest.query.WorkflowCatalogQueryExpressionBuilder;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.GenericInformationRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.QueryDslWorkflowRevisionRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.VariableRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRevisionRepository;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParser;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
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

import com.google.common.collect.ImmutableMap;


/**
 * @author ActiveEon Team
 */
@Service
public class WorkflowRevisionService {

    /**
     * This value is used to detect whether the full workflow XML payload
     * must be returned to clients or if workflow's metadata only are enough.
     * The value has to be used with query parameter {@code alt}.
     */
    private static final String SUPPORTED_ALT_VALUE = "xml";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private WorkflowRevisionResourceAssembler workflowRevisionResourceAssembler;

    @Autowired
    private GenericInformationRepository genericInformationRepository;

    @Autowired
    private QueryDslWorkflowRevisionRepository queryDslWorkflowRevisionRepository;

    @Autowired
    private VariableRepository variableRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowRevisionRepository workflowRevisionRepository;

    @Transactional
    public ObjectMetadata createWorkflowRevision(String kind, Long bucketId, Optional<Long> workflowId,
                                                 byte[] proActiveWorkflowXmlContent, Optional<String> layout) {
        try {
            ProActiveWorkflowParser proActiveWorkflowParser = new ProActiveWorkflowParser(new ByteArrayInputStream(proActiveWorkflowXmlContent));

            return createWorkflowRevision(kind, bucketId,
                                          workflowId,
                                          proActiveWorkflowParser.parse(),
                                          layout,
                                          proActiveWorkflowXmlContent);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public ObjectMetadata createWorkflowRevision(String kind, Long bucketId, Optional<Long> workflowId,
                                                 ProActiveWorkflowParserResult proActiveWorkflowParserResult, Optional<String> layout,
                                                 byte[] proActiveWorkflowXmlContent) {
        Bucket bucket = findBucket(bucketId);

        CatalogObject catalogObject = null;
        CatalogObjectRevision catalogObjectRevision;
        String layoutValue = layout.orElse("");

        long revisionNumber = 1;

        if (workflowId.isPresent()) {
            catalogObject = findWorkflow(workflowId.get());
            revisionNumber = catalogObject.getLastRevisionId() + 1;
        }

        catalogObjectRevision = new CatalogObjectRevision(kind, bucketId,
                                                revisionNumber,
                                                proActiveWorkflowParserResult.getJobName(),
                                                proActiveWorkflowParserResult.getProjectName(),
                                                LocalDateTime.now(),
                                                layoutValue,
                                                proActiveWorkflowXmlContent);

        catalogObjectRevision.addKeyValueList();

        catalogObjectRevision.addGenericInformation(createEntityGenericInformation(proActiveWorkflowParserResult.getGenericInformation()));

        catalogObjectRevision.addVariables(createEntityVariable(proActiveWorkflowParserResult.getVariables()));

        catalogObjectRevision = workflowRevisionRepository.save(catalogObjectRevision);

        if (!workflowId.isPresent()) {
            catalogObject = new CatalogObject(bucket, catalogObjectRevision);
        } else {
            catalogObject.addRevision(catalogObjectRevision);
        }

        workflowRepository.save(catalogObject);

        return new ObjectMetadata(catalogObjectRevision);
    }

    protected List<GenericInformation> createEntityGenericInformation(ImmutableMap<String, String> genericInformation) {
        return genericInformation.entrySet()
                                 .stream()
                                 .map(entry -> new GenericInformation(entry.getKey(), entry.getValue()))
                                 .collect(Collectors.toList());
    }

    protected List<Variable> createEntityVariable(ImmutableMap<String, String> variables) {
        return variables.entrySet()
                        .stream()
                        .map(entry -> new Variable(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
    }

    protected CatalogObject findWorkflow(long workflowId) {
        CatalogObject catalogObject = workflowRepository.findOne(workflowId);

        if (catalogObject == null) {
            throw new WorkflowNotFoundException();
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

    public PagedResources listWorkflows(Long bucketId, Optional<Long> workflowId, Optional<String> query,
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
                page = workflowRevisionRepository.getRevisions(workflowId.get(), pageable);
            }
        } else {
            if (query.isPresent()) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(bucketId,
                                                                                          queryExpressionContext,
                                                                                          pageable);
            } else {
                page = workflowRepository.getMostRecentRevisions(bucketId, pageable);
            }
        }

        return assembler.toResource(page, workflowRevisionResourceAssembler);
    }

    private QueryExpressionContext createJpaQueryExpression(String workflowCatalogQuery)
            throws QueryExpressionBuilderException {
        WorkflowCatalogQueryExpressionBuilder builder = new WorkflowCatalogQueryExpressionBuilder(workflowCatalogQuery);

        return builder.build();
    }

    public ResponseEntity<?> getWorkflow(Long bucketId, Long workflowId, Optional<Long> revisionId,
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

        ObjectMetadata objectMetadata = new ObjectMetadata(catalogObjectRevision);

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
            catalogObjectRevision = workflowRevisionRepository.getWorkflowRevision(bucketId, workflowId, revisionId.get());
        } else {
            catalogObjectRevision = workflowRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
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
                catalogObjectRevision = workflowRevisionRepository.getWorkflowRevision(bucketId,
                                                                                  workflowId,
                                                                                  catalogObjectRevision.getRevisionId());
                workflowRevisionRepository.delete(catalogObjectRevision);
            } else {
                catalogObjectRevision = workflowRevisionRepository.getWorkflowRevision(bucketId,
                                                                                  workflowId,
                                                                                  revisionId.get());
                workflowRevisionRepository.delete(catalogObjectRevision);
            }
        } else {
            catalogObjectRevision = workflowRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
            workflowRepository.delete(catalogObject);
        }
        ObjectMetadata objectMetadata = new ObjectMetadata(catalogObjectRevision);
        objectMetadata.add(createLink(bucketId, workflowId, catalogObjectRevision));
        return ResponseEntity.ok(objectMetadata);
    }

    public Link createLink(Long bucketId, Long workflowId, CatalogObjectRevision catalogObjectRevision) {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(WorkflowRevisionController.class).get(bucketId,
                                                                                                            workflowId,
                                                                                                            catalogObjectRevision.getRevisionId(),
                                                                                                            null));

        // alt request parameter name and value is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
