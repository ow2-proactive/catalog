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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * The purpose of this class is to perform test against the WorkflowRevisionController, assuming
 * there is no workflow ID specified. Consequently, only most recent workflow revisions
 * should be returned whatever the query is.
 *
 * @author ActiveEon Team
 * @see WorkflowRevisionController#list(Long, Long, Optional, Pageable, PagedResourcesAssembler)
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(Parameterized.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest
public class WorkflowRevisionControllerQueryIntegrationTest extends AbstractWorkflowRevisionControllerTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private Logger log = LoggerFactory.getLogger(WorkflowRevisionControllerQueryIntegrationTest.class);

    private BucketMetadata firstBucket;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        Assertion.create("", ImmutableSet.of("A", "B", "C", "D", "E", "F", "G%", "Amazon"))
                },
                {
                        Assertion.create("name=\"A\"", ImmutableSet.of("A"))
                },
                {
                        Assertion.create("name=\"\"", ImmutableSet.of())
                },
                {
                        Assertion.create("name=\"\"", ImmutableSet.of())
                },
                {
                        Assertion.create("project_name=\"\"", ImmutableSet.of("A", "B", "C", "D", "Amazon"))
                },
                {
                        Assertion.create("project_name=\"Fab%\"", ImmutableSet.of("E", "F"))
                },
                {
                        Assertion.create("project_name=\"%bi%\"", ImmutableSet.of("E", "F"))
                },
                {
                        Assertion.create("project_name=\"%ple\"", ImmutableSet.of("E", "F"))
                },
                {
                        Assertion.create("project_name=\"%donotexists%\"", ImmutableSet.of())
                },
                {
                        Assertion.create("name=\"G\\%\"", ImmutableSet.of("G%"))
                },
                {
                        Assertion.create("variable(\"CPU\", \"5%\")", ImmutableSet.of("D"))
                },
                {
                        Assertion.create("variable(\"%\", \"%\")", ImmutableSet.of("A", "B", "D", "E", "F"))
                },
                {
                        Assertion.create("variable(\"Provider\", \"%\")", ImmutableSet.of("E", "F"))
                },
                {
                        Assertion.create("variable(\"Provider\", \"Amazon\")", ImmutableSet.of("F"))
                },
                {
                        Assertion.create(
                                "generic_information(\"Infrastructure\",\"Amazon EC2\")",
                                ImmutableSet.of("A"))
                },
                {
                        Assertion.create(
                                "generic_information(\"linebreak\", \"\\r\\n\")",
                                ImmutableSet.of("G%"))
                },
                {
                        Assertion.create(
                                "generic_information(\"Infrastructure\", \"Amazon EC2\") " +
                                        "OR generic_information(\"Cloud\", \"Amazon EC2\")",
                                ImmutableSet.of("A", "B"))
                },
                {
                        Assertion.create(
                                "generic_information(\"Infrastructure\", \"Amazon EC2\") " +
                                        "OR generic_information(\"Cloud\", \"Amazon EC2\") " +
                                        "AND variable(\"CPU\", \"%\")",
                                ImmutableSet.of("A", "B"))
                },
                {
                        Assertion.create(
                                "generic_information(\"Infrastructure\", \"Amazon EC2\") " +
                                        "AND generic_information(\"Cloud\", \"Amazon EC2\") " +
                                        "OR variable(\"CPU\", \"%\")",
                                ImmutableSet.of("A", "B", "D"))
                },
                {
                        Assertion.create(
                                "variable(\"CPU\", \"%\") AND name=\"B\" " +
                                        "AND generic_information(\"Cloud\", \"Amazon EC2\")",
                                ImmutableSet.of("B"))
                },
                {
                        Assertion.create(
                                "generic_information(\"Infrastructure\", \"Amazon EC2\") " +
                                        "AND generic_information(\"Type\", \"Public\") "
                                        + "AND variable(\"CPU\", \"%\") OR generic_information(\"Cloud\", \"Amazon EC2\") "
                                        + "AND variable(\"CPU\", \"%\") OR name=\"Amazon\"",
                                ImmutableSet.of("A", "B", "Amazon"))
                },

                /*
                 * Below are queries with invalid syntax
                 */

                {
                        Assertion.createSyntacticallyIncorrect(
                                "generic_information=(\"Infrastructure\", \"Amazon EC2\")")
                },

        });
    }

    private final Assertion assertion;

    public WorkflowRevisionControllerQueryIntegrationTest(Assertion assertion) {
        this.assertion = assertion;
    }

    @Before
    public void setup() throws Exception {
        firstBucket = bucketService.createBucket("first");

        // Workflow A
        ImmutablePair<WorkflowMetadata, ProActiveWorkflowParserResult> workflow =
                createWorkflowFirstBucket("", "A",
                        ImmutableMap.of("Infrastructure", "Amazon EC2", "Type", "Public"),
                        ImmutableMap.of("CPU", "4"));

        workflowRevisionService.createWorkflowRevision(
                firstBucket.id, Optional.of(workflow.getLeft().id), workflow.getRight(), new byte[0]);

        // Workflow B
        workflow = createWorkflowFirstBucket("", "B",
                ImmutableMap.of("Cloud", "Amazon EC2", "Type", "private"),
                ImmutableMap.of("CPU", "2"));

        workflowRevisionService.createWorkflowRevision(
                firstBucket.id, Optional.of(workflow.getLeft().id), workflow.getRight(), new byte[0]);

        // Workflow C
        createWorkflowFirstBucket("", "C", ImmutableMap.of("Infrastructure", "OpenStack"), ImmutableMap.of());

        // Workflow D
        createWorkflowFirstBucket("", "D", ImmutableMap.of(), ImmutableMap.of("CPU", "5"));

        // Workflow E
        createWorkflowFirstBucket("FabienExample", "E",
                ImmutableMap.of("Provider", "Google", "Fournisseur", "Amazon"),
                ImmutableMap.of("Provider", "Google", "Fournisseur", "Amazon"));

        // Workflow F
        createWorkflowFirstBucket("FabienExample", "F",
                ImmutableMap.of("Provider", "Amazon"),
                ImmutableMap.of("Provider", "Amazon"));

        // Workflow G
        createWorkflowFirstBucket("CharacterEscaping", "G%",
                ImmutableMap.of("linebreak", "\\r\\n"),
                ImmutableMap.of());

        // Workflow Amazon
        createWorkflowFirstBucket("", "Amazon", ImmutableMap.of(), ImmutableMap.of());


        // Workflows that belong to second bucket

        BucketMetadata secondBucket = bucketService.createBucket("second");

        // Workflow Dummy
        createWorkflow(secondBucket, "", "Dummy", ImmutableMap.of(), ImmutableMap.of());

        // Workflow A
        createWorkflow(secondBucket, "",
                "A", ImmutableMap.of("Infrastructure", "Amazon EC2", "Type", "Public"),
                ImmutableMap.of("CPU", "4"));
    }

    private ImmutablePair<WorkflowMetadata, ProActiveWorkflowParserResult> createWorkflowFirstBucket(
            String projectName, String name,
            ImmutableMap<String, String> genericInformation, ImmutableMap<String, String> variable) {
        return createWorkflow(firstBucket, projectName, name, genericInformation, variable);
    }

    private ImmutablePair<WorkflowMetadata, ProActiveWorkflowParserResult> createWorkflow(
            BucketMetadata bucket,
            String projectName, String name,
            ImmutableMap<String, String> genericInformation, ImmutableMap<String, String> variable) {

        ProActiveWorkflowParserResult proActiveWorkflowParserResult =
                new ProActiveWorkflowParserResult(projectName, name,
                        genericInformation, variable);

        WorkflowMetadata workflow = workflowService.createWorkflow(
                bucket.id, proActiveWorkflowParserResult, new byte[0]);

        return ImmutablePair.of(workflow, proActiveWorkflowParserResult);
    }

    @Test
    public void testFindMostRecentWorkflowRevisions() {
        ValidatableResponse response =
                findMostRecentWorkflowRevisions(assertion.query);

        if (assertion.expectedWorkflowRevisionNames == null) {
            response.assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }

        response.assertThat().statusCode(org.apache.http.HttpStatus.SC_OK);

        if (assertion.expectedWorkflowRevisionNames.isEmpty()) {
            response.assertThat().body("page.totalElements", is(0));
            return;
        }

        ArrayList<HashMap<Object, Object>> workflowRevisionsFound =
                response.extract()
                        .body().jsonPath().get("_embedded.workflowMetadataList");

        Set<String> names = workflowRevisionsFound.stream().map(
                workflowRevision -> (String) workflowRevision.get("name")).collect(
                Collectors.toSet());

        Sets.SetView<String> difference =
                Sets.symmetricDifference(names,
                        assertion.expectedWorkflowRevisionNames);

        if (!difference.isEmpty()) {
            Assert.fail(
                    "Expected " + assertion.expectedWorkflowRevisionNames + " but received " + names);
        }
    }

    private static class Assertion {

        public final String query;

        public final Set<String> expectedWorkflowRevisionNames;

        /**
         * Create a new assertion.
         *
         * @param query                         The query to test.
         * @param expectedWorkflowRevisionNames The name of the workflows which are expected to be returned.
         */
        private Assertion(String query, Set<String> expectedWorkflowRevisionNames) {
            this.query = query;
            this.expectedWorkflowRevisionNames = expectedWorkflowRevisionNames;
        }

        public static Assertion create(String query, @NotNull Set<String> expectedWorkflowRevisionNames) {
            return new Assertion(query, expectedWorkflowRevisionNames);
        }

        public static Assertion createSyntacticallyIncorrect(String query) {
            return new Assertion(query, null);
        }

    }

    public ValidatableResponse findMostRecentWorkflowRevisions(String wcqlQuery) {
        Response response = given().pathParam("bucketId", firstBucket.id)
                .queryParam("size", 999)
                .queryParam("query", wcqlQuery)
                .when().get(WORKFLOWS_RESOURCE);

        logQueryAndResponse(wcqlQuery, response);

        return response.then().assertThat();
    }

    public ValidatableResponse findAllWorkflowRevisions(String wcqlQuery, long workflowId) {
        Response response = given().pathParam("bucketId", firstBucket.id).pathParam("workflowId", workflowId)
                .queryParam("size", 999)
                .queryParam("query", wcqlQuery)
                .when().get(WORKFLOW_REVISIONS_RESOURCE);

        logQueryAndResponse(wcqlQuery, response);

        return response.then().assertThat();
    }

    private void logQueryAndResponse(String wcqlQuery, Response response) {
        log.info("WCQL query used is '{}'", wcqlQuery);
        log.info("Response is:\n{}", prettify(response.asString()));
    }

}