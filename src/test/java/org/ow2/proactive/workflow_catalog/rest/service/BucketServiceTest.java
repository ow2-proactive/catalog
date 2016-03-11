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

import java.time.LocalDateTime;

import org.ow2.proactive.workflow_catalog.rest.assembler.BucketResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author ActiveEon Team
 */
public class BucketServiceTest {

    @InjectMocks
    private BucketService bucketService;

    @Mock
    private BucketRepository bucketRepository;

    private final String DefaultBucketName = "BucketServiceTest";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateBucket() throws Exception {
        Bucket mockedBucket = newMockedBucket(1L, "BUCKET-NAME-TEST", LocalDateTime.now());
        when(bucketRepository.save(any(Bucket.class))).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.createBucket(
                "BUCKET-NAME-TEST", DefaultBucketName);
        verify(bucketRepository, times(1)).save(any(Bucket.class));
        assertEquals(mockedBucket.getName(), bucketMetadata.name);
        assertEquals(mockedBucket.getCreatedAt(), bucketMetadata.createdAt);
        assertEquals(mockedBucket.getId(), bucketMetadata.id);
        assertEquals(mockedBucket.getOwner(), bucketMetadata.owner);
    }

    @Test
    public void testGetBucketMetadataValidBucket() throws Exception {
        Bucket mockedBucket = newMockedBucket(1L, "BUCKET-NAME-TEST", LocalDateTime.now());
        when(bucketRepository.findOne(anyLong())).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(1L);
        verify(bucketRepository, times(1)).findOne(anyLong());
        assertEquals(mockedBucket.getName(), bucketMetadata.name);
        assertEquals(mockedBucket.getCreatedAt(), bucketMetadata.createdAt);
        assertEquals(mockedBucket.getId(), bucketMetadata.id);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testGetBucketMetadataInvalidBucket() throws Exception {
        when(bucketRepository.findOne(anyLong())).thenReturn(null);
        bucketService.getBucketMetadata(1L);
    }

    @Test
    public void testListBuckets() throws Exception {
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        when(bucketRepository.findAll(any(Pageable.class))).thenReturn(null);
        bucketService.listBuckets(null, mockedAssembler);
        verify(bucketRepository, times(1)).findAll(any(Pageable.class));
        verify(mockedAssembler, times(1)).toResource(any(PageImpl.class),
                any(BucketResourceAssembler.class));
    }

    private Bucket newMockedBucket(Long id, String name, LocalDateTime createdAt) {
        Bucket mockedBucket = mock(Bucket.class);
        when(mockedBucket.getId()).thenReturn(id);
        when(mockedBucket.getName()).thenReturn(name);
        when(mockedBucket.getCreatedAt()).thenReturn(createdAt);
        return mockedBucket;
    }

}