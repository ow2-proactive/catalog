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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class CatalogObjectRevisionControllerIntegrationTest extends AbstractCatalogObjectRevisionControllerTest {

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketId}/resources";

    private static final String CATALOG_OBJECT_REVISIONS_RESOURCE = "/buckets/{bucketId}/resources/{name}/revisions";

    private static final String BUCKETS_RESOURCE = "/buckets";
    
    private static final Long SLEEP_TIME = 501L; //in miliseconds

    protected BucketMetadata bucket;

    protected HashMap<String, Object> firstCatalogObjectRevision;

    protected HashMap<String, Object> secondCatalogObjectRevision;

    protected HashMap<String, Object> catalogObjectRevisionAlone;

    private static final String contentType = "application/xml";

    private LocalDateTime secondCatalogObjectRevisionCommitTime;

    @Before
    public void setup() throws IOException, InterruptedException {

        HashMap<String, Object> result = given().parameters("name",
                                                            "bucket",
                                                            "owner",
                                                            "WorkflowRevisionControllerIntegrationTestUser")
                                                .when()
                                                .post(BUCKETS_RESOURCE)
                                                .then()
                                                .statusCode(HttpStatus.SC_CREATED)
                                                .extract()
                                                .path("");

        bucket = new BucketMetadata(((Integer) result.get("id")).longValue(),
                                    (String) result.get("name"),
                                    (String) result.get("owner"));

        // Add an object of kind "workflow" into first bucket
        catalogObjectRevisionAlone = given().pathParam("bucketId", bucket.getMetaDataId())
                                            .queryParam("kind", "workflow")
                                            .queryParam("name", "WF_1_Rev_1")
                                            .queryParam("commitMessage", "alone commit")
                                            .queryParam("contentType", contentType)
                                            .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                            .when()
                                            .post(CATALOG_OBJECTS_RESOURCE)
                                            .then()
                                            .statusCode(HttpStatus.SC_CREATED)
                                            .extract()
                                            .path("");

        Thread.sleep(SLEEP_TIME);
        firstCatalogObjectRevision = given().pathParam("bucketId", bucket.getMetaDataId())
                                            .pathParam("name", "WF_1_Rev_1")
                                            .queryParam("commitMessage", "first commit")
                                            .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                            .when()
                                            .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                                            .then()
                                            .statusCode(HttpStatus.SC_CREATED)
                                            .extract()
                                            .path("");

        Thread.sleep(SLEEP_TIME);
        secondCatalogObjectRevision = given().pathParam("bucketId", bucket.getMetaDataId())
                                             .pathParam("name", "WF_1_Rev_1")
                                             .queryParam("commitMessage", "second commit")
                                             .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-updated.xml"))
                                             .when()
                                             .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                                             .then()
                                             .statusCode(HttpStatus.SC_CREATED)
                                             .extract()
                                             .path("");

        secondCatalogObjectRevisionCommitTime = LocalDateTime.parse((String) secondCatalogObjectRevision.get("commit_time"),
                                                                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    }

    @After
    public void cleanup() {
        IntegrationTestUtil.cleanup();
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnSavedWorkflow() {

        given().pathParam("bucketId", bucket.getMetaDataId())
               .pathParam("name", "WF_1_Rev_1")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("bucket_id", is(bucket.getMetaDataId().intValue()))
               .body("name", is("WF_1_Rev_1"));
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnUnprocessableEntityIfInvalidSyntax() {
        given().pathParam("bucketId", bucket.getMetaDataId())
               .pathParam("name", "WF_1_Rev_1")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-invalid-syntax.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testCreateWorkflowRevisionShouldWorkIfNoProjectNameInXmlPayload() {
        given().pathParam("bucketId", bucket.getMetaDataId())
               .pathParam("name", "WF_1_Rev_1")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-no-project-name.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnUnsupportedMediaTypeWithoutBody() {
        given().pathParam("bucketId", bucket.getMetaDataId())
               .pathParam("name", "WF_1_Rev_1")
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 35434245)
               .pathParam("name", "WF_1_Rev_1")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowRevisionShouldReturnSavedWorkflowRevision() {
        ValidatableResponse validatableResponse = given().pathParam("bucketId",
                                                                    secondCatalogObjectRevision.get("bucket_id"))
                                                         .pathParam("name", "WF_1_Rev_1")
                                                         .pathParam("commitTime",
                                                                    secondCatalogObjectRevisionCommitTime.atZone(ZoneId.systemDefault())
                                                                                                         .toInstant()
                                                                                                         .toEpochMilli())
                                                         .when()
                                                         .get(CATALOG_OBJECT_REVISION_RESOURCE)
                                                         .then()
                                                         .assertThat();

        String responseString = validatableResponse.extract().asString();

        System.out.println(responseString);

        System.out.println(secondCatalogObjectRevisionCommitTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        validatableResponse.statusCode(HttpStatus.SC_OK)
                           .body("bucket_id", is(secondCatalogObjectRevision.get("bucket_id")))
                           .body("name", is(secondCatalogObjectRevision.get("name")))
                           .body("commit_time", is(secondCatalogObjectRevision.get("commit_time")))
                           .body("object_key_values", hasSize(6))
                           //check generic_information label
                           .body("object_key_values[0].label", is("generic_information"))
                           .body("object_key_values[0].key", is("genericInfo1"))
                           .body("object_key_values[0].value", is("genericInfo1ValueUpdated"))
                           .body("object_key_values[1].label", is("generic_information"))
                           .body("object_key_values[1].key", is("genericInfo2"))
                           .body("object_key_values[1].value", is("genericInfo2ValueUpdated"))
                           //check job info
                           .body("object_key_values[2].label", is("job_information"))
                           .body("object_key_values[2].key", is("name"))
                           .body("object_key_values[2].value", is("Valid Workflow Updated"))
                           .body("object_key_values[3].label", is("job_information"))
                           .body("object_key_values[3].key", is("project_name"))
                           .body("object_key_values[3].value", is("Project Name Updated"))
                           //check variables label
                           .body("object_key_values[4].label", is("variable"))
                           .body("object_key_values[4].key", is("var1"))
                           .body("object_key_values[4].value", is("var1ValueUpdated"))
                           .body("object_key_values[5].label", is("variable"))
                           .body("object_key_values[5].key", is("var2"))
                           .body("object_key_values[5].value", is("var2ValueUpdated"));
    }

    @Test
    public void testGetWorkflowRevisionPayloadShouldReturnSavedRawObject() throws IOException {
        Response response = given().pathParam("bucketId", secondCatalogObjectRevision.get("bucket_id"))
                                   .pathParam("name", "WF_1_Rev_1")
                                   .pathParam("commitTime",
                                              secondCatalogObjectRevisionCommitTime.atZone(ZoneId.systemDefault())
                                                                                   .toInstant()
                                                                                   .toEpochMilli())
                                   .when()
                                   .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw");

        Arrays.equals(ByteStreams.toByteArray(response.asInputStream()),
                      IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml"));

        response.then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
               .pathParam("name", "1")
               .pathParam("commitTime", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
               .pathParam("name", "1")
               .pathParam("commitTime", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketId", 1)
               .pathParam("name", "workflow_test")
               .pathParam("commitTime", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketId", 1)
               .pathParam("name", "workflow_test")
               .pathParam("commitTime", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingCommitId() {
        given().pathParam("bucketId", 1)
               .pathParam("name", "workflow_test")
               .pathParam("commitTime", "42")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingRevisionId() {
        given().pathParam("bucketId", 1)
               .pathParam("name", "workflow_test")
               .pathParam("commitTime", "42")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListWorkflowRevisionsShouldReturnSavedRevisions() {
        IntStream.rangeClosed(1, 25).forEach(i -> {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            given().pathParam("bucketId", bucket.getMetaDataId())
                   .pathParam("name", "WF_1_Rev_1")
                   .queryParam("commitMessage", "commit message")
                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-updated.xml"))
                   .when()
                   .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                   .then()
                   .statusCode(HttpStatus.SC_CREATED);
        });

        Response response = given().pathParam("bucketId", bucket.getMetaDataId())
                                   .pathParam("name", "WF_1_Rev_1")
                                   .when()
                                   .get(CATALOG_OBJECT_REVISIONS_RESOURCE);

        response.then().assertThat().statusCode(HttpStatus.SC_OK).body("", hasSize(28));
    }

}
