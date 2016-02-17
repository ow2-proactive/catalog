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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest
public class WorkflowRevisionControllerQueryIntegrationTestOLD_TO_REMOVE extends AbstractWorkflowRevisionControllerTest {

    private static final Logger log = LoggerFactory.getLogger(
            WorkflowRevisionControllerQueryIntegrationTestOLD_TO_REMOVE.class);

    // number of workflows to create (must be > 9)
    private static final int NUMBER_OF_WORKFLOWS = 10;

    // number of revisions to add per workflow created (must be > 0)
    private static final int NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD = 2;

    // total number of revisions for a given workflow
    private static final int NUMBER_OF_WORFLOW_REVISIONS_PER_WORKFLOW = 1 + NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD;

    // total number of workflow revisions added to the workflow catalog
    private static final int TOTAL_NUMBER_OF_WORKFLOW_REVISIONS =
            (NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD + 1) * NUMBER_OF_WORKFLOWS;

    private BucketMetadata bucket;

    @Before
    public void setup() throws IOException {
        bucket = bucketService.createBucket("bucket");

        // create workflows
        for (int workflowIndex = 1; workflowIndex <= NUMBER_OF_WORKFLOWS; workflowIndex++) {
            ProActiveWorkflowParserResult proActiveWorkflowParserResult =
                    new ProActiveWorkflowParserResult("projectName",
                            "name" + workflowIndex, createKeyValues(workflowIndex),
                            createKeyValues(workflowIndex));

            WorkflowMetadata workflow =
                    workflowService.createWorkflow(bucket.id, proActiveWorkflowParserResult, new byte[0]);

            // insert new revisions
            for (int revisionIndex = 1; revisionIndex <= NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD; revisionIndex++) {
                proActiveWorkflowParserResult =
                        new ProActiveWorkflowParserResult("projectName",
                                "name" + workflowIndex, createKeyValues(workflowIndex, revisionIndex + 1),
                                createKeyValues(workflowIndex, revisionIndex + 1));

                workflowRevisionService.createWorkflowRevision(
                        bucket.id, Optional.of(workflow.id), proActiveWorkflowParserResult, new byte[0]);
            }
        }
    }

    @Test
    public void testFindAllWorkflowRevisions() {
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD; i++) {
            query.append("(variable.name=\"revision\" AND variable.value=\"revision" + (i + 1) + "\")");

            if (i < NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD - 1) {
                query.append(" OR ");
            }
        }

        ValidatableResponse allWorkflowRevisions = findAllWorkflowRevisions(query.toString(), 1);
        allWorkflowRevisions.body(
                "_embedded.workflowMetadataList", hasSize(NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD));
    }

    public ValidatableResponse findAllWorkflowRevisions(String wcqlQuery, long workflowId) {
        Response response = given().pathParam("bucketId", bucket.id).pathParam("workflowId", workflowId)
                .queryParam("size", TOTAL_NUMBER_OF_WORKFLOW_REVISIONS)
                .queryParam("query", wcqlQuery)
                .when().get(WORKFLOW_REVISIONS_RESOURCE);

        logQueryAndResponse(wcqlQuery, response);

        return response.then().assertThat();
    }

    public ValidatableResponse findMostRecentWorkflowRevisions(String wcqlQuery) {
        Response response = given().pathParam("bucketId", bucket.id)
                .queryParam("size", TOTAL_NUMBER_OF_WORKFLOW_REVISIONS)
                .queryParam("query", wcqlQuery)
                .when().get(WORKFLOWS_RESOURCE);

        logQueryAndResponse(wcqlQuery, response);

        return response.then().assertThat();
    }

    private void logQueryAndResponse(String wcqlQuery, Response response) {
        log.info("WCQL query used is '{}'", wcqlQuery);
        log.info("Response is:\n{}", prettify(response.asString()));
    }

    private ImmutableMap<String, String> createKeyValues(int workflowIndex) {
        return createKeyValues(workflowIndex, -1);
    }

    private ImmutableMap<String, String> createKeyValues(int workflowIndex, int revisionIndex) {
        Map<String, String> result = new HashMap<>();

        for (int multiple = 2; multiple <= 5; multiple++) {
            if (workflowIndex % multiple == 0) {
                String key = "multipleOf" + multiple;
                result.put(key, "true");
            }
        }

        for (int i = 1; i <= workflowIndex; i++) {
            result.put("key" + i, "value" + i);
        }

        result.put("revision", revisionIndex == -1 ? "revision1" : "revision" + revisionIndex);
        result.put("workflow", "" + workflowIndex);

        return ImmutableMap.copyOf(result);
    }

    private int numberOfMultipleMixedAndOr2() {
        return numberOfMultiple(i -> i % 3 == 0 || i % 2 == 0 || i % 4 == 0 || i % 5 == 0);
    }

    private int numberOfMultipleMixedAndOr() {
        return numberOfMultiple(i -> i % 3 == 0 || i % 2 == 0 && i % 4 == 0);
    }

    private int numberOfMultipleMixedAndOrWithBrackets() {
        return numberOfMultiple(i -> (i % 3 == 0 || i % 2 == 0) && i % 4 == 0);
    }

    private int numberOfMultipleOf2And4() {
        return numberOfMultiple(i -> i % 2 == 0 && i % 4 == 0);
    }

    private int numberOfMultipleOf2() {
        return numberOfMultiple(i -> i % 2 == 0);
    }

    private int numberOfMultiple(Predicate<Integer> predicate) {
        int result = 0;

        for (int i = 1; i < NUMBER_OF_WORKFLOWS; i++) {
            if (predicate.test(i)) {
                result++;
            }
        }

        return result;
    }

}