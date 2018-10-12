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
package org.ow2.proactive.catalog.service;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.graphql.bean.CatalogObjectConnection;
import org.ow2.proactive.catalog.graphql.fetcher.CatalogObjectFetcher;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author ActiveEon Team
 * @since 11/07/2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class GraphqlServiceIntegrationTest {

    @Autowired
    private GraphqlService graphqlService;

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    @Autowired
    private CatalogObjectFetcher.CatalogObjectMapper catalogObjectMapper;

    private BucketMetadata bucket;

    private List<Metadata> keyValues;

    private byte[] workflowAsByteArray;

    private byte[] workflowAsByteArrayUpdated;

    private long firstCommitTime;

    private long secondCommitTime;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws IOException, NotAuthenticatedException, AccessDeniedException, InterruptedException {
        doReturn("link").when(catalogObjectMapper).generatLink(anyString(), anyString());

        bucket = bucketService.createBucket("bucket", "CatalogObjectServiceIntegrationTest");
        keyValues = Collections.singletonList(new Metadata("key", "value", "type"));

        workflowAsByteArray = IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml");
        workflowAsByteArrayUpdated = IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml");
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucket.getName(),
                                                                                       "catalog1",
                                                                                       "object",
                                                                                       "commit message",
                                                                                       "application/xml",
                                                                                       keyValues,
                                                                                       workflowAsByteArray,
                                                                                       null);
        firstCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Thread.sleep(1); // to be sure that a new revision time will be different from previous revision time
        catalogObject = catalogObjectService.createCatalogObjectRevision(bucket.getName(),
                                                                         "catalog1",
                                                                         "commit message 2",
                                                                         keyValues,
                                                                         workflowAsByteArrayUpdated);
        secondCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog2",
                                                 "object",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        keyValues = Collections.singletonList(new Metadata("key", "value2", "type"));

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog3",
                                                 "workflow",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog4",
                                                 "workflow",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

    }

    @After
    public void deleteBucket() {
        bucketService.cleanAll();
    }

    @Test
    public void testGetAllCatalogObjects() {
        String query = "{\n" + "  allCatalogObjects {\n" + "    edges {\n" + "      bucketName\n" + "      name\n" +
                       "      kind\n" + "      contentType\n" + "      metadata {\n" + "        key\n" +
                       "        value\n" + "        label\n" + "      }\n" + "      commitMessage\n" +
                       "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" +
                       "    totalCount\n" + "  }  \n" + "}\n";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(4);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
        assertThat(connection.getPage()).isEqualTo(0);
        assertThat(connection.getSize()).isEqualTo(50);

    }

    @Test
    public void testPageInfoQuery() {
        String query = "{\n" + "  allCatalogObjects(pageInfo:{page:1, size:2}) {\n" + "    edges {\n" +
                       "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                       "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                       "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                       "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(2);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isTrue();
        assertThat(connection.getPage()).isEqualTo(1);
        assertThat(connection.getSize()).isEqualTo(2);
    }

    @Test
    public void testNameQuery() {
        String query = "{\n" + "  allCatalogObjects(where:{nameArg:{eq:\"catalog2\"}}) {\n" + "    edges {\n" +
                       "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                       "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                       "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                       "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(1);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
        assertThat(connection.getEdges().stream().anyMatch(e -> !e.getName().equals("catalog2"))).isFalse();

        query = "{\n" + "  allCatalogObjects(where:{nameArg:{like:\"%log2\"}}) {\n" + "    edges {\n" +
                "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" + "      metadata {\n" +
                "        key\n" + "        value\n" + "        label\n" + "      }\n" + "      commitMessage\n" +
                "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" +
                "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(1);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
        assertThat(connection.getEdges().stream().anyMatch(e -> !e.getName().endsWith("log2"))).isFalse();
    }

    @Test
    public void testBucketIdQuery() {
        String query = "{\n" + "  allCatalogObjects(where:{bucketNameArg:{eq:\"" + bucket.getName() + "\"}}) {\n" +
                       "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" +
                       "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                       "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" +
                       "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" +
                       "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(4);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(1);

        query = "{\n" + "  allCatalogObjects(where:{bucketNameArg:{eq:\"" + bucket.getName() + "\"}}) {\n" +
                "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(4);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(1);
    }

    @Test
    public void testBucketNameLikeQuery() {
        String query = "{\n" + "  allCatalogObjects(where:{bucketNameArg:{like:\"" + bucket.getName() + "\"}}) {\n" +
                       "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" +
                       "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                       "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" +
                       "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" +
                       "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(4);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(1);
    }

    @Test
    public void testKindQuery() {
        String query = "{\n" + "  allCatalogObjects(where:{kindArg:{eq:\"object\"}}) {\n" + "    edges {\n" +
                       "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                       "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                       "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                       "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(2);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.getEdges().stream().anyMatch(e -> !e.getKind().equals("object"))).isFalse();

        query = "{\n" + "  allCatalogObjects(where:{kindArg:{ne:\"object\"}}) {\n" + "    edges {\n" +
                "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" + "      metadata {\n" +
                "        key\n" + "        value\n" + "        label\n" + "      }\n" + "      commitMessage\n" +
                "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" +
                "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(2);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
        assertThat(connection.getEdges().stream().anyMatch(e -> e.getKind().equals("object"))).isFalse();

        query = "{\n" + "  allCatalogObjects(where:{kindArg:{like:\"%work%\"}}) {\n" + "    edges {\n" +
                "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" + "      metadata {\n" +
                "        key\n" + "        value\n" + "        label\n" + "      }\n" + "      commitMessage\n" +
                "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" +
                "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(2);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
        assertThat(connection.getEdges().stream().anyMatch(e -> !e.getKind().contains("work"))).isFalse();
    }

    @Test
    public void testMetadataQuery() {
        String query = "{\n" + "  allCatalogObjects(where:{metadataArg:{key:\"key\", value:{eq:\"value\"}}}) {\n" +
                       "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" +
                       "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                       "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" +
                       "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" +
                       "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}\n";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(2);
        assertThat(connection.getTotalPage()).isEqualTo(1);

        query = "{\n" + "  allCatalogObjects(where:{metadataArg:{key:\"key\", value:{ne:\"value\"}}}) {\n" +
                "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}\n";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(2);
        assertThat(connection.getTotalCount()).isEqualTo(2);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();

        query = "{\n" + "  allCatalogObjects(where:{metadataArg:{key:\"key\", value:{like:\"value%\"}}}) {\n" +
                "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" + "      contentType\n" +
                "      metadata {\n" + "        key\n" + "        value\n" + "        label\n" + "      }\n" +
                "      commitMessage\n" + "      commitDateTime\n" + "    }\n" + "    page\n" + "    size\n" +
                "    totalPage\n" + "    totalCount\n" + "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}\n";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(4);
        assertThat(connection.getTotalCount()).isEqualTo(4);
        assertThat(connection.getTotalPage()).isEqualTo(1);
        assertThat(connection.isHasNext()).isFalse();
        assertThat(connection.isHasPrevious()).isFalse();
    }

    @Test
    public void testSimpleAndQuery() throws IOException {
        String query = "{\n" +
                       "  allCatalogObjects(where:{AND:[{nameArg:{eq:\"catalog1\"}}, {kindArg:{eq:\"object\"}}]}) {\n" +
                       "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" +
                       "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                       "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" +
                       "    link}\n" + "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" +
                       "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}\n";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(1);
        assertThat(connection.getTotalCount()).isEqualTo(1);
        assertThat(connection.getTotalPage()).isEqualTo(1);
    }

    @Test
    public void testComplexAndQuery() throws IOException {
        // new bucket
        BucketMetadata bucket2 = bucketService.createBucket("bucket2", "CatalogObjectServiceIntegrationTest");
        keyValues = Collections.singletonList(new Metadata("key2", "", "type"));

        catalogObjectService.createCatalogObject(bucket2.getName(),
                                                 "catalog1",
                                                 "object",
                                                 "commit message",
                                                 "application/xml",
                                                 keyValues,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml"),
                                                 null);

        String query = "{\n" +
                       "  allCatalogObjects(where:{OR:[{AND:[{nameArg:{eq:\"catalog1\"}}, {kindArg:{eq:\"object\"}}, {metadataArg:{key:\"key2\", value:{like:\"%%\"}}}]}, {AND:[{kindArg:{eq:\"workflow\"}}, {metadataArg:{key:\"key\",value:{eq:\"value2\"}}}]}]}) {\n" +
                       "    edges {\n" + "      bucketName\n" + "      name\n" + "      kind\n" +
                       "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                       "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" +
                       "    }\n" + "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" +
                       "    hasNext\n" + "    hasPrevious\n" + "  }  \n" + "}\n";

        Map<String, Object> map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        Map objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        CatalogObjectConnection connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(3);
        assertThat(connection.getTotalCount()).isEqualTo(3);
        assertThat(connection.getTotalPage()).isEqualTo(1);

        query = "{\n" +
                "  allCatalogObjects(where:{OR:[{AND:[{OR:[{nameArg:{eq:\"catalog1\"}},{nameArg:{eq:\"catalog2\"}}]}, {bucketNameArg:{eq:\"" +
                bucket.getName() + "\"}}]}, {AND:[{nameArg:{eq:\"catalog1\"}}, {bucketNameArg:{eq:\"" +
                bucket2.getName() + "\"}}]}]}) {\n" + "    edges {\n" + "      bucketName\n" + "      name\n" +
                "      kind\n" + "      contentType\n" + "      metadata {\n" + "        key\n" + "        value\n" +
                "        label\n" + "      }\n" + "      commitMessage\n" + "      commitDateTime\n" + "    }\n" +
                "    page\n" + "    size\n" + "    totalPage\n" + "    totalCount\n" + "    hasNext\n" +
                "    hasPrevious\n" + "  }  \n" + "}\n";

        map = graphqlService.executeQuery(query, null, null, null);

        assertThat(map.get("errors")).isNull();
        assertThat(map.get("data")).isNotNull();
        objects = (Map) ((Map) map.get("data")).get("allCatalogObjects");
        connection = mapper.convertValue(objects, CatalogObjectConnection.class);
        assertThat(connection.getEdges()).hasSize(3);
        assertThat(connection.getTotalCount()).isEqualTo(3);
        assertThat(connection.getTotalPage()).isEqualTo(1);
    }
}
