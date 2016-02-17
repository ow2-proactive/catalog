package org.ow2.proactive.workflow_catalog.rest.service.repository;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.controller.AbstractWorkflowRevisionControllerTest;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.util.ProActiveWorkflowParser;
import com.google.common.io.ByteStreams;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * Integration tests associated to {@link QueryDslWorkflowRevisionRepository}.
 * <p/>
 * The idea is to start a Web application with a in-memory database
 * that is pre-allocated with several workflow revisions. Then, two main tests
 * are executed. Each runs multiple queries against the database (as sub-test)
 * and check the number of results returned and their value.
 *
 * @author ActiveEon Team
 */
@ActiveProfiles("default")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebIntegrationTest
public class QueryDslWorkflowRevisionRepositoryIntegrationTest2 extends AbstractWorkflowRevisionControllerTest {

    private static final Logger log = LoggerFactory.getLogger(
            QueryDslWorkflowRevisionRepositoryIntegrationTest2.class);

    @Autowired
    private QueryDslWorkflowRevisionRepository queryDslWorkflowRevisionRepository;

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
    public void setUp() throws IOException, XMLStreamException {
        bucket = bucketService.createBucket("test");

        String[] files = new String[] {
//                "/home/lpellegr/Téléchargements/wcql-tests/A.xml",
//                "/home/lpellegr/Téléchargements/wcql-tests/B.xml",
//                "/home/lpellegr/Téléchargements/wcql-tests/C.xml",
//                "/home/lpellegr/Téléchargements/wcql-tests/D.xml",
                "/home/lpellegr/Téléchargements/wcql-tests/E.xml",
                "/home/lpellegr/Téléchargements/wcql-tests/F.xml",
        };

        for (String file : files) {
            final byte[] bytes = ByteStreams.toByteArray(new FileInputStream(file));

            ProActiveWorkflowParser parser =
                    new ProActiveWorkflowParser(new FileInputStream(file));

            workflowService.createWorkflow(bucket.id, parser.parse(), bytes);
        }
    }


//    @Test
//    public void test() {
//        final Page<WorkflowRevision> custom =
//                queryDslWorkflowRevisionRepository.findCustom(bucket.id,
//                        new PageRequest(0, 100));
//
//        final List<WorkflowRevision> content = custom.getContent();
//
//        System.out.println("NB QUERY RESULT=" + content.size());
//
//        for (WorkflowRevision wr : content) {
//            System.out.println("WORKFLOW REVISION " + wr);
//        }
//
//    }

}