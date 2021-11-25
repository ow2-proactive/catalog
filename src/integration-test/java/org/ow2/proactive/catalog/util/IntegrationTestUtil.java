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
import org.ow2.proactive.catalog.dto.BucketGrantMetadata;

import com.google.common.io.ByteStreams;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
public class IntegrationTestUtil {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketName}/resources";

    private static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketName}/resources/{name}";

    private static final String BUCKETS_GRANTS_RESOURCE = "/buckets/grant";

    private static final String BUCKETS_GRANTS_RESOURCE_USER = "/buckets/grant/{username}";

    private static final String OBJECT_GRANT_RESOURCE = "/buckets/{bucketName}/grant/resources/{catalogObjectName}/grant";

    private static final String OBJECT_GRANTS_RESOURCE = "/buckets/{bucketName}/grant";

    private static final String CATALOG_OBJECT_NAME = "object";

    public static void cleanup() {
        // Delete bucket grants
        final String currentUser = "admin";
        final String bucketName = "test";
        final String grantee = "user";

        List<HashMap<String, String>> data = given().header("sessionID", "12345")
                                                    .pathParam("username", currentUser)
                                                    .get(BUCKETS_GRANTS_RESOURCE_USER)
                                                    .then()
                                                    .statusCode(HttpStatus.SC_OK)
                                                    .extract()
                                                    .path("");

        data.stream().forEach(grant -> {
            if (grant.get("granteeType").equals("user")) {
                given().header("sessionID", "12345")
                       .parameters("bucketName", bucketName, "currentUser", currentUser, "username", grantee)
                       .delete(BUCKETS_GRANTS_RESOURCE)
                       .then()
                       .statusCode(HttpStatus.SC_OK);
            } else {
                given().header("sessionID", "12345")
                       .parameters("bucketName", bucketName, "currentUser", currentUser, "userGroup", grantee)
                       .delete(BUCKETS_GRANTS_RESOURCE)
                       .then()
                       .statusCode(HttpStatus.SC_OK);
            }
        });
        // End delete bucket grants

        List<HashMap<String, String>> bucketMetadataList = given().get(BUCKETS_RESOURCE)
                                                                  .then()
                                                                  .statusCode(HttpStatus.SC_OK)
                                                                  .extract()
                                                                  .path("");

        bucketMetadataList.stream().forEach(bucketMetadata -> {
            List<HashMap<String, String>> catalogObjectMetadataList = given().pathParam("bucketName",
                                                                                        bucketMetadata.get("name"))
                                                                             .when()
                                                                             .get(CATALOG_OBJECTS_RESOURCE)
                                                                             .then()
                                                                             .extract()
                                                                             .path("");

            catalogObjectMetadataList.stream().forEach(catalogObjectMetadata -> {
                given().header("sessionID", "12345")
                       .pathParam("bucketName", catalogObjectMetadata.get("bucket_name"))
                       .pathParam("name", catalogObjectMetadata.get("name"))
                       .delete(CATALOG_OBJECT_RESOURCE)
                       .then()
                       .statusCode(HttpStatus.SC_OK);
            });
        });

        given().header("sessionID", "12345").delete(BUCKETS_RESOURCE).then().statusCode(HttpStatus.SC_OK);
    }

    public static void clearObjectGrants() {
        final String currentUser = "admin";
        final String bucketName = "test";
        final String grantee = "user";

        // Delete objects grants
        List<HashMap<String, String>> objectGrantsData = given().header("sessionID", "12345")
                                                                .pathParam("bucketName", bucketName)
                                                                .parameters("currentUser", currentUser)
                                                                .get(OBJECT_GRANTS_RESOURCE)
                                                                .then()
                                                                .statusCode(HttpStatus.SC_OK)
                                                                .extract()
                                                                .path("");

        if (!objectGrantsData.isEmpty()) {
            objectGrantsData.stream().forEach(grant -> {
                if (grant.get("granteeType").equals("user")) {
                    given().header("sessionID", "12345")
                           .pathParam("bucketName", bucketName)
                           .pathParam("catalogObjectName", CATALOG_OBJECT_NAME)
                           .parameters("currentUser", currentUser, "username", grantee)
                           .delete(OBJECT_GRANT_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.SC_OK);
                } else {
                    given().header("sessionID", "12345")
                           .pathParam("bucketName", bucketName)
                           .pathParam("catalogObjectName", CATALOG_OBJECT_NAME)
                           .parameters("currentUser", currentUser, "userGroup", grantee)
                           .delete(OBJECT_GRANT_RESOURCE)
                           .then()
                           .statusCode(HttpStatus.SC_OK);
                }
            });
        }
        // End delete object grants
    }

    public static byte[] getWorkflowAsByteArray(String filename) throws IOException {
        log.debug("The file path of loading file: " + getWorkflowFile(filename).getPath());
        return ByteStreams.toByteArray(new FileInputStream(getWorkflowFile(filename)));
    }

    public static byte[] getScriptAsByteArray(String filename) throws IOException {
        log.debug("The file path of loading file: " + getScriptFile(filename).getPath());
        return ByteStreams.toByteArray(new FileInputStream(getScriptFile(filename)));
    }

    public static File getWorkflowFile(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/workflows/" + filename).getFile());
    }

    public static File getScriptFile(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/scripts/" + filename).getFile());
    }

    public static File getPCWRule(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/pcw-rules/" + filename).getFile());
    }

    public static File getArchiveFile(String filename) {
        return new File(IntegrationTestUtil.class.getResource("/archives/" + filename).getFile());
    }

    /**
     *
     * @param bucketName
     * @param bucketOwner
     * @return Get bucket ID from response to create an object in it
     */
    public static String createBucket(String bucketName, String bucketOwner) {
        return given().header("sessionID", "12345")
                      .parameters("name", bucketName, "owner", bucketOwner)
                      .when()
                      .post(BUCKETS_RESOURCE)
                      .then()
                      .extract()
                      .path("name");
    }

    /**
     *  @param bucketId
     * @param name
     * @param projectName
     * @param kind
     * @param objectContentType
     * @param commitMessage
     * @param file
     */
    public static void postObjectToBucket(String bucketId, String name, String projectName, String kind,
            String objectContentType, String commitMessage, File file) {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketId)
               .queryParam("kind", kind)
               .queryParam("name", name)
               .queryParam("projectName", projectName)
               .queryParam("commitMessage", commitMessage)
               .queryParam("objectContentType", objectContentType)
               .multiPart(file)
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);
    }

    /**
     *
     * @param bucketId
     * @param kind
     * @param name
     * @param commitMessage
     * @param file
     */
    public static void postObjectToBucket(String bucketId, String kind, String name, String commitMessage, File file) {
        given().header("sessionID", "12345")
               .pathParam("bucketName", bucketId)
               .queryParam("kind", kind)
               .queryParam("name", name)
               .queryParam("commitMessage", commitMessage)
               .multiPart(file)
               .when()
               .post(CATALOG_OBJECTS_RESOURCE);
    }

    /**
     *
     * @param bucketId
     */
    public static void postDefaultWorkflowToBucket(String bucketId) {
        postObjectToBucket(bucketId,
                           "my workflow",
                           "myobjectprojectname",
                           "workflow",
                           "application/xml",
                           "first commit",
                           IntegrationTestUtil.getWorkflowFile("workflow.xml"));
    }
}
