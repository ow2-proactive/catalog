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

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.IOException;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = RANDOM_PORT)
public class CatalogObjectReportControllerIntegrationTest extends AbstractRestAssuredTest {

    private BucketMetadata bucket;

    private String bucketObjectName = "workflowname";

    private String projectName = "projectName";

    @Before
    public void setup() throws IOException {

        HashMap<String, Object> result = given().header("sessionID", "12345")
                                                .parameters("name",
                                                            "my-bucket",
                                                            "owner",
                                                            "BucketControllerIntegrationTestUser")
                                                .when()
                                                .post(BUCKETS_RESOURCE)
                                                .then()
                                                .statusCode(HttpStatus.SC_CREATED)
                                                .extract()
                                                .path("");

        bucket = new BucketMetadata((String) result.get("name"), (String) result.get("owner"));

        // Add an object of kind "workflow" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", bucketObjectName)
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);
    }

    @After
    public void cleanup() {
        IntegrationTestUtil.cleanup();
    }

    @Test
    public void getReportTest() {
        given().when().get(CATALOG_OBJECT_REPORT).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void getReportForSelectedObjectsTest() {

        RequestSpecification request = given().pathParam("bucketName", bucket.getName());

        JSONArray requestParamArray = new JSONArray();
        requestParamArray.add(bucketObjectName);

        request.header("Content-Type", "application/json");
        request.body(requestParamArray.toJSONString());

        Response response = request.post(CATALOG_OBJECT_BUCKET_REPORT);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }
}
