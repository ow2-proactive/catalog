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
import static junit.framework.TestCase.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author ActiveEon Team
 * @since 2019-03-27
 */

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class CatalogObjectCallGraphServiceIntegrationTest {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    @Autowired
    CatalogObjectCallGraphService catalogObjectCallGraphService;

    private BucketMetadata bucket;

    private BucketMetadata secondBucket;

    private final static String KIND = "workflow";

    private final static String USERNAME = "username";

    @Before
    public void setup() {
        bucket = bucketService.createBucket("bucket", "CatalogObjectCallGraphServiceIntegrationTest");
        secondBucket = bucketService.createBucket("bucket1", "CatalogObjectCallGraphServiceIntegrationTest");
        assertThat(bucket).isNotNull();
        assertThat(bucket.getOwner()).isEqualTo("CatalogObjectCallGraphServiceIntegrationTest");
        assertThat(bucket.getName()).isNotNull();
    }

    @After
    public void deleteBucket() {
        bucketService.cleanAll();
    }

    @Test
    public void testCallGraphGeneration() throws IOException {
        String bucketName = bucket.getName();
        String secondBucketName = secondBucket.getName();
        String aWorkflow = "A_Workflow";
        String bWorkflow = "B_Workflow";
        String cWorkflow = "C_Workflow";
        String dWorkflow = "D_Workflow";
        String eWorkflow = "E_Workflow";
        String fWorkflow = "F_Workflow";
        String gWorkflow = "G_Workflow";
        String hWorkflow = "H_Workflow";
        List<String> authorisedBucketsNames = new ArrayList<>();
        authorisedBucketsNames.add(bucketName);
        authorisedBucketsNames.add(secondBucketName);

        catalogObjectService.createCatalogObject(bucketName,
                                                 dWorkflow,
                                                 KIND,
                                                 "commit message of D_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/D_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(bucketName,
                                                 eWorkflow,
                                                 KIND,
                                                 "commit message of E_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/E_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(bucketName,
                                                 cWorkflow,
                                                 KIND,
                                                 "commit message of C_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/C_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(bucketName,
                                                 bWorkflow,
                                                 KIND,
                                                 "commit message of B_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/B_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(bucketName,
                                                 aWorkflow,
                                                 KIND,
                                                 "commit message of A_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/A_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(secondBucketName,
                                                 fWorkflow,
                                                 KIND,
                                                 "commit message of F_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/F_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(secondBucketName,
                                                 gWorkflow,
                                                 KIND,
                                                 "commit message of G_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/G_Workflow.xml"),
                                                 null);

        catalogObjectService.createCatalogObject(secondBucketName,
                                                 hWorkflow,
                                                 KIND,
                                                 "commit message of H_Workflow",
                                                 USERNAME,
                                                 MediaType.APPLICATION_XML,
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("call-graph/H_Workflow.xml"),
                                                 null);

        byte[] bytesCallGraphImage = catalogObjectCallGraphService.generateBytesCallGraphImage(authorisedBucketsNames,
                                                                                               java.util.Optional.of(KIND),
                                                                                               java.util.Optional.of(MediaType.APPLICATION_XML));

        File callGraphJPGImage = new File("src/integration-test/resources/call-graph.png");
        ByteArrayInputStream bis = new ByteArrayInputStream(bytesCallGraphImage);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "png", callGraphJPGImage);
        assertTrue(callGraphJPGImage.exists());
    }
}
