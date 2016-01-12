/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.controller;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.InMemoryConfiguration;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRepository;
import org.ow2.proactive.workflow_catalog.rest.util.IntegrationTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author ActiveEon Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, InMemoryConfiguration.class})
@WebIntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WorkflowControllerIntegrationTest {

    private static final String WORKFLOWS_RESOURCE = "/buckets/{bucketId}/workflows";

    private static final String WORKFLOW_RESOURCE = "/buckets/{bucketId}/workflows/{workflowId}";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private WorkflowService workflowService;

    private Bucket bucket;

    private WorkflowMetadata workflow;

    @Before
    public void setup() throws IOException {
        bucket = bucketRepository.save(new Bucket("myBucket"));
        workflow = workflowService.createWorkflow(
                bucket.getId(), IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"));
    }

    @Test
    public void testCreateWorkflowShouldReturnSavedWorkflow() {
        given().pathParam("bucketId", bucket.getId())
                .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                .when().post(WORKFLOWS_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_CREATED)
                .body("bucket_id", is(bucket.getId().intValue()))
                .body("id", is(2))
                .body("name", is("Valid Workflow"))
                .body("project_name", is("Project Name"))
                .body("revision", is(1))
                .body("generic_information", hasSize(2))
                .body("generic_information[0].key", is("genericInfo1"))
                .body("generic_information[0].value", is("genericInfo1Value"))
                .body("generic_information[1].key", is("genericInfo2"))
                .body("generic_information[1].value", is("genericInfo2Value"))
                .body("variables", hasSize(2))
                .body("variables[0].key", is("var1"))
                .body("variables[0].value", is("var1Value"))
                .body("variables[1].key", is("var2"))
                .body("variables[1].value", is("var2Value"));
    }

    @Test
    public void testCreateWorkflowShouldReturnUnsupportedMediaTypeWithoutBody() {
        given().pathParam("bucketId", bucket.getId())
                .when().post(WORKFLOWS_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void testCreateWorkflowShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
                .multiPart(IntegrationTestUtil.getWorkflowFile("workflow.xml"))
                .when().post(WORKFLOWS_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowMetadataShouldReturnSavedWorkflowMetadata() {
        given().pathParam("bucketId", 1).pathParam("workflowId", 1)
                .when().get(WORKFLOW_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .body("bucket_id", is(bucket.getId().intValue()))
                .body("id", is(workflow.id.intValue()))
                .body("name", is("Valid Workflow"))
                .body("project_name", is("Project Name"))
                .body("revision", is(1))
                .body("generic_information", hasSize(2))
                .body("generic_information[0].key", is("genericInfo1"))
                .body("generic_information[0].value", is("genericInfo1Value"))
                .body("generic_information[1].key", is("genericInfo2"))
                .body("generic_information[1].value", is("genericInfo2Value"))
                .body("variables", hasSize(2))
                .body("variables[0].key", is("var1"))
                .body("variables[0].value", is("var1Value"))
                .body("variables[1].key", is("var2"))
                .body("variables[1].value", is("var2Value"));
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnSavedXmlPayload() throws IOException {
        Response response =
                given().pathParam("bucketId", 1).pathParam("workflowId", 1)
                        .when().get(WORKFLOW_RESOURCE + "?alt=payload");

        Arrays.equals(
                ByteStreams.toByteArray(response.asInputStream()),
                workflowRepository.getMostRecentWorkflowRevision(1L, 1L).getXmlPayload());

        response.then()
                .assertThat().statusCode(HttpStatus.SC_OK)
                .contentType("application/xml");
    }

    @Test
    public void testGetWorkflowMetadataShouldReturnReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42).pathParam("workflowId", 1)
                .when().get(WORKFLOW_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42).pathParam("workflowId", 1)
                .when().get(WORKFLOW_RESOURCE + "?alt=payload").then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowMetadataShouldReturnReturnNotFoundIfNonExistingWorkflowId() {
        given().pathParam("bucketId", 1).pathParam("workflowId", 42)
                .when().get(WORKFLOW_RESOURCE).then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testGetWorkflowPayloadShouldReturnReturnNotFoundIfNonExistingWorkflowId() {
        given().pathParam("bucketId", 1).pathParam("workflowId", 42)
                .when().get(WORKFLOW_RESOURCE + "?alt=payload").then()
                .assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListWorkflowsShouldReturnSavedWorkflows() {
        given().pathParam("bucketId", bucket.getId())
                .when().get(WORKFLOWS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testListWorkflowsShouldReturnNotFoundIfNonExistingBucketId() {
        given().pathParam("bucketId", 42)
                .when().get(WORKFLOWS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

}