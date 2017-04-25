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
package org.ow2.proactive.workflow_catalog.rest.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.assembler.WorkflowRevisionResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectControllerTest {

    @InjectMocks
    private WorkflowController workflowController;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Mock
    WorkflowRevisionResourceAssembler workflowResourceAssembler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(null);
        workflowController.create(1L, Optional.empty(), Optional.empty(), file);
        verify(workflowService, times(1)).createWorkflow(1L, Optional.empty(), null);
    }

    @Test
    public void testCreateWorkflows() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(null);
        workflowController.create(1L, Optional.empty(), Optional.of("zip"), file);
        verify(workflowService, times(1)).createWorkflows(1L, Optional.empty(), null);
    }

    @Test
    public void testGetWorkflowsAsArchive() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        List<Long> idList = new ArrayList<>();
        idList.add(0L);
        workflowController.get(1L, idList, Optional.of("zip"), response);
        verify(workflowService, times(1)).getWorkflowsAsArchive(1L, idList);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setContentType("application/zip");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
        verify(sos, times(1)).write(Mockito.any());
        verify(sos, times(1)).flush();
    }

    @Test
    public void testList() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        when(pagedResourcesAssembler.toResource(any(Page.class),
                                                any(WorkflowRevisionResourceAssembler.class))).thenReturn(mock(PagedResources.class));
        workflowController.list(1L, Optional.empty(), null, pagedResourcesAssembler);
        verify(workflowService, times(1)).listWorkflows(anyLong(),
                                                        any(Optional.class),
                                                        any(Pageable.class),
                                                        any(PagedResourcesAssembler.class));
    }

    @Test
    public void testGet() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        workflowController.get(1L, Collections.singletonList(2L), Optional.empty(), response);
        verify(workflowService, times(1)).getWorkflowMetadata(1L, 2L, Optional.empty());
    }

    @Test
    public void testDelete() throws Exception {
        workflowController.delete(1L, 2L);
        verify(workflowService, times(1)).delete(1L, 2L);
    }
}
