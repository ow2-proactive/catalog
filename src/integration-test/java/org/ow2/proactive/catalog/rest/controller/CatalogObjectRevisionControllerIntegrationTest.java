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
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class CatalogObjectRevisionControllerIntegrationTest extends AbstractCatalogObjectRevisionControllerTest {

    protected BucketEntity bucket;

    protected CatalogObjectMetadata firstCatalogObjectRevision;

    protected CatalogObjectMetadata secondCatalogObjectRevision;

    protected CatalogObjectMetadata catalogObjectRevisionAlone;

    private static final String contentType = "application/xml";

    @Before
    public void setup() throws IOException {
        bucketRepository.deleteAll();
        catalogObjectRepository.deleteAll();

        bucket = bucketRepository.save(new BucketEntity("bucket", "WorkflowRevisionControllerIntegrationTestUser"));

        catalogObjectRevisionAlone = catalogObjectService.createCatalogObject(bucket.getId(),
                                                                              "WF_1_Rev_1",
                                                                              "workflow",
                                                                              "alone commit",
                                                                              contentType,
                                                                              IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));

        firstCatalogObjectRevision = catalogObjectService.createCatalogObjectRevision(bucket.getId(),
                                                                                      "WF_1_Rev_1",
                                                                                      "first commit",
                                                                                      IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));

        secondCatalogObjectRevision = catalogObjectService.createCatalogObjectRevision(bucket.getId(),
                                                                                       "WF_1_Rev_1",
                                                                                       "second commit",
                                                                                       IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml"));

    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnSavedWorkflow() {

        given().pathParam("bucketId", bucket.getId())
               .pathParam("name", "WF_1_Rev_1")
               .queryParam("commitMessage", "first commit")
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("bucket_id", is(bucket.getId().intValue()))
               .body("name", is("WF_1_Rev_1"));
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnUnprocessableEntityIfInvalidSyntax() {
        given().pathParam("bucketId", bucket.getId())
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
        given().pathParam("bucketId", bucket.getId())
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
        given().pathParam("bucketId", bucket.getId())
               .pathParam("name", "WF_1_Rev_1")
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
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
        given().pathParam("bucketId", secondCatalogObjectRevision.getBucketId())
               .pathParam("name", "WF_1_Rev_1")
               .pathParam("commitTime",
                          secondCatalogObjectRevision.getCommitDateTime()
                                                     .atZone(ZoneId.systemDefault())
                                                     .toInstant()
                                                     .toEpochMilli())
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("bucket_id", is(bucket.getId().intValue()))
               .body("name", is(secondCatalogObjectRevision.getName()))
               .body("commit_time", is(secondCatalogObjectRevision.getCommitDateTime().toString()))
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
        Response response = given().pathParam("bucketId", secondCatalogObjectRevision.getBucketId())
                                   .pathParam("name", "WF_1_Rev_1")
                                   .pathParam("commitTime",
                                              secondCatalogObjectRevision.getCommitDateTime()
                                                                         .atZone(ZoneId.systemDefault())
                                                                         .toInstant()
                                                                         .toEpochMilli())
                                   .when()
                                   .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw");

        Arrays.equals(ByteStreams.toByteArray(response.asInputStream()),
                      catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(secondCatalogObjectRevision.getBucketId(),
                                                                                            "WF_1_Rev_1",
                                                                                            secondCatalogObjectRevision.getCommitDateTime()
                                                                                                                       .atZone(ZoneId.systemDefault())
                                                                                                                       .toInstant()
                                                                                                                       .toEpochMilli())
                                                     .getRawObject());

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
                catalogObjectService.createCatalogObjectRevision(secondCatalogObjectRevision.getBucketId(),
                                                                 "WF_1_Rev_1",
                                                                 "commit message",
                                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        });

        int size = catalogObjectService.listCatalogObjectRevisions(secondCatalogObjectRevision.getBucketId(),
                                                                   "WF_1_Rev_1")
                                       .size();

        Response response = given().pathParam("bucketId", secondCatalogObjectRevision.getBucketId())
                                   .pathParam("name", "WF_1_Rev_1")
                                   .when()
                                   .get(CATALOG_OBJECT_REVISIONS_RESOURCE);

        response.then().assertThat().statusCode(HttpStatus.SC_OK).body("", hasSize(size));
    }

}
