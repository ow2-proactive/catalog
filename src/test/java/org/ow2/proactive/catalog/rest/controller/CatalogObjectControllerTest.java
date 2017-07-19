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
import static org.mockito.Mockito.never;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;


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
    private ArchiveManagerHelper archiveManagerHelper;

    @Test
    public void testGetCatalogObjectsAsArchive() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        List<String> nameList = new ArrayList<>();
        nameList.add("workflowname");
        when(catalogObjectService.getCatalogObjectsAsZipArchive(1L, nameList)).thenReturn(new byte[0]);
        catalogObjectController.list(1L, Optional.empty(), Optional.of(nameList), response);
        verify(catalogObjectService, times(1)).getCatalogObjectsAsZipArchive(1L, nameList);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setContentType("application/zip");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
        verify(sos, times(1)).write(Mockito.any());
        verify(sos, times(1)).flush();
    }

    @Test
    public void testGetCatalogObjectsAsArchiveWithMissingObject() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        List<String> nameList = new ArrayList<>();
        nameList.add("workflowname");
        when(catalogObjectService.getCatalogObjectsAsZipArchive(1L, nameList)).thenReturn(null);
        catalogObjectController.list(1L, Optional.empty(), Optional.of(nameList), response);
        verify(catalogObjectService, times(1)).getCatalogObjectsAsZipArchive(1L, nameList);
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testList() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        BucketEntity bucket = mock(BucketEntity.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        catalogObjectController.list(1L, Optional.empty(), Optional.empty(), response);
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
        ResponseEntity responseEntity = catalogObjectController.getRaw(1L, "name");
        verify(catalogObjectService, times(1)).getCatalogRawObject(anyLong(), anyString());
        assertThat(responseEntity).isNotNull();
    }

    @Test
    public void testDelete() throws Exception {
        doNothing().when(catalogObjectService).delete(anyLong(), anyString());
        catalogObjectController.delete(1L, "name");
        verify(catalogObjectService, times(1)).delete(anyLong(), anyString());
    }
}
