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
package org.ow2.proactive.catalog.rest.controller;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.catalog.Application;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.restassured.response.Response;


/**
 * @author ActiveEon Team
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@Transactional
@WebIntegrationTest(randomPort = true)
public class BucketControllerIntegrationTest extends AbstractRestAssuredTest {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String BUCKET_RESOURCE = "/buckets/{bucketId}";

    @Autowired
    private BucketRepository bucketRepository;

    @Before
    public void setup() {
        given().delete(BUCKETS_RESOURCE).then().statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void testCreateBucketShouldReturnSavedBucket() {
        final String bucketName = "test";
        final String bucketOwner = "BucketControllerIntegrationTest";

        Response response = given().parameters("name", bucketName, "owner", bucketOwner).when().post(BUCKETS_RESOURCE);

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
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
        final String ownerValue = "newowner";
        final String bucketNameValue = "newbucket";
        given().parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CREATED);
        given().parameters(ownerKey, ownerValue, bucketNameKey, bucketNameValue)
               .post(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    public void testGetBucketShouldBeNotFoundIfNonExistingId() {
        given().pathParam("bucketId", 42L).get(BUCKET_RESOURCE).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testListBucketsShouldReturnSavedBuckets() {

        List<BucketEntity> buckets = IntStream.rangeClosed(1, 25)
                                              .mapToObj(i -> new BucketEntity("bucket" + i,
                                                                              "BucketResourceAssemblerTestUser"))
                                              .collect(Collectors.toList());

        buckets.stream().forEach(bucket -> given().parameters("name", bucket.getName(), "owner", bucket.getOwner())
                                                  .when()
                                                  .post(BUCKETS_RESOURCE));

        when().get(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_OK).body("", hasSize(25));
    }

    @Test
    public void testListBucketsOwnerShouldReturnNothing() {
        given().parameters("name", "TotosBucket", "owner", "toto").when().post(BUCKETS_RESOURCE);

        given().param("owner", "nonExistingUser")
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(0));
    }

    @Test
    public void testListOneBucketNameTwoOwners() {
        final String bucketName = "TheBucketOfLove";
        final String userAlice = "Alice";
        final String userBob = "Bob";
        given().parameters("name", bucketName, "owner", userAlice).when().post(BUCKETS_RESOURCE);
        given().parameters("name", bucketName, "owner", userBob).when().post(BUCKETS_RESOURCE);

        // list all -> should return the 2 buckets
        when().get(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_OK).body("", hasSize(2));

        // list alice -> should return one only
        given().param("owner", userAlice)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));

        // list bob -> should return one only
        given().param("owner", userBob)
               .get(BUCKETS_RESOURCE)
               .then()
               .assertThat()
               .statusCode(HttpStatus.SC_OK)
               .body("", hasSize(1));
    }

}
