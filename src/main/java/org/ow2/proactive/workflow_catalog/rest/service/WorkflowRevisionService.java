/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
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
import com.google.common.collect.ImmutableMap;
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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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
            byte[] proActiveWorkflowXmlContent) {
        try {
            ProActiveWorkflowParser proActiveWorkflowParser =
                    new ProActiveWorkflowParser(
                            new ByteArrayInputStream(proActiveWorkflowXmlContent));

            return createWorkflowRevision(bucketId, workflowId,
                    proActiveWorkflowParser.parse(), proActiveWorkflowXmlContent);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    @Transactional
    public WorkflowMetadata createWorkflowRevision(Long bucketId, Optional<Long> workflowId,
            ProActiveWorkflowParserResult proActiveWorkflowParserResult,
            byte[] proActiveWorkflowXmlContent) {
        Bucket bucket = findBucket(bucketId);

        Workflow workflow = null;
        WorkflowRevision workflowRevision;

        long revisionNumber = 1;

        if (workflowId.isPresent()) {
            workflow = findWorkflow(workflowId.get());
            revisionNumber = workflow.getLastRevisionId() + 1;
        }

        workflowRevision =
                new WorkflowRevision(
                        bucketId, revisionNumber, proActiveWorkflowParserResult.getJobName(),
                        proActiveWorkflowParserResult.getProjectName(), LocalDateTime.now(),
                        proActiveWorkflowXmlContent);

        workflowRevision.addGenericInformation(createEntityGenericInformation(
                proActiveWorkflowParserResult.getGenericInformation()));

        workflowRevision.addVariables(createEntityVariable(
                proActiveWorkflowParserResult.getVariables()));

        workflowRevision = workflowRevisionRepository.save(workflowRevision);

        if (!workflowId.isPresent()) {
            workflow = new Workflow(bucket, workflowRevision);
        } else {
            workflow.addRevision(workflowRevision);
        }

        workflowRepository.save(workflow);

        return new WorkflowMetadata(workflowRevision);
    }

    protected List<GenericInformation> createEntityGenericInformation(
            ImmutableMap<String, String> genericInformation) {
        return genericInformation.entrySet().stream()
                .map(entry -> new GenericInformation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    protected List<Variable> createEntityVariable(ImmutableMap<String, String> variables) {
        return variables.entrySet().stream()
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

            if (query.isPresent()) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(
                        bucketId, workflowId.get(), queryExpressionContext, pageable);
            } else {
                page = workflowRevisionRepository.getRevisions(workflowId.get(), pageable);
            }
        } else {
            if (query.isPresent()) {
                QueryExpressionContext queryExpressionContext = createJpaQueryExpression(query.get());

                page = queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(
                        bucketId, queryExpressionContext, pageable);
            } else {
                page = workflowRepository.getMostRecentRevisions(bucketId, pageable);
            }
        }

        return assembler.toResource(page, workflowRevisionResourceAssembler);
    }

    private QueryExpressionContext createJpaQueryExpression(
            String workflowCatalogQuery) throws QueryExpressionBuilderException {
        WorkflowCatalogQueryExpressionBuilder builder =
                new WorkflowCatalogQueryExpressionBuilder(workflowCatalogQuery);

        return builder.build();
    }

    public ResponseEntity<?> getWorkflow(Long bucketId, Long workflowId, Optional<Long> revisionId,
            Optional<String> alt) {
        findBucket(bucketId);
        findWorkflow(workflowId);

        WorkflowRevision workflowRevision;

        if (revisionId.isPresent()) {
            workflowRevision = workflowRevisionRepository.getWorkflowRevision(
                    bucketId, workflowId, revisionId.get());
        } else {
            workflowRevision = workflowRepository.getMostRecentWorkflowRevision(bucketId, workflowId);
        }

        if (workflowRevision == null) {
            throw new RevisionNotFoundException();
        }

        if (alt.isPresent()) {
            String altValue = alt.get();

            if (!altValue.equalsIgnoreCase(SUPPORTED_ALT_VALUE)) {
                throw new UnsupportedMediaTypeException(
                        "Unsupported media type '" + altValue + "' (only '" + SUPPORTED_ALT_VALUE + "' is allowed)");
            }

            byte[] bytes = workflowRevision.getXmlPayload();

            return ResponseEntity.ok()
                    .contentLength(bytes.length)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(new InputStreamResource(new ByteArrayInputStream(bytes)));
        }

        WorkflowMetadata workflowMetadata =
                new WorkflowMetadata(workflowRevision);

        workflowMetadata.add(createLink(bucketId, workflowId, revisionId, workflowRevision));

        return ResponseEntity.ok(workflowMetadata);
    }

    public Link createLink(Long bucketId, Long workflowId, Optional<Long> revisionId,
            WorkflowRevision workflowRevision) {
        ControllerLinkBuilder controllerLinkBuilder =
                linkTo(methodOn(WorkflowRevisionController.class)
                        .get(bucketId, workflowId, workflowRevision.getRevisionId(), null));

        // alt request parameter name and value is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
