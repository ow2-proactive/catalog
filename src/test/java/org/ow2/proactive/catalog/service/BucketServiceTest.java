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
package org.ow2.proactive.catalog.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DefaultCatalogObjectsFolderNotFoundException;
import org.ow2.proactive.catalog.service.exception.DefaultRawCatalogObjectsFolderNotFoundException;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class BucketServiceTest {

    @InjectMocks
    private BucketService bucketService;

    @InjectMocks
    private CatalogObjectService catalogObjectService;

    @Mock
    private BucketRepository bucketRepository;

    private static final String DEFAULT_BUCKET_NAME = "BucketServiceTest";

    @Test
    public void testCreateBucket() throws Exception {
        BucketEntity mockedBucket = newMockedBucket(1L, "BUCKET-NAME-TEST", LocalDateTime.now());
        when(bucketRepository.save(any(BucketEntity.class))).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.createBucket("BUCKET-NAME-TEST", DEFAULT_BUCKET_NAME);
        verify(bucketRepository, times(1)).save(any(BucketEntity.class));
        assertEquals(mockedBucket.getName(), bucketMetadata.getName());
        assertEquals(mockedBucket.getId(), bucketMetadata.getMetaDataId());
        assertEquals(mockedBucket.getOwner(), bucketMetadata.getOwner());
    }

    @Test
    public void testGetBucketMetadataValidBucket() throws Exception {
        BucketEntity mockedBucket = newMockedBucket(1L, "BUCKET-NAME-TEST", LocalDateTime.now());
        when(bucketRepository.findOne(anyLong())).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata(1L);
        verify(bucketRepository, times(1)).findOne(anyLong());
        assertEquals(mockedBucket.getName(), bucketMetadata.getName());
        assertEquals(mockedBucket.getId(), bucketMetadata.getMetaDataId());
    }

    @Test(expected = BucketNotFoundException.class)
    public void testGetBucketMetadataInvalidBucket() throws Exception {
        when(bucketRepository.findOne(anyLong())).thenReturn(null);
        bucketService.getBucketMetadata(1L);
    }

    @Test
    public void testListBucketsNoOwner() throws Exception {
        listBucket(Optional.empty(), Optional.empty());
    }

    @Test
    public void testListBucketsWithOwner() throws Exception {
        listBucket(Optional.of("toto"), Optional.empty());
    }

    @Test
    public void testListBucketsNoOwnerWithKind() throws Exception {
        listBucket(Optional.empty(), Optional.of("workflow"));
    }

    @Test(expected = DefaultCatalogObjectsFolderNotFoundException.class)
    public void testPopulateCatalogFromInvalidFolder() throws Exception {
        final String[] buckets = { "NonExistentBucket" };
        BucketEntity mockedBucket = newMockedBucket(1L, "mockedBucket", null);
        when(bucketRepository.save(any(BucketEntity.class))).thenReturn(mockedBucket);
        catalogObjectService.populateCatalog(buckets, "/this-folder-doesnt-exist", "/raw-objects");
    }

    @Test(expected = DefaultRawCatalogObjectsFolderNotFoundException.class)
    public void testPopulateCatalogFromInvalidRawFolder() throws Exception {
        final String[] buckets = { "NonExistentBucket" };
        BucketEntity mockedBucket = newMockedBucket(1L, "mockedBucket", null);
        when(bucketRepository.save(any(BucketEntity.class))).thenReturn(mockedBucket);
        catalogObjectService.populateCatalog(buckets, "/default-objects", "/this-folder-doesnt-exist");
    }

    private void listBucket(Optional<String> owner, Optional<String> kind) {
        when(bucketRepository.findAll()).thenReturn(Collections.EMPTY_LIST);
        bucketService.listBuckets(owner, kind);
        if (owner.isPresent()) {
            verify(bucketRepository, times(1)).findByOwner(anyString());
        } else if (kind.isPresent()) {
            verify(bucketRepository, times(1)).findContainingKind(anyString());
        } else {
            verify(bucketRepository, times(1)).findAll();
        }
    }

    private BucketEntity newMockedBucket(Long id, String name, LocalDateTime createdAt) {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(id);
        when(mockedBucket.getName()).thenReturn(name);
        return mockedBucket;
    }
}
