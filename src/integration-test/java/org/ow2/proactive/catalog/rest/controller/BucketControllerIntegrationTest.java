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
    public void testCreateDuplicatedBucketNameDifferentUser() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String ownerValue1 = "newowner1";
        final String ownerValue2 = "newowner2";
        final String bucketNameValue = "newbucket";
        given().parameters(ownerKey, ownerValue1, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
        given().parameters(ownerKey, ownerValue2, bucketNameKey, bucketNameValue)
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
    public void testListOneOwnerTwoBuckets() {
        final String bucketName1 = "TheBucketOfLove";
        final String bucketName2 = "TheBucketOfPain";
        final String userAlice = "Alice";
        given().parameters("name", bucketName1, "owner", userAlice).when().post(BUCKETS_RESOURCE);
        given().parameters("name", bucketName2, "owner", userAlice).when().post(BUCKETS_RESOURCE);

        // list alice buckets -> should return buckets
        given().param("owner", userAlice)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));
    }

    @Test
    public void testListBucketsGivenKindAndEmptyBucket() throws UnsupportedEncodingException {
        final String bucketName1 = "BucketWithObjectWorkflow";
        final String bucketName2 = "EmptyBucket";
        final String bucketNameWithSomeObjects = "BucketWithSomeObjects";
        // Get bucket ID from response to create an object in it
        Integer bucket1Id = IntegrationTestUtil.createBucket(bucketName1, "owner");

        IntegrationTestUtil.createBucket(bucketName2, "owner");

        // Add an object of kind "myobjectkind" into first bucket
        IntegrationTestUtil.postObjectToBucket(bucket1Id,
                                               "myobjectkind",
                                               "myobjectname",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        Integer bucketWithSomeObjectsId = IntegrationTestUtil.createBucket(bucketNameWithSomeObjects, "owner");

        IntegrationTestUtil.postObjectToBucket(bucketWithSomeObjectsId,
                                               "differentkind",
                                               "myobjectname",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // list workflow -> should return one only
        given().param("kind", "myobjectkind")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));
    }

    @Test
    public void testListBucketsByOwnerIsInContainingKindAndEmptyBucket() throws UnsupportedEncodingException {
        final String BucketAdminOwnerWorkflowKind = "BucketAdminOwnerWorkflowKind";
        final String BucketAdminOwnerEmptyBucket = "BucketAdminOwnerEmptyBucket";
        final String BucketAdminOwnerOtherKind = "BucketAdminOwnerOtherKind";
        final String BucketAdminOwnerMixedKind = "BucketAdminOwnerMixedKind";
        final String BucketUserOwnerWorkflowKind = "BucketUserOwnerWorkflowKind";

        final String adminOwner = "admin";
        final String userOwner = "user";

        Integer bucketAdminOwnerWorkflowKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerWorkflowKind,
                                                                                  adminOwner);
        IntegrationTestUtil.createBucket(BucketAdminOwnerEmptyBucket, adminOwner);
        Integer BucketAdminOwnerOtherKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerOtherKind, adminOwner);
        Integer BucketAdminOwnerMixedKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerMixedKind, adminOwner);
        Integer BucketUserOwnerWorkflowKindId = IntegrationTestUtil.createBucket(BucketUserOwnerWorkflowKind,
                                                                                 userOwner);

        IntegrationTestUtil.postDefaultWorkflowToBucket(bucketAdminOwnerWorkflowKindId);
        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerOtherKindId,
                                               "other kind",
                                               "my workflow",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postDefaultWorkflowToBucket(BucketAdminOwnerMixedKindId);
        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "other kind",
                                               "other object",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postDefaultWorkflowToBucket(BucketUserOwnerWorkflowKindId);

        // list bucket with the given owner -> should return one only
        given().param("owner", adminOwner)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(4));

        // list bucket with the given kind -> should return the specified buckets and empty buckets
        given().param("kind", "workflow")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(4));

        // list bucket with the given owner and kind of objects inside bucket -> should return the specified buckets
        given().param("kind", "workflow")
               .param("owner", adminOwner)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(3));
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
               .queryParam("objectContentType", MediaType.APPLICATION_ATOM_XML_VALUE)
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
