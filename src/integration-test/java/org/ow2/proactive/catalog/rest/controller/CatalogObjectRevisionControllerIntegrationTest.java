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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.ow2.proactive.catalog.util.LinkUtil.SPACE_ENCODED_AS_PERCENT_20;
import static org.ow2.proactive.catalog.util.LinkUtil.SPACE_ENCODED_AS_PLUS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
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
public class CatalogObjectRevisionControllerIntegrationTest extends AbstractCatalogObjectRevisionControllerTest {

    private static final Long SLEEP_TIME = 503L; //in milliseconds

    protected BucketMetadata bucket;

    protected HashMap<String, Object> firstCatalogObjectRevision;

    protected HashMap<String, Object> secondCatalogObjectRevision;

    protected HashMap<String, Object> catalogObjectRevisionAlone;

    private static final String objectContentType = "application/xml";

    private LocalDateTime secondCatalogObjectRevisionCommitTime;

    @Before
    public void setup() throws IOException, InterruptedException {

        HashMap<String, Object> result = given().header("sessionID", "12345")
                                                .parameters("name",
                                                            "bucket",
                                                            "owner",
                                                            "WorkflowRevisionControllerIntegrationTestUser")
                                                .when()
                                                .post(BUCKETS_RESOURCE)
                                                .then()
                                                .statusCode(HttpStatus.SC_CREATED)
                                                .extract()
                                                .path("");

        bucket = new BucketMetadata((String) result.get("name"), (String) result.get("owner"));

        // Add an object of kind "workflow" into first bucket
        catalogObjectRevisionAlone = given().header("sessionID", "12345")
                                            .pathParam("bucketName", bucket.getName())
                                            .queryParam("kind", "workflow")
                                            .queryParam("name", "WF_1_Rev_1.xml")
                                            .queryParam("commitMessage", "alone commit")
                                            .queryParam("objectContentType", objectContentType)
                                            .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                            .when()
                                            .post(CATALOG_OBJECTS_RESOURCE)
                                            .then()
                                            .statusCode(HttpStatus.SC_CREATED)
                                            .extract()
                                            .path("");

        Thread.sleep(SLEEP_TIME);
        firstCatalogObjectRevision = given().header("sessionID", "12345")
                                            .pathParam("bucketName", bucket.getName())
                                            .pathParam("name", "WF_1_Rev_1.xml")
                                            .queryParam("commitMessage", "first commit")
                                            .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                            .when()
                                            .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                                            .then()
                                            .statusCode(HttpStatus.SC_CREATED)
                                            .extract()
                                            .path("");

        Thread.sleep(SLEEP_TIME);
        secondCatalogObjectRevision = given().header("sessionID", "12345")
                                             .pathParam("bucketName", bucket.getName())
                                             .pathParam("name", "WF_1_Rev_1.xml")
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

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "WF_1_Rev_1.xml")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("bucket_name", is(bucket.getName()))
               .body("name", is("WF_1_Rev_1.xml"));
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnUnprocessableEntityIfInvalidSyntax() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "WF_1_Rev_1.xml")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-invalid-syntax.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
               .body(ERROR_MESSAGE, containsString(ParsingObjectException.ERROR_MESSAGE));
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnCreatedIfNoProjectNameInXmlPayload() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "WF_1_Rev_1.xml")
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
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "WF_1_Rev_1.xml")
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnNotFoundIfNonExistingBucketName() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", "non-existing-bucket")
               .pathParam("name", "WF_1_Rev_1.xml")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new BucketNotFoundException("non-existing-bucket").getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowRevisionShouldReturnSavedWorkflowRevision() throws UnsupportedEncodingException {
        ValidatableResponse validatableResponse = given().pathParam("bucketName", bucket.getName())
                                                         .pathParam("name", "WF_1_Rev_1.xml")
                                                         .pathParam("commitTimeRaw",
                                                                    secondCatalogObjectRevisionCommitTime.atZone(ZoneId.systemDefault())
                                                                                                         .toInstant()
                                                                                                         .toEpochMilli())
                                                         .when()
                                                         .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
                                                         .then()
                                                         .assertThat();

        String responseString = validatableResponse.extract().asString();

        System.out.println(responseString);

        System.out.println(secondCatalogObjectRevisionCommitTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        String encodedObjectName = URLEncoder.encode(secondCatalogObjectRevision.get("name").toString(), "UTF-8")
                                             .replace(SPACE_ENCODED_AS_PLUS, SPACE_ENCODED_AS_PERCENT_20);

        validatableResponse.statusCode(HttpStatus.SC_OK)
                           .body("bucket_name", is(secondCatalogObjectRevision.get("bucket_name")))
                           .body("name", is(secondCatalogObjectRevision.get("name")))
                           .body("commit_time", is(secondCatalogObjectRevision.get("commit_time")))
                           .body("object_key_values", hasSize(14))
                           //check generic_information label
                           .body("object_key_values.find { it.key=='bucketName' }.label", is("generic_information"))
                           .body("object_key_values.find { it.key=='bucketName' }.value", is("bucket"))

                           .body("object_key_values.find { it.key=='description' }.label", is("General"))
                           .body("object_key_values.find { it.key=='description' }.value",
                                 is("\n" + "         A catalogObject that executes cmd in JVM. \n" + "    "))

                           .body("object_key_values.find { it.key=='genericInfo1' }.label", is("generic_information"))
                           .body("object_key_values.find { it.key=='genericInfo1' }.value",
                                 is("genericInfo1ValueUpdated"))

                           .body("object_key_values.find { it.key=='genericInfo2' }.label", is("generic_information"))
                           .body("object_key_values.find { it.key=='genericInfo2' }.value",
                                 is("genericInfo2ValueUpdated"))

                           .body("object_key_values.find { it.key=='group' }.label", is("generic_information"))
                           .body("object_key_values.find { it.key=='group' }.value",
                                 is("WorkflowRevisionControllerIntegrationTestUser"))

                           .body("object_key_values.find { it.key=='main.icon' }.label", is("General"))
                           .body("object_key_values.find { it.key=='main.icon' }.value",
                                 is("/automation-dashboard/styles/patterns/img/wf-icons/wf-default-icon.png"))

                           //check job info
                           .body("object_key_values.find { it.key=='name' }.label", is("job_information"))
                           .body("object_key_values.find { it.key=='name' }.value", is("Valid Workflow Updated"))

                           .body("object_key_values.find { it.key=='project_name' }.label", is("job_information"))
                           .body("object_key_values.find { it.key=='project_name' }.value", is("Project Name Updated"))

                           //check variables label
                           .body("object_key_values.find { it.key=='var1' }.label", is("variable"))
                           .body("object_key_values.find { it.key=='var1' }.value", is("var1ValueUpdated"))

                           .body("object_key_values.find { it.key=='var2' }.label", is("variable"))
                           .body("object_key_values.find { it.key=='var2' }.value", is("var2ValueUpdated"))
                           //check link references
                           .body("_links.content.href",
                                 containsString("/buckets/" + bucket.getName() + "/resources/" + encodedObjectName +
                                                "/revisions/" + secondCatalogObjectRevision.get("commit_time_raw") +
                                                "/raw"))
                           .body("_links.relative.href",
                                 is("buckets/" + bucket.getName() + "/resources/" + encodedObjectName + "/revisions/" +
                                    secondCatalogObjectRevision.get("commit_time_raw")));
    }

    @Test
    public void testGetWorkflowRevisionPayloadShouldReturnSavedRawObject() throws IOException {
        Response response = given().pathParam("bucketName", bucket.getName())
                                   .pathParam("name", "WF_1_Rev_1.xml")
                                   .pathParam("commitTimeRaw",
                                              secondCatalogObjectRevisionCommitTime.atZone(ZoneId.systemDefault())
                                                                                   .toInstant()
                                                                                   .toEpochMilli())
                                   .when()
                                   .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME + "/raw");

        Arrays.equals(ByteStreams.toByteArray(response.asInputStream()),
                      IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml"));

        response.then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingBucketName() {
        given().pathParam("bucketName", "non-existing")
               .pathParam("name", "object-name")
               .pathParam("commitTimeRaw", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new RevisionNotFoundException("non-existing",
                                                                          "object-name",
                                                                          1).getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingBucketName() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "object-name")
               .pathParam("commitTimeRaw", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new RevisionNotFoundException(bucket.getName(),
                                                                          "object-name",
                                                                          1).getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_test")
               .pathParam("commitTimeRaw", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "workflow_test",
                                                           1).getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_test")
               .pathParam("commitTimeRaw", "1")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "workflow_test",
                                                           1).getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingCommitId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_test")
               .pathParam("commitTimeRaw", "42")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "workflow_test",
                                                           42).getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingRevisionId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_test")
               .pathParam("commitTimeRaw", "42")
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "workflow_test",
                                                           42).getLocalizedMessage()));
    }

    @Test
    public void testListWorkflowRevisionsShouldReturnSavedRevisions() {
        IntStream.rangeClosed(1, 25).forEach(i -> {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            given().header("sessionID", "12345")
                   .pathParam("bucketName", bucket.getName())
                   .pathParam("name", "WF_1_Rev_1.xml")
                   .queryParam("commitMessage", "commit message")
                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow-updated.xml"))
                   .when()
                   .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
                   .then()
                   .statusCode(HttpStatus.SC_CREATED);
        });

        Response response = given().pathParam("bucketName", bucket.getName())
                                   .pathParam("name", "WF_1_Rev_1.xml")
                                   .when()
                                   .get(CATALOG_OBJECT_REVISIONS_RESOURCE);

        response.then().assertThat().statusCode(HttpStatus.SC_OK).body("", hasSize(28));
    }

    @Test
    public void testRestoreVersion() {
        String firstCommitMessage = "First commit message";
        // Create a new object in the bucket
        Response response = given().header("sessionID", "12345")
                                   .pathParam("bucketName", bucket.getName())
                                   .queryParam("name", "restoredworkflow")
                                   .queryParam("kind", "workflow")
                                   .queryParam("commitMessage", firstCommitMessage)
                                   .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
                                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                   .when()
                                   .post(CATALOG_OBJECTS_RESOURCE)
                                   .then()
                                   .statusCode(HttpStatus.SC_CREATED)
                                   .extract()
                                   .response();

        String commitTime = response.path("object[0].commit_time_raw");

        //Add a new revision to the created object
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitMessage", "Second commit message")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //Restore the first version
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .pathParam("commitTimeRaw", commitTime)
               .when()
               .put(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", containsString(firstCommitMessage));

        //Check that last revision is the restored one
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", containsString(firstCommitMessage));
    }

    @Test
    public void testRestoreVersionWithWrongParam() {
        // Create a new object in the bucket
        Response response = given().header("sessionID", "12345")
                                   .pathParam("bucketName", bucket.getName())
                                   .queryParam("kind", "workflow")
                                   .queryParam("name", "restoredworkflow")
                                   .queryParam("commitMessage", "First commit")
                                   .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
                                   .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                                   .when()
                                   .post(CATALOG_OBJECTS_RESOURCE)
                                   .then()
                                   .statusCode(HttpStatus.SC_CREATED)
                                   .extract()
                                   .response();

        String commitTime = response.path("object[0].commit_time_raw");

        //Add a new revision to the created object
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .queryParam("commitMessage", "Second commit message")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //Check wrong bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName() + 1)
               .pathParam("name", "restoredworkflow")
               .pathParam("commitTimeRaw", 0)
               .when()
               .put(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException("bucket1", "restoredworkflow", 0).getLocalizedMessage()));

        //Check wrong bucketName
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "wrongrestoredworkflow")
               .pathParam("commitTimeRaw", commitTime)
               .when()
               .put(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "wrongrestoredworkflow",
                                                           Long.valueOf(commitTime)).getLocalizedMessage()));

        //Check wrong time
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "restoredworkflow")
               .pathParam("commitTimeRaw", commitTime + 1)
               .when()
               .put(CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new RevisionNotFoundException(bucket.getName(),
                                                           "restoredworkflow",
                                                           Long.valueOf(commitTime + 1)).getLocalizedMessage()));
    }

}
