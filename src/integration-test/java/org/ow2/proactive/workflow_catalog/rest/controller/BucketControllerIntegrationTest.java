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

import com.jayway.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ow2.proactive.workflow_catalog.rest.Application;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.BucketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author ActiveEon Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
// TODO use specific in-memory configuration for tests
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BucketControllerIntegrationTest {

    private static final String BUCKETS_RESOURCE = "/buckets";

    private static final String BUCKET_RESOURCE = "/buckets/{bucketId}";

    @Autowired
    private BucketRepository bucketRepository;

    @Test
    public void testCreateBucketShouldReturnSavedBucket() {
        String bucketName = "test";

        Response response =
                given().parameters("name", bucketName).
                        when().post(BUCKETS_RESOURCE);

        Object createdAt = response.getBody().jsonPath().get("created_at");
        assertThat(createdAt).isNotNull();

        response.then().assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("id", is(1))
                .body("name", equalTo(bucketName));
    }

    @Test
    public void testCreateBucketShouldReturnBadRequestWithoutBody() {
        when().post(BUCKETS_RESOURCE).then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void testGetBucketShouldReturnSavedBucket() throws Exception {
        Bucket bucket = bucketRepository.save(createBucket("myBucket"));

        given().pathParam("bucketId", 1L).
                when().get(BUCKET_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(bucket.getId().intValue()))
                .body("created_at", equalTo(bucket.getCreatedAt().toString()))
                .body("name", equalTo(bucket.getName()));
    }

    @Test
    public void testGetBucketShouldBeBadRequestIfNonExistingId() {
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
        int nbBuckets = 25;

        List<Bucket> buckets = new ArrayList<>(nbBuckets);

        for (int i = 0; i < nbBuckets; i++) {
            buckets.add(createBucket("bucket" + i));
        }

        bucketRepository.save(buckets);
        System.out.println(when().get(BUCKETS_RESOURCE).thenReturn().asString());
        when().get(BUCKETS_RESOURCE).then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("page.number", is(0))
                .body("page.totalElements", is(nbBuckets));
    }

    private Bucket createBucket(String bucketName) {
        return new Bucket(bucketName, LocalDateTime.now());
    }

}