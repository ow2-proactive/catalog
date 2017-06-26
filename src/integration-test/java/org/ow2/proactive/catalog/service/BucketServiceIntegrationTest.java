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
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.rest.controller.AbstractRestAssuredTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.path.json.JsonPath;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class BucketServiceIntegrationTest extends AbstractRestAssuredTest {

    private static final Logger log = LoggerFactory.getLogger(BucketServiceIntegrationTest.class);

    private static final String DEFAULT_OBJECTS_FOLDER = "/default-objects";

    private static final String RAW_OBJECTS_FOLDER = "/raw-objects";

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketId}/resources";

    @Autowired
    private BucketService bucketService;

    @Test
    public void testPopulateCatalogEmpty() throws Exception {
        bucketService.populateCatalog(new String[] {}, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);
        when().get(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    /*
     * Create 3 buckets, check that the buckets exists
     * 1 bucket is empty
     */
    @Test
    public void testPopulateCatalogCheckBucketsCreation() throws Exception {
        given().delete(BUCKETS_RESOURCE).then().statusCode(HttpStatus.SC_OK);

        final String[] buckets = { "Examples", "Cloud-automation", "Toto" };
        bucketService.populateCatalog(buckets, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);

        // verify that all buckets have been created in the Catalog
        String response = when().get(BUCKETS_RESOURCE)
                                .then()
                                .assertThat()
                                .statusCode(HttpStatus.SC_OK)
                                .extract()
                                .response()
                                .asString();

        List<Map<String, ?>> jsonList = JsonPath.from(response).get("");

        jsonList.stream().forEach(map -> {
            String name = (String) map.get("name");
            Integer id = (Integer) map.get("id");
            int nbWorkflows = 0;
            String[] workflows = new File(Application.class.getResource(DEFAULT_OBJECTS_FOLDER).getPath() +
                                          File.separator + name).list();
            if (workflows != null) {
                nbWorkflows = workflows.length;
            }

            if (nbWorkflows > 0) {

                String bucketResponse = given().pathParam("bucketId", id)
                                               .when()
                                               .get(CATALOG_OBJECTS_RESOURCE)
                                               .then()
                                               .assertThat()
                                               .statusCode(HttpStatus.SC_OK)
                                               .extract()
                                               .response()
                                               .asString();

                List<Map<String, ?>> bucketWorkflowList = JsonPath.from(bucketResponse).get("");
                assertThat(bucketWorkflowList).hasSize(nbWorkflows);
            } else {
                given().pathParam("bucketId", id)
                       .when()
                       .get(CATALOG_OBJECTS_RESOURCE)
                       .then()
                       .assertThat()
                       .statusCode(HttpStatus.SC_NOT_FOUND);
            }

        });

    }

}
