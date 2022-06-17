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
import static org.hamcrest.Matchers.*;
import static org.ow2.proactive.catalog.util.GrantHelper.GROUP_GRANTEE_TYPE;
import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
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
public class BucketGrantControllerIntegrationTest extends AbstractRestAssuredTest {

    private final String bucketName = "test";

    private final String bucketOwner = "BucketControllerIntegrationTest";

    private final String grantee = "user";

    private final String accessType = "read";

    @After
    public void cleanup() {
        IntegrationTestUtil.deleteBucketGrants();
        IntegrationTestUtil.cleanup();
    }

    @Test
    public void testCreateBucketGrantForSpecificUserShouldReturnSavedGrant() {
        Response createBucketResponse = given().header("sessionID", "12345")
                                               .parameters("name", bucketName, "owner", bucketOwner)
                                               .when()
                                               .post(BUCKETS_RESOURCE);

        createBucketResponse.then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_CREATED)
                            .body("name", is(bucketName))
                            .body("owner", is(bucketOwner));

        Response createBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType", accessType, "username", grantee)
                                                    .when()
                                                    .post(BUCKET_GRANTS_RESOURCE_USER);

        createBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_CREATED)
                                 .body("accessType", is(accessType))
                                 .body("granteeType", is(USER_GRANTEE_TYPE))
                                 .body("grantee", is(grantee));
    }

    @Test
    public void testCreateBucketGrantForSpecificUserGroupShouldReturnSavedGrant() {
        Response createBucketResponse = given().header("sessionID", "12345")
                                               .parameters("name", bucketName, "owner", bucketOwner)
                                               .when()
                                               .post(BUCKETS_RESOURCE);

        createBucketResponse.then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_CREATED)
                            .body("name", is(bucketName))
                            .body("owner", is(bucketOwner));

        Response createBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType",
                                                                accessType,
                                                                "priority",
                                                                1,
                                                                "userGroup",
                                                                grantee)
                                                    .when()
                                                    .post(BUCKET_GRANTS_RESOURCE_GROUP);

        createBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_CREATED)
                                 .body("accessType", is(accessType))
                                 .body("granteeType", is(GROUP_GRANTEE_TYPE))
                                 .body("grantee", is(grantee));
    }

    @Test
    public void testUpdateBucketGrantForSpecificUser() {
        Response createBucketResponse = given().header("sessionID", "12345")
                                               .parameters("name", bucketName, "owner", bucketOwner)
                                               .when()
                                               .post(BUCKETS_RESOURCE);

        createBucketResponse.then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_CREATED)
                            .body("name", is(bucketName))
                            .body("owner", is(bucketOwner));

        Response createBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType", accessType, "username", grantee)
                                                    .when()
                                                    .post(BUCKET_GRANTS_RESOURCE_USER);

        createBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_CREATED)
                                 .body("accessType", is(accessType))
                                 .body("grantee", is(grantee));

        Response updateBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType", "admin", "username", grantee)
                                                    .when()
                                                    .put(BUCKET_GRANTS_RESOURCE_USER);

        updateBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_OK)
                                 .body("granteeType", is(USER_GRANTEE_TYPE))
                                 .body("accessType", not(accessType))
                                 .body("grantee", is(grantee));
    }

    @Test
    public void testUpdateBucketGrantForSpecificUserGroup() {
        Response createBucketResponse = given().header("sessionID", "12345")
                                               .parameters("name", bucketName, "owner", bucketOwner)
                                               .when()
                                               .post(BUCKETS_RESOURCE);

        createBucketResponse.then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_CREATED)
                            .body("name", is(bucketName))
                            .body("owner", is(bucketOwner));

        Response createBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType",
                                                                accessType,
                                                                "priority",
                                                                1,
                                                                "userGroup",
                                                                grantee)
                                                    .when()
                                                    .post(BUCKET_GRANTS_RESOURCE_GROUP);

        createBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_CREATED)
                                 .body("accessType", is(accessType))
                                 .body("grantee", is(grantee));

        Response updateBucketGrantResponse = given().header("sessionID", "12345")
                                                    .pathParam("bucketName", bucketName)
                                                    .parameters("accessType",
                                                                "admin",
                                                                "priority",
                                                                1,
                                                                "userGroup",
                                                                grantee)
                                                    .when()
                                                    .put(BUCKET_GRANTS_RESOURCE_GROUP);

        updateBucketGrantResponse.then()
                                 .assertThat()
                                 .statusCode(HttpStatus.SC_OK)
                                 .body("accessType", not(accessType))
                                 .body("grantee", is(grantee));
    }
}
