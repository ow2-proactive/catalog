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

import com.google.common.collect.Lists;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.controller.WorkflowRevisionController;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.*;
import org.ow2.proactive.workflow_catalog.rest.exceptions.*;
import org.ow2.proactive.workflow_catalog.rest.service.repository.*;
import org.ow2.proactive.workflow_catalog.rest.util.WorkflowParser;
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

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author ActiveEon Team
 */
@Service
public class WorkflowRevisionService {

    private static final String SUPPORTED_ALT_VALUE = "xml";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private WorkflowRevisionResourceAssembler workflowRevisionResourceAssembler;

    @Autowired
    private GenericInformationRepository genericInformationRepository;

    @Autowired
    private VariableRepository variableRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowRevisionRepository workflowRevisionRepository;

    @Transactional
    public WorkflowMetadata createWorkflowRevision(Long bucketId, Optional<Long> workflowId, byte[] xmlPayload) {
        Bucket bucket = findBucket(bucketId);

        try {
            WorkflowParser parser = new WorkflowParser(new ByteArrayInputStream(xmlPayload));
            parser.parse();

            String projectName = parser.getProjectName().orElseThrow(
                    getMissingElementException("No project name defined.")
            );

            String name = parser.getJobName().orElseThrow(
                    getMissingElementException("No job name defined.")
            );

            Iterable<GenericInformation> genericInformation = getGenericInformation(parser);
            Iterable<Variable> variables = getVariable(parser);

            Workflow workflow = null;
            WorkflowRevision workflowRevision;

            long revisionNumber = 1;

            if (workflowId.isPresent()) {
                workflow = findWorkflow(workflowId.get());
                revisionNumber = workflow.getLastRevisionId() + 1;
            }

            workflowRevision =
                    new WorkflowRevision(
                            bucketId, revisionNumber, name, projectName, LocalDateTime.now(),
                            Lists.newArrayList(genericInformation),
                            Lists.newArrayList(variables),
                            xmlPayload);

            workflowRevision = workflowRevisionRepository.save(workflowRevision);

            if (!workflowId.isPresent()) {
                workflow = new Workflow(bucket, workflowRevision);
            } else {
                workflow.addRevision(workflowRevision);
            }

            workflowRepository.save(workflow);

            return new WorkflowMetadata(workflowRevision);
        } catch (XMLStreamException e) {
            throw new UnprocessableEntityException(e);
        }
    }

    protected Workflow findWorkflow(long workflowId) {
        Workflow workflow = workflowRepository.findOne(workflowId);

        if (workflow == null) {
            throw new WorkflowNotFoundException();
        }

        return workflow;
    }

    private Iterable<GenericInformation> getGenericInformation(WorkflowParser parser) {
        return parser.getGenericInformation().entrySet().stream()
                .map(entry -> new GenericInformation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Iterable<Variable> getVariable(WorkflowParser parser) {
        return parser.getVariables().entrySet().stream()
                .map(entry -> new Variable(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Supplier<UnprocessableEntityException> getMissingElementException(String message) {
        return () -> new UnprocessableEntityException("XML does not validate against Schema. " + message);
    }

    protected Bucket findBucket(Long bucketId) {
        Bucket bucket = bucketRepository.findOne(bucketId);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return bucket;
    }

    public PagedResources listWorkflows(Long bucketId, Optional<Long> workflowId, Pageable pageable, PagedResourcesAssembler assembler) {
        findBucket(bucketId);

        Page<WorkflowRevision> page;

        if (workflowId.isPresent()) {
            findWorkflow(workflowId.get());
            page = workflowRevisionRepository.getRevisions(workflowId.get(), pageable);
        } else {
            page = workflowRepository.getMostRecentRevisions(bucketId, pageable);
        }

        return assembler.toResource(page, workflowRevisionResourceAssembler);
    }

    public ResponseEntity<?> getWorkflow(Long bucketId, Long workflowId, Optional<Long> revisionId, Optional<String> alt) {
        findBucket(bucketId);
        findWorkflow(workflowId);

        WorkflowRevision workflowRevision;

        if (revisionId.isPresent()) {
            workflowRevision = workflowRevisionRepository.getWorkflowRevision(bucketId, workflowId, revisionId.get());
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

    public Link createLink(Long bucketId, Long workflowId, Optional<Long> revisionId, WorkflowRevision workflowRevision) {
        ControllerLinkBuilder controllerLinkBuilder =
                linkTo(methodOn(WorkflowRevisionController.class)
                        .get(bucketId, workflowId, workflowRevision.getRevisionId(), null));

        // alt request parameter name and valud is added manually
        // otherwise a converter needs to be configured
        // for Optional class
        return new Link(controllerLinkBuilder.toString() + "?alt=" + SUPPORTED_ALT_VALUE).withRel("content");
    }

}
