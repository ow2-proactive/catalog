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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.entity.Workflow;
import org.ow2.proactive.workflow_catalog.rest.exceptions.BucketNotFoundException;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRevisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ActiveEon Team
 */
public class WorkflowServiceTest {

    @InjectMocks
    private WorkflowRevisionService workflowRevisionService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private WorkflowRevisionRepository workflowRevisionRepository;

    @Mock
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Mock
    private WorkflowRevisionResourceAssembler workflowResourceAssembler;

    private static final Long EXISTING_ID = 1L;
    private static final Long NON_EXISTING_ID = 2L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindBucketExisting() {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(EXISTING_ID)).thenReturn(bucket);
        assertEquals(workflowRevisionService.findBucket(EXISTING_ID), bucket);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testFindBucketNonExisting() {
        workflowRevisionService.findBucket(NON_EXISTING_ID);
    }

    @Test
    public void testListWorkflowsExistingBucket() throws Exception {
        Bucket expectedBucket = newMockedBucket(EXISTING_ID, "Expected Bucket", 10L);

        when(bucketRepository.findOne(EXISTING_ID)).thenReturn(expectedBucket);

        PagedResources actualPage = workflowRevisionService.listWorkflows(
                EXISTING_ID, Optional.empty(), null, pagedResourcesAssembler);
        Long actualNbElements = actualPage.getMetadata().getTotalElements();

        assertEquals(10L, actualNbElements.longValue());
    }

    @Test(expected = BucketNotFoundException.class)
    public void testListWorkflowsNonExistingBucket1() {
        workflowRevisionService.listWorkflows(
                NON_EXISTING_ID, Optional.empty(), null, pagedResourcesAssembler);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testListWorkflowsNonExistingBucket2() {
        workflowRevisionService.listWorkflows(
                NON_EXISTING_ID, Optional.of(NON_EXISTING_ID), null, pagedResourcesAssembler);
    }

    private Bucket newMockedBucket(Long id, String name, Long nbWorkflows) {
        Bucket bucket = mock(Bucket.class);
        when(bucket.getId()).thenReturn(id);
        when(bucket.getName()).thenReturn(name);
        if (nbWorkflows > 0) {
            List<Workflow> workflowList = LongStream.rangeClosed(1L, nbWorkflows)
                    .boxed()
                    .map((value) -> newMockedWorkflow(value))
                    .collect(Collectors.toList());
            when(bucket.getWorkflows()).thenReturn(workflowList);

            PagedResources<WorkflowMetadata> pagedResources = mock(PagedResources.class);
            PagedResources.PageMetadata pageMetadata = mock(PagedResources.PageMetadata.class);

            when(pageMetadata.getTotalElements()).thenReturn(nbWorkflows);
            when(pagedResources.getMetadata()).thenReturn(pageMetadata);

            Collection<WorkflowMetadata> workflowMetadataCol = workflowList.stream()
                    .map(w -> new WorkflowMetadata(id, w.getId(), null, null, null, null, null, null))
                    .collect(Collectors.toList());

            when(workflowRevisionRepository.getRevisions(
                    Matchers.anyLong(), Matchers.any(Pageable.class)))
                    .thenReturn(mock(PageImpl.class));
            when(pagedResources.getContent()).thenReturn(workflowMetadataCol);
            when(pagedResourcesAssembler.toResource(Matchers.any(Page.class),
                    Matchers.any(WorkflowRevisionResourceAssembler.class))).thenReturn(pagedResources);
        }
        return bucket;
    }

    private Workflow newMockedWorkflow(Long id) {
        Workflow workflow = mock(Workflow.class);
        when(workflow.getId()).thenReturn(id);
        return workflow;
    }
}
