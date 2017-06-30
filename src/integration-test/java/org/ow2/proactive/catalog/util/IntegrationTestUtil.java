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
package org.ow2.proactive.catalog.util;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpStatus;

import com.google.common.io.ByteStreams;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
public class IntegrationTestUtil {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketId}/resources";

    private static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketId}/resources/{name}";

    public static void cleanup() {
        List<HashMap<String, String>> bucketMetadataList = given().get(BUCKETS_RESOURCE)
                                                                  .then()
                                                                  .statusCode(HttpStatus.SC_OK)
                                                                  .extract()
                                                                  .path("");

        bucketMetadataList.stream().forEach(bucketMetadata -> {
            List<HashMap<String, String>> catalogObjectMetadataList = given().pathParam("bucketId",
                                                                                        bucketMetadata.get("id"))
                                                                             .when()
                                                                             .get(CATALOG_OBJECTS_RESOURCE)
                                                                             .then()
                                                                             .extract()
                                                                             .path("");

            catalogObjectMetadataList.stream().forEach(catalogObjectMetadata -> {
                given().pathParam("bucketId", catalogObjectMetadata.get("bucket_id"))
                       .pathParam("name", catalogObjectMetadata.get("name"))
                       .delete(CATALOG_OBJECT_RESOURCE)
                       .then()
                       .statusCode(HttpStatus.SC_OK);
            });
        });

        given().delete(BUCKETS_RESOURCE).then().statusCode(HttpStatus.SC_OK);
    }

    public static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        log.debug("The file path of loading file: " + getWorkflowFile(filename).getPath());
        return ByteStreams.toByteArray(new FileInputStream(getWorkflowFile(filename)));
    }

    public static File getWorkflowFile(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/workflows/" + filename).getFile());
    }

    public static File getArchiveFile(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/archives/" + filename).getFile());
    }

}
