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

import static com.google.common.truth.Truth.assertThat;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
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
public class CatalogObjectControllerIntegrationTest extends AbstractRestAssuredTest {

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketName}/resources";

    private static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketName}/resources/{name}";

    private static final String CATALOG_OBJECT_REVISIONS_RESOURCE = "/buckets/{bucketName}/resources/{name}/revisions";

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    private BucketMetadata bucket;

    @Before
    public void setup() throws IOException {
        HashMap<String, Object> result = given().parameters("name",
                                                            "my-bucket",
                                                            "owner",
                                                            "BucketControllerIntegrationTestUser")
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
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflowname")
               .queryParam("commitMessage", "commit message")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
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
    public void testCreateWorkflowShouldReturnSavedWorkflow() {
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object[0].bucket_id", is(bucket.getName()))
               .body("object[0].kind", is("workflow"))
               .body("object[0].name", is("workflow_test"))

               .body("object[0].object_key_values", hasSize(8))
               //check job info
               .body("object[0].object_key_values[0].label", is("job_information"))
               .body("object[0].object_key_values[0].key", is("project_name"))
               .body("object[0].object_key_values[0].value", is("Project Name"))
               .body("object[0].object_key_values[1].label", is("job_information"))
               .body("object[0].object_key_values[1].key", is("name"))
               .body("object[0].object_key_values[1].value", is("Valid Workflow"))
               //check variables label
               .body("object[0].object_key_values[2].label", is("variable"))
               .body("object[0].object_key_values[2].key", is("var1"))
               .body("object[0].object_key_values[2].value", is("var1Value"))
               .body("object[0].object_key_values[3].label", is("variable"))
               .body("object[0].object_key_values[3].key", is("var2"))
               .body("object[0].object_key_values[3].value", is("var2Value"))
               //check generic_information label
               .body("object[0].object_key_values[4].label", is("generic_information"))
               .body("object[0].object_key_values[4].key", is("genericInfo1"))
               .body("object[0].object_key_values[4].value", is("genericInfo1Value"))
               .body("object[0].object_key_values[5].label", is("generic_information"))
               .body("object[0].object_key_values[5].key", is("genericInfo2"))
               .body("object[0].object_key_values[5].value", is("genericInfo2Value"))
               .body("object[0].content_type", is(MediaType.APPLICATION_XML.toString()));
    }

    @Test
    public void testCreatePCWRuleShouldReturnSavedRule() {
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "pcw-rule")
               .queryParam("name", "pcw-rule test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", "application/json")
               .multiPart(IntegrationTestUtil.getPCWRule("pcwRuleExample.json"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object[0].bucket_id", is(bucket.getName()))
               .body("object[0].kind", is("pcw-rule"))
               .body("object[0].name", is("pcw-rule test"))

               .body("object[0].object_key_values", hasSize(8))
               //check pcw metadata info
               .body("object[0].object_key_values[0].label", is("General"))
               .body("object[0].object_key_values[0].key", is("name"))
               .body("object[0].object_key_values[0].value", is("ruleNodeIsUpMetric"))
               .body("object[0].object_key_values[1].label", is("PollConfiguration"))
               .body("object[0].object_key_values[1].key", is("PollType"))
               .body("object[0].object_key_values[1].value", is("Ping"))
               .body("object[0].object_key_values[2].label", is("PollConfiguration"))
               .body("object[0].object_key_values[2].key", is("pollingPeriodInSeconds"))
               .body("object[0].object_key_values[2].value", is("100"))
               .body("object[0].object_key_values[3].label", is("PollConfiguration"))
               .body("object[0].object_key_values[3].key", is("calmPeriodInSeconds"))
               .body("object[0].object_key_values[3].value", is("50"))
               .body("object[0].object_key_values[4].label", is("PollConfiguration"))
               .body("object[0].object_key_values[4].key", is("kpis"))
               .body("object[0].object_key_values[4].value",
                     is("[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]"))
               .body("object[0].object_key_values[5].label", is("PollConfiguration"))
               .body("object[0].object_key_values[5].key", is("NodeUrls"))
               .body("object[0].object_key_values[5].value",
                     is("[\"localhost\",\"service:jmx:rmi:///jndi/rmi://192.168.1.122:52304/rmnode\"]"))
               .body("object[0].content_type", is("application/json"));
    }

    @Test
    public void testCreateWrongPCWRuleShouldReturnError() {
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "pcw-rule")
               .queryParam("name", "pcw-rule test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", "application/json")
               .multiPart(IntegrationTestUtil.getPCWRule("pcwRuleWrongToParse.json"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testCreateWorkflowShouldReturnUnsupportedMediaTypeWithoutBody() {
        given().pathParam("bucketName", bucket.getName())
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowShouldReturnNotFoundIfNonExistingbucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnLatestSavedWorkflowRevision() throws IOException {

        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .queryParam("commitMessage", "commit message2")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        HashMap<String, Object> thirdWFRevision = given().pathParam("bucketName", bucket.getName())
                                                         .pathParam("name", "workflowname")
                                                         .queryParam("commitMessage", "commit message3")
                                                         .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                                         .when()
                                                         .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                                                         .then()
                                                         .statusCode(HttpStatus.SC_CREATED)
                                                         .extract()
                                                         .path("");

        ValidatableResponse response = given().pathParam("bucketName", bucket.getName())
                                              .pathParam("name", "workflowname")
                                              .when()
                                              .get(CATALOG_OBJECT_RESOURCE)
                                              .then()
                                              .assertThat()
                                              .statusCode(HttpStatus.SC_OK);

        response.body("bucket_id", is(thirdWFRevision.get("bucket_id")))
                .body("name", is(thirdWFRevision.get("name")))
                .body("commit_time", is(thirdWFRevision.get("commit_time")))
                .body("object_key_values", hasSize(8))
                //check generic_information label
                .body("object_key_values[0].label", is("generic_information"))
                .body("object_key_values[0].key", is("bucketName"))
                .body("object_key_values[0].value", is("my-bucket"))
                .body("object_key_values[1].label", is("generic_information"))
                .body("object_key_values[1].key", is("genericInfo1"))
                .body("object_key_values[1].value", is("genericInfo1Value"))
                .body("object_key_values[2].label", is("generic_information"))
                .body("object_key_values[2].key", is("genericInfo2"))
                .body("object_key_values[2].value", is("genericInfo2Value"))
                .body("object_key_values[3].label", is("generic_information"))
                .body("object_key_values[3].key", is("group"))
                .body("object_key_values[3].value", is("BucketControllerIntegrationTestUser"))
                //check job info
                .body("object_key_values[4].label", is("job_information"))
                .body("object_key_values[4].key", is("name"))
                .body("object_key_values[4].value", is("Valid Workflow"))
                .body("object_key_values[5].label", is("job_information"))
                .body("object_key_values[5].key", is("project_name"))
                .body("object_key_values[5].value", is("Project Name"))
                //check variables label
                .body("object_key_values[6].label", is("variable"))
                .body("object_key_values[6].key", is("var1"))
                .body("object_key_values[6].value", is("var1Value"))
                .body("object_key_values[7].label", is("variable"))
                .body("object_key_values[7].key", is("var2"))
                .body("object_key_values[7].value", is("var2Value"))
                .body("content_type", is(MediaType.APPLICATION_XML.toString()));
    }

    @Test
    public void testGetRawWorkflowShouldReturnSavedRawObject() throws IOException {
        Response response = given().pathParam("bucketName", bucket.getName())
                                   .pathParam("name", "workflowname")
                                   .when()
                                   .get(CATALOG_OBJECT_RESOURCE + "/raw");

        Arrays.equals(ByteStreams.toByteArray(response.asInputStream()),
                      IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));

        response.then().assertThat().statusCode(HttpStatus.SC_OK).contentType(MediaType.APPLICATION_XML.toString());
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingbucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .pathParam("name", "23")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingbucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .pathParam("name", "42")
               .when()
               .get(CATALOG_OBJECT_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "42")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "42")
               .when()
               .get(CATALOG_OBJECT_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListWorkflowsShouldReturnSavedWorkflows() {
        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testListWorkflowsShouldReturnNotFoundIfNonExistingbucketName() {
        List<?> response = given().pathParam("bucketName", "non-existing-bucket")
                                  .when()
                                  .get(CATALOG_OBJECTS_RESOURCE)
                                  .then()
                                  .assertThat()
                                  .statusCode(HttpStatus.SC_OK)
                                  .extract()
                                  .path("");
        assertThat(response).isEmpty();
    }

    @Test
    public void testDeleteExistingObject() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .when()
               .delete(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);

        // check that the object is really gone
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteNonExistingWorkflow() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "42")
               .when()
               .delete(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetCatalogObjectsAsArchive() {

        // Add an second object of kind "workflow" into first bucket
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflowname2")
               .queryParam("commitMessage", "commit message")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE + "?name=workflowname,workflowname2")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .contentType(ZIP_CONTENT_TYPE);
    }

    @Test
    public void testGetCatalogObjectsAsArchiveWithNotExistingWorkflow() {

        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE + "?name=workflowname,workflownamenonexistent")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_PARTIAL_CONTENT);
    }

    @Test
    public void testCreateWorkflowsFromArchive() {
        String firstCommitMessage = "First commit";
        String archiveCommitMessage = "Import from archive";

        //Create a workflow with the bucketName of a workflow of the archive
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_existing")
               .queryParam("commitMessage", firstCommitMessage)
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getArchiveFile("workflow_0.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);

        //Check that workflow_exinsting has a a first revision
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_existing")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(firstCommitMessage));

        //Check that workflow_new has no revisions
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_new")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);

        //Create workflows from the archive
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("commitMessage", archiveCommitMessage)
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getArchiveFile("archive.zip"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);

        //Check that workflow_existing has a new revision
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_existing")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(archiveCommitMessage));

        //Check that workflow_new was created
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_new")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(archiveCommitMessage));
    }

    @Test
    public void testCreateWorkflowsFromArchiveWithBadArchive() {
        given().pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("commitMessage", "Import from archive")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getArchiveFile("workflow_0.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testRestoreVersion() {
        String firstCommitMessage = "First commit message";
        // Create a new object in the bucket
        Response response = given().pathParam("bucketName", bucket.getName())
                                   .queryParam("kind", "workflow")
                                   .queryParam("name", "restoredworkflow")
                                   .queryParam("commitMessage", firstCommitMessage)
                                   .queryParam("contentType", MediaType.APPLICATION_XML.toString())
                                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                   .when()
                                   .post(CATALOG_OBJECTS_RESOURCE)
                                   .then()
                                   .statusCode(HttpStatus.SC_CREATED)
                                   .extract()
                                   .response();

        String commitTime = response.path("object[0].commit_time_raw");

        //Add a new revision to the created object
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitMessage", "Second commit message")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //Restore the first version
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitTime", commitTime)
               .when()
               .put(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(firstCommitMessage));

        //Check that last revision is the restored one
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(firstCommitMessage));
    }

    @Test
    public void testRestoreVersionWithWrongParam() {
        // Create a new object in the bucket
        Response response = given().pathParam("bucketName", bucket.getName())
                                   .queryParam("kind", "workflow")
                                   .queryParam("name", "restoredworkflow")
                                   .queryParam("commitMessage", "First commit")
                                   .queryParam("contentType", MediaType.APPLICATION_XML.toString())
                                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                   .when()
                                   .post(CATALOG_OBJECTS_RESOURCE)
                                   .then()
                                   .statusCode(HttpStatus.SC_CREATED)
                                   .extract()
                                   .response();

        String commitTime = response.path("object[0].commit_time_raw");

        //Add a new revision to the created object
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitMessage", "Second commit message")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //Check wrong bucket
        given().pathParam("bucketName", bucket.getName() + 1)
               .pathParam("name", "restoredworkflow")
               .queryParam("commitTime", 0)
               .when()
               .put(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);

        //Check wrong bucketName
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "wrongrestoredworkflow")
               .queryParam("commitTime", commitTime)
               .when()
               .put(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);

        //Check wrong time
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitTime", commitTime + 1)
               .when()
               .put(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

}
