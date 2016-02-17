//package org.ow2.proactive.workflow_catalog.rest.service.repository;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//
//import org.ow2.proactive.workflow_catalog.rest.Application;
//import org.ow2.proactive.workflow_catalog.rest.controller.AbstractWorkflowRevisionControllerTest;
//import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
//import org.ow2.proactive.workflow_catalog.rest.dto.WorkflowMetadata;
//import org.ow2.proactive.workflow_catalog.rest.entity.QGenericInformation;
//import org.ow2.proactive.workflow_catalog.rest.entity.QVariable;
//import org.ow2.proactive.workflow_catalog.rest.entity.QWorkflowRevision;
//import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
//import org.ow2.proactive.workflow_catalog.rest.query.QueryExpressionContext;
//import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParserResult;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.ImmutableSet;
//import com.mysema.query.BooleanBuilder;
//import com.mysema.query.types.Predicate;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.WebIntegrationTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import static com.google.common.truth.Truth.assertThat;
//import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
//
///**
// * Integration tests associated to {@link QueryDslWorkflowRevisionRepository}.
// * <p/>
// * The idea is to start a Web application with a in-memory database
// * that is pre-allocated with several workflow revisions. Then, two main tests
// * are executed. Each runs multiple queries against the database (as sub-test)
// * and check the number of results returned and their value.
// *
// * @author ActiveEon Team
// */
//@ActiveProfiles("test")
//@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = { Application.class })
//@WebIntegrationTest
//public class QueryDslWorkflowRevisionRepositoryIntegrationTest extends AbstractWorkflowRevisionControllerTest {
//
//    private static final Logger log = LoggerFactory.getLogger(
//            QueryDslWorkflowRevisionRepositoryIntegrationTest.class);
//
//    @Autowired
//    private QueryDslWorkflowRevisionRepository queryDslWorkflowRevisionRepository;
//
//    // number of workflows to create (must be > 9)
//    private static final int NUMBER_OF_WORKFLOWS = 10;
//
//    // number of revisions to add per workflow created (must be > 0)
//    private static final int NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD = 2;
//
//    // total number of revisions for a given workflow
//    private static final int NUMBER_OF_WORFLOW_REVISIONS_PER_WORKFLOW = 1 + NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD;
//
//    // total number of workflow revisions added to the workflow catalog
//    private static final int TOTAL_NUMBER_OF_WORKFLOW_REVISIONS =
//            (NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD + 1) * NUMBER_OF_WORKFLOWS;
//
//    private BucketMetadata bucket;
//
//    @Before
//    public void setUp() {
//        bucket = bucketService.createBucket("test");
//
//        // create workflows
//        for (int workflowIndex = 0; workflowIndex < NUMBER_OF_WORKFLOWS; workflowIndex++) {
//            ProActiveWorkflowParserResult proActiveWorkflowParserResult =
//                    new ProActiveWorkflowParserResult("projectName",
//                            "name" + workflowIndex, createKeyValues(workflowIndex),
//                            createKeyValues(workflowIndex));
//
//            WorkflowMetadata workflow =
//                    workflowService.createWorkflow(bucket.id, proActiveWorkflowParserResult, new byte[0]);
//
//            // insert new revisions
//            for (int revisionIndex = 0; revisionIndex < NUMBER_OF_WORKFLOW_REVISIONS_TO_ADD; revisionIndex++) {
//                proActiveWorkflowParserResult =
//                        new ProActiveWorkflowParserResult("projectName",
//                                "name" + workflowIndex, createKeyValues(workflowIndex),
//                                createKeyValues(workflowIndex));
//
//                workflowRevisionService.createWorkflowRevision(
//                        bucket.id, Optional.of(workflow.id), proActiveWorkflowParserResult, new byte[0]);
//            }
//        }
//    }
//
//    @Test
//    public void testFindAllWorkflowRevisions() {
//        performTestFindAllWorkflowRevisions(createTestInputs(NUMBER_OF_WORFLOW_REVISIONS_PER_WORKFLOW));
//    }
//
//    @Test
//    public void testFindMostRecentWorkflowRevisions() {
//        performTestFindMostRecentWorkflowRevisions(createTestInputs(1));
//    }
//
//    @Test
//    public void testFindAllWorkflowRevisionsMustTargetSpecificBucket() {
//        doQuery(queryContext ->
//                queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(
//                        queryContext.bucketId, queryContext.predicateContext, queryContext.pageRequest));
//    }
//
//    @Test
//    public void testFindMostRecentWorkflowRevisionsMustTargetSpecificBucket() {
//        doQuery(queryContext ->
//                queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(
//                        queryContext.bucketId, queryContext.predicateContext, queryContext.pageRequest));
//    }
//
//    private Page<WorkflowRevision> doQuery(
//            Function<QueryContext, Page<WorkflowRevision>> function) {
//        BucketMetadata bucket = bucketService.createBucket("another");
//
//        ProActiveWorkflowParserResult workflowParserResult =
//                new ProActiveWorkflowParserResult(
//                        "projectName", "name", ImmutableMap.of(), ImmutableMap.of());
//
//        workflowService.createWorkflow(bucket.id, workflowParserResult, new byte[0]);
//
//        Page<WorkflowRevision> page =
//                function.apply(new QueryContext(bucket.id,
//                        createPredicateContext(new BooleanBuilder()),
//                        new PageRequest(0, TOTAL_NUMBER_OF_WORKFLOW_REVISIONS + 1)));
//
//        assertThat(page).hasSize(1);
//
//        return page;
//    }
//
//    private TestInput[] createTestInputs(int expectedNbResultsMultiple) {
//        return new TestInput[] {
//                // no predicateContext defined must return all revisions
//                new TestInput(builder -> createPredicateContext(builder),
//                        NUMBER_OF_WORKFLOWS * expectedNbResultsMultiple),
//
//                // search workflow revision with projectName that exists
//                new TestInput(
//                        builder -> createPredicateContext(builder.and(
//                                QWorkflowRevision.workflowRevision.name
//                                        .eq("name" + (NUMBER_OF_WORKFLOWS / 2)))),
//                        1 * expectedNbResultsMultiple,
//                        revisions -> {
//                            assertThat(
//                                    revisions.get(0).getName())
//                                    .isEqualTo("name" + (NUMBER_OF_WORKFLOWS / 2));
//                        }
//                ),
//
//                // search workflow revision with projectName that does not exist
//                new TestInput(
//                        builder -> createPredicateContext(builder.and(
//                                QWorkflowRevision.workflowRevision.projectName
//                                        .eq("nonExistingProjectName"))),
//                        0 * expectedNbResultsMultiple),
//
//                // search workflow revisions based on multiple OR conditions
//                new TestInput(
//                        builder -> {
//                            return createPredicateContext(
//                                    builder.or(QWorkflowRevision.workflowRevision.name.eq("name1"))
//                                            .or(QWorkflowRevision.workflowRevision.name.eq("name3"))
//                                            .or(QGenericInformation.genericInformation.key.eq("key17"))
//                                            .or(QVariable.variable.key.eq("key19")));
//                        },
//                        4 * expectedNbResultsMultiple,
//                        revisions -> {
//                            revisions.forEach(
//                                    revision ->
//                                            assertThat(revision.getName())
//                                                    .isAnyOf("name1", "name3", "name7", "name9"));
//                        }
//                ),
//
//                // search workflow revision based on multiple AND conditions
//                new TestInput(
//                        builder ->
//                                createPredicateContext(
//                                        builder.and(QWorkflowRevision.workflowRevision.projectName.eq(
//                                                "projectName"))
//                                                .and(QWorkflowRevision.workflowRevision.name.eq("name1"))
//                                                .and(QGenericInformation.genericInformation.key.eq("key11"))
//                                                .and(QVariable.variable.value.eq("value11"))),
//                        1 * expectedNbResultsMultiple,
//                        revisions -> {
//                            assertThat(revisions.get(0).getName().equals("name1"));
//                        }
//                ),
//
//                // search workflow revision based on multiple OR conditions
//                new TestInput(
//                        builder ->
//                                createPredicateContext(
//                                        builder.or(QWorkflowRevision.workflowRevision.name.eq("name1"))
//                                                .or(QWorkflowRevision.workflowRevision.name.eq(
//                                                        "name" + (NUMBER_OF_WORKFLOWS / 2)))
//                                                .or(QWorkflowRevision.workflowRevision.name.eq(
//                                                        "name" + (NUMBER_OF_WORKFLOWS - 1)))),
//                        3 * expectedNbResultsMultiple
//                ),
//
//                // search workflow revision based on multiple AND conditions on same attribute
//                new TestInput(
//                        builder -> {
//                            List<QGenericInformation> aliases = ImmutableList.of(
//                                    new QGenericInformation("gi1"),
//                                    new QGenericInformation("gi2"),
//                                    new QGenericInformation("gi3")
//                            );
//
//                            return new QueryExpressionContext(
//                                    builder.and(aliases.get(0).key.eq("key1"))
//                                            .and(aliases.get(1).key.eq("key2"))
//                                            .and(aliases.get(2).key.eq("key3")),
//                                    ImmutableSet.copyOf(aliases), ImmutableSet.of());
//                        },
//                        NUMBER_OF_WORKFLOWS * expectedNbResultsMultiple
//                ),
//
//                // search workflow revisions based on mixed and nested AND and OR conditions
//
//                new TestInput(
//                        builder -> {
//                            BooleanBuilder parent = new BooleanBuilder();
//                            parent.or(QWorkflowRevision.workflowRevision.name.eq("name1"));
//
//                            builder.and(QWorkflowRevision.workflowRevision.projectName.eq("projectName"));
//                            builder.and(QGenericInformation.genericInformation.key.eq(
//                                    "workflowIndexIsMultipleOf2"));
//                            builder.and(QGenericInformation.genericInformation.value.eq("true"));
//                            parent.or(builder);
//
//                            parent.or(QWorkflowRevision.workflowRevision.name.eq("name3"));
//
//                            return createPredicateContext(parent);
//                        },
//                        ((NUMBER_OF_WORKFLOWS / 2) + 2) * expectedNbResultsMultiple,
//                        revisions -> {
//                            for (WorkflowRevision revision : revisions) {
//                                String name = revision.getName();
//                                char indexCharacter = name.charAt(name.length() - 1);
//
//                                int index = Integer.parseInt("" + indexCharacter);
//
//                                boolean validIndexes = index == 1 || index == 3 || (index % 2 == 0);
//
//                                assertThat(validIndexes).isTrue();
//                            }
//                        }
//                )
//        };
//    }
//
//    private QueryExpressionContext createPredicateContext(Predicate predicate) {
//        return new QueryExpressionContext(predicate,
//                ImmutableSet.of(QGenericInformation.genericInformation),
//                ImmutableSet.of(QVariable.variable));
//    }
//
//    private void performTestFindAllWorkflowRevisions(TestInput[] testInputs) {
//        performTest(testInputs,
//                (predicateContext, pageable) ->
//                        queryDslWorkflowRevisionRepository.findAllWorkflowRevisions(
//                                bucket.id,
//                                predicateContext,
//                                new PageRequest(0, TOTAL_NUMBER_OF_WORKFLOW_REVISIONS))
//        );
//    }
//
//    private void performTestFindMostRecentWorkflowRevisions(TestInput[] testInputs) {
//        performTest(testInputs,
//                (predicateContext, pageable) -> {
//                    Page<WorkflowRevision> mostRecentWorkflowRevisions =
//                            queryDslWorkflowRevisionRepository.findMostRecentWorkflowRevisions(
//                                    bucket.id,
//                                    predicateContext,
//                                    new PageRequest(0, TOTAL_NUMBER_OF_WORKFLOW_REVISIONS));
//
//                    log.info("Query predicate is '{}'", predicateContext.getExpression());
//                    log.info("Number of workflow revision found is {}\n",
//                            mostRecentWorkflowRevisions.getTotalElements());
//
//                    return mostRecentWorkflowRevisions;
//                }
//        );
//    }
//
//    private void performTest(TestInput[] input,
//            BiFunction<QueryExpressionContext, Pageable, Page<WorkflowRevision>> function) {
//        for (TestInput testInput : input) {
//            Page<WorkflowRevision> workflowRevisions =
//                    function.apply(
//                            testInput.predicateContext,
//                            new PageRequest(0, TOTAL_NUMBER_OF_WORKFLOW_REVISIONS));
//
//            if (workflowRevisions.getTotalElements() != testInput.expectedNumberOfResults) {
//                Assert.fail("Expected " + testInput.expectedNumberOfResults +
//                        " results but got " + workflowRevisions.getTotalElements()
//                        + " with predicateContext " + testInput.predicateContext);
//            }
//
//            testInput.testAssertions(workflowRevisions.getContent());
//        }
//    }
//
//    private ImmutableMap<String, String> createKeyValues(int workflowIndex) {
//        return createKeyValues(workflowIndex, -1);
//    }
//
//    private ImmutableMap<String, String> createKeyValues(int workflowIndex, int revisionIndex) {
//        ImmutableMap.Builder<String, String> result = ImmutableMap.builder();
//
//        // key, values common to all workflow revisions that belong to a same workflow
//        result.put("key1" + workflowIndex, "value1" + workflowIndex);
//
//        result.put("key2" + workflowIndex, "value2" + workflowIndex);
//        result.put("key3" + workflowIndex, "value3" + workflowIndex);
//        result.put("key4" + workflowIndex, "value4" + workflowIndex);
//
//        result.put("key1", "value1");
//        result.put("key2", "value2");
//        result.put("key3", "value3");
//
//        result.put("revision", revisionIndex == -1 ? "1" : "" + revisionIndex);
//        result.put("workflowIndexIsMultipleOf2", workflowIndex % 2 == 0 ? "true" : "false");
//        result.put("workflow", "" + workflowIndex);
//
//        return result.build();
//    }
//
//    private static class QueryContext {
//
//        public final long bucketId;
//
//        public final QueryExpressionContext predicateContext;
//
//        public final PageRequest pageRequest;
//
//        public QueryContext(long bucketId,
//                QueryExpressionContext predicateContext,
//                PageRequest pageRequest) {
//            this.bucketId = bucketId;
//            this.predicateContext = predicateContext;
//            this.pageRequest = pageRequest;
//        }
//    }
//
//    private static class TestInput {
//
//        public final QueryExpressionContext predicateContext;
//
//        public final int expectedNumberOfResults;
//
//        public final Optional<Assertions> assertions;
//
//        /**
//         * Create a test input.
//         *
//         * @param function                a function that builds the predicateContext to test
//         * @param expectedNumberOfResults the expected number of results
//         */
//        public TestInput(Function<BooleanBuilder, QueryExpressionContext> function, int expectedNumberOfResults) {
//            this(function.apply(new BooleanBuilder()), expectedNumberOfResults, null);
//        }
//
//        /**
//         * Create a test input.
//         *
//         * @param function                a function that builds the predicateContext to test
//         * @param expectedNumberOfResults the expected number of results
//         * @param assertions              extra assertions to execute
//         */
//        public TestInput(Function<BooleanBuilder, QueryExpressionContext> function, int expectedNumberOfResults,
//                Assertions assertions) {
//            this(function.apply(new BooleanBuilder()), expectedNumberOfResults, assertions);
//        }
//
//        public TestInput(QueryExpressionContext predicateContext, int expectedNumberOfResults,
//                Assertions assertions) {
//            this.predicateContext = predicateContext;
//            this.expectedNumberOfResults = expectedNumberOfResults;
//            this.assertions = Optional.ofNullable(assertions);
//        }
//
//        public void testAssertions(List<WorkflowRevision> workflowRevisions) {
//            assertions.ifPresent(a -> a.execute(workflowRevisions));
//        }
//
//    }
//
//    private interface Assertions {
//
//        void execute(List<WorkflowRevision> revisions);
//
//    }
//
//}