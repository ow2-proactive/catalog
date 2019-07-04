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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.IntegrationTestConfig;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectNameReference;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.dto.DependsOnCatalogObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.KindOrContentTypeIsNotValidException;
import org.ow2.proactive.catalog.util.IntegrationTestUtil;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author ActiveEon Team
 * @since 25/06/2017
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = IntegrationTestConfig.class)
public class CatalogObjectServiceIntegrationTest {

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Autowired
    private BucketService bucketService;

    private BucketMetadata bucket;

    private List<Metadata> keyValues;

    private byte[] workflowAsByteArray;

    private byte[] workflowAsByteArrayUpdated;

    private long firstCommitTime;

    private long secondCommitTime;

    @Autowired
    private SeparatorUtility separatorUtility;

    @Before
    public void setup() throws IOException, InterruptedException {
        PASchedulerProperties.CATALOG_REST_URL.updateProperty("http://localhost:8080/catalog");
        bucket = bucketService.createBucket("bucket", "CatalogObjectServiceIntegrationTest");
        keyValues = Collections.singletonList(new Metadata("key", "value", "type"));

        workflowAsByteArray = IntegrationTestUtil.getWorkflowAsByteArray("workflow.xml");
        workflowAsByteArrayUpdated = IntegrationTestUtil.getWorkflowAsByteArray("workflow-updated.xml");
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject(bucket.getName(),
                                                                                       "object-name-1",
                                                                                       "object",
                                                                                       "commit message",
                                                                                       "username",
                                                                                       "application/xml",
                                                                                       keyValues,
                                                                                       workflowAsByteArray,
                                                                                       null);
        firstCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Thread.sleep(1); // to be sure that a new revision time will be different from previous revision time
        catalogObject = catalogObjectService.createCatalogObjectRevision(bucket.getName(),
                                                                         "object-name-1",
                                                                         "commit message 2",
                                                                         "username",
                                                                         keyValues,
                                                                         workflowAsByteArrayUpdated);
        secondCommitTime = catalogObject.getCommitDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-2",
                                                 "object",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-3",
                                                 "workflow",
                                                 "commit message",
                                                 "username",
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
    public void testWorkflowCatalogObjectWithDependsOnModel() throws IOException {
        String aObject = "A_Object";
        String bObject = "B_Object";
        String bucketName = bucket.getName();

        // First commit of the A_Object to the catalog
        catalogObjectService.createCatalogObject(bucketName,
                                                 aObject,
                                                 "workflow",
                                                 "first commit message of A_Object",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-2.xml"),
                                                 null);

        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucketName,
                                                                                                    aObject);

        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("first commit message of A_Object");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("workflow");
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(11);

        List<String> dependsOnKeys = catalogObjectMetadata.getMetadataList()
                                                          .stream()
                                                          .filter(metadata -> metadata.getLabel()
                                                                                      .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                                          .map(Metadata::getKey)
                                                          .collect(Collectors.toList());

        // Check that depends on Metadata are well extracted from both job and task variables without duplication
        assertThat(dependsOnKeys).contains("data-connectors/FTP");
        assertThat(dependsOnKeys).contains("finance/QuantLib");
        assertThat(dependsOnKeys).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(dependsOnKeys).hasSize(3);

        //'bucket/A_Object' depends on bucket/B_Object'
        //check the correctness of the DB
        long firstCommitTimeDependsOn = catalogObjectMetadata.getCommitDateTime()
                                                             .atZone(ZoneId.systemDefault())
                                                             .toInstant()
                                                             .toEpochMilli();
        CatalogObjectDependencies catalogObjectDependencyListOfAObjectFirstCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                               aObject,
                                                                                                                               firstCommitTimeDependsOn);
        List<String> BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit = catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                                                                                 .stream()
                                                                                                                                 .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                                                 .collect(Collectors.toList());
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).hasSize(3);
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).contains("data-connectors/FTP");
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).contains("finance/QuantLib");
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(catalogObjectDependencyListOfAObjectFirstCommit.getCalledByList()).hasSize(0);

        // Second commit of the bucket/A_Object to the catalog which has the dependency bucket/A_Object' depends on bucket/B_Object
        catalogObjectService.createCatalogObjectRevision(bucketName,
                                                         aObject,
                                                         "second commit message of A_Object",
                                                         "username",
                                                         IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model.xml"));

        //if no revision number is specified, get ObjectDependencyList of the  last revision of the given object
        CatalogObjectDependencies catalogObjectDependencyListOfAObjectSecondCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                                aObject);
        List<String> BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit = catalogObjectDependencyListOfAObjectSecondCommit.getDependsOnList()
                                                                                                                                   .stream()
                                                                                                                                   .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                                                   .collect(Collectors.toList());
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).hasSize(4);
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                 bObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains("data-connectors/FTP");
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains("finance/QuantLib");
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(catalogObjectDependencyListOfAObjectSecondCommit.getCalledByList()).hasSize(0);

        //  Create a new catalog object 'bucket/B_Object' which is called by 'bucket/A_Object' and check that its calledBy list is not empty
        catalogObjectService.createCatalogObject(bucketName,
                                                 bObject,
                                                 "workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-2.xml"),
                                                 null);

        CatalogObjectDependencies catalogObjectDependencyListOfBObject = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                    bObject);
        List<String> BucketAndObjectNameDependsOnListOfBObjectFromDB = catalogObjectDependencyListOfBObject.getDependsOnList()
                                                                                                           .stream()
                                                                                                           .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                           .collect(Collectors.toList());
        assertThat(BucketAndObjectNameDependsOnListOfBObjectFromDB).hasSize(3);
        assertThat(BucketAndObjectNameDependsOnListOfBObjectFromDB).contains("data-connectors/FTP");
        assertThat(BucketAndObjectNameDependsOnListOfBObjectFromDB).contains("finance/QuantLib");
        assertThat(BucketAndObjectNameDependsOnListOfBObjectFromDB).contains("deep-learning-workflows/Custom_Sentiment_Analysis_In_Bing_News");
        assertThat(catalogObjectDependencyListOfBObject.getCalledByList()).hasSize(1);
        assertThat(catalogObjectDependencyListOfBObject.getCalledByList()).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                            aObject));
    }

    @Test
    public void testConsistencyOfDependsOnAndCalledBy() throws IOException {
        String bucketName = bucket.getName();
        String aObject = "A_Object";
        String bObject = "B_Object";
        String cObject = "C_Object";
        String dObject = "D_Object";
        String eObject = "E_Object";

        /************ FIRST TEST ****************/

        // First, creation of the bucket/B_Object and bucket/C_Object workflows in the catalog
        CatalogObjectMetadata catalogObjectMetadataOfFirstVersionOfBObject = catalogObjectService.createCatalogObject(bucketName,
                                                                                                                      bObject,
                                                                                                                      "workflow",
                                                                                                                      "commit message for B_Object",
                                                                                                                      "username",
                                                                                                                      "application/xml",
                                                                                                                      Collections.EMPTY_LIST,
                                                                                                                      IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-2.xml"),
                                                                                                                      null);
        catalogObjectMetadataOfFirstVersionOfBObject.getCommitDateTime()
                                                    .atZone(ZoneId.systemDefault())
                                                    .toInstant()
                                                    .toEpochMilli();

        catalogObjectService.createCatalogObject(bucketName,
                                                 cObject,
                                                 "workflow",
                                                 "commit message for C_Object",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-2.xml"),
                                                 null);

        // Second, creation of the bucket/A_Object workflow in the catalog which depends on the bucket/B_Object and bucket/C_Object workflows
        CatalogObjectMetadata catalogObjectMetadataOfFirstVersionOfAObject = catalogObjectService.createCatalogObject(bucketName,
                                                                                                                      aObject,
                                                                                                                      "workflow",
                                                                                                                      "First commit message of A_Object",
                                                                                                                      "username",
                                                                                                                      "application/xml",
                                                                                                                      Collections.EMPTY_LIST,
                                                                                                                      IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-first-commit.xml"),
                                                                                                                      null);

        long firstCommitTimeOfAObject = catalogObjectMetadataOfFirstVersionOfAObject.getCommitDateTime()
                                                                                    .atZone(ZoneId.systemDefault())
                                                                                    .toInstant()
                                                                                    .toEpochMilli();

        //Retrieve the CatalogObjectDependencies of the bucket/A_Object
        CatalogObjectDependencies catalogObjectDependencyListOfAObjectFirstCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                               aObject);
        List<String> BucketAndObjectNameDependsOnListOfAObjectFromDB = catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                                                                      .stream()
                                                                                                                      .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                                      .collect(Collectors.toList());

        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDB).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                     bObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDB).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                     cObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDB).hasSize(2);

        // Check that the bucket/A_Object workflow has the info that the bucket/B_Object workflow is in the Catalog --> isInCatalog=True
        assertTrue(catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                  .stream()
                                                                  .filter(dependsOnCatalogObject -> dependsOnCatalogObject.getBucketAndObjectName()
                                                                                                                          .equals(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                                                          bObject)))
                                                                  .map(DependsOnCatalogObject::isInCatalog)
                                                                  .collect(Collectors.toList())
                                                                  .get(0));

        assertThat(catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                  .stream()
                                                                  .filter(dependsOnCatalogObject -> dependsOnCatalogObject.getBucketAndObjectName()
                                                                                                                          .equals(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                                                          bObject)))
                                                                  .map(DependsOnCatalogObject::isInCatalog)
                                                                  .collect(Collectors.toList())).hasSize(1);

        // Check that the bucket/A_Object workflow has the info that the bucket/C_Object workflow is in the Catalog --> isInCatalog=True
        assertTrue(catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                  .stream()
                                                                  .filter(dependsOnCatalogObject -> dependsOnCatalogObject.getBucketAndObjectName()
                                                                                                                          .equals(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                                                          cObject)))
                                                                  .map(DependsOnCatalogObject::isInCatalog)
                                                                  .collect(Collectors.toList())
                                                                  .get(0));

        /************ SECOND TEST ****************/

        //Second commit of the bucket/A_Object workflow to the catalog which depends on bucket/D_Object and bucket/E_Object (different from the first commit bucket/A_Object' and bucket/B_Object)
        CatalogObjectMetadata catalogObjectMetadataOfSecondVersionOfAObject = catalogObjectService.createCatalogObjectRevision(bucketName,
                                                                                                                               aObject,
                                                                                                                               "second commit message of A_Object",
                                                                                                                               "username",
                                                                                                                               IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-second-commit.xml"));

        long secondCommitTimeOfAObject = catalogObjectMetadataOfSecondVersionOfAObject.getCommitDateTime()
                                                                                      .atZone(ZoneId.systemDefault())
                                                                                      .toInstant()
                                                                                      .toEpochMilli();

        //Check that depends on list of the second commit contains only bucket/D_Object and bucket/E_Object and its size is equals to 2
        //Note that if the revision number is not specified, getObjectDependencies method processes the last revision of the given catalog object

        CatalogObjectDependencies catalogObjectDependencyListOfAObjectSecondCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                                aObject,
                                                                                                                                secondCommitTimeOfAObject);
        List<String> BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit = catalogObjectDependencyListOfAObjectSecondCommit.getDependsOnList()
                                                                                                                                   .stream()
                                                                                                                                   .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                                                   .collect(Collectors.toList());
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).hasSize(2);
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                 dObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBSecondCommit).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                 eObject));
        assertThat(catalogObjectDependencyListOfAObjectSecondCommit.getCalledByList()).hasSize(0);

        //Check that bucket/B_Object and bucket/C_Object workflows are not called by the bucket/A_Object anymore (here the version is not considered)
        //First we check the bucket/B_Object
        CatalogObjectDependencies catalogObjectDependencyListOfBObject = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                    bObject);
        assertThat(catalogObjectDependencyListOfBObject.getCalledByList()).hasSize(0);

        //Second we check the bucket/C_Object
        CatalogObjectDependencies catalogObjectDependencyListOfCObject = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                    bObject);
        assertThat(catalogObjectDependencyListOfCObject.getCalledByList()).hasSize(0);

        /************ THIRD TEST ****************/

        // Make sure that the first version of bucket/A_Object still have bucket/B_Object and bucket/C_Object workflows in the dependsOn list object
        //First retrieve the catalogObjectDependency list of the first revision of the bucket/A_Object workflow

        catalogObjectDependencyListOfAObjectFirstCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                     aObject,
                                                                                                     firstCommitTimeOfAObject);
        List<String> BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit = catalogObjectDependencyListOfAObjectFirstCommit.getDependsOnList()
                                                                                                                                 .stream()
                                                                                                                                 .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                                                                 .collect(Collectors.toList());

        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                bObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).contains(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                cObject));
        assertThat(BucketAndObjectNameDependsOnListOfAObjectFromDBFirstCommit).hasSize(2);

        /************ FOURTH TEST ****************/

        //First, commit a third version of bucket/A_Object which contains a single dependency depends on bucket/D_Object/1551960076669

        CatalogObjectMetadata catalogObjectMetadataOfThirdVersionOfAObject = catalogObjectService.createCatalogObjectRevision(bucketName,
                                                                                                                              aObject,
                                                                                                                              "third commit message of A_Object",
                                                                                                                              "username",
                                                                                                                              IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-with-commit-time.xml"));

        long thirdCommitTimeOfAObject = catalogObjectMetadataOfThirdVersionOfAObject.getCommitDateTime()
                                                                                    .atZone(ZoneId.systemDefault())
                                                                                    .toInstant()
                                                                                    .toEpochMilli();

        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectRevision(bucketName,
                                                                                                    aObject,
                                                                                                    thirdCommitTimeOfAObject);

        List<String> dependsOnKeys = catalogObjectMetadata.getMetadataList()
                                                          .stream()
                                                          .filter(metadata -> metadata.getLabel()
                                                                                      .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                                          .map(Metadata::getKey)
                                                          .collect(Collectors.toList());

        assertThat(dependsOnKeys).contains("bucket/D_Object");
        assertThat(dependsOnKeys).hasSize(1);

        assertThat(catalogObjectMetadata.getMetadataList()
                                        .stream()
                                        .filter(metadata -> metadata.getLabel()
                                                                    .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                        .map(Metadata::getValue)
                                        .collect(Collectors.toList())).contains("1551960076669");

        CatalogObjectDependencies catalogObjectDependencyListOfAObjectThirdCommit = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                                               aObject,
                                                                                                                               thirdCommitTimeOfAObject);

        //check that bucket/D_Object/1551960076669 object is not in the catalog DB which means that isInCatalog=False
        assertFalse(catalogObjectDependencyListOfAObjectThirdCommit.getDependsOnList()
                                                                   .stream()
                                                                   .filter(dependsOnCatalogObject -> dependsOnCatalogObject.getBucketAndObjectName()
                                                                                                                           .equals(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                                                           dObject)))
                                                                   .collect(Collectors.toList())
                                                                   .get(0)
                                                                   .isInCatalog());

    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testCatalogObjectNotFound() throws IOException {
        String bucketName = bucket.getName();
        String aObject = "A_Object";
        String dObject = "D_Object";

        catalogObjectService.createCatalogObject(bucketName,
                                                 aObject,
                                                 "workflow",
                                                 "First commit message of A_Object",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_variables_with_catalog_object_model-with-commit-time.xml"),
                                                 null);

        catalogObjectService.getObjectDependencies(bucketName, dObject);

    }

    @Test
    public void testParseWorkflowContainingScriptUrl() throws Exception {
        String workflowName = "workflow-with-script-url";
        String bucketName = bucket.getName();
        // First commit of the A_Object to the catalog
        catalogObjectService.createCatalogObject(bucketName,
                                                 workflowName,
                                                 "workflow",
                                                 "first commit message",
                                                 "username",
                                                 "application/xml",
                                                 Collections.EMPTY_LIST,
                                                 IntegrationTestUtil.getWorkflowAsByteArray("workflow_with_script_url.xml"),
                                                 null);

        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucketName,
                                                                                                    workflowName);

        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("first commit message");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("workflow");
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(15);

        List<String> dependsOnKeys = catalogObjectMetadata.getMetadataList()
                                                          .stream()
                                                          .filter(metadata -> metadata.getLabel()
                                                                                      .equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL))
                                                          .map(Metadata::getKey)
                                                          .collect(Collectors.toList());

        // Check that depends on Metadata are well extracted from the script urls at all levels (task, pre, post, cleaning...) without duplication
        assertThat(dependsOnKeys).contains("basic-examples/Native_Task");
        assertThat(dependsOnKeys).contains("cloud-automation-scripts/Service_Start");
        assertThat(dependsOnKeys).contains("cloud-automation-scripts/Pre_Trigger_Action");
        assertThat(dependsOnKeys).contains("scripts/update_variables_from_system");
        assertThat(dependsOnKeys).contains("scripts/update_variables_from_file");
        assertThat(dependsOnKeys).hasSize(5);

        //check the consistency of the DB
        long firstCommitTimeDependsOn = catalogObjectMetadata.getCommitDateTime()
                                                             .atZone(ZoneId.systemDefault())
                                                             .toInstant()
                                                             .toEpochMilli();
        CatalogObjectDependencies catalogObjectDependencyList = catalogObjectService.getObjectDependencies(bucketName,
                                                                                                           workflowName,
                                                                                                           firstCommitTimeDependsOn);
        List<String> BucketAndObjectNameDependsOnListFromDB = catalogObjectDependencyList.getDependsOnList()
                                                                                         .stream()
                                                                                         .map(DependsOnCatalogObject::getBucketAndObjectName)
                                                                                         .collect(Collectors.toList());
        assertThat(BucketAndObjectNameDependsOnListFromDB).hasSize(5);
        assertThat(BucketAndObjectNameDependsOnListFromDB).contains("basic-examples/Native_Task");
        assertThat(BucketAndObjectNameDependsOnListFromDB).contains("cloud-automation-scripts/Service_Start");
        assertThat(BucketAndObjectNameDependsOnListFromDB).contains("cloud-automation-scripts/Pre_Trigger_Action");
        assertThat(BucketAndObjectNameDependsOnListFromDB).contains("scripts/update_variables_from_system");
        assertThat(BucketAndObjectNameDependsOnListFromDB).contains("scripts/update_variables_from_file");
        assertThat(catalogObjectDependencyList.getCalledByList()).hasSize(0);

    }

    @Test
    public void testListCatalogObjectsInBucket() {
        List<CatalogObjectMetadata> catalogObjects = catalogObjectService.listCatalogObjects(Arrays.asList(bucket.getName()));
        assertThat(catalogObjects).hasSize(3);
    }

    @Test
    public void testGetAllKinds() {
        Set<String> listKinds = catalogObjectService.getKinds();
        assertThat(listKinds).hasSize(2);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-4",
                                                 "workflow/new",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
        listKinds = catalogObjectService.getKinds();
        assertThat(listKinds).hasSize(3);
        assertThat(listKinds).contains("workflow/new");
    }

    @Test
    public void testUpdateObjectMetadata() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.updateObjectMetadata(bucket.getName(),
                                                                                                "object-name-1",
                                                                                                Optional.of("updated-kind"),
                                                                                                Optional.of("updated-contentType"));
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message 2");
        assertThat(catalogObjectMetadata.getUsername()).isEqualTo("username");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(3);
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("updated-kind");
    }

    @Test(expected = KindOrContentTypeIsNotValidException.class)
    public void testUpdateObjectMetadataWrongKind() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.updateObjectMetadata(bucket.getName(),
                                                                                                "object-name-1",
                                                                                                Optional.of("updated-kind//a asdf"),
                                                                                                Optional.of("updated-contentType"));
    }

    @Test(expected = KindOrContentTypeIsNotValidException.class)
    public void testCreateObjectWrongKind() {
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "object-name-2",
                                                 "updated-kind//a asdf",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
    }

    @Test
    public void testListCatalogObjectsByKindInBucket() {
        List<CatalogObjectMetadata> catalogObjects = catalogObjectService.listCatalogObjectsByKindAndContentType(Arrays.asList(bucket.getName()),
                                                                                                   "object", "");
        assertThat(catalogObjects).hasSize(2);

        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog4",
                                                 "workflow-general",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjects = catalogObjectService.listCatalogObjectsByKindAndContentType(Arrays.asList(bucket.getName()),
                                                                       "workflow-general", "");
        assertThat(catalogObjects).hasSize(1);

        catalogObjects = catalogObjectService.listCatalogObjectsByKindAndContentType(Arrays.asList(bucket.getName()), "WORKFLOW", "");
        assertThat(catalogObjects).hasSize(2);
    }

    @Test
    public void testGetDefaultCatalogObject() {
        CatalogObjectMetadata catalogObjectMetadata = catalogObjectService.getCatalogObjectMetadata(bucket.getName(),
                                                                                                    "object-name-1");
        assertThat(catalogObjectMetadata.getCommitMessage()).isEqualTo("commit message 2");
        assertThat(catalogObjectMetadata.getKind()).isEqualTo("object");
        assertThat(catalogObjectMetadata.getMetadataList()).hasSize(3);
        assertThat(catalogObjectMetadata.getContentType()).isEqualTo("application/xml");
    }

    @Test
    public void testGetDefaultCatalogRawObject() {
        CatalogRawObject rawObject = catalogObjectService.getCatalogRawObject(bucket.getName(), "object-name-1");
        assertThat(rawObject.getRawObject()).isNotNull();
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArrayUpdated);
    }

    @Test
    public void testListCatalogObjectRevisions() {

        List<CatalogObjectMetadata> metadataList = catalogObjectService.listCatalogObjectRevisions(bucket.getName(),
                                                                                                   "object-name-1");
        assertThat(metadataList).hasSize(2);
    }

    @Test
    public void testGetCatalogObjectsNameReferenceByKindAndContentType() {

        final String sessionId = "12345";
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/xml"))).hasSize(1);
        //Adding two catalog object of kind "workflow" and contentType "application/xml"
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_1",
                                                 "workflow/new",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_2",
                                                 "standard/workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/xml"))).hasSize(3);

        //Adding a new catalog object of kind "script" and contentType "text/x-groovy"
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_3",
                                                 "script",
                                                 "commit message",
                                                 "username",
                                                 "text/x-groovy",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/xml"))).hasSize(3);
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("sCriPt"),
                                                                                                     Optional.of("text/x-groovy"))).hasSize(1);

        //Adding a new catalog object of kind "rule" and contentType "text/x-sh"
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_4",
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "text/x-sh",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        //Adding a new catalog object of kind "rule" and contentType "text/css"
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_5",
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "text/css",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/xml"))).hasSize(3);
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("sCriPt"),
                                                                                                     Optional.of("text/x-groovy"))).hasSize(1);
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("ruLE"),
                                                                                                     Optional.of("text/x-sh"))).hasSize(1);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("ruLE"),
                                                                                                     Optional.empty())).hasSize(2);

        //create a second bucket and add some catalog objects to it

        BucketMetadata secondBucket = bucketService.createBucket("second-bucket",
                                                                 "CatalogObjectServiceIntegrationTest");

        catalogObjectService.createCatalogObject(secondBucket.getName(),
                                                 "catalog_object_1",
                                                 "workflow/new",
                                                 "commit message",
                                                 "username",
                                                 "application/python",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);
        catalogObjectService.createCatalogObject(secondBucket.getName(),
                                                 "catalog_object_2",
                                                 "standard/workflow",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        catalogObjectService.createCatalogObject(secondBucket.getName(),
                                                 "catalog_object_3",
                                                 "script",
                                                 "commit message",
                                                 "username",
                                                 "application/xml",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        //Adding another object to the first bucket
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 "catalog_object_6",
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "application/python",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/xml"))).hasSize(4);
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.of("application/python"))).hasSize(1);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("workflow"),
                                                                                                     Optional.empty())).hasSize(5);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("sCriPt"),
                                                                                                     Optional.empty())).hasSize(2);
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.of("ruLE"),
                                                                                                     Optional.empty())).hasSize(3);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.empty(),
                                                                                                     Optional.of("application/python"))).hasSize(2);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.empty(),
                                                                                                     Optional.of("application/xml"))).hasSize(7);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.empty(),
                                                                                                     Optional.of("text/x-groovy"))).hasSize(1);

        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.empty(),
                                                                                                     Optional.of("text/x-sh"))).hasSize(1);

        //Check total number of existing objects in the Catalog
        assertThat(catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                     sessionId,
                                                                                                     Optional.empty(),
                                                                                                     Optional.empty())).hasSize(12);
    }

    @Test
    public void testGetCatalogObjectsNameReferenceByKindAndContentTypeInBucketAddedOrder() {
        // The buckets order in returned list should be in the same order as the buckets was added to DB

        String secondBucketName = "aaa-bucket";
        BucketMetadata secondBucket = bucketService.createBucket(secondBucketName,
                                                                 "CatalogObjectServiceIntegrationTest");

        BucketMetadata emptyBucket = bucketService.createBucket("empty-bucket", "CatalogObjectServiceIntegrationTest");

        //Adding the object to the new bucket
        catalogObjectService.createCatalogObject(secondBucket.getName(),
                                                 "object-in-new-bucket-0",
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "application/python",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        //Adding another object to the first bucket
        String firstObjName = "object-first-bucket-0";
        catalogObjectService.createCatalogObject(bucket.getName(),
                                                 firstObjName,
                                                 "rule",
                                                 "commit message",
                                                 "username",
                                                 "application/python",
                                                 keyValues,
                                                 workflowAsByteArray,
                                                 null);

        List<CatalogObjectNameReference> catalogObjectNameReferences = catalogObjectService.getAccessibleCatalogObjectsNameReferenceByKindAndContentType(false,
                                                                                                                                                         "123",
                                                                                                                                                         Optional.empty(),
                                                                                                                                                         Optional.empty());

        assertThat(catalogObjectNameReferences).hasSize(5);
        assertThat(catalogObjectNameReferences.get(0).getBucketName()).isEqualTo(bucket.getName());
        assertThat(catalogObjectNameReferences.get(1).getBucketName()).isEqualTo(bucket.getName());
        assertThat(catalogObjectNameReferences.get(2).getBucketName()).isEqualTo(bucket.getName());
        assertThat(catalogObjectNameReferences.get(3).getBucketName()).isEqualTo(bucket.getName());
        assertThat(catalogObjectNameReferences.get(4).getBucketName()).isEqualTo(secondBucketName);
    }

    @Test
    public void testGetCatalogObjectRevision() throws UnsupportedEncodingException {

        CatalogObjectMetadata metadata = catalogObjectService.getCatalogObjectRevision(bucket.getName(),
                                                                                       "object-name-1",
                                                                                       firstCommitTime);
        assertThat(metadata.getCommitMessage()).isEqualTo("commit message");
    }

    @Test
    public void testGetCatalogObjectRevisionRaw() throws UnsupportedEncodingException {

        CatalogRawObject rawObject = catalogObjectService.getCatalogObjectRevisionRaw(bucket.getName(),
                                                                                      "object-name-1",
                                                                                      firstCommitTime);
        assertThat(rawObject.getCommitMessage()).isEqualTo("commit message");
        assertThat(rawObject.getRawObject()).isEqualTo(workflowAsByteArray);
    }

}
