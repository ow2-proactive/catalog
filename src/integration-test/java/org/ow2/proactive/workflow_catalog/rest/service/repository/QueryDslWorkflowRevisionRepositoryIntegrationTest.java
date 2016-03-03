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
package org.ow2.proactive.workflow_catalog.rest.service.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.controller.AbstractWorkflowRevisionControllerTest;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.QGenericInformation;
import org.ow2.proactive.workflow_catalog.rest.entity.QWorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.entity.Variable;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionContext;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
import com.google.common.collect.ImmutableMap;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * Integration tests associated to {@link QueryDslWorkflowRevisionRepository}.
 * <p>
 * The idea is to start a Web application with a in-memory database that is
 * pre-allocated with several workflow revisions. Then, tests run a query
 * against the database.
 *
 * @author ActiveEon Team
 * @see QueryDslWorkflowRevisionRepository
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class QueryDslWorkflowRevisionRepositoryIntegrationTest extends AbstractWorkflowRevisionControllerTest {

    private static final Logger log = LoggerFactory.getLogger(
            QueryDslWorkflowRevisionRepositoryIntegrationTest.class);

    private static final int NUMBER_OF_BUCKETS = 2; // must be >= 2

    private static final int NUMBER_OF_WORKFLOWS = 10; // must be >= 2

    @Autowired
    private QueryDslWorkflowRevisionRepository queryDslWorkflowRevisionRepository;

    private List<BucketMetadata> buckets;

    @Before
    public void setUp() throws IOException, XMLStreamException {
        buckets = new ArrayList<>(NUMBER_OF_BUCKETS);

        for (int bucketIndex = 1; bucketIndex <= NUMBER_OF_BUCKETS; bucketIndex++) {
            BucketMetadata bucket = bucketService.createBucket("bucket" + bucketIndex);
            buckets.add(bucket);

            for (int workflowIndex = 1; workflowIndex <= NUMBER_OF_WORKFLOWS; workflowIndex++) {
                ProActiveWorkflowParserResult proActiveWorkflowParserResult =
                        new ProActiveWorkflowParserResult(
                                "projectName", "bucket" + bucketIndex + "Name" + workflowIndex,
                                createKeyValues(bucketIndex, workflowIndex, 1),
                                createKeyValues(bucketIndex, workflowIndex, 1));

                WorkflowMetadata workflow =
                        workflowService.createWorkflow(
                                bucket.id, proActiveWorkflowParserResult, new byte[0]);

                for (int revisionIndex = 2; revisionIndex <= workflowIndex; revisionIndex++) {
                    proActiveWorkflowParserResult =
                            new ProActiveWorkflowParserResult("projectName",
                                    "bucket" + bucketIndex + "Name" + workflowIndex,
                                    createKeyValues(bucketIndex, workflowIndex, revisionIndex),
                                    createKeyValues(bucketIndex, workflowIndex, revisionIndex));

                    workflowRevisionService.createWorkflowRevision(
                            bucket.id, Optional.of(workflow.id), proActiveWorkflowParserResult, new byte[0]);
                }
            }
        }
    }

    @Test
    public void testFindAllWorkflowRevisions1() {
        findAllWorkflowRevisions(buckets.get(0), 1);
    }

    @Test
    public void testFindAllWorkflowRevisions2() {
        findAllWorkflowRevisions(buckets.get(0), NUMBER_OF_WORKFLOWS / 2);
    }

    @Test
    public void testFindAllWorkflowRevisions3() {
        findAllWorkflowRevisions(buckets.get(0), NUMBER_OF_WORKFLOWS);
    }

    @Test
    public void testFindAllWorkflowRevisions4() {
        ListSubQuery<WorkflowRevision> revisionSubQuery = new JPASubQuery().from(
                QGenericInformation.genericInformation)
                .where(
                        QGenericInformation.genericInformation.key.eq("revisionIndex").and(
                                QGenericInformation.genericInformation.value.eq(
                                        Integer.toString(NUMBER_OF_WORKFLOWS)))
                )
                .list(QGenericInformation.genericInformation.workflowRevision);

        BucketMetadata bucket = buckets.get(0);

        Page<WorkflowRevision> result = findAllWorkflowRevisions(bucket,
                NUMBER_OF_WORKFLOWS,
                Optional.of(
                        QWorkflowRevision.workflowRevision.in(
                                new JPASubQuery()
                                        .from(QWorkflowRevision.workflowRevision)
                                        .where(QWorkflowRevision.workflowRevision.in(revisionSubQuery))
                                        .list(QWorkflowRevision.workflowRevision))), 1);

        assertThat(
                result.getContent().get(0).getName()).isEqualTo(
                "bucket" + bucket.id + "Name" + Integer.toString(NUMBER_OF_WORKFLOWS));
    }

    @Test
    public void testFindMostRecentWorkflowRevisions1() {
        findMostRecentWorkflowRevisions(buckets.get(1));
    }

    @Test
    public void testFindMostRecentWorkflowRevisions2() {
        findMostRecentWorkflowRevisions(buckets.get(1));
    }

    @Test
    public void testFindMostRecentWorkflowRevisions3() {
        findMostRecentWorkflowRevisions(buckets.get(1));
    }

    @Test
    public void testFindMostRecentWorkflowRevisions4() {
        findMostRecentWorkflowRevisions(
                buckets.get(1), Optional.of(
                        QWorkflowRevision.workflowRevision.name.eq("bucket2Name1")), 1);
    }

    private Page<WorkflowRevision> findMostRecentWorkflowRevisions(BucketMetadata bucket) {
        return findMostRecentWorkflowRevisions(bucket, Optional.empty(), NUMBER_OF_WORKFLOWS);
    }

    private Page<WorkflowRevision> findMostRecentWorkflowRevisions(
            BucketMetadata bucket,
            Optional<BooleanExpression> booleanExpression, int expectedNumberOfWorkflowRevisions) {

        Page<WorkflowRevision> result = doQueryfindMostRecentWorkflowRevisions(bucket, booleanExpression);

        assertContainRightVariable(bucket, result);

        assertThat(result.getTotalElements()).isEqualTo(expectedNumberOfWorkflowRevisions);

        return result;
    }

    private void assertContainRightVariable(BucketMetadata bucket, Page<WorkflowRevision> result) {
        for (WorkflowRevision workflowRevision : result.getContent()) {
            assertThat(bucket.id).isEqualTo(findBucketIndex(workflowRevision));
        }
    }

    private Page<WorkflowRevision> findAllWorkflowRevisions(
            BucketMetadata bucket, int workflowIndex) {
        return findAllWorkflowRevisions(
                bucket, workflowIndex, Optional.empty(), getNumberOfWorkflowRevisions(workflowIndex));
    }

    private Page<WorkflowRevision> findAllWorkflowRevisions(
            BucketMetadata bucket, int workflowIndex,
            Optional<BooleanExpression> booleanExpression, int expectedNumberOfWorkflowRevisions) {

        Page<WorkflowRevision> result = doQueryfindAllWorkflowRevisions(bucket, workflowIndex,
                booleanExpression);

        assertContainRightVariable(bucket, result);

        assertThat(result.getTotalElements()).isEqualTo(expectedNumberOfWorkflowRevisions);

        return result;
    }

    private int findBucketIndex(WorkflowRevision workflowRevision) {
        List<Variable> variables = workflowRevision.getVariables();

        int bucketIndex = -1;

        for (Variable variable : variables) {
            if (variable.getName().equals("bucketIndex")) {
                bucketIndex = Integer.parseInt(variable.getValue());
                break;
            }
        }
        return bucketIndex;
    }

    private Page<WorkflowRevision> doQueryfindAllWorkflowRevisions(
            BucketMetadata bucket, long workflowId, Optional<BooleanExpression> booleanExpression) {

        return queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(
                bucket.id, workflowId,
                createAnyQueryExpression(booleanExpression),
                new PageRequest(0, getTotalNumberOfWorkflowRevisions()));
    }

    private Page<WorkflowRevision> doQueryfindMostRecentWorkflowRevisions(
            BucketMetadata bucket, Optional<BooleanExpression> booleanExpression) {

        return queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(
                bucket.id,
                createAnyQueryExpression(booleanExpression),
                new PageRequest(0, getTotalNumberOfWorkflowRevisions()));
    }

    private QueryExpressionContext createAnyQueryExpression(Optional<BooleanExpression> booleanExpression) {
        if (booleanExpression.isPresent()) {
            return new QueryExpressionContext(booleanExpression.get());
        }

        return new QueryExpressionContext(
                QWorkflowRevision.workflowRevision.id.isNotNull());
    }

    private int getNumberOfWorkflowRevisions(int workflowIndex) {
        return workflowIndex;
    }

    private int getTotalNumberOfWorkflowRevisionsPerBucket() {
        return (NUMBER_OF_WORKFLOWS * (NUMBER_OF_WORKFLOWS - 1)) / 2;
    }

    private int getTotalNumberOfWorkflowRevisions() {
        return getTotalNumberOfWorkflowRevisionsPerBucket() * NUMBER_OF_BUCKETS;
    }

    private ImmutableMap<String, String> createKeyValues(
            int bucketIndex, int workflowIndex, int revisionIndex) {
        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
        result.put("bucketIndex", Integer.toString(bucketIndex));
        result.put("workflowIndex", Integer.toString(workflowIndex));
        result.put("revisionIndex", Integer.toString(revisionIndex));
        return result.build();
    }

}