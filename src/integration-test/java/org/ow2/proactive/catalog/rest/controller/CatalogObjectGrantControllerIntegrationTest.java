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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jayway.restassured.response.Response;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class CatalogObjectGrantControllerIntegrationTest extends AbstractRestAssuredTest {

    private final String bucketName = "test";

    private final String catalogObjectName = "object";

    private final String grantee = "user";

    private final String accessType = "read";

    @Before
    public void setup() {
        String currentUser = "admin";
        HashMap<String, Object> result = given().header("sessionID", "12345")
                                                .parameters("name", bucketName, "owner", currentUser)
                                                .when()
                                                .post(BUCKETS_RESOURCE)
                                                .then()
                                                .statusCode(HttpStatus.SC_CREATED)
                                                .extract()
                                                .path("");

        BucketMetadata bucket = new BucketMetadata((String) result.get("name"), (String) result.get("owner"));

        // Add an object of kind "workflow" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", catalogObjectName)
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
        IntegrationTestUtil.clearObjectGrants();
        IntegrationTestUtil.cleanup();
    }

    @Test
    public void testCreateCatalogObjectGrantForSpecificUserShouldReturnSavedGrant() {
        Response createCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType",
                                                                       accessType,
                                                                       "priority",
                                                                       1,
                                                                       "username",
                                                                       grantee)
                                                           .when()
                                                           .post(CATALOG_OBJECT_GRANTS_RESOURCE_USER);

        createCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .body("granteeType", is("user"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", is(accessType));
    }

    @Test
    public void testCreateCatalogObjectGrantForSpecificGroupShouldReturnSavedGrant() {
        Response createCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType",
                                                                       accessType,
                                                                       "priority",
                                                                       1,
                                                                       "userGroup",
                                                                       grantee)
                                                           .when()
                                                           .post(CATALOG_OBJECTS_GRANTS_RESOURCE_GROUP);

        createCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .body("granteeType", is("group"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", is(accessType));
    }

    @Test
    public void testUpdateCatalogObjectGrantForSpecificUserShouldReturnUpdatedGrant() {
        Response createCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType", accessType, "username", grantee)
                                                           .when()
                                                           .post(CATALOG_OBJECT_GRANTS_RESOURCE_USER);

        createCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .body("granteeType", is("user"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", is(accessType));

        Response updateCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType", "admin", "username", grantee)
                                                           .when()
                                                           .put(CATALOG_OBJECT_GRANTS_RESOURCE_USER);
        ;

        updateCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .statusCode(HttpStatus.SC_OK)
                                        .body("granteeType", is("user"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", not(accessType));
    }

    @Test
    public void testUpdateCatalogObjectGrantForSpecificGroupShouldReturnUpdatedGrant() {
        Response createCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType",
                                                                       accessType,
                                                                       "priority",
                                                                       1,
                                                                       "userGroup",
                                                                       grantee)
                                                           .when()
                                                           .post(CATALOG_OBJECTS_GRANTS_RESOURCE_GROUP);

        createCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .body("granteeType", is("group"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", is(accessType));

        Response updateCatalogObjectGrantResponse = given().header("sessionID", "12345")
                                                           .pathParam("bucketName", bucketName)
                                                           .pathParam("catalogObjectName", catalogObjectName)
                                                           .parameters("accessType",
                                                                       "admin",
                                                                       "priority",
                                                                       1,
                                                                       "userGroup",
                                                                       grantee)
                                                           .when()
                                                           .put(CATALOG_OBJECTS_GRANTS_RESOURCE_GROUP);

        updateCatalogObjectGrantResponse.then()
                                        .assertThat()
                                        .statusCode(HttpStatus.SC_OK)
                                        .body("granteeType", is("group"))
                                        .body("grantee", is(grantee))
                                        .body("accessType", not(accessType));
    }
}
