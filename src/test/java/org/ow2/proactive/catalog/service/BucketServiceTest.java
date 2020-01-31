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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.exception.BucketNameIsNotValidException;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DeleteNonEmptyBucketException;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.catalog.util.parser.SupportedParserKinds;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class BucketServiceTest {

    private static final String DEFAULT_BUCKET_NAME = "BucketServiceTest";

    @InjectMocks
    private BucketService bucketService;

    @Mock
    private CatalogObjectService catalogObjectService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private BucketNameValidator bucketNameValidator;

    @Test
    public void testThatEmptyListIsReturnedIfListAndKindAreNull() {
        assertThat(bucketService.listBuckets((List<String>) null, null, null, null)).isEmpty();
        verify(bucketRepository, times(0)).findByOwnerIsInContainingKindAndContentTypeAndObjectName(any(),
                                                                                                    any(),
                                                                                                    any(),
                                                                                                    any());
        verify(bucketRepository, times(0)).findByOwnerIn(any());
    }

    @Test
    public void testCreateBucket() throws Exception {
        BucketEntity mockedBucket = newMockedBucket(1L, "bucket-name", LocalDateTime.now());
        when(bucketRepository.save(any(BucketEntity.class))).thenReturn(mockedBucket);
        when(bucketNameValidator.isValid(anyString())).thenReturn(true);
        BucketMetadata bucketMetadata = bucketService.createBucket("BUCKET-NAME-TEST", DEFAULT_BUCKET_NAME);
        verify(bucketRepository, times(1)).save(any(BucketEntity.class));
        verify(bucketNameValidator, times(1)).isValid(anyString());
        assertEquals(mockedBucket.getBucketName(), bucketMetadata.getName());
        assertEquals(mockedBucket.getOwner(), bucketMetadata.getOwner());
    }

    @Test
    public void testUpdateBucketOwner() throws Exception {
        BucketEntity mockedBucket = newMockedBucket(1L, "bucket-name", LocalDateTime.now());
        BucketEntity mockedBucketWithOwner = newMockedBucket(1L, "bucket-name", LocalDateTime.now());
        mockedBucketWithOwner.setOwner(DEFAULT_BUCKET_NAME);
        List<CatalogObjectRevisionEntity> objectsList = new ArrayList<>();
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .catalogObject(CatalogObjectEntity.builder()
                                                                                                                               .kind(SupportedParserKinds.WORKFLOW.toString())
                                                                                                                               .build())
                                                                                             .build();
        objectsList.add(catalogObjectRevisionEntity);

        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(mockedBucket);
        when(bucketRepository.save(mockedBucket)).thenReturn(mockedBucketWithOwner);
        when(catalogObjectService.listCatalogObjectsEntities((Arrays.asList("BUCKET-NAME-TEST")))).thenReturn(objectsList);

        BucketMetadata bucketMetadata = bucketService.updateOwnerByBucketName("BUCKET-NAME-TEST", DEFAULT_BUCKET_NAME);
        verify(mockedBucket, times(1)).setOwner(DEFAULT_BUCKET_NAME);
        verify(bucketRepository, times(1)).findOneByBucketName("BUCKET-NAME-TEST");
        verify(bucketRepository, times(1)).save(mockedBucket);
        verify(catalogObjectService, times(1)).listCatalogObjectsEntities(Arrays.asList("BUCKET-NAME-TEST"));
        verify(catalogObjectService, times(1)).createCatalogObjectRevision(catalogObjectRevisionEntity,
                                                                           BucketService.COMMIT_MESSAGE_UPDATE_BUCKET);
        assertEquals(mockedBucketWithOwner.getBucketName(), bucketMetadata.getName());
        assertEquals(mockedBucketWithOwner.getOwner(), bucketMetadata.getOwner());
    }

    @Test(expected = BucketNameIsNotValidException.class)
    public void testCreateBucketWithInvalidName() {
        when(bucketNameValidator.isValid(anyString())).thenReturn(false);
        bucketService.createBucket("Bucket-Wrong.name");

    }

    @Test
    public void testGetBucketMetadataValidBucket() throws Exception {
        BucketEntity mockedBucket = newMockedBucket(1L, "bucket-name", LocalDateTime.now());
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.getBucketMetadata("bucket-name");
        verify(bucketRepository, times(1)).findOneByBucketName(anyString());
        assertEquals(mockedBucket.getBucketName(), bucketMetadata.getName());
    }

    @Test(expected = BucketNotFoundException.class)
    public void testGetBucketMetadataInvalidBucket() throws Exception {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        bucketService.getBucketMetadata("bucket-name");
    }

    @Test
    public void testListBucketsNoOwner() throws Exception {
        listBucket(null, Optional.empty(), Optional.empty());
    }

    @Test
    public void testListBucketsWithOwner() throws Exception {
        listBucket("toto", Optional.empty(), Optional.empty());
    }

    @Test
    public void testListBucketsNoOwnerWithKind() throws Exception {
        listBucket(null, Optional.of("workflow"), Optional.empty());
    }

    @Test
    public void testDeleteEmptyBucket() {
        BucketEntity mockedBucket = newMockedBucket(1L, "bucket-name", LocalDateTime.now());

        when(mockedBucket.getCatalogObjects()).thenReturn(new HashSet<>());
        when(bucketRepository.findBucketForUpdate(anyString())).thenReturn(mockedBucket);
        BucketMetadata bucketMetadata = bucketService.deleteEmptyBucket("bucket-name");
        verify(bucketRepository, times(1)).findBucketForUpdate("bucket-name");
        verify(bucketRepository, times(1)).delete(1L);
        assertEquals(bucketMetadata.getName(), mockedBucket.getBucketName());
    }

    @Test(expected = BucketNotFoundException.class)
    public void testDeleteInvalidBucket() {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        bucketService.deleteEmptyBucket("bucket-name");
    }

    @Test(expected = DeleteNonEmptyBucketException.class)
    public void testNotEmptyBucket() {
        BucketEntity mockedBucket = newMockedBucket(1L, "bucket-name", LocalDateTime.now());
        Set<CatalogObjectEntity> objects = new HashSet<>();
        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(mockedBucket.getId(),
                                                                                                                        "catalog"))
                                                                     .kind("object")
                                                                     .contentType("application/xml")
                                                                     .bucket(mockedBucket)
                                                                     .build();
        objects.add(catalogObjectEntity);
        when(mockedBucket.getCatalogObjects()).thenReturn(objects);
        when(bucketRepository.findBucketForUpdate(anyString())).thenReturn(mockedBucket);
        bucketService.deleteEmptyBucket("bucket-name");
        verify(bucketRepository, times(1)).findBucketForUpdate("bucket-name");
    }

    private void listBucket(String owner, Optional<String> kind, Optional<String> contentType) {
        when(bucketRepository.findAll()).thenReturn(Collections.emptyList());
        bucketService.listBuckets(owner, (kind), (contentType));
        if (!StringUtils.isEmpty(owner)) {
            verify(bucketRepository, times(1)).findByOwnerIn(anyList());
        } else if (kind.isPresent()) {
            verify(bucketRepository, times(1)).findContainingKindAndContentTypeAndObjectName(anyString(),
                                                                                             anyString(),
                                                                                             anyString());
        } else {
            verify(bucketRepository, times(1)).findAll();
        }
    }

    private BucketEntity newMockedBucket(Long id, String name, LocalDateTime createdAt) {
        BucketEntity mockedBucket = mock(BucketEntity.class);
        when(mockedBucket.getId()).thenReturn(id);
        when(mockedBucket.getBucketName()).thenReturn(name);
        return mockedBucket;
    }
}
