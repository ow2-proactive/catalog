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
import static org.hamcrest.Matchers.equalTo;
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
import org.ow2.proactive.catalog.service.exception.BucketAlreadyExistingException;
import org.ow2.proactive.catalog.service.exception.BucketNameIsNotValidException;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DeleteNonEmptyBucketException;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class BucketControllerIntegrationTest extends AbstractRestAssuredTest {

    private final ObjectMapper mapper = new ObjectMapper();

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
    public void testCreateBucketWrongName() {
        Response response = given().parameters("name", "bucket.Wrong.Name-", "bucket-owner", "bucketOwner")
                                   .when()
                                   .post(BUCKETS_RESOURCE);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("error_message",
                      equalTo(new BucketNameIsNotValidException("bucket.Wrong.Name-").getLocalizedMessage()));
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
               .statusCode(HttpStatus.SC_CONFLICT)
               .body("error_message",
                     equalTo(new BucketAlreadyExistingException(bucketNameValue, ownerValue).getLocalizedMessage()));
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
               .statusCode(HttpStatus.SC_CONFLICT)
               .body("error_message",
                     equalTo(new BucketAlreadyExistingException(bucketNameValue, ownerValue2).getLocalizedMessage()));
    }

    @Test
    public void testGetBucketShouldBeNotFoundIfNonExistingId() {
        given().pathParam("bucketName", "non-existing-bucket")
               .get(BUCKET_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body("error_message",
                     equalTo(new BucketNotFoundException("non-existing-bucket").getLocalizedMessage()));
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

        buckets.stream()
               .forEach(bucket -> given().parameters("name", bucket.getBucketName(), "owner", bucket.getOwner())
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
        final String bucketName1 = "bucket-of-love";
        final String bucketName2 = "bucket-of-pain";
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
    public void testListBucketsGivenPrefixKindAndEmptyBucket() throws UnsupportedEncodingException {
        final String bucketNameForMyObjects = "bucket-with-object-workflow";
        final String bucketNameForEmpty = "empty-bucket";
        final String bucketNameWithSomeObjects = "bucket-with-some-objects";
        final String bucketNameForMyObjectsWithGeneralPrefix = "bucket-with-my-objects-general";
        // Get bucket ID from response to create an object in it
        String bucketIdWithMyObjects = IntegrationTestUtil.createBucket(bucketNameForMyObjects, "owner");
        String bucketIdWithmyObjectsGeneral = IntegrationTestUtil.createBucket(bucketNameForMyObjectsWithGeneralPrefix,
                                                                               "owner");

        IntegrationTestUtil.createBucket(bucketNameForEmpty, "owner");

        // Add an object of kind "my-object-kind" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithMyObjects,
                                               "MY-objecT-Kind",
                                               "myobjectname",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // Add an object of kind "my-object-general" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithmyObjectsGeneral,
                                               "My-oBJeCt-General",
                                               "myobjectname",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        String bucketWithSomeObjectsId = IntegrationTestUtil.createBucket(bucketNameWithSomeObjects, "owner");

        IntegrationTestUtil.postObjectToBucket(bucketWithSomeObjectsId,
                                               "differentkind",
                                               "myobjectname",
                                               "first commit",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // list buckets by specific kind -> should return one specified bucket and empty bucket
        given().param("kind", "my-object-kind")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));

        // list buckets by prefix kind -> should return two buckets, matching kind pattern, and empty bucket
        given().param("kind", "MY-Object")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(3));
    }

    @Test
    public void testListBucketsByOwnerIsInContainingKindAndEmptyBucket() throws UnsupportedEncodingException {
        final String BucketAdminOwnerWorkflowKind = "bucket-admin-owner-workflow-kind";
        final String BucketAdminOwnerEmptyBucket = "bucket-admin-owner-empty-bucket";
        final String BucketAdminOwnerOtherKind = "bucket-admin-owner-other-kind";
        final String BucketAdminOwnerMixedKind = "bucket-admin-owner-mixed-kind";
        final String BucketUserOwnerWorkflowKind = "bucket-user-owner-workflow-kind";

        final String adminOwner = "admin";
        final String userOwner = "user";

        String bucketAdminOwnerWorkflowKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerWorkflowKind,
                                                                                 adminOwner);
        IntegrationTestUtil.createBucket(BucketAdminOwnerEmptyBucket, adminOwner);
        String BucketAdminOwnerOtherKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerOtherKind, adminOwner);
        String BucketAdminOwnerMixedKindId = IntegrationTestUtil.createBucket(BucketAdminOwnerMixedKind, adminOwner);
        String BucketUserOwnerWorkflowKindId = IntegrationTestUtil.createBucket(BucketUserOwnerWorkflowKind, userOwner);

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

        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "other kind",
                                               "other new object",
                                               "first commit",
                                               "bucket-admin-owner-content-type",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "new-kind",
                                               "first commit",
                                               "bucket-admin-owner-content-type",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "new-kind-2",
                                               "other new object",
                                               "first commit",
                                               "new-content-2",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postDefaultWorkflowToBucket(BucketUserOwnerWorkflowKindId);

        // list bucket with the given owner -> should return one only
        given().param("owner", adminOwner)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(4));

        // list bucket with the given kind -> should return the specified buckets and empty
        // buckets
        given().param("kind", "workflow")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(4));

        // list bucket with the given kind -> should return the specified buckets and empty
        // buckets
        given().param("kind", "new-kind")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list bucket with the given owner and kind of objects inside bucket -> should return
        // the specified buckets
        given().param("kind", "workflow")
               .param("owner", adminOwner)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(3));

        // list bucket with the given kind of objects and content type inside bucket -> should
        // return the specified buckets
        given().param("owner", adminOwner)
               .param("kind", "new-kind-2")
               .param("objectContentType", "new-content-2")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by specific kind and specified content type -> should return one specified bucket and empty bucket
        given().param("kind", "my-object-kind")
               .param("contentType", "my-content")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
    }

    @Test
    public void testDeleteEmptyBucket() {
        // Get bucket ID from response to create an object in it
        String bucketName = given().parameters("name", "bucket-name", "owner", "owner")
                                   .when()
                                   .post(BUCKETS_RESOURCE)
                                   .then()
                                   .extract()
                                   .path("name");

        given().pathParam("bucketName", bucketName)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);

        // check that the bucket is really gone
        given().pathParam("bucketName", bucketName)
               .when()
               .get(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonEmptyBucket() throws IOException {
        final String bucketName = "bucket-with-object";

        // Get bucket name from response to create an object in it
        given().parameters("name", bucketName, "owner", "owner")
               .when()
               .post(BUCKETS_RESOURCE)
               .then()
               .extract()
               .path("name");

        // Add an object of kind "workflow" into first bucket
        given().pathParam("bucketName", bucketName)
               .queryParam("kind", "myobjectkind")
               .queryParam("name", "myTestName")
               .queryParam("commitMessage", "first commit")
               .queryParam("objectContentType", MediaType.APPLICATION_ATOM_XML_VALUE)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);

        given().pathParam("bucketName", bucketName)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_FORBIDDEN)
               .body("error_message", equalTo(new DeleteNonEmptyBucketException(bucketName).getLocalizedMessage()));

        // check that the bucket is still there
        given().pathParam("bucketName", bucketName)
               .when()
               .get(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testDeleteNonExistingBucket() {
        given().pathParam("bucketName", "some-bucket")
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body("error_message", equalTo(new BucketNotFoundException("some-bucket").getLocalizedMessage()));
    }

}
