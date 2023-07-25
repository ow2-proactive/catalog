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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
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

        Response response = given().header("sessionID", "12345")
                                   .parameters("name", bucketName, "owner", bucketOwner)
                                   .when()
                                   .post(BUCKETS_RESOURCE);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("name", is(bucketName))
                .body("owner", is(bucketOwner));
    }

    @Test
    public void testCreateBucketShouldReturnBadRequestWithoutBody() {
        given().header("sessionID", "12345")
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateBucketWrongName() {
        Response response = given().header("sessionID", "12345")
                                   .parameters("name", "bucket.Wrong.Name-", "bucket-owner", "bucketOwner")
                                   .when()
                                   .post(BUCKETS_RESOURCE);
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(ERROR_MESSAGE,
                      equalTo(new BucketNameIsNotValidException("bucket.Wrong.Name-").getLocalizedMessage()));
    }

    @Test
    public void testCreateDuplicatedBucketSameUser() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String ownerValue = "newowner";
        final String bucketNameValue = "newbucket";
        given().header("sessionID", "12345")
               .parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
        given().header("sessionID", "12345")
               .parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CONFLICT)
               .body(ERROR_MESSAGE,
                     equalTo(new BucketAlreadyExistingException(bucketNameValue, ownerValue).getLocalizedMessage()));
    }

    @Test
    public void testCreateDuplicatedBucketNameDifferentUser() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String ownerValue1 = "newowner1";
        final String ownerValue2 = "newowner2";
        final String bucketNameValue = "newbucket";
        given().header("sessionID", "12345")
               .parameters(ownerKey, ownerValue1, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
        given().header("sessionID", "12345")
               .parameters(ownerKey, ownerValue2, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CONFLICT)
               .body(ERROR_MESSAGE,
                     equalTo(new BucketAlreadyExistingException(bucketNameValue, ownerValue2).getLocalizedMessage()));
    }

    @Test
    public void testGetBucketShouldBeNotFoundIfNonExistingId() {
        given().pathParam("bucketName", "non-existing-bucket")
               .get(BUCKET_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new BucketNotFoundException("non-existing-bucket").getMessage()));
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
               .forEach(bucket -> given().header("sessionID", "12345")
                                         .parameters("name", bucket.getBucketName(), "owner", bucket.getOwner())
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
        given().header("sessionID", "12345")
               .parameters("name", "TotosBucket", "owner", "toto")
               .when()
               .post(BUCKETS_RESOURCE);

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
        given().header("sessionID", "12345")
               .parameters("name", bucketName1, "owner", userAlice)
               .when()
               .post(BUCKETS_RESOURCE);
        given().header("sessionID", "12345")
               .parameters("name", bucketName2, "owner", userAlice)
               .when()
               .post(BUCKETS_RESOURCE);

        // list alice buckets -> should return buckets
        given().param("owner", userAlice)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));
    }

    @Test
    public void testUpdateBucketOwner() {
        final String bucketName = "bucket-of-love";
        final String userAlice = "Alice";
        final String userBob = "Bob";
        given().header("sessionID", "12345")
               .parameters("name", bucketName, "owner", userAlice)
               .when()
               .post(BUCKETS_RESOURCE);

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketName)
               .parameters("owner", userBob)
               .when()
               .put(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);

        // list bob buckets
        given().param("owner", userBob)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
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
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "MY-objecT-Kind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // Add an object of kind "my-object-general" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithmyObjectsGeneral,
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "My-oBJeCt-General",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        String bucketWithSomeObjectsId = IntegrationTestUtil.createBucket(bucketNameWithSomeObjects, "owner");

        IntegrationTestUtil.postObjectToBucket(bucketWithSomeObjectsId,
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "differentkind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // list buckets by specific kind -> should return one specified bucket
        given().param("kind", "my-object-kind")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by prefix kind -> should return two buckets, matching kind pattern
        given().param("kind", "MY-Object")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));
    }

    @Test
    public void testListBucketsGivenKindList() throws UnsupportedEncodingException {
        final String bucketNameForWorkflows = "bucket-with-workflow";
        final String bucketNameWithSomeRules = "bucket-with-rules";
        final String bucketNameForMyObjectsWithScripts = "bucket-with-scripts";
        // Get bucket ID from response to create an object in it
        String bucketIdWithMyWorkflows = IntegrationTestUtil.createBucket(bucketNameForWorkflows, "owner");
        String bucketWithMyRules = IntegrationTestUtil.createBucket(bucketNameWithSomeRules, "owner");
        String bucketIdWithMyScripts = IntegrationTestUtil.createBucket(bucketNameForMyObjectsWithScripts, "owner");

        // Add an object of kind "my-object-kind" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithMyWorkflows,
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "workflow",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // Add an object of kind "my-object-general" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithMyScripts,
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "ScripT",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(bucketWithMyRules,
                                               "myobjectname",
                                               "myobjectprojectname",
                                               "RULE",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // list buckets by kind list -> should return all buckets, matching kindlist pattern
        given().param("kind", "workflow,SCRIPT,rule")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(3));

        // list buckets by kind list -> should return all buckets, matching kindlist pattern
        given().param("kind", "work,SCRI,rul")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(3));

        // list buckets by kind list -> should return  buckets, matching kindlist pattern
        given().param("kind", "workflow/pca,SCRIPT+s,rules")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(0));

        // list buckets by kind list and allBuckets -> should return all buckets with empty count
        List<HashMap<String, Object>> bucketEntityWithContentCountList1 = given().param("kind",
                                                                                        "workflow/pca,SCRIPT+s,rules")
                                                                                 .param("allBuckets", "true")
                                                                                 .get(BUCKETS_RESOURCE)
                                                                                 .then()
                                                                                 .assertThat()
                                                                                 .statusCode(HttpStatus.SC_OK)
                                                                                 .extract()
                                                                                 .path("");

        assertThat(bucketEntityWithContentCountList1, hasSize(3));

        assertEquals(0, sumContentCount(bucketEntityWithContentCountList1));

    }

    @Test
    public void testContentCountGivenObjectName() throws UnsupportedEncodingException {
        final String firstBucket = "first-bucket";
        final String emptyBucket = "empty-bucket";
        final String secondBucket = "second-ucket";
        final String workflowKind = "workflow";
        final String pcaKind = "pca";
        final String ruleKind = "rule";

        // Get bucket ID from response to create an object in it
        String bucketIdOfFirstBucket = IntegrationTestUtil.createBucket(firstBucket, "owner");
        String bucketIdOfEmptyBucket = IntegrationTestUtil.createBucket(emptyBucket, "owner");
        String bucketIdOfSecondBucket = IntegrationTestUtil.createBucket(secondBucket, "owner");

        // Add three objects to the first bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdOfFirstBucket,
                                               "object1",
                                               "myobjectprojectname",
                                               workflowKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(bucketIdOfFirstBucket,
                                               "object2",
                                               "myobjectprojectname",
                                               workflowKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(bucketIdOfFirstBucket,
                                               "abc",
                                               "myobjectprojectname",
                                               pcaKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        List<HashMap<String, Object>> bucketEntityWithContentCountList1 = given().param("contentType",
                                                                                        MediaType.APPLICATION_ATOM_XML_VALUE)
                                                                                 .param("kind", workflowKind)
                                                                                 .get(BUCKETS_RESOURCE)
                                                                                 .then()
                                                                                 .assertThat()
                                                                                 .statusCode(HttpStatus.SC_OK)
                                                                                 .extract()
                                                                                 .path("");

        assertEquals(2, sumContentCount(bucketEntityWithContentCountList1));

        // Add three objects to the second bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdOfSecondBucket,
                                               "object-abc",
                                               "myobjectprojectname",
                                               workflowKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(bucketIdOfSecondBucket,
                                               "object2",
                                               "myobjectprojectname",
                                               ruleKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(bucketIdOfSecondBucket,
                                               "object3",
                                               "myobjectprojectname",
                                               ruleKind,
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        List<HashMap<String, Object>> bucketEntityWithContentCountList2 = given().param("contentType",
                                                                                        MediaType.APPLICATION_ATOM_XML_VALUE)
                                                                                 .param("objectName", "abc")
                                                                                 .get(BUCKETS_RESOURCE)
                                                                                 .then()
                                                                                 .assertThat()
                                                                                 .statusCode(HttpStatus.SC_OK)
                                                                                 .extract()
                                                                                 .path("");

        assertEquals(2, sumContentCount(bucketEntityWithContentCountList2));

        List<HashMap<String, Object>> bucketEntityWithContentCountList3 = given().param("contentType",
                                                                                        MediaType.APPLICATION_ATOM_XML_VALUE)
                                                                                 .param("kind", workflowKind)
                                                                                 .param("objectName", "object")
                                                                                 .get(BUCKETS_RESOURCE)
                                                                                 .then()
                                                                                 .assertThat()
                                                                                 .statusCode(HttpStatus.SC_OK)
                                                                                 .extract()
                                                                                 .path("");

        assertEquals(3, sumContentCount(bucketEntityWithContentCountList3));

    }

    private int sumContentCount(List<HashMap<String, Object>> bucketEntityWithContentCountList) {

        return bucketEntityWithContentCountList.stream().mapToInt(map -> (Integer) map.get("objectCount")).sum();
    }

    @Test
    public void testListBucketsGivenObjectNameAndEmptyBucket() throws UnsupportedEncodingException {
        final String bucketNameForMyObjects = "bucket-with-object-workflow";
        final String bucketNameForEmpty = "empty-bucket";
        final String bucketNameWithSomeObjects = "bucket-with-some-objects";
        final String bucketNameWithOtherObjects = "bucket-with-other-objects";
        // Get bucket ID from response to create an object in it
        String bucketIdWithMyObjects = IntegrationTestUtil.createBucket(bucketNameForMyObjects, "owner");

        IntegrationTestUtil.createBucket(bucketNameForEmpty, "owner");

        // Add an object of kind "my-object-kind" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithMyObjects,
                                               "my-object-name-1",
                                               "myobjectprojectname",
                                               "MY-objecT-Kind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        String bucketWithSomeObjectsId = IntegrationTestUtil.createBucket(bucketNameWithSomeObjects, "owner");

        IntegrationTestUtil.postObjectToBucket(bucketWithSomeObjectsId,
                                               "my-object-name-2",
                                               "myobjectprojectname",
                                               "differentkind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        String bucketIdWithOtherObjects = IntegrationTestUtil.createBucket(bucketNameWithOtherObjects, "owner");
        // Add an object of kind "my-object-kind" into specific bucket
        IntegrationTestUtil.postObjectToBucket(bucketIdWithOtherObjects,
                                               "other-names",
                                               "myobjectprojectname",
                                               "MY-objecT-Kind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        // list all buckets by any Name -> should return all buckets with empty bucket
        given().param("objectName", "")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(4));

        // list buckets by specific Name -> should return one specified bucket
        given().param("objectName", "my-object-name-1")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by general Name -> should return one specified buckets
        given().param("objectName", "my-object-name")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));

        // list buckets by non-existing Name -> should return 0
        given().param("objectName", "non-existing name")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(0));

        // list buckets by specific kind and Name -> should return one specified bucket
        given().param("kind", "my-object-kind")
               .param("objectName", "my-object-name")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by content-type and Name -> should return two buckets, matching contentType, and name pattern
        given().param("contentType", MediaType.APPLICATION_ATOM_XML_VALUE)
               .param("objectName", "my-object")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(2));

        // list buckets by content-type, kind and Name -> should return 0
        given().param("contentType", MediaType.APPLICATION_ATOM_XML_VALUE)
               .param("kind", "object-kind")
               .param("objectName", "object")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(0));

        // list buckets by specific Name and owner -> should return one specified bucket
        given().param("objectName", "my-object-name-1")
               .param("owner", "owner")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
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
                                               "my workflow",
                                               "myobjectprojectname",
                                               "other-kind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postDefaultWorkflowToBucket(BucketAdminOwnerMixedKindId);
        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "other object",
                                               "myobjectprojectname",
                                               "other-kind",
                                               MediaType.APPLICATION_ATOM_XML_VALUE,
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "new object 1",
                                               "myobjectprojectname",
                                               "other-kind",
                                               "bucket-admin-owner-content-type",
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        //String bucketId, String kind, String name, String contentType, String commitMessage, File file
        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "new-kind",
                                               "new object 2",
                                               "bucket-admin-owner-content-type",
                                               "first commit",
                                               IntegrationTestUtil.getWorkflowFile("workflow.xml"));

        IntegrationTestUtil.postObjectToBucket(BucketAdminOwnerMixedKindId,
                                               "new object 3",
                                               "myobjectprojectname",
                                               "new-kind-2",
                                               "new-content-2",
                                               "first commit",
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
               .body("", hasSize(3));

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
               .body("", hasSize(2));

        // list bucket with the given kind of objects and Content-Type inside bucket -> should
        // return the specified buckets
        given().param("owner", adminOwner)
               .param("kind", "new-kind-2")
               .param("contentType", "new-content-2")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by specific kind and specified Content-Type -> should return one specified bucket
        given().param("kind", "other-kind")
               .param("contentType", "bucket-admin-owner-content-type")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list buckets by specific kind, specified Content-Type and Object name-> should return one specified bucket
        given().param("kind", "other-kind")
               .param("objectName", "object")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
    }

    @Test
    public void testDeleteEmptyBucket() {
        // Get bucket ID from response to create an object in it
        String bucketName = given().header("sessionID", "12345")
                                   .parameters("name", "bucket-name", "owner", "owner")
                                   .when()
                                   .post(BUCKETS_RESOURCE)
                                   .then()
                                   .extract()
                                   .path("name");

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketName)
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
        given().header("sessionID", "12345")
               .parameters("name", bucketName, "owner", "owner")
               .when()
               .post(BUCKETS_RESOURCE)
               .then()
               .extract()
               .path("name");

        // Add an object of kind "workflow" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketName)
               .queryParam("kind", "myobjectkind")
               .queryParam("name", "myTestName")
               .queryParam("commitMessage", "first commit")
               .queryParam("objectContentType", MediaType.APPLICATION_ATOM_XML_VALUE)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketName)
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_FORBIDDEN)
               .body(ERROR_MESSAGE, equalTo(new DeleteNonEmptyBucketException(bucketName).getLocalizedMessage()));

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
        given().header("sessionID", "12345")
               .pathParam("bucketName", "some-bucket")
               .when()
               .delete(BUCKET_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new BucketNotFoundException("some-bucket").getLocalizedMessage()));
    }

}
