package org.ow2.proactive.workflow_catalog.rest.service;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.entity.Workflow;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.exceptions.BucketNotFoundException;
import org.ow2.proactive.workflow_catalog.rest.exceptions.UnprocessableEntityException;
import org.ow2.proactive.workflow_catalog.rest.exceptions.WorkflowNotFoundException;
import org.ow2.proactive.workflow_catalog.rest.service.repository.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by paraita on 1/12/16.
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
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateWorkflowRevisionWithInvalidBucket() throws Exception {
        when(bucketRepository.findOne(Matchers.anyLong())).thenReturn(null);
        workflowRevisionService.createWorkflowRevision(DUMMY_ID, Optional.empty(), null);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testCreateWorkflowRevisionNoProjectName() throws IOException {
        when(bucketRepository.findOne(Matchers.anyLong())).thenReturn(mock(Bucket.class));
        workflowRevisionService.createWorkflowRevision(DUMMY_ID, Optional.empty(),
                getWorkflowAsByteArray("invalid-workflow.xml"));
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

    @Ignore
    @Test
    public void testGetWorkflow() throws Exception {
        assertTrue(false);
    }

    private void listWorkflows(Optional<Long> wId) {
        when(bucketRepository.findOne(anyLong())).thenReturn(mock(Bucket.class));
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        when(mockedAssembler.toResource(any(PageImpl.class), any(WorkflowRevisionResourceAssembler.class)))
                .thenReturn(null);

        if (wId.isPresent()) {
            when(workflowRevisionRepository.getRevisions(anyLong(), any(Pageable.class)))
                    .thenReturn(mock(PageImpl.class));
        }
        else {
            when(workflowRepository.getMostRecentRevisions(anyLong(), any(Pageable.class)))
                    .thenReturn(mock(PageImpl.class));
        }

        workflowRevisionService.listWorkflows(DUMMY_ID, wId, null, mockedAssembler);
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

        verify(genericInformationRepository, times(1)).save(any(List.class));
        verify(variableRepository, times(1)).save(any(List.class));
        verify(workflowRevisionRepository, times(1)).save(any(WorkflowRevision.class));

        assertEquals(name, actualWFMetadata.name);
        assertEquals(projectName, actualWFMetadata.projectName);
        assertEquals(EXISTING_ID, actualWFMetadata.bucketId);
        assertEquals(EXISTING_ID, actualWFMetadata.revision);
    }

    private static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        return ByteStreams.toByteArray(
                new FileInputStream(getWorkflowFile(filename)));
    }

    private static File getWorkflowFile(String filename) {
        return new File(WorkflowRevisionServiceTest.class.getResource("/workflows/" + filename).getFile());
    }
}