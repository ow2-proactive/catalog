/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.workflow_catalog.rest.controller;

import java.util.Optional;

import org.ow2.proactive.workflow_catalog.rest.service.BucketAlreadyExisting;
import org.ow2.proactive.workflow_catalog.rest.service.BucketService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author ActiveEon Team
 */
public class BucketControllerTest {

    @InjectMocks
    private BucketController bucketController;

    @Mock
    private BucketService bucketService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        final String bucketTestUser = "BucketControllerTestUser";
        bucketController.create("DUMMY", bucketTestUser);
        verify(bucketService, times(1)).createBucket("DUMMY", bucketTestUser);
    }

    @Test(expected = BucketAlreadyExisting.class)
    public void testCreateDuplicate() throws Exception {
        final String bucketTestUser = "BucketControllerTestUser";
        bucketController.create("DUMMY", bucketTestUser);
        bucketController.create("DUMMY", bucketTestUser);
    }

    @Test
    public void testGetMetadata() throws Exception {
        bucketController.getMetadata(1L);
        verify(bucketService, times(1)).getBucketMetadata(1L);
    }

    @Test
    public void testListNoOwner() throws Exception {
        Pageable mockedPageable = mock(Pageable.class);
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        bucketController.list(Optional.empty(), mockedPageable, mockedAssembler);
        verify(bucketService, times(1)).listBuckets(Optional.empty(), mockedPageable, mockedAssembler);
    }

    @Test
    public void testListWithOwner() throws Exception {
        Pageable mockedPageable = mock(Pageable.class);
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        final Optional<String> owner = Optional.of("toto");
        bucketController.list(owner, mockedPageable, mockedAssembler);
        verify(bucketService, times(1)).listBuckets(owner, mockedPageable, mockedAssembler);
    }
}