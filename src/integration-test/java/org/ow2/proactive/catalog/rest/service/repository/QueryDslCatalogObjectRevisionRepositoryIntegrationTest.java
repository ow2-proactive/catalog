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
package org.ow2.proactive.catalog.rest.service.repository;

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.rest.Application;
import org.ow2.proactive.catalog.rest.controller.AbstractCatalogObjectRevisionControllerTest;
import org.ow2.proactive.catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.catalog.rest.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.catalog.rest.entity.KeyValueMetadata;
import org.ow2.proactive.catalog.rest.entity.QCatalogObjectRevision;
import org.ow2.proactive.catalog.rest.entity.QKeyValueMetadata;
import org.ow2.proactive.catalog.rest.query.QueryExpressionContext;
import org.ow2.proactive.catalog.rest.util.parser.CatalogObjectParserResult;
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

import com.google.common.collect.ImmutableList;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;


/**
 * Integration tests associated to {@link QueryDslCatalogObjectRevisionRepository}.
 * <p>
 * The idea is to start a Web application with a in-memory database that is
 * pre-allocated with several workflow revisions. Then, tests run a query
 * against the database.
 *
 * @author ActiveEon Team
 * @see QueryDslCatalogObjectRevisionRepository
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest(randomPort = true)
public class QueryDslCatalogObjectRevisionRepositoryIntegrationTest
        extends AbstractCatalogObjectRevisionControllerTest {

    private static final Logger log = LoggerFactory.getLogger(QueryDslCatalogObjectRevisionRepositoryIntegrationTest.class);

    private static final int NUMBER_OF_BUCKETS = 2; // must be >= 2

    private static final int NUMBER_OF_CATALOG_OBJECTS = 10; // must be >= 2

    @Autowired
    private QueryDslCatalogObjectRevisionRepository queryDslCatalogObjectRevisionRepository;

    private List<BucketMetadata> buckets;

    @Before
    public void setUp() throws IOException, XMLStreamException {
        buckets = new ArrayList<>(NUMBER_OF_BUCKETS);

        for (int bucketIndex = 1; bucketIndex <= NUMBER_OF_BUCKETS; bucketIndex++) {
            BucketMetadata bucket = bucketService.createBucket("bucket" + bucketIndex);
            buckets.add(bucket);

            for (int workflowIndex = 1; workflowIndex <= NUMBER_OF_CATALOG_OBJECTS; workflowIndex++) {

                CatalogObjectMetadata workflow = catalogObjectService.createCatalogObject(bucket.id,
                                                                                          "workflow",
                                                                                          "name",
                                                                                          "commit message",
                                                                                          Optional.empty(),
                                                                                          new byte[0]);

                for (int revisionIndex = 2; revisionIndex <= workflowIndex; revisionIndex++) {

                    catalogObjectRevisionService.createCatalogObjectRevision(bucket.id,
                                                                             "workflow",
                                                                             "name",
                                                                             "commit message",
                                                                             Optional.of(workflow.id),
                                                                             Optional.empty(),
                                                                             createKeyValues(bucketIndex,
                                                                                             workflowIndex,
                                                                                             revisionIndex),
                                                                             new byte[0]);
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
        findAllWorkflowRevisions(buckets.get(0), NUMBER_OF_CATALOG_OBJECTS / 2);
    }

    @Test
    public void testFindAllWorkflowRevisions3() {
        findAllWorkflowRevisions(buckets.get(0), NUMBER_OF_CATALOG_OBJECTS);
    }

    @Test
    public void testFindAllWorkflowRevisions4() {
        ListSubQuery<CatalogObjectRevision> revisionSubQuery = new JPASubQuery().from(QKeyValueMetadata.keyValueMetadata)
                                                                                .where(QKeyValueMetadata.keyValueMetadata.key.eq("revisionIndex")
                                                                                                                             .and(QKeyValueMetadata.keyValueMetadata.value.eq(Integer.toString(NUMBER_OF_CATALOG_OBJECTS))))
                                                                                .list(QKeyValueMetadata.keyValueMetadata.catalogObjectRevision);

        BucketMetadata bucket = buckets.get(0);

        Page<CatalogObjectRevision> result = findAllWorkflowRevisions(bucket,
                                                                      NUMBER_OF_CATALOG_OBJECTS,
                                                                      Optional.of(QCatalogObjectRevision.catalogObjectRevision.in(new JPASubQuery().from(QCatalogObjectRevision.catalogObjectRevision)
                                                                                                                                                   .where(QCatalogObjectRevision.catalogObjectRevision.in(revisionSubQuery))
                                                                                                                                                   .list(QCatalogObjectRevision.catalogObjectRevision))),
                                                                      1);

        assertThat(result.getContent().get(0).getName()).isEqualTo("bucket" + bucket.id + "Name" +
                                                                   Integer.toString(NUMBER_OF_CATALOG_OBJECTS));
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
        findMostRecentWorkflowRevisions(buckets.get(1),
                                        Optional.of(QCatalogObjectRevision.catalogObjectRevision.name.eq("bucket2Name1")),
                                        1);
    }

    private Page<CatalogObjectRevision> findMostRecentWorkflowRevisions(BucketMetadata bucket) {
        return findMostRecentWorkflowRevisions(bucket, Optional.empty(), NUMBER_OF_CATALOG_OBJECTS);
    }

    private Page<CatalogObjectRevision> findMostRecentWorkflowRevisions(BucketMetadata bucket,
            Optional<BooleanExpression> booleanExpression, int expectedNumberOfWorkflowRevisions) {

        Page<CatalogObjectRevision> result = doQueryfindMostRecentWorkflowRevisions(bucket, booleanExpression);

        assertContainRightVariable(bucket, result);

        assertThat(result.getTotalElements()).isEqualTo(expectedNumberOfWorkflowRevisions);

        return result;
    }

    private void assertContainRightVariable(BucketMetadata bucket, Page<CatalogObjectRevision> result) {
        for (CatalogObjectRevision catalogObjectRevision : result.getContent()) {
            assertThat(bucket.id).isEqualTo(findBucketIndex(catalogObjectRevision));
        }
    }

    private Page<CatalogObjectRevision> findAllWorkflowRevisions(BucketMetadata bucket, int workflowIndex) {
        return findAllWorkflowRevisions(bucket,
                                        workflowIndex,
                                        Optional.empty(),
                                        getNumberOfWorkflowRevisions(workflowIndex));
    }

    private Page<CatalogObjectRevision> findAllWorkflowRevisions(BucketMetadata bucket, int workflowIndex,
            Optional<BooleanExpression> booleanExpression, int expectedNumberOfWorkflowRevisions) {

        Page<CatalogObjectRevision> result = doQueryfindAllWorkflowRevisions(bucket, workflowIndex, booleanExpression);

        assertContainRightVariable(bucket, result);

        assertThat(result.getTotalElements()).isEqualTo(expectedNumberOfWorkflowRevisions);

        return result;
    }

    private int findBucketIndex(CatalogObjectRevision catalogObjectRevision) {
        List<KeyValueMetadata> keyValueList = catalogObjectRevision.getKeyValueMetadataList();

        int bucketIndex = -1;

        for (KeyValueMetadata keyValue : keyValueList) {
            if (keyValue.getKey().equals("bucketIndex")) {
                bucketIndex = Integer.parseInt(keyValue.getValue());
                break;
            }
        }
        return bucketIndex;
    }

    private Page<CatalogObjectRevision> doQueryfindAllWorkflowRevisions(BucketMetadata bucket, long workflowId,
            Optional<BooleanExpression> booleanExpression) {

        return queryDslCatalogObjectRevisionRepository.findAllCatalogObjectRevisions(bucket.id,
                                                                                     workflowId,
                                                                                     createAnyQueryExpression(booleanExpression),
                                                                                     new PageRequest(0,
                                                                                                     getTotalNumberOfWorkflowRevisions()));
    }

    private Page<CatalogObjectRevision> doQueryfindMostRecentWorkflowRevisions(BucketMetadata bucket,
            Optional<BooleanExpression> booleanExpression) {

        return queryDslCatalogObjectRevisionRepository.findMostRecentCatalogObjectRevisions(bucket.id,
                                                                                            createAnyQueryExpression(booleanExpression),
                                                                                            new PageRequest(0,
                                                                                                            getTotalNumberOfWorkflowRevisions()));
    }

    private QueryExpressionContext createAnyQueryExpression(Optional<BooleanExpression> booleanExpression) {
        if (booleanExpression.isPresent()) {
            return new QueryExpressionContext(booleanExpression.get());
        }

        return new QueryExpressionContext(QCatalogObjectRevision.catalogObjectRevision.commitId.isNotNull());
    }

    private int getNumberOfWorkflowRevisions(int workflowIndex) {
        return workflowIndex;
    }

    private int getTotalNumberOfWorkflowRevisionsPerBucket() {
        return (NUMBER_OF_CATALOG_OBJECTS * (NUMBER_OF_CATALOG_OBJECTS - 1)) / 2;
    }

    private int getTotalNumberOfWorkflowRevisions() {
        return getTotalNumberOfWorkflowRevisionsPerBucket() * NUMBER_OF_BUCKETS;
    }

    private ImmutableList<KeyValueMetadata> createKeyValues(int bucketIndex, int workflowIndex, int revisionIndex) {
        ImmutableList.Builder<KeyValueMetadata> result = ImmutableList.builder();
        result.add(new KeyValueMetadata("generic_information", "bucketIndex", Integer.toString(bucketIndex)));
        result.add(new KeyValueMetadata("generic_information", "workflowIndex", Integer.toString(workflowIndex)));
        result.add(new KeyValueMetadata("generic_information", "revisionIndex", Integer.toString(revisionIndex)));
        result.add(new KeyValueMetadata("variable", "bucketIndex", Integer.toString(bucketIndex)));
        result.add(new KeyValueMetadata("variable", "workflowIndex", Integer.toString(workflowIndex)));
        result.add(new KeyValueMetadata("variable", "revisionIndex", Integer.toString(revisionIndex)));
        return result.build();
    }

}
