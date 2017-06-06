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
import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.rest.Application;
import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.entity.Bucket;
import org.ow2.proactive.catalog.rest.util.IntegrationTestUtil;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class CatalogObjectRevisionControllerIntegrationTest extends AbstractCatalogObjectRevisionControllerTest {

    protected Bucket bucket;

    protected CatalogObjectMetadata firstCatalogObjectRevision;

    protected CatalogObjectMetadata secondCatalogObjectRevision;

    protected CatalogObjectMetadata catalogObjectRevisionAlone;

    private static final String contentType = "application/xml";

    @Before
    public void setup() throws IOException {
        bucket = bucketRepository.save(new Bucket("bucket", "WorkflowRevisionControllerIntegrationTestUser"));

        catalogObjectRevisionAlone = catalogObjectRevisionService.createCatalogObjectRevision(bucket.getId(),
                                                                                              "workflow",
                                                                                              "WF_2_Rev_1",
                                                                                              "alone commit",
                                                                                              Optional.empty(),
                                                                                              contentType,
                                                                                              IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));

        firstCatalogObjectRevision = catalogObjectRevisionService.createCatalogObjectRevision(bucket.getId(),
                                                                                              "workflow",
                                                                                              "WF_1_Rev_1",
                                                                                              "first commit",
                                                                                              Optional.empty(),
                                                                                              contentType,
                                                                                              IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));

        secondCatalogObjectRevision = catalogObjectRevisionService.createCatalogObjectRevision(bucket.getId(),
                                                                                               "workflow",
                                                                                               "WF_1_Rev_2",
                                                                                               "second commit",
                                                                                               Optional.of(firstCatalogObjectRevision.id),
                                                                                               contentType,
                                                                                               IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml"));

    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnSavedWorkflow() {
        int lastUuidForNewRevision = secondCatalogObjectRevision.commitId.intValue() + 1;

        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED)
               .body("bucket_id", is(bucket.getId().intValue()))
               .body("id", is(firstCatalogObjectRevision.id.intValue()))
               .body("id", is(secondCatalogObjectRevision.id.intValue()))
               .body("name", is("workflow_test"))
               .body("commit_id", is(lastUuidForNewRevision));
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnUnprocessableEntityIfInvalidSyntax() {
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
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
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
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
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowRevisionShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
               .when()
               .post(CATALOG_OBJECT_REVISIONS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowRevisionShouldReturnSavedWorkflowRevision() {
        given().pathParam("bucketId", secondCatalogObjectRevision.bucketId)
               .pathParam("objectId", secondCatalogObjectRevision.id)
               .pathParam("commitId", secondCatalogObjectRevision.commitId)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("bucket_id", is(bucket.getId().intValue()))
               .body("id", is(secondCatalogObjectRevision.id.intValue()))
               .body("name", is(secondCatalogObjectRevision.name))
               .body("commit_id", is(secondCatalogObjectRevision.commitId.intValue()))
               .body("object_key_values", hasSize(4))
               //check variables label
               .body("object_key_values[0].label", is("variable"))
               .body("object_key_values[0].key", is("var1"))
               .body("object_key_values[0].value", is("var1ValueUpdated"))
               .body("object_key_values[1].label", is("variable"))
               .body("object_key_values[1].key", is("var2"))
               .body("object_key_values[1].value", is("var2ValueUpdated"))
               //check generic_information label
               .body("object_key_values[2].label", is("generic_information"))
               .body("object_key_values[2].key", is("genericInfo1"))
               .body("object_key_values[2].value", is("genericInfo1ValueUpdated"))
               .body("object_key_values[3].label", is("generic_information"))
               .body("object_key_values[3].key", is("genericInfo2"))
               .body("object_key_values[3].value", is("genericInfo2ValueUpdated"));
    }

    @Test
    public void testGetWorkflowRevisionPayloadShouldReturnSavedRawObject() throws IOException {
        Response response = given().pathParam("bucketId", secondCatalogObjectRevision.bucketId)
                                   .pathParam("objectId", secondCatalogObjectRevision.id)
                                   .pathParam("commitId", secondCatalogObjectRevision.commitId)
                                   //                                      .queryParam("contentType", "application/xml")
                                   .when()
                                   .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw");

        Arrays.equals(ByteStreams.toByteArray(response.asInputStream()),
                      catalogObjectRevisionRepository.getCatalogObjectRevision(secondCatalogObjectRevision.bucketId,
                                                                               secondCatalogObjectRevision.id,
                                                                               secondCatalogObjectRevision.commitId)
                                                     .getRawObject());

        response.then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
               .pathParam("objectId", 1)
               .pathParam("commitId", 1)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
               .pathParam("objectId", 1)
               .pathParam("commitId", 1)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketId", 1)
               .pathParam("objectId", 42)
               .pathParam("commitId", 1)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingObjectId() {
        given().pathParam("bucketId", 1)
               .pathParam("objectId", 42)
               .pathParam("commitId", 1)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE + "/raw")
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowShouldReturnNotFoundIfNonExistingCommitId() {
        given().pathParam("bucketId", 1)
               .pathParam("objectId", 1)
               .pathParam("commitId", 42)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnNotFoundIfNonExistingRevisionId() {
        given().pathParam("bucketId", 1)
               .pathParam("objectId", 1)
               .pathParam("commitId", 42)
               .queryParam("kind", "workflow")
               .queryParam("name", "workflow_test")
               .queryParam("commitMessage", "first commit")
               .queryParam("contentType", contentType)
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
                catalogObjectRevisionService.createCatalogObjectRevision(secondCatalogObjectRevision.bucketId,
                                                                         "workflow",
                                                                         "name",
                                                                         "commit message",
                                                                         Optional.of(secondCatalogObjectRevision.id),
                                                                         contentType,
                                                                         IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        });

        Response response = given().pathParam("bucketId", secondCatalogObjectRevision.bucketId)
                                   .pathParam("objectId", secondCatalogObjectRevision.id)
                                   .when()
                                   .get(CATALOG_OBJECT_REVISIONS_RESOURCE);

        int pageSize = response.getBody().jsonPath().getInt("page.size");

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.catalogObjectMetadataList", hasSize(pageSize))
                .body("page.number", is(0))
                .body("page.totalElements", is(25 + 2));
    }

    @Test
    public void testDeleteTheUniqueWorkflowRevision() {
        given().when()
               .pathParam("bucketId", bucket.getId())
               .pathParam("objectId", catalogObjectRevisionAlone.id)
               .pathParam("commitId", catalogObjectRevisionAlone.commitId)
               .delete(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testDeleteAPreviousWorkflowRevisionOfMultipleRevisions() {
        // because they're from the same workflow
        assertEquals(firstCatalogObjectRevision.id, secondCatalogObjectRevision.id);

        // firstWorkflowRevision is the previous revision
        // secondWorkflowRevision is the current revision
        // The workflow currently references secondWorkflowRevision
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .pathParam("commitId", firstCatalogObjectRevision.commitId)
               .when()
               .delete(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
        // check that the workflow continues to
        // reference secondWorkflowRevision
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", secondCatalogObjectRevision.id)
               .pathParam("commitId", secondCatalogObjectRevision.commitId)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
        // and that the previous revision is actually gone
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .pathParam("commitId", firstCatalogObjectRevision.commitId)
               .when()
               .get(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testDeleteTheLatestWorkflowRevisionOfMultipleRevisions() {
        // because they're from the same workflow
        assertEquals(firstCatalogObjectRevision.id, secondCatalogObjectRevision.id);

        // firstWorkflowRevision is the previous revision
        // secondWorkflowRevision is the current revision
        // The workflow currently references secondWorkflowRevision
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", secondCatalogObjectRevision.id)
               .pathParam("commitId", secondCatalogObjectRevision.commitId)
               .when()
               .delete(CATALOG_OBJECT_REVISION_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);

        // check that the workflow references the previous version
        given().pathParam("bucketId", bucket.getId())
               .pathParam("objectId", firstCatalogObjectRevision.id)
               .when()
               .get(CATALOG_OBJECT_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK);
    }
}
