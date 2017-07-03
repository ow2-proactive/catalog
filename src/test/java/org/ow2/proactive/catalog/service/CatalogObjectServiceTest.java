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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.KeyValueMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;

import com.google.common.collect.ImmutableList;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectServiceTest {

    public static final String COMMIT_MESSAGE = "commit message";

    public static final String APPLICATION_XML = "application/xml";

    public static final String OBJECT = "object";

    public static final String NAME = "catalog";

    @InjectMocks
    private CatalogObjectService catalogObjectService;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Mock
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Mock
    private BucketRepository bucketRepository;

    @Test(expected = BucketNotFoundException.class)
    public void testCreateCatalogObjectWithInvalidBucket() {
        when(bucketRepository.findOne(anyLong())).thenReturn(null);
        catalogObjectService.createCatalogObject(1L, NAME, OBJECT, COMMIT_MESSAGE, APPLICATION_XML, null);
    }

    @Test
    public void testCreateCatalogObject() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(bucketRepository.findOne(anyLong())).thenReturn(bucketEntity);
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(System.currentTimeMillis());
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);

        List<KeyValueMetadata> keyValues = ImmutableList.of(new KeyValueMetadata("key", "value", null));

        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(1L,
                                                                                       NAME,
                                                                                       OBJECT,
                                                                                       COMMIT_MESSAGE,
                                                                                       APPLICATION_XML,
                                                                                       keyValues,
                                                                                       null);
        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObject.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObject.getKind()).isEqualTo(OBJECT);
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getKeyValueMetadataList()).isNotEmpty();
        assertThat(catalogObject.getKeyValueMetadataList()).hasSize(1);
    }

    @Test
    public void testGetCatalogObjectMetadata() {
        long now = System.currentTimeMillis();

        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(now);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyLong(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        CatalogObjectMetadata objectMetadata = catalogObjectService.getCatalogObjectMetadata(1L, "name");

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(anyLong(),
                                                                                                 anyString());
        assertThat(objectMetadata).isNotNull();
        assertThat(objectMetadata.getName()).isEqualTo(NAME);
        assertThat(objectMetadata.getKind()).isEqualTo(OBJECT);
        assertThat(objectMetadata.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(objectMetadata.getCommitDateTime()
                                 .atZone(ZoneId.systemDefault())
                                 .toInstant()
                                 .toEpochMilli() == now).isTrue();
        assertThat(objectMetadata.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);

    }

    private CatalogObjectEntity newCatalogObjectEntity(long now) {
        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(1L,
                                                                                                                        "catalog"))
                                                                     .kind("object")
                                                                     .contentType("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueMetadataEntity> keyvalues = ImmutableList.of(new KeyValueMetadataEntity("key", "value", null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(COMMIT_MESSAGE)
                                                                                             .commitTime(now)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectEntity;
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevisionEntity(long now) {
        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(1L,
                                                                                                                        "catalog"))
                                                                     .kind("object")
                                                                     .contentType("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueMetadataEntity> keyvalues = ImmutableList.of(new KeyValueMetadataEntity("key", "value", null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(COMMIT_MESSAGE)
                                                                                             .commitTime(now)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectRevisionEntity;
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testFindWorkflowInvalidId() throws Exception {
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyLong(),
                                                                                    anyString())).thenReturn(null);
        catalogObjectService.getCatalogObjectMetadata(1L, "name");
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testCreateCatalogObjectRevisionNotFound() {
        when(catalogObjectRepository.findOne(any(CatalogObjectEntity.CatalogObjectEntityKey.class))).thenReturn(null);
        List<KeyValueMetadataEntity> keyvalues = ImmutableList.of(new KeyValueMetadataEntity("key", "value", null));
        catalogObjectService.createCatalogObjectRevision(1L, NAME, COMMIT_MESSAGE, null);
    }

    @Test
    public void testCreateCatalogObjectRevision() {
        CatalogObjectEntity catalogObjectEntity = newCatalogObjectEntity(System.currentTimeMillis());
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = newCatalogObjectRevisionEntity(System.currentTimeMillis());
        when(catalogObjectRepository.findOne(any(CatalogObjectEntity.CatalogObjectEntityKey.class))).thenReturn(catalogObjectEntity);
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectRevisionEntity);
        List<KeyValueMetadata> keyvalues = ImmutableList.of(new KeyValueMetadata("key", "value", null));
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObjectRevision(1L,
                                                                                               NAME,
                                                                                               COMMIT_MESSAGE,
                                                                                               keyvalues,
                                                                                               null);
        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObject.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObject.getKind()).isEqualTo(OBJECT);
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getKeyValueMetadataList()).isNotEmpty();
        assertThat(catalogObject.getKeyValueMetadataList()).hasSize(1);
    }

    @Test(expected = RevisionNotFoundException.class)
    public void testGetCatalogObjectRevisionNotFound() {
        long now = System.currentTimeMillis();
        when(catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(anyLong(),
                                                                                   anyString(),
                                                                                   anyLong())).thenReturn(null);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectService.getCatalogObjectRevisionEntityByCommitTime(1L,
                                                                                                                                  NAME,
                                                                                                                                  now);

    }

    @Test
    public void testGetCatalogObjectRevision() {
        long now = System.currentTimeMillis();
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(now);
        when(catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(anyLong(),
                                                                                   anyString(),
                                                                                   anyLong())).thenReturn(catalogObjectEntity);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectService.getCatalogObjectRevisionEntityByCommitTime(1L,
                                                                                                                                  NAME,
                                                                                                                                  now);

        assertThat(catalogObjectRevisionEntity).isNotNull();
        assertThat(catalogObjectRevisionEntity.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getKind()).isEqualTo(OBJECT);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getId().getName()).isEqualTo(NAME);
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).isNotEmpty();
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).hasSize(1);
    }
}
