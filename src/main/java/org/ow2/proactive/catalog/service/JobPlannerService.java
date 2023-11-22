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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.Validate;
import org.ow2.proactive.catalog.dto.AssociatedObject;
import org.ow2.proactive.catalog.dto.AssociatedObjectsByBucket;
import org.ow2.proactive.catalog.service.helper.CommonRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.extern.log4j.Log4j2;


@Service
@Lazy
@Log4j2
public class JobPlannerService {

    private final CommonRestTemplate commonRestTemplate;

    @Value("${pa.job-planner.rest.url}")
    private String jobPlannerRestUrl;

    @Value("${pa.job-planner.planned_objects.path}")
    private String plannedObjectsPaths;

    @Value("${pa.job-planner.planned_object.status.path}")
    private String plannedObjectStatusPath;

    @Value("${pa.job-planner.cache.timeout}")
    private String jobPlannerCacheTimeout;

    private Cache<String, List<AssociatedObjectsByBucket>> jobPlannerAllAssociationsCache;

    private Cache<String, AssociatedObject> jobPlannerAssociationCache;

    private static final String ALL_ASSOCIATIONS = "ALL";

    private ReentrantLock mainReentrantLock = new ReentrantLock();

    private Map<String, ReentrantLock> objectLocks = new ConcurrentHashMap<>();

    @Autowired
    public JobPlannerService() {
        this.commonRestTemplate = new CommonRestTemplate();
        int timeout = jobPlannerCacheTimeout != null ? Integer.parseInt(jobPlannerCacheTimeout) : 0;
        jobPlannerAllAssociationsCache = CacheBuilder.newBuilder().expireAfterWrite(timeout, TimeUnit.SECONDS).build();
        jobPlannerAssociationCache = CacheBuilder.newBuilder().expireAfterWrite(timeout, TimeUnit.SECONDS).build();
    }

    public List<AssociatedObjectsByBucket> getAssociatedObjects(String sessionId) {
        Validate.notNull(sessionId, "SessionId must not be null");
        List<AssociatedObjectsByBucket> answer;
        try {
            mainReentrantLock.lock();
            answer = jobPlannerAllAssociationsCache.getIfPresent(ALL_ASSOCIATIONS);
            if (answer == null) {
                ResponseEntity<AssociatedObjectsByBucket[]> listResponseEntity;
                String url = jobPlannerRestUrl + plannedObjectsPaths;
                HttpHeaders headers = new HttpHeaders();
                headers.set("sessionid", sessionId);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<String> entity = new HttpEntity<String>(headers);
                try {
                    listResponseEntity = commonRestTemplate.getRestTemplate()
                                                           .exchange(url,
                                                                     HttpMethod.GET,
                                                                     entity,
                                                                     AssociatedObjectsByBucket[].class);
                    answer = Arrays.asList(listResponseEntity.getBody());
                } catch (HttpStatusCodeException httpException) {
                    log.error(String.format("Could not retrieve job-planner associated objects, http response is: %s",
                                            httpException.getResponseBodyAsString()));
                    answer = Collections.emptyList();
                }
                jobPlannerAllAssociationsCache.put(ALL_ASSOCIATIONS, answer);
            }
        } finally {
            mainReentrantLock.unlock();
        }
        return answer;
    }

    public AssociatedObject getAssociatedObject(String sessionId, String bucketName, String objectName) {
        Validate.notNull(sessionId, "SessionId must not be null");
        Validate.notNull(bucketName, "bucketName must not be null");
        Validate.notNull(objectName, "objectName must not be null");
        String key = bucketName + "_" + objectName;
        objectLocks.putIfAbsent(key, new ReentrantLock());
        AssociatedObject answer;
        try {
            objectLocks.get(key).lock();
            answer = jobPlannerAssociationCache.getIfPresent(key);
            if (answer == null) {
                ResponseEntity<AssociatedObject> responseEntity;
                String url = jobPlannerRestUrl + plannedObjectStatusPath;
                HttpHeaders headers = new HttpHeaders();
                headers.set("sessionid", sessionId);
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<String> entity = new HttpEntity<String>(headers);
                try {
                    responseEntity = commonRestTemplate.getRestTemplate().exchange(url,
                                                                                   HttpMethod.GET,
                                                                                   entity,
                                                                                   AssociatedObject.class,
                                                                                   bucketName,
                                                                                   objectName);
                    answer = responseEntity.getBody();
                } catch (HttpStatusCodeException httpException) {
                    log.error(String.format("Could not retrieve job-planner associated object, http response is: %s",
                                            httpException.getResponseBodyAsString()));
                    answer = new AssociatedObject(objectName, new HashSet<>());
                }
                jobPlannerAssociationCache.put(key, answer);
            }
        } finally {
            objectLocks.get(key).unlock();
        }
        return answer;
    }

}
