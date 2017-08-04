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
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
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
public class BucketControllerIntegrationTest extends AbstractRestAssuredTest {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String BUCKET_RESOURCE = "/buckets/{bucketId}";

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketId}/resources";

    private static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketId}/resources/{name}";

    @After
    public void cleanup() {
        IntegrationTestUtil.cleanup();
    }

    @Test
    public void testCreateBucketShouldReturnSavedBucket() {
        final String bucketName = "test";
        final String bucketOwner = "BucketControllerIntegrationTest";

        Response response = given().parameters("name", bucketName, "owner", bucketOwner).when().post(BUCKETS_RESOURCE);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", is(bucketName))
                .body("owner", is(bucketOwner));
    }

    @Test
    public void testCreateBucketShouldReturnBadRequestWithoutBody() {
        when().post(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicatedBucketSameUser() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String ownerValue = "newowner";
        final String bucketNameValue = "newbucket";
        given().parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
        given().parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testGetBucketShouldBeNotFoundIfNonExistingId() {
        given().pathParam("bucketId", 42L).get(BUCKET_RESOURCE).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListBucketsShouldReturnSavedBuckets() {

        List<?> existingBucketsList = given().get(BUCKETS_RESOURCE)
                                             .then()
                                             .statusCode(HttpStatus.SC_OK)
                                             .extract()
                                             .path("");

        List<BucketEntity> buckets = IntStream.rangeClosed(1, 25)
                                              .mapToObj(i -> new BucketEntity("bucket" + i,
                                                                              "BucketResourceAssemblerTestUser"))
                                              .collect(Collectors.toList());

        buckets.stream().forEach(bucket -> given().parameters("name", bucket.getName(), "owner", bucket.getOwner())
                                                  .when()
                                                  .post(BUCKETS_RESOURCE));

        given().parameter("owner", "BucketResourceAssemblerTestUser")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(25 + existingBucketsList.size()));
    }

    @Test
    public void testListBucketsOwnerShouldReturnNothing() {
        given().parameters("name", "TotosBucket", "owner", "toto").when().post(BUCKETS_RESOURCE);

        given().param("owner", "nonExistingUser")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(0));
    }

    @Test
    public void testListOneBucketNameTwoOwners() {
        final String bucketName = "TheBucketOfLove";
        final String userAlice = "Alice";
        final String userBob = "Bob";
        given().parameters("name", bucketName, "owner", userAlice).when().post(BUCKETS_RESOURCE);
        given().parameters("name", bucketName, "owner", userBob).when().post(BUCKETS_RESOURCE);

        // list alice -> should return one only
        given().param("owner", userAlice)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list bob -> should return one only
        given().param("owner", userBob)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
    }

    @Test
    public void testListBucketsGivenKindAndEmptyBucket() throws UnsupportedEncodingException {
        final String bucketName1 = "BucketWithObjectWorkflow";
        final String bucketName2 = "EmptyBucket";
        final String bucketNameWithSomeObjects = "BucketWithSomeObjects";
        // Get bucket ID from response to create an object in it
        Integer bucket1Id = given().parameters("name", bucketName1, "owner", "owner")
                                   .when()
                                   .post(BUCKETS_RESOURCE)
                                   .then()
                                   .extract()
                                   .path("id");

        given().parameters("name", bucketName2, "owner", "owner ").when().post(BUCKETS_RESOURCE);

        // Add an object of kind "myobjectkind" into first bucket
        given().pathParam("bucketId", bucket1Id)
               .queryParam("kind", "myobjectkind")
               .queryParam("name", "myobjectname")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", "application/xml")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);

        Integer bucketWithSomeObjectsId = given().parameters("name", bucketNameWithSomeObjects, "owner", "owner")
                                                 .when()
                                                 .post(BUCKETS_RESOURCE)
                                                 .then()
                                                 .extract()
                                                 .path("id");

        // Add an object of kind "differentkind" into third bucket
        given().pathParam("bucketId", bucketWithSomeObjectsId)
               .queryParam("kind", "differentkind")
               .queryParam("name", "myobjectname")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", "application/xml")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);

        // list workflow -> should return one only
        given().param("kind", "myobjectkind")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));
    }

    @Test
    public void testDeleteEmptyBucket() {
        // Get bucket ID from response to create an object in it
        Integer bucketId = given().parameters("name", "bucketName", "owner", "owner")
                                  .when()
                                  .post(BUCKETS_RESOURCE)
                                  .then()
                                  .extract()
                                  .path("id");

        given().pathParam("bucketId", bucketId)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);

        // check that the bucket is really gone
        given().pathParam("bucketId", bucketId)
               .when()
               .get(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonEmptyBucket() throws IOException {
        final String bucketName = "BucketWithObject";

        // Get bucket ID from response to create an object in it
        Integer bucketId = given().parameters("name", bucketName, "owner", "owner")
                                  .when()
                                  .post(BUCKETS_RESOURCE)
                                  .then()
                                  .extract()
                                  .path("id");

        // Add an object of kind "workflow" into first bucket
        given().pathParam("bucketId", bucketId)
               .queryParam("kind", "myobjectkind")
               .queryParam("name", "myTestName")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", "application/xml")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);

        given().pathParam("bucketId", bucketId)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_FORBIDDEN);

        // check that the bucket is still there
        given().pathParam("bucketId", bucketId)
               .when()
               .get(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testDeleteNonExistingBucket() {
        given().pathParam("bucketId", 35434247)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

}
