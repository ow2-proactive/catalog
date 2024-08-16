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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.CatalogObjectReportService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.springframework.http.HttpHeaders;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectReportControllerTest {

    @InjectMocks
    private CatalogObjectReportController catalogObjectReportController;

    @Mock
    private CatalogObjectReportService catalogObjectReportService;

    @Mock
    private BucketService bucketService;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Test
    public void getReport() throws Exception {

        String ownerName = "xxx";
        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        byte[] content = "some data to test".getBytes();

        List<BucketMetadata> authorisedBuckets = Lists.newArrayList(new BucketMetadata("bucket2", "xxx"),
                                                                    new BucketMetadata("bucket5", "xxx"));

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);

        when(bucketService.listBuckets(ownerName, kind, contentType)).thenReturn(authorisedBuckets);
        when(catalogObjectReportService.generateBytesReportZip(anyList(),
                                                               anyObject(),
                                                               anyObject())).thenReturn(content);

        catalogObjectReportController.getReport(response, "sessionid", "xxx", Optional.empty(), Optional.empty());
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_DISPOSITION,
                                             "attachment; filename=\"catalog_report.zip\"");
        verify(response, times(1)).addHeader(HttpHeaders.CONTENT_ENCODING, "binary");

        verify(sos, times(1)).write(content);
        verify(sos, times(1)).flush();

    }

    @Test
    public void getReportForSelectedObjectsWithNames() throws Exception {

        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        byte[] content = "some data to test".getBytes();

        List<BucketMetadata> authorisedBuckets = Lists.newArrayList(new BucketMetadata("bucket2", "xxx"),
                                                                    new BucketMetadata("bucket5", "xxx"));

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);

        when(bucketService.listBuckets("xxx", kind, contentType)).thenReturn(authorisedBuckets);
        when(catalogObjectReportService.generateBytesReportForSelectedObjects(anyString(),
                                                                              anyList(),
                                                                              anyObject(),
                                                                              anyObject())).thenReturn(content);

        String bucketName = "basic-examples";
        Optional<List<String>> catalogObjectsNames = Optional.of(Lists.newArrayList("object1"));
        catalogObjectReportController.getReportForSelectedObjects(response,
                                                                  "sessionId",
                                                                  bucketName,
                                                                  kind,
                                                                  contentType,
                                                                  catalogObjectsNames);
        verify(response, times(1)).addHeader("Content-size", new Integer(content.length).toString());
        verify(response, times(1)).setCharacterEncoding("UTF-8");

        verify(sos, times(1)).write(content);

        verify(sos, times(1)).flush();

    }

    @Test
    public void getReportForSelectedObjectsWithoutNames() throws Exception {

        Optional<String> kind = Optional.empty();
        Optional<String> contentType = Optional.empty();

        byte[] content = "some data to test".getBytes();

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream sos = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(sos);

        when(catalogObjectReportService.generateBytesReport(anyList(), anyObject(), anyObject())).thenReturn(content);

        String bucketName = "basic-examples";
        Optional<List<String>> catalogObjectsNames = Optional.empty();
        catalogObjectReportController.getReportForSelectedObjects(response,
                                                                  "sessionId",
                                                                  bucketName,
                                                                  kind,
                                                                  contentType,
                                                                  catalogObjectsNames);
        verify(response, times(1)).addHeader("Content-size", new Integer(content.length).toString());
        verify(response, times(1)).setCharacterEncoding("UTF-8");

        verify(sos, times(1)).write(content);

        verify(sos, times(1)).flush();

    }

}
