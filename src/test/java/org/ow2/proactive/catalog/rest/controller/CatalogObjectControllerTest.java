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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
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
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
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

    @Mock
    private RawObjectResponseCreator rawObjectResponseCreator;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Test
    public void testGetCatalogObjectsAsArchive() throws IOException, NotAuthenticatedException, AccessDeniedException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        List<String> nameList = new ArrayList<>();
        nameList.add("workflowname");
        ZipArchiveContent content = new ZipArchiveContent();
        content.setContent(new byte[0]);
        when(catalogObjectService.getCatalogObjectsAsZipArchive("bucket-name", nameList)).thenReturn(content);
        catalogObjectController.list("",
                                     "bucket-name",
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.of(nameList),
                                     response);
        verify(catalogObjectService, times(1)).getCatalogObjectsAsZipArchive("bucket-name", nameList);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(response, times(1)).setContentType("application/zip");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_ENCODING, "binary");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"archive.zip\"");
        verify(sos, times(1)).write(any());
        verify(sos, times(1)).flush();
    }

    @Test
    public void testGetCatalogObjectsAsArchiveWithMissingObject()
            throws IOException, NotAuthenticatedException, AccessDeniedException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        List<String> nameList = new ArrayList<>();
        nameList.add("workflowname");
        ZipArchiveContent content = new ZipArchiveContent();
        content.setContent(new byte[0]);
        content.setPartial(true);
        when(catalogObjectService.getCatalogObjectsAsZipArchive("bucket-name", nameList)).thenReturn(content);
        catalogObjectController.list("",
                                     "bucket-name",
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.of(nameList),
                                     response);
        verify(catalogObjectService, times(1)).getCatalogObjectsAsZipArchive("bucket-name", nameList);
        verify(response, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testList() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);
        BucketEntity bucket = mock(BucketEntity.class);
        when(bucketRepository.findOneByBucketName("bucket-name")).thenReturn(bucket);
        catalogObjectController.list("",
                                     "bucket-name",
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.empty(),
                                     Optional.empty(),
                                     response);
        verify(catalogObjectService, times(1)).listCatalogObjects(anyList(),
                                                                  any(Optional.class),
                                                                  any(Optional.class),
                                                                  any(Optional.class));
    }

    @Test
    public void testGetRaw() throws Exception {
        CatalogRawObject rawObject = new CatalogRawObject("bucket-name",
                                                          "name",
                                                          "object",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          "username",
                                                          Collections.emptyList(),
                                                          new byte[0],
                                                          "xml");
        ResponseEntity responseEntity = ResponseEntity.ok().body(1);

        when(restApiAccessService.isAPublicBucket(anyString())).thenReturn(true);

        when(catalogObjectService.getCatalogRawObject(anyString(), anyString())).thenReturn(rawObject);
        when(rawObjectResponseCreator.createRawObjectResponse(rawObject)).thenReturn(responseEntity);
        ResponseEntity responseEntityFromController = catalogObjectController.getRaw("", "bucket-name", "name");
        verify(catalogObjectService, times(1)).getCatalogRawObject(anyString(), anyString());
        verify(rawObjectResponseCreator, times(1)).createRawObjectResponse(rawObject);
        assertThat(responseEntityFromController).isNotNull();
        assertThat(responseEntityFromController).isEqualTo(responseEntity);
    }

    @Test
    public void testDelete() throws Exception {
        CatalogObjectMetadata mock = new CatalogObjectMetadata("bucket-name",
                                                               "name",
                                                               "object",
                                                               "application/xml",
                                                               1400343L,
                                                               "commit message",
                                                               "username",
                                                               Collections.emptyList(),
                                                               "xml");
        when(catalogObjectService.delete(anyString(), anyString())).thenReturn(mock);
        CatalogObjectMetadata result = catalogObjectController.delete("", "bucket-name", "name");
        assertThat(mock).isEqualTo(result);
        verify(catalogObjectService, times(1)).delete(anyString(), anyString());
    }
}
