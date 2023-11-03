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
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.ow2.proactive.catalog.util.LinkUtil.SPACE_ENCODED_AS_PERCENT_20;
import static org.ow2.proactive.catalog.util.LinkUtil.SPACE_ENCODED_AS_PLUS;
import static org.ow2.proactive.catalog.util.RawObjectResponseCreator.WORKFLOW_EXTENSION;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.core.StringStartsWith;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.exception.*;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.ow2.proactive.catalog.util.parser.SupportedParserKinds;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class }, webEnvironment = RANDOM_PORT)
public class CatalogObjectControllerIntegrationTest extends AbstractRestAssuredTest {

    private static final String ZIP_CONTENT_TYPE = "application/zip";

    private BucketMetadata bucket;

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
               .queryParam("name", "workflowname")
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
    public void testCreateWorkflowShouldReturnSavedWorkflow() {

        Map<String, Object> expected = new HashMap<>();
        expected.put("label", "job_information");
        expected.put("key", "project_name");
        expected.put("value", "Project Name");

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "Workflow/specific-workflow-kind")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object[0].bucket_name", is(bucket.getName()))
               .body("object[0].kind", is("Workflow/specific-workflow-kind"))
               .body("object[0].name", is("workflow_test"))
               .body("object[0].extension", is("xml"))

               .body("object[0].object_key_values", hasSize(15))
               //check job info
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'project_name' }.value",
                     is("Project Name"))
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'name' }.value",
                     is("Valid Workflow"))
               //check variables label
               .body("object[0].object_key_values.find { it.label == 'variable' && it.key == 'var1' }.value",
                     is("var1Value"))
               .body("object[0].object_key_values.find { it.label == 'variable' && it.key == 'var2' }.value",
                     is("var2Value"))
               //check General label
               .body("object[0].object_key_values.find { it.label == 'General' && it.key == 'description' }.value",
                     is("\n" + "         A workflow that executes cmd in JVM. \n" + "    "))
               //check generic_information label
               .body("object[0].object_key_values.find { it.label == 'generic_information' && it.key == 'genericInfo1' }.value",
                     is("genericInfo1Value"))
               .body("object[0].object_key_values.find { it.label == 'generic_information' && it.key == 'genericInfo2' }.value",
                     is("genericInfo2Value"))
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'visualization' }.value",
                     equalToIgnoringWhiteSpace(getJobVisualizationExpectedContent()))
               .body("object[0].content_type", is(MediaType.APPLICATION_XML.toString()));
    }

    @Test
    public void testUpdateObjectMetadataAndGetItSavedObjectFromCatalog() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .queryParam("kind", "updated-kind")
               .queryParam("contentType", "updated-contentType")
               .when()
               .put(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("bucket_name", is(bucket.getName()))
               .body("kind", is("updated-kind"))
               .body("content_type", is("updated-contentType"))
               .body("extension", is("xml"));

        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("bucket_name", is(bucket.getName()))
               .body("kind", is("updated-kind"))
               .body("content_type", is("updated-contentType"));
    }

    @Test
    public void testGetAllKindsFromCatalog() throws JsonProcessingException {
        // Add an object of kind "workflow" into first bucket
        // The object with same kind should be already present in catalog
        String kindsQuery = "/buckets/kinds";
        String workflowKind = "workflow";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", workflowKind)
               .queryParam("name", "new workflow")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);
        List<String> allKinds = new ArrayList<>();
        allKinds.add(workflowKind);
        given().when().get(kindsQuery).then().assertThat().statusCode(HttpStatus.SC_OK).body("", is(allKinds));

        String newKindMy = "workflow/new_kind/mine";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", newKindMy)
               .queryParam("name", "new object")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);
        allKinds.add("workflow/new_kind");
        allKinds.add(newKindMy);
        given().when().get(kindsQuery).then().assertThat().statusCode(HttpStatus.SC_OK).body("", is(allKinds));

        String newKindNotMine = "workflow/new_kind/not-my";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", newKindNotMine)
               .queryParam("name", "new not my object")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        List<String> allKindsWithRoots = new ArrayList<>();
        allKindsWithRoots.add(workflowKind);
        allKindsWithRoots.add("workflow/new_kind");
        allKindsWithRoots.add(newKindMy);
        allKindsWithRoots.add(newKindNotMine);
        given().when().get(kindsQuery).then().assertThat().statusCode(HttpStatus.SC_OK).body("", is(allKindsWithRoots));
    }

    @Test
    public void testGetAllContentTypesFromCatalog() throws JsonProcessingException {
        String contentTypesQuery = "/buckets/content-types";
        String objectContentType = MediaType.APPLICATION_OCTET_STREAM.toString();
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "my-kind")
               .queryParam("name", "new object")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", objectContentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);
        List<String> allContentTypes = new ArrayList<>();
        allContentTypes.add(objectContentType);
        allContentTypes.add("application/xml"); // the object was pushed in the setup method
        given().when()
               .get(contentTypesQuery)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", is(allContentTypes));
    }

    @Test
    public void testCreateObjectWrongKind() {
        String wrongKind = "workflow//my";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", wrongKind)
               .queryParam("name", "new workflow")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body(ERROR_MESSAGE,
                     equalTo(new KindOrContentTypeIsNotValidException(wrongKind, "kind").getLocalizedMessage()));
    }

    @Test
    public void testCreateObjectWrongContentType() {
        String wrongContentType = "app/json-/";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow/pca")
               .queryParam("name", "new workflow")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", wrongContentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body(ERROR_MESSAGE,
                     equalTo(new KindOrContentTypeIsNotValidException(wrongContentType,
                                                                      "Content-Type").getLocalizedMessage()));
    }

    @Test
    public void testCreateObjectWrongName() {
        String wrongName = "app/json-/";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow/pca")
               .queryParam("name", wrongName)
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", "application/json")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_BAD_REQUEST)
               .body(ERROR_MESSAGE, equalTo(new ObjectNameIsNotValidException(wrongName).getLocalizedMessage()));
    }

    @Test
    public void testCreateWorkflowWithSpecificSymbolsInNameAndCheckReturnSavedWorkflow() throws IOException {
        String objectNameWithSpecificSymbols = "workflow$with&specific&symbols+in name:$&%ae.extension";
        String encodedObjectName = URLEncoder.encode(objectNameWithSpecificSymbols, "UTF-8")
                                             .replace(SPACE_ENCODED_AS_PLUS, SPACE_ENCODED_AS_PERCENT_20);

        //create the workflow and check returned metadata
        given().header("sessionID", "12345")
               .urlEncodingEnabled(true)
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow/specific-workflow-kind")
               .queryParam("name", objectNameWithSpecificSymbols)
               .queryParam("commitMessage", "first")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object[0].bucket_name", is(bucket.getName()))
               .body("object[0].kind", is("workflow/specific-workflow-kind"))
               .body("object[0].name", is(objectNameWithSpecificSymbols))
               .body("object[0].extension", is("xml"))

               .body("object[0].object_key_values", hasSize(15))
               //check job info
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'project_name' }.value",
                     is("Project Name"))
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'name' }.value",
                     is("Valid Workflow"))
               //check variables label
               .body("object[0].object_key_values.find { it.label == 'variable' && it.key == 'var1' }.value",
                     is("var1Value"))
               .body("object[0].object_key_values.find { it.label == 'variable' && it.key == 'var2' }.value",
                     is("var2Value"))
               //check General label
               .body("object[0].object_key_values.find { it.label == 'General' && it.key == 'description' }.value",
                     is("\n" + "         A workflow that executes cmd in JVM. \n" + "    "))
               //check generic_information label
               .body("object[0].object_key_values.find { it.label == 'generic_information' && it.key == 'genericInfo1' }.value",
                     is("genericInfo1Value"))
               .body("object[0].object_key_values.find { it.label == 'generic_information' && it.key == 'genericInfo2' }.value",
                     is("genericInfo2Value"))
               .body("object[0].object_key_values.find { it.label == 'job_information' && it.key == 'visualization' }.value",
                     equalToIgnoringWhiteSpace(getJobVisualizationExpectedContent()))
               .body("object[0].content_type", is(MediaType.APPLICATION_XML.toString()))
               //check link references
               .body("object[0].links[0].href",
                     containsString("/buckets/" + bucket.getName() + "/resources/" + encodedObjectName + "/raw"))
               .body("object[0].links[0].rel", is("content"))
               .body("object[0].links[1].href", is("buckets/" + bucket.getName() + "/resources/" + encodedObjectName))
               .body("object[0].links[1].rel", is("relative"));

        //check get the raw object, created on previous request with specific name
        Response rawResponse = given().urlEncodingEnabled(true)
                                      .pathParam("bucketName", bucket.getName())
                                      .pathParam("name", objectNameWithSpecificSymbols)
                                      .when()
                                      .get(CATALOG_OBJECT_RESOURCE + "/raw");
        Arrays.equals(ByteStreams.toByteArray(rawResponse.asInputStream()),
                      IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));
        rawResponse.then().assertThat().statusCode(HttpStatus.SC_OK).contentType(MediaType.APPLICATION_XML.toString());
        rawResponse.then()
                   .assertThat()
                   .header(HttpHeaders.CONTENT_DISPOSITION,
                           is("attachment; filename=\"" + objectNameWithSpecificSymbols + WORKFLOW_EXTENSION + "\""));
        rawResponse.then().assertThat().header(HttpHeaders.CONTENT_TYPE,
                                               is(new StringStartsWith(MediaType.APPLICATION_XML.toString())));

        //check get metadata of created object with specific name
        ValidatableResponse metadataResponse = given().pathParam("bucketName", bucket.getName())
                                                      .pathParam("name", objectNameWithSpecificSymbols)
                                                      .when()
                                                      .get(CATALOG_OBJECT_RESOURCE)
                                                      .then()
                                                      .assertThat()
                                                      .statusCode(HttpStatus.SC_OK);
        metadataResponse.body("links[0].rel", is("content"))
                        .body("links[0].href",
                              containsString("/buckets/" + bucket.getName() + "/resources/" + encodedObjectName +
                                             "/raw"))
                        .body("links[1].rel", is("relative"))
                        .body("links[1].href", is("buckets/" + bucket.getName() + "/resources/" + encodedObjectName));
    }

    @Test
    public void testCreatePCWRuleShouldReturnSavedRule() throws IOException {
        String ruleName = "pcw-rule test.rule";
        String fileExtension = "xml";
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "Rule/cpu")
               .queryParam("name", ruleName)
               .queryParam("commitMessage", "first commit")
               .queryParam("objectContentType", MediaType.APPLICATION_XML_VALUE)
               .multiPart(IntegrationTestUtil.getPCWRule("CPURule." + fileExtension))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object[0].bucket_name", is(bucket.getName()))
               .body("object[0].kind", is("Rule/cpu"))
               .body("object[0].name", is(ruleName))
               .body("object[0].extension", is(fileExtension))

               .body("object[0].object_key_values", hasSize(3))
               //check pcw metadata info
               .body("object[0].object_key_values[0].label", is("General"))
               .body("object[0].object_key_values[0].key", is("main.icon"))
               .body("object[0].object_key_values[0].value", is(SupportedParserKinds.PCW_RULE.getDefaultIcon()));

        //check get the raw object, created on previous request with specific name
        Response rawResponse = given().urlEncodingEnabled(true)
                                      .pathParam("bucketName", bucket.getName())
                                      .pathParam("name", ruleName)
                                      .when()
                                      .get(CATALOG_OBJECT_RESOURCE + "/raw");
        Arrays.equals(ByteStreams.toByteArray(rawResponse.asInputStream()),
                      ByteStreams.toByteArray(new FileInputStream(IntegrationTestUtil.getPCWRule("CPURule.xml"))));
        rawResponse.then().assertThat().statusCode(HttpStatus.SC_OK).contentType(MediaType.APPLICATION_XML.toString());
        rawResponse.then().assertThat().header(HttpHeaders.CONTENT_DISPOSITION,
                                               is("attachment; filename=\"" + ruleName + "." + fileExtension + "\""));
        rawResponse.then().assertThat().header(HttpHeaders.CONTENT_TYPE,
                                               is(new StringStartsWith(MediaType.APPLICATION_XML.toString())));
    }

    @Test
    public void testCreateObjectAlreadyExisting() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflowname")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CONFLICT)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectAlreadyExistingException(bucket.getName(),
                                                                       "workflowname").getLocalizedMessage()));
    }

    @Test
    public void testCreateWorkflowShouldReturnUnsupportedMediaTypeWithoutBody() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowShouldReturnNotFoundIfNonExistingBucketName() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", "non-existing-bucket")
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new BucketNotFoundException("non-existing-bucket").getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowShouldReturnLatestSavedWorkflowRevision() throws IOException {

        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflowname")
               .queryParam("commitMessage", "commit message2")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        HashMap<String, Object> thirdWFRevision = given().header("sessionID", "12345")
                                                         .pathParam("bucketName", bucket.getName())
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

        response.body("bucket_name", is(thirdWFRevision.get("bucket_name")))
                .body("name", is(thirdWFRevision.get("name")))
                .body("commit_time", is(thirdWFRevision.get("commit_time")))
                .body("object_key_values", hasSize(15))
                //check generic_information label
                .body("object_key_values.find { it.key=='bucketName' }.label", is("generic_information"))
                .body("object_key_values.find { it.key=='bucketName' }.value", is("my-bucket"))
                //check General label
                .body("object_key_values.find { it.key=='description' }.label", is("General"))
                .body("object_key_values.find { it.key=='description' }.value",
                      is("\n" + "         A workflow that executes cmd in JVM. \n" + "    "))

                .body("object_key_values.find { it.key=='genericInfo1' }.label", is("generic_information"))
                .body("object_key_values.find { it.key=='genericInfo1' }.value", is("genericInfo1Value"))

                .body("object_key_values.find { it.key=='genericInfo2' }.label", is("generic_information"))
                .body("object_key_values.find { it.key=='genericInfo2' }.value", is("genericInfo2Value"))

                .body("object_key_values.find { it.key=='group' }.label", is("generic_information"))
                .body("object_key_values.find { it.key=='group' }.value", is("BucketControllerIntegrationTestUser"))

                .body("object_key_values.find { it.key=='main.icon' }.label", is("General"))
                .body("object_key_values.find { it.key=='main.icon' }.value",
                      is("/automation-dashboard/styles/patterns/img/wf-icons/wf-default-icon.png"))

                //check job info
                .body("object_key_values.find { it.key=='name' }.label", is("job_information"))
                .body("object_key_values.find { it.key=='name' }.value", is("Valid Workflow"))

                .body("object_key_values.find { it.key=='project_name' }.label", is("job_information"))
                .body("object_key_values.find { it.key=='project_name' }.value", is("Project Name"))

                //check variables label
                .body("object_key_values.find { it.key=='var1' }.label", is("variable"))
                .body("object_key_values.find { it.key=='var1' }.value", is("var1Value"))

                .body("object_key_values.find { it.key=='var2' }.label", is("variable"))
                .body("object_key_values.find { it.key=='var2' }.value", is("var2Value"))

                .body("object_key_values.find { it.label == 'job_information' && it.key == 'visualization' }.value",
                      equalToIgnoringWhiteSpace(getJobVisualizationExpectedContent()))
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
    public void testGetCatalogObjectsByObjectKindAndContentTypeAndName() {
        String pcaKind = "pca";
        String scriptTaskKind = "Script/task";
        String groovyContentType = "text/x-groovy";

        // Add an object of kind "pca" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", pcaKind)
               .queryParam("name", "pca-WorkflowName")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        // Add an object of kind "pca" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", pcaKind)
               .queryParam("name", "pca-other-object-name")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        // Add an object of kind "script" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", scriptTaskKind)
               .queryParam("name", "pca-other-object-name-2")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", groovyContentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //check general name
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "workflow")
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        //check specific name
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "pca-workflowname")
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        //check specific 'pca' name and kind
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "pca-workflowname")
               .queryParam("kind", "pca")
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        //check specific name and content-type
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "object-name")
               .queryParam("contentType", groovyContentType)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        //check specific name and kind, content-type
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "name")
               .queryParam("kind", "pca")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        //check specific name and kind, content-type
        given().pathParam("bucketName", bucket.getName())
               .queryParam("objectName", "workflow")
               .queryParam("kind", "pca")
               .queryParam("contentType", MediaType.APPLICATION_XML.toString())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));
    }

    @Test
    public void testGetCatalogObjectsByProjectNameAndLastCommitTime() throws InterruptedException {
        // Tiny wait to ensure that the catalog object created in @Before has a different commit time than the following one
        long testStartTimeMilli = tinyWait();

        String pcaKind = "pca";
        String scriptTaskKind = "Script/task";
        String groovyContentType = "text/x-groovy";
        String projectName = "projectName";

        // Add an object in project projectName
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", pcaKind)
               .queryParam("name", "pca-WorkflowName")
               .queryParam("commitMessage", "commit message")
               .queryParam("projectName", projectName)
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        long pcaOtherObjectNameCreationTime = tinyWait();

        // Add an object not part of project projectName
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", pcaKind)
               .queryParam("name", "pca-other-object-name")
               .queryParam("commitMessage", "commit message")
               .queryParam("projectName", "another project")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        // Add a second object not part of project projectName
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", scriptTaskKind)
               .queryParam("name", "pca-other-object-name-2")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", groovyContentType)
               .queryParam("projectName", "another project")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //check that total with no project name filter, all objects are returned
        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(4));

        //check that project name filter returns only catalog object in the project
        given().pathParam("bucketName", bucket.getName())
               .queryParam("projectName", projectName)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        //check that list object created before the start of the test = 1 (= object created in @Before)
        given().pathParam("bucketName", bucket.getName())
               .queryParam("lastCommitTimeLessThan", testStartTimeMilli)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        // Test before second object creation time.
        given().pathParam("bucketName", bucket.getName())
               .queryParam("lastCommitTimeLessThan", pcaOtherObjectNameCreationTime)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        // Test after creation of first catalog object
        given().pathParam("bucketName", bucket.getName())
               .queryParam("lastCommitTimeGreater", testStartTimeMilli)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(3));

        // Test after creation of second catalog object
        given().pathParam("bucketName", bucket.getName())
               .queryParam("lastCommitTimeGreater", pcaOtherObjectNameCreationTime)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        // Test time between first and second object creation
        given().pathParam("bucketName", bucket.getName())
               .queryParam("lastCommitTimeLessThan", pcaOtherObjectNameCreationTime)
               .queryParam("lastCommitTimeGreater", testStartTimeMilli)
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));
    }

    @Test
    public void testGetCatalogObjectsNameReferenceByKindAndContentType() {
        String workflowKind = "workflow";
        String pcaKind = "pca";
        String scriptTaskKind = "Script/task";
        String xmlContentType = "application/xml";
        String groovyContentType = "text/x-groovy";

        given().header("sessionID", "12345")
               .queryParam("kind", workflowKind)
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        // Add a second object of kind "workflow" and contentType "text/x-groovy" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", workflowKind)
               .queryParam("name", "new workflow 1")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", groovyContentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        // Add a third object of kind "pca" and contentType "application/xml" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", pcaKind)
               .queryParam("name", "new workflow 2")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().header("sessionID", "12345")
               .queryParam("kind", workflowKind)
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        given().header("sessionID", "12345")
               .queryParam("kind", workflowKind)
               .queryParam("contentType", groovyContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        given().header("sessionID", "12345")
               .queryParam("kind", workflowKind)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        given().header("sessionID", "12345")
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        given().header("sessionID", "12345")
               .queryParam("kind", pcaKind)
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        //Create a second bucket
        HashMap<String, Object> result = given().header("sessionID", "12345")
                                                .parameters("name",
                                                            "my-second-bucket",
                                                            "owner",
                                                            "BucketControllerIntegrationTestUser")
                                                .when()
                                                .post(BUCKETS_RESOURCE)
                                                .then()
                                                .statusCode(HttpStatus.SC_CREATED)
                                                .extract()
                                                .path("");

        BucketMetadata secondBucket = new BucketMetadata((String) result.get("name"), (String) result.get("owner"));

        // Add two objects of kind "workflow" and "Script/task" into the second bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", secondBucket.getName())
               .queryParam("kind", workflowKind)
               .queryParam("name", "new workflow 1")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().header("sessionID", "12345")
               .pathParam("bucketName", secondBucket.getName())
               .queryParam("kind", scriptTaskKind)
               .queryParam("name", "new workflow 2")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().header("sessionID", "12345")
               .queryParam("kind", workflowKind)
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        given().header("sessionID", "12345")
               .queryParam("kind", scriptTaskKind)
               .queryParam("contentType", xmlContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(1));

        given().header("sessionID", "12345")
               .pathParam("bucketName", secondBucket.getName())
               .queryParam("kind", scriptTaskKind)
               .queryParam("name", "new workflow 3")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", groovyContentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().header("sessionID", "12345")
               .queryParam("contentType", groovyContentType)
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(2));

        // Check total number of existing objects in the Catalog
        given().header("sessionID", "12345")
               .when()
               .get(CATALOG_OBJECTS_REFERENCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("results.size()", equalTo(6));

    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingBucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .pathParam("name", "object-name")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException("non-existing-bucket",
                                                                "object-name").getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingbucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .pathParam("name", "object-name")
               .when()
               .get(CATALOG_OBJECT_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException("non-existing-bucket",
                                                                "object-name").getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "non-existing-object")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException(bucket.getName(),
                                                                "non-existing-object").getLocalizedMessage()));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "non-existing-object")
               .when()
               .get(CATALOG_OBJECT_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException(bucket.getName(),
                                                                "non-existing-object").getLocalizedMessage()));
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
    public void testListWorkflowsShouldReturnNotFoundIfNonExistingBucketName() {
        given().pathParam("bucketName", "non-existing-bucket")
               .when()
               .get(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE, equalTo(new BucketNotFoundException("non-existing-bucket").getLocalizedMessage()));
    }

    @Test
    public void testDeleteExistingObject() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
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
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException(bucket.getName(),
                                                                "workflowname").getLocalizedMessage()));
    }

    @Test
    public void testDeleteNonExistingWorkflow() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .pathParam("name", "non-existing-object")
               .when()
               .delete(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND)
               .body(ERROR_MESSAGE,
                     equalTo(new CatalogObjectNotFoundException(bucket.getName(),
                                                                "non-existing-object").getLocalizedMessage()));
    }

    @Test
    public void testGetCatalogObjectsAsArchive() {

        // Add an second object of kind "workflow" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflowname2")
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE + "?listObjectNamesForArchive=workflowname,workflowname2")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .contentType(ZIP_CONTENT_TYPE);
    }

    @Test
    public void testGetCatalogObjectWithSpecialSymbolsNamesAsArchive() {
        String nameWithSpecialSymbols = "wf n:$ %ae.myextension";
        // Add an second object of kind "workflow" into first bucket
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", nameWithSpecialSymbols)
               .queryParam("commitMessage", "commit message")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .statusCode(HttpStatus.SC_CREATED);

        //get zip file for workflow name's list separated by coma
        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE + "?listObjectNamesForArchive=workflowname," + nameWithSpecialSymbols)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .contentType(ZIP_CONTENT_TYPE);
    }

    @Test
    public void testGetCatalogObjectsAsArchiveWithNotExistingWorkflow() {

        given().pathParam("bucketName", bucket.getName())
               .when()
               .get(CATALOG_OBJECTS_RESOURCE + "?listObjectNamesForArchive=workflowname,workflownamenonexistent")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_PARTIAL_CONTENT);
    }

    @Test
    public void testCreateWorkflowsFromArchive() {
        String firstCommitMessage = "First commit";
        String archiveCommitMessage = "Import from archive";

        //Create a workflow with the bucketName of a workflow of the archive
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_existing")
               .queryParam("commitMessage", firstCommitMessage)
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);

        //Check that workflow_existing has a a first revision
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_existing")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(firstCommitMessage))
               .body("content_type", is(MediaType.APPLICATION_XML.toString()))
               .body("extension", is("xml"));

        //Check that workflow_new has no revisions
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_new")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);

        //Create workflows from the archive
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("commitMessage", archiveCommitMessage)
               .queryParam("objectContentType", MediaType.MULTIPART_FORM_DATA.toString())
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
               .body("commit_message", is(archiveCommitMessage))
               .body("content_type", is(MediaType.APPLICATION_XML.toString()))
               .body("extension", is("xml"));

        //Check that workflow_new was created
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "workflow_new")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(archiveCommitMessage))
               .body("content_type", is(MediaType.APPLICATION_XML.toString()))
               .body("extension", is("xml"));
    }

    @Test
    public void testCreateObjectDifferentTypeFromArchive() {
        String archiveCommitMessage = "Import from archive";

        //Create objects from the archive
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "my-kind")
               .queryParam("commitMessage", archiveCommitMessage)
               .queryParam("objectContentType", MediaType.MULTIPART_FORM_DATA.toString())
               .multiPart(IntegrationTestUtil.getArchiveFile("filesGCP.zip"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("object", hasSize(12));

        //Check that the object was created
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "cmdFile")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(archiveCommitMessage))
               .body("content_type", is("application/x-bat"))
               .body("extension", is("bat"));

        //Check that the object was created
        given().pathParam("bucketName", bucket.getName())
               .pathParam("name", "array")
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("commit_message", is(archiveCommitMessage))
               .body("content_type", is(MediaType.APPLICATION_JSON_VALUE))
               .body("extension", is("json"));
    }

    @Test
    public void testCreateWorkflowsFromArchiveWithBadArchive() {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucket.getName())
               .queryParam("kind", "workflow")
               .queryParam("commitMessage", "Import from archive")
               .queryParam("objectContentType", MediaType.APPLICATION_XML.toString())
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECTS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);
    }

    private String getJobVisualizationExpectedContent() {
        return "<html><head><link rel=\"stylesheet\" href=\"/studio/styles/studio-standalone.css\"><style>\n" +
               "        #workflow-designer {\n" + "            left:0 !important;\n" +
               "            top:0 !important;\n" + "            width:1427px;\n" + "            height:905px;\n" +
               "            }\n" +
               "        </style></head><body><div style=\"position:relative;top:-259px;left:-350.5px\"><div class=\"task _jsPlumb_endpoint_anchor_ ui-draggable active-task\" id=\"jsPlumb_1_1\" style=\"top: 309px; left: 450.5px;\"><a class=\"task-name\"><img src=\"/studio/images/Groovy.png\" width=\"20px\">&nbsp;<span class=\"name\">Groovy_Task</span></a></div><div class=\"_jsPlumb_endpoint source-endpoint dependency-source-endpoint connected _jsPlumb_endpoint_anchor_ ui-draggable ui-droppable\" style=\"position: absolute; height: 20px; width: 20px; left: 491px; top: 339px;\"><svg style=\"position:absolute;left:0px;top:0px\" width=\"20\" height=\"20\" pointer-events=\"all\" position=\"absolute\" version=\"1.1\"\n" +
               "      xmlns=\"http://www.w3.org/1999/xhtml\"><circle cx=\"10\" cy=\"10\" r=\"10\" version=\"1.1\"\n" +
               "      xmlns=\"http://www.w3.org/1999/xhtml\" fill=\"#666\" stroke=\"none\" style=\"\"></circle></svg></div></div></body></html>";
    }

    private long tinyWait() throws InterruptedException {
        Thread.sleep(4); // to be sure that a new revision time will be different from previous revision time
        return Instant.now().toEpochMilli();
    }
}
