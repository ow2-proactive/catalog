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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.truth.Truth.assertThat;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest(randomPort = true)
public class BucketControllerIntegrationTest extends AbstractRestAssuredTest {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String BUCKET_RESOURCE = "/buckets/{bucketId}";

    @Autowired
    private BucketRepository bucketRepository;

    @Test
    public void testCreateBucketShouldReturnSavedBucket() {
        final String bucketName = "test";
        final String bucketOwner = "BucketControllerIntegrationTest";

        Response response =
                given().parameters("name", bucketName, "owner", bucketOwner).
                        when().post(BUCKETS_RESOURCE);

        Object createdAt = response.getBody().jsonPath().get("created_at");
        assertThat(createdAt).isNotNull();

        response.then().assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .body("name", is(bucketName))
                .body("owner", is(bucketOwner));
    }

    @Test
    public void testCreateBucketShouldReturnBadRequestWithoutBody() {
        when().post(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testCreateDuplicatedBucketSameUser() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String ownerValue = "toto";
        final String bucketNameValue = "TotosBucket";
        given().parameters(ownerKey,ownerValue, bucketNameKey, bucketNameValue).post(BUCKETS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_CREATED);
        given().parameters(ownerKey,ownerValue, bucketNameKey, bucketNameValue).post(BUCKETS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testCreateDuplicatedBucketDifferentUsers() {
        final String ownerKey = "owner";
        final String bucketNameKey = "name";
        final String bucketNameValue = "OneSpecialBucket";
        given().parameters(ownerKey, "Alice", bucketNameKey, bucketNameValue).post(BUCKETS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_CREATED);
        given().parameters(ownerKey, "Bob", bucketNameKey, bucketNameValue).post(BUCKETS_RESOURCE)
                .then().assertThat().statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testGetBucketShouldReturnSavedBucket() throws Exception {
        Bucket bucket = bucketRepository.save(new Bucket("myBucket","BucketControllerIntegrationTestUser"));
        final long bucketId = bucket.getId();
        final String bucketName = bucket.getName();
        final LocalDateTime bucketCreatedAt = bucket.getCreatedAt();
        JsonPath jsonPath = given().pathParam("bucketId", 1L).when().get(BUCKET_RESOURCE).thenReturn().jsonPath();
        LocalDateTime actualDate = LocalDateTime.parse(jsonPath.getString("created_at"));
        assertEquals(jsonPath.getLong("id"), bucketId);
        assertEquals(jsonPath.getString("name"), bucketName);
        assertEquals(actualDate, bucketCreatedAt);
    }

    @Test
    public void testGetBucketShouldBeNotFoundIfNonExistingId() {
        given().pathParam("bucketId", 42L).get(BUCKET_RESOURCE).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListBucketsShouldReturnEmptyContent() {
        when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("page.number", is(0))
                .body("page.totalElements", is(0));
    }

    @Test
    public void testListBucketsShouldReturnSavedBuckets() {
        List<Bucket> buckets = IntStream.rangeClosed(1, 25)
                .mapToObj(i -> new Bucket("bucket" + i, "BucketResourceAssemblerTestUser"))
                .collect(Collectors.toList());

        bucketRepository.save(buckets);

        when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.bucketMetadataList", hasSize(20))
                .body("page.number", is(0))
                .body("page.totalElements", is(buckets.size()));
    }

    @Test
    public void testListBucketsOwnerShouldReturnNothing() {
        bucketRepository.save(new Bucket("TotosBucket","toto"));

        given().param("owner", "nonExistingUser").get(BUCKETS_RESOURCE)
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("page.number", is(0))
                .body("page.totalElements", is(0));
    }

    @Test
    public void testListBucketsOwnerShouldReturnOneBucketOnly() {
        final String owner = "toto";
        bucketRepository.save(new Bucket("TotosBucket",owner));

        given().param("owner", owner).get(BUCKETS_RESOURCE)
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.bucketMetadataList", hasSize(1))
                .body("page.number", is(0));
    }

    @Test
    public void testListOneBucketNameTwoOwners() {
        final String bucketName = "TheBucketOfLove";
        final String userAlice = "Alice";
        final String userBob = "Bob";
        bucketRepository.save(new Bucket(bucketName, userAlice));
        bucketRepository.save(new Bucket(bucketName, userBob));

        // TODO not finished
        // list all -> should return the 2 buckets
        when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.bucketMetadataList", hasSize(2))
                .body("page.number", is(0));

        // list alice -> should return one only
        given().param("owner", userAlice).get(BUCKETS_RESOURCE)
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.bucketMetadataList", hasSize(1))
                .body("page.number", is(0));

        // list bob -> should return one only
        given().param("owner", userBob).get(BUCKETS_RESOURCE)
                .then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("_embedded.bucketMetadataList", hasSize(1))
                .body("page.number", is(0));
    }


}