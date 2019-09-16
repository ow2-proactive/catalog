package org.ow2.proactive.catalog.rest.controller;


import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.CatalogObjectReportService;
import org.ow2.proactive.catalog.service.RestApiAccessService;


@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectReportControllerTestDspot {
    @InjectMocks
    private CatalogObjectReportController catalogObjectReportController;

    @Mock
    private CatalogObjectReportService catalogObjectReportService;

    @Mock
    private BucketService bucketService;

    @Mock
    private RestApiAccessService restApiAccessService;

    @Test(timeout = 10000)
    public void getReportnull5435_failAssert0() throws Exception {
        try {
            List<String> ownerName = Lists.newArrayList("xxx");
            Optional<String> kind = Optional.empty();
            Optional<String> contentType = Optional.empty();
            byte[] content = "some data to test".getBytes();
            List<BucketMetadata> authorisedBuckets = Lists.newArrayList(new BucketMetadata("bucket2", "xxx"), new BucketMetadata("bucket5", "xxx"));
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
            ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
            Mockito.when(response.getOutputStream()).thenReturn(sos);
            Mockito.when(bucketService.listBuckets("xxx", kind, contentType)).thenReturn(null);
            Mockito.when(catalogObjectReportService.generateBytesReport(Matchers.anyList(), Matchers.anyObject(), Matchers.anyObject())).thenReturn(content);
            catalogObjectReportController.getReport(response, "sessionid", "xxx", Optional.empty(), Optional.empty());
            Mockito.verify(response, Mockito.times(1)).addHeader("Content-size", new Integer(content.length).toString());
            Mockito.verify(response, Mockito.times(1)).setCharacterEncoding("UTF-8");
            Mockito.verify(sos, Mockito.times(1)).write(content);
            Mockito.verify(sos, Mockito.times(1)).flush();
            org.junit.Assert.fail("getReportnull5435 should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            Assert.assertEquals(null, expected.getMessage());
        }
    }
}

