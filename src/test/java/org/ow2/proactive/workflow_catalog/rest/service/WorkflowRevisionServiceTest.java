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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.entity.Workflow;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.GenericInformationRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.VariableRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRevisionRepository;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author ActiveEon Team
 */
public class WorkflowRevisionServiceTest {

    @InjectMocks
    private WorkflowRevisionService workflowRevisionService;

    @Mock
    private WorkflowRevisionRepository workflowRevisionRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private GenericInformationRepository genericInformationRepository;

    @Mock
    private VariableRepository variableRepository;

    private static final Long DUMMY_ID = 0L;
    private static final Long EXISTING_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        workflowRevisionService = Mockito.spy(workflowRevisionService);

        Mockito.doReturn(new Link("test"))
                .when(workflowRevisionService)
                .createLink(
                        Matchers.any(Long.class),
                        Matchers.any(Long.class),
                        Matchers.any(Optional.class),
                        Matchers.any(WorkflowRevision.class));
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateWorkflowRevisionWithInvalidBucket() throws Exception {
        when(bucketRepository.findOne(Matchers.anyLong())).thenReturn(null);
        workflowRevisionService.createWorkflowRevision(DUMMY_ID, Optional.empty(),
                new ProActiveWorkflowParserResult("projectName", "name",
                        ImmutableMap.of(), ImmutableMap.of()), new byte[0]);
    }

