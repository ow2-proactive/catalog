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
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.entity.GenericInformation;
import org.ow2.proactive.workflow_catalog.rest.entity.Variable;
import org.ow2.proactive.workflow_catalog.rest.entity.Workflow;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
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
    public WorkflowMetadata createWorkflowRevision(Long bucketId, Optional<Long> workflowId,
            byte[] proActiveWorkflowXmlContent, Optional<String> layout) {
        try {
            ProActiveWorkflowParser proActiveWorkflowParser = new ProActiveWorkflowParser(new ByteArrayInputStream(proActiveWorkflowXmlContent));

            return createWorkflowRevision(bucketId,
                                          workflowId,
                                          proActiveWorkflowParser.parse(),
                                          layout,
                                          proActiveWorkflowXmlContent);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public WorkflowMetadata createWorkflowRevision(Long bucketId, Optional<Long> workflowId,
            ProActiveWorkflowParserResult proActiveWorkflowParserResult, Optional<String> layout,
            byte[] proActiveWorkflowXmlContent) {
        Bucket bucket = findBucket(bucketId);

        Workflow workflow = null;
        WorkflowRevision workflowRevision;
        String layoutValue = layout.orElse("");

        long revisionNumber = 1;

        if (workflowId.isPresent()) {
            workflow = findWorkflow(workflowId.get());
            revisionNumber = workflow.getLastRevisionId() + 1;
        }

        workflowRevision = new WorkflowRevision(bucketId,
                                                revisionNumber,
                                                proActiveWorkflowParserResult.getJobName(),
                                                proActiveWorkflowParserResult.getProjectName(),
                                                LocalDateTime.now(),
                                                layoutValue,
                                                proActiveWorkflowXmlContent);

        workflowRevision.addGenericInformation(createEntityGenericInformation(proActiveWorkflowParserResult.getGenericInformation()));

        workflowRevision.addVariables(createEntityVariable(proActiveWorkflowParserResult.getVariables()));

        workflowRevision = workflowRevisionRepository.save(workflowRevision);

        if (!workflowId.isPresent()) {
            workflow = new Workflow(bucket, workflowRevision);
        } else {
            workflow.addRevision(workflowRevision);
        }

        workflowRepository.save(workflow);

        return new WorkflowMetadata(workflowRevision);
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

    protected Workflow findWorkflow(long workflowId) {
        Workflow workflow = workflowRepository.findOne(workflowId);

        if (workflow == null) {
            throw new WorkflowNotFoundException();
        }

        return workflow;
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
        Page<WorkflowRevision> page;

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

        WorkflowRevision workflowRevision = getWorkflowRevision(bucketId, workflowId, revisionId);

        if (alt.isPresent()) {
            String altValue = alt.get();

            if (!altValue.equalsIgnoreCase(SUPPORTED_ALT_VALUE)) {
                throw new UnsupportedMediaTypeException("Unsupported media type '" + altValue + "' (only '" +
                                                        SUPPORTED_ALT_VALUE + "' is allowed)");
            }

            byte[] bytes = workflowRevision.getXmlPayload();

            return ResponseEntity.ok()
                                 .contentLength(bytes.length)
                                 .contentType(MediaType.APPLICATION_XML)
                                 .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        }

        WorkflowMetadata workflowMetadata = new WorkflowMetadata(workflowRevision);

        workflowMetadata.add(createLink(bucketId, workflowId, workflowRevision));

        return ResponseEntity.ok(workflowMetadata);
    }

    public List<WorkflowRevision> getWorkflowsRevisions(Long bucketId, List<Long> idList) {
        findBucket(bucketId);
        List<WorkflowRevision> workflows = idList.stream()
                                                 .map(workflowId -> getWorkflowRevision(bucketId,
                                                                                        workflowId,
                                                                                        Optional.empty()))
                                                 .collect(Collectors.toList());
        return workflows;
    }

    private WorkflowRevision getWorkflowRevision(Long bucketId, Long workflowId, Optional<Long> revisionId) {
        WorkflowRevision workflowRevision;

        if (revisionId.isPresent()) {
            workflowRevision = workflowRevisionRepository.getWorkflowRevision(bucketId, workflowId, revisionId.get());
        } else {
            workflowRevision = workflowRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
        }

        if (workflowRevision == null) {
            throw new RevisionNotFoundException();
        }

        return workflowRevision;
    }

    /**
     * Generic method to delete either a specific WorkflowRevision or a complete Workflow
     * and all of its dependencies.
     * Deleting a previous WorkflowRevision will not impact the current revision of a Workflow.
     * Deleting the current WorkflowRevision will:
     * <ul>
     *     <li>also delete the Workflow if it has only one WorkflowRevision</li>
     *     <li>impact the Workflow by referencing the previous WorkflowRevision if it had more than one WorkflowRevision.</li>
     * </ul>
     * @param bucketId The id of the Bucket containing the Workflow
     * @param workflowId The id of the Workflow containing the WorkflowRevision
     * @param revisionId The revision number of the Workflow
     * @return The deleted WorkflowRevision metadata
     */
    public ResponseEntity<?> delete(Long bucketId, Long workflowId, Optional<Long> revisionId) {
        Workflow workflow = findWorkflow(workflowId);
        WorkflowRevision workflowRevision = null;
        if (revisionId.isPresent() && workflow.getRevisions().size() > 1) {
            if (revisionId.get() == workflow.getLastRevisionId()) {
                Iterator iter = workflow.getRevisions().iterator();
                workflowRevision = (WorkflowRevision) iter.next();
                WorkflowRevision newWorkflowRevisionReference = (WorkflowRevision) iter.next();
                workflow.setLastRevisionId(newWorkflowRevisionReference.getRevisionId());
                workflowRevision = workflowRevisionRepository.getWorkflowRevision(bucketId,
                                                                                  workflowId,
                                                                                  workflowRevision.getRevisionId());
                workflowRevisionRepository.delete(workflowRevision);
            } else {
                workflowRevision = workflowRevisionRepository.getWorkflowRevision(bucketId,
                                                                                  workflowId,
                                                                                  revisionId.get());
                workflowRevisionRepository.delete(workflowRevision);
            }
        } else {
            workflowRevision = workflowRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
            workflowRepository.delete(workflow);
        }
        WorkflowMetadata workflowMetadata = new WorkflowMetadata(workflowRevision);
        workflowMetadata.add(createLink(bucketId, workflowId, workflowRevision));
        return ResponseEntity.ok(workflowMetadata);
    }

    public Link createLink(Long bucketId, Long workflowId, WorkflowRevision workflowRevision) {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(WorkflowRevisionController.class).get(bucketId,
                                                                                                            workflowId,
                                                                                                            workflowRevision.getRevisionId(),
                                                                                                            null));

        // alt request parameter name and value is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
