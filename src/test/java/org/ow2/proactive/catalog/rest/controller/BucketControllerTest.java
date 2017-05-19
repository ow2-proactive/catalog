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
package org.ow2.proactive.catalog.rest.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.controller.BucketController;
import org.ow2.proactive.catalog.rest.service.BucketService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;


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