    @Test
    public void testCreateWorkflowWithGenericInfosAndVariables1() throws IOException {
        createWorkflow("WR-NAME-GI-VARS", "WR-PROJ-NAME-GI-VARS", "workflow.xml",
                Optional.empty());
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithGenericInfosAndVariables2() throws IOException {
        createWorkflow("WR-NAME-GI-VARS", "WR-PROJ-NAME-GI-VARS", "workflow.xml",
                Optional.of(EXISTING_ID));
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithoutGenericInfosOrVariables1() throws IOException {
        createWorkflow("WR-NAME", "WR-PROJ-NAME", "workflow-no-generic-information-no-variable.xml",
                Optional.empty());
        // assertions are done in the called method
    }

    @Test
    public void testCreateWorkflowWithoutGenericInfosOrVariables2() throws IOException {
        createWorkflow("WR-NAME", "WR-PROJ-NAME", "workflow-no-generic-information-no-variable.xml",
                Optional.of(EXISTING_ID));
        // assertions are done in the called method
    }

    @Test(expected = WorkflowNotFoundException.class)
    public void testFindWorkflowInvalidId() throws Exception {
        when(workflowRepository.findOne(anyLong())).thenReturn(null);
        workflowRevisionService.findWorkflow(DUMMY_ID);
    }

    @Test
    public void testFindWorkflow() throws Exception {
        when(workflowRepository.findOne(EXISTING_ID)).thenReturn(mock(Workflow.class));
        workflowRevisionService.findWorkflow(EXISTING_ID);
        verify(workflowRepository, times(1)).findOne(EXISTING_ID);
    }

    @Test
    public void testFindBucketExisting() {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(EXISTING_ID)).thenReturn(bucket);
        assertEquals(workflowRevisionService.findBucket(EXISTING_ID), bucket);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testFindBucketNonExisting() {
        workflowRevisionService.findBucket(DUMMY_ID);
    }

    @Test
    public void testListWorkflowsWithWorkflowId() throws Exception {
        listWorkflows(Optional.of(DUMMY_ID));
        verify(workflowRevisionRepository, times(1)).getRevisions(anyLong(), any(Pageable.class));
    }

    @Test
    public void testListWorkflowsWithoutWorkflowId() throws Exception {
        listWorkflows(Optional.empty());
        verify(workflowRepository, times(1)).getMostRecentRevisions(anyLong(), any(Pageable.class));
    }

    @Test(expected = RevisionNotFoundException.class)
    public void testGetWorkflowWithInvalidRevisionId() throws Exception {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(workflowRepository.findOne(anyLong())).thenReturn(mock(Workflow.class));
        when(workflowRepository.getMostRecentWorkflowRevision(anyLong(), anyLong()))
                .thenReturn(null);
        workflowRevisionService.getWorkflow(DUMMY_ID, DUMMY_ID,
                Optional.empty(), Optional.empty());
    }

    @Test
    public void testGetWorkflowWithValidRevisionIdNoPayload() throws Exception {
        getWorkflow(Optional.of(DUMMY_ID), Optional.empty());
        verify(workflowRevisionRepository, times(1)).getWorkflowRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    private void getWorkflow(Optional<Long> revisionId, Optional<String> alt) throws IOException {

        Workflow mockedWf = mock(Workflow.class);
        when(mockedWf.getId()).thenReturn(DUMMY_ID);

        WorkflowRevision wfRev = new WorkflowRevision(DUMMY_ID, DUMMY_ID, "WR-TEST", "WR-PROJ-NAME",
                LocalDateTime.now(),
                Lists.newArrayList(), Lists.newArrayList(), getWorkflowAsByteArray("workflow.xml"));
        wfRev.setWorkflow(mockedWf);

        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(workflowRepository.findOne(anyLong())).thenReturn(mock(Workflow.class));

        if (revisionId.isPresent()) {
            when(workflowRevisionRepository.getWorkflowRevision(anyLong(), anyLong(), anyLong()))
                    .thenReturn(wfRev);
        } else {
            when(workflowRepository.getMostRecentWorkflowRevision(anyLong(), anyLong()))
                    .thenReturn(wfRev);
        }

        workflowRevisionService.getWorkflow(DUMMY_ID, DUMMY_ID, revisionId, alt);

        verify(bucketRepository, times(1)).findOne(DUMMY_ID);
        verify(workflowRepository, times(1)).findOne(DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutRevisionINoPayload() throws Exception {
        getWorkflow(Optional.empty(), Optional.empty());
        verify(workflowRepository, times(1)).getMostRecentWorkflowRevision(DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithValidRevisionIdWithPayload() throws Exception {
        getWorkflow(Optional.of(DUMMY_ID), Optional.of("xml"));
        verify(workflowRevisionRepository, times(1)).getWorkflowRevision(DUMMY_ID, DUMMY_ID, DUMMY_ID);
    }

    @Test
    public void testGetWorkflowWithoutValidRevisionIdWithPayload() throws Exception {
        getWorkflow(Optional.empty(), Optional.of("xml"));
        verify(workflowRepository, times(1)).getMostRecentWorkflowRevision(DUMMY_ID, DUMMY_ID);
    }

    private void listWorkflows(Optional<Long> wId) throws QueryExpressionBuilderException {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(workflowRepository.findOne(anyLong())).thenReturn(mock(Workflow.class));
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        when(mockedAssembler.toResource(any(PageImpl.class), any(WorkflowRevisionResourceAssembler.class)))
                .thenReturn(null);

        if (wId.isPresent()) {
            when(workflowRevisionRepository.getRevisions(anyLong(), any(Pageable.class)))
                    .thenReturn(mock(PageImpl.class));
        } else {
            when(workflowRepository.getMostRecentRevisions(anyLong(), any(Pageable.class)))
                    .thenReturn(mock(PageImpl.class));
        }

        workflowRevisionService.listWorkflows(DUMMY_ID, wId, Optional.empty(), null, mockedAssembler);
    }

    private void createWorkflow(String name, String projectName, String fileName, Optional<Long> wId)
            throws IOException {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        when(genericInformationRepository.save(any(List.class))).thenReturn(Lists.newArrayList());
        when(variableRepository.save(any(List.class))).thenReturn(Lists.newArrayList());
        when(workflowRevisionRepository.save(any(WorkflowRevision.class)))
                .thenReturn(new WorkflowRevision(EXISTING_ID, EXISTING_ID, name, projectName,
                        LocalDateTime.now(), Lists.newArrayList(), Lists.newArrayList(),
                        getWorkflowAsByteArray(fileName)));

        if (wId.isPresent()) {
            when(workflowRepository.findOne(anyLong()))
                    .thenReturn(new Workflow(mock(Bucket.class), Lists.newArrayList()));
        }

        WorkflowMetadata actualWFMetadata = workflowRevisionService.createWorkflowRevision(
                DUMMY_ID, wId, getWorkflowAsByteArray(fileName));

        verify(workflowRevisionRepository, times(1)).save(any(WorkflowRevision.class));

        assertEquals(name, actualWFMetadata.name);
        assertEquals(projectName, actualWFMetadata.projectName);
        assertEquals(EXISTING_ID, actualWFMetadata.bucketId);
        assertEquals(EXISTING_ID, actualWFMetadata.revisionId);
    }

    private static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        return ByteStreams.toByteArray(
                new FileInputStream(getWorkflowFile(filename)));
    }

    private static File getWorkflowFile(String filename) {
        return new File(WorkflowRevisionServiceTest.class.getResource("/workflows/" + filename).getFile());
    }
}