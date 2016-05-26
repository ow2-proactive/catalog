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

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.controller.AbstractRestAssuredTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest(randomPort = true)
public class BucketServiceIntegrationTest extends AbstractRestAssuredTest {

    private static final Logger log = LoggerFactory.getLogger(BucketServiceIntegrationTest.class);
    private static final String DEFAULT_WORKFLOWS_FOLDER = "/default-workflows";
    private static final String BUCKETS_RESOURCE = "/buckets";
    private static final String WORKFLOWS_RESOURCE = "/buckets/{bucketId}/workflows";

    @Autowired
    private BucketService bucketService;

    @Test
    public void testPopulateCatalogEmpty() throws Exception {
        bucketService.populateCatalog(new String[]{}, DEFAULT_WORKFLOWS_FOLDER);
        when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("page.number", is(0))
                .body("page.totalElements", is(0));
    }

    /*
        Create 3 buckets, check that the buckets exists
        1 bucket is empty
     */
    @Test
    public void testPopulateCatalogCheckBucketsCreation() throws Exception {
        final String[] buckets = {"Templates", "Cloud-automation", "Toto"};
        bucketService.populateCatalog(buckets, DEFAULT_WORKFLOWS_FOLDER);

        // verify that all buckets have been created in the Catalog
        String response = when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("page.number", is(0))
                .body("page.totalElements", is(buckets.length))
                .body("_embedded.bucketMetadataList.name", hasItems(buckets))
                .extract().response().asString();

        // verify that buckets contains the same number of workflows as in the disk
        for (String bucket : buckets) {
            int nbWorkflows = 0;
            String[] workflows = new File(Application.class
                    .getResource(DEFAULT_WORKFLOWS_FOLDER).getPath() + File.separator + bucket).list();
            if (workflows != null) {
                nbWorkflows = workflows.length;
            }
            Long bucketId = from(response).getLong(
                    "_embedded.bucketMetadataList.find {b -> b.name== '" + bucket + "'}.id");
            given()
                    .pathParam("bucketId", bucketId)
                    .when()
                    .get(WORKFLOWS_RESOURCE)
                    .then()
                    .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("page.totalElements", is(nbWorkflows));
        }
    }

}
