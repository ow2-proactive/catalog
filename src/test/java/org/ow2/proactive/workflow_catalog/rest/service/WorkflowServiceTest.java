///*
// * ProActive Parallel Suite(TM): The Java(TM) library for
// *    Parallel, Distributed, Multi-Core Computing for
// *    Enterprise Grids & Clouds
// *
// * Copyright (C) 1997-2016 INRIA/University of
// *                 Nice-Sophia Antipolis/ActiveEon
// * Contact: proactive@ow2.org or contact@activeeon.com
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Affero General Public License
// * as published by the Free Software Foundation; version 3 of
// * the License.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this library; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
// * USA
// *
// * If needed, contact us to obtain a release under GPL Version 2 or 3
// * or a different license than the AGPL.
// *
// * Initial developer(s):               The ProActive Team
// *                         http://proactive.inria.fr/team_members.htm
// */
//package org.ow2.proactive.workflow_catalog.rest.service;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowResourceAssembler;
//import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
//import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
//import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
//import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
//import org.ow2.proactive.workflow_catalog.rest.exceptions.BucketNotFoundException;
//import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
//import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PagedResourcesAssembler;
//import org.springframework.hateoas.PagedResources;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.LongStream;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
///**
// * @author ActiveEon Team
// */
//public class WorkflowServiceTest {
//
//    @InjectMocks
//    private WorkflowRevisionService workflowRevisionService;
//
//    @Mock
//    private BucketRepository bucketRepository;
//
//    @Mock
//    private WorkflowRepository workflowRepository;
//
//    @Mock
//    private PagedResourcesAssembler pagedResourcesAssembler;
//
//    @Mock
//    private WorkflowRevisionResourceAssembler workflowResourceAssembler;
//
//    private static final Long EXISTING_BUCKET = 1L;
//    private static final Long NON_EXISTING_BUCKET = 2L;
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    public void testFindBucketExisting() {
//        Bucket bucket = mock(Bucket.class);
//        when(bucketRepository.findOne(EXISTING_BUCKET)).thenReturn(bucket);
//        assertEquals(workflowRevisionService.findBucket(EXISTING_BUCKET), bucket);
//    }
//
//    @Test(expected = BucketNotFoundException.class)
//    public void testFindBucketNonExisting() {
//        workflowRevisionService.findBucket(NON_EXISTING_BUCKET);
//    }
//
//    @Test
//    public void testListWorkflowsExistingBucket() throws Exception {
//        Bucket expectedBucket = newMockedBucket(EXISTING_BUCKET, "Expected Bucket", 10L);
//        Map<Long, WorkflowRevision> expectedWorkflowsMetadata = expectedBucket.getWorkflowRevisions().stream()
//                .collect(Collectors.toMap(WorkflowRevision::getId, Function.identity()));
//        when(bucketRepository.findOne(EXISTING_BUCKET)).thenReturn(expectedBucket);
//
//        PagedResources actualPage = workflowRevisionService.listWorkflows(EXISTING_BUCKET, null, pagedResourcesAssembler);
//        Long actualNbElements = actualPage.getMetadata().getTotalElements();
//
//        assertEquals(10L, actualNbElements.longValue());
//
//        for (Object w : actualPage.getContent()) {
//            WorkflowMetadata workflow = (WorkflowMetadata) w;
//            assertTrue(expectedWorkflowsMetadata.containsKey(workflow.id));
//        }
//    }
//
//    @Test(expected = BucketNotFoundException.class)
//    public void testListWorkflowsNonExistingBucket() {
//        workflowRevisionService.listWorkflows(NON_EXISTING_BUCKET, null, pagedResourcesAssembler);
//    }
//
//    private Bucket newMockedBucket(Long id, String name, Long nbWorkflows) {
//        Bucket bucket = mock(Bucket.class);
//        when(bucket.getId()).thenReturn(id);
//        when(bucket.getName()).thenReturn(name);
//        if (nbWorkflows > 0) {
//            List<WorkflowRevision> workflowRevisionList = LongStream.rangeClosed(1L, nbWorkflows)
//                    .boxed()
//                    .map((value) -> newMockedWorkflowRevision(value))
//                    .collect(Collectors.toList());
//            when(bucket.getWorkflowRevisions()).thenReturn(workflowRevisionList);
//
//            PagedResources<WorkflowMetadata> pagedResources = mock(PagedResources.class);
//            PagedResources.PageMetadata pageMetadata = mock(PagedResources.PageMetadata.class);
//
//            when(pageMetadata.getTotalElements()).thenReturn(nbWorkflows);
//            when(pagedResources.getMetadata()).thenReturn(pageMetadata);
//
//            Collection<WorkflowMetadata> workflowMetadataCol = workflowRevisionList.stream()
//                    .map(w -> new WorkflowMetadata(id, w.getId(), null, null, null, null, null, null, null))
//                    .collect(Collectors.toList());
//
//            when(workflowRepository.findByBucket(Matchers.any(Bucket.class), Matchers.any(Pageable.class)))
//                    .thenReturn(mock(PageImpl.class));
//            when(pagedResources.getContent()).thenReturn(workflowMetadataCol);
//            when(pagedResourcesAssembler.toResource(Matchers.any(Page.class),
//                    Matchers.any(WorkflowRevisionResourceAssembler.class))).thenReturn(pagedResources);
//        }
//        return bucket;
//    }
//
//    private WorkflowRevision newMockedWorkflowRevision(Long id) {
//        WorkflowRevision workflowRevision = mock(WorkflowRevision.class);
//        when(workflowRevision.getId()).thenReturn(id);
//        return workflowRevision;
//    }
//}
