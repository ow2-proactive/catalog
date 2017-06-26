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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.rest.controller.validator.CatalogObjectNamePathParam;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectControllerTest {

    @InjectMocks
    private CatalogObjectController catalogObjectController;

    @Mock
    private CatalogObjectService catalogObjectService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Test
    public void testCreate() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(null);
        catalogObjectController.create(1L, "workflow", "name", "Commit message", "application/xml", file);
        verify(catalogObjectService, times(1)).createCatalogObject(1L,
                                                                   "workflow",
                                                                   "name",
                                                                   "Commit message",
                                                                   "application/xml",
                                                                   null);

        catalogObjectController.create(1L, "image", "name", "Commit message", "image/gif", file);
        verify(catalogObjectService, times(1)).createCatalogObject(1L,
                                                                   "image",
                                                                   "name",
                                                                   "Commit message",
                                                                   "image/gif",
                                                                   null);
    }

    @Test
    public void testList() throws Exception {
        BucketEntity bucket = mock(BucketEntity.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        catalogObjectController.list(1L, Optional.empty());
        verify(catalogObjectService, times(1)).listCatalogObjects(anyLong());
    }

    @Test
    public void testGetRaw() throws Exception {
        CatalogRawObject rawObject = new CatalogRawObject(1L,
                                                          "name",
                                                          "object",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        when(catalogObjectService.getCatalogRawObject(anyLong(), anyString())).thenReturn(rawObject);
        ResponseEntity responseEntity = catalogObjectController.getRaw(1L, new CatalogObjectNamePathParam("name"));
        verify(catalogObjectService, times(1)).getCatalogRawObject(anyLong(), anyString());
        assertThat(responseEntity).isNotNull();
    }

    @Test
    public void testGet() throws Exception {
        when(catalogObjectService.getCatalogObjectMetadata(anyLong(),
                                                           anyString())).thenReturn(new CatalogObjectMetadata(1L,
                                                                                                              "name",
                                                                                                              "object",
                                                                                                              "application/xml",
                                                                                                              1400343L,
                                                                                                              "commit message",
                                                                                                              Collections.emptyList()));
        catalogObjectController.get(1L, new CatalogObjectNamePathParam("name"));
        verify(catalogObjectService, times(1)).getCatalogObjectMetadata(anyLong(), anyString());
    }

    @Test
    public void testDelete() throws Exception {
        doNothing().when(catalogObjectService).delete(anyLong(), anyString());
        catalogObjectController.delete(1L, new CatalogObjectNamePathParam("name"));
        verify(catalogObjectService, times(1)).delete(anyLong(), anyString());
    }
}
