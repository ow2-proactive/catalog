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
package org.ow2.proactive.catalog.repository;

import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


public interface BucketGrantRepository extends JpaRepository<BucketGrantEntity, Long>,
        JpaSpecificationExecutor<BucketGrantEntity>, QueryDslPredicateExecutor<BucketGrantEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1")
    List<BucketGrantEntity> findAllGrantsByBucket(long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.profiteer = ?2 AND bge.grantee='user'")
    BucketGrantEntity findBucketGrantByUsername(long bucketId, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.profiteer = ?2 AND bge.grantee='group'")
    BucketGrantEntity findBucketGrantByUserGroup(long bucketId, String userGroup);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.creator = ?1")
    List<BucketGrantEntity> findAllGrantsCreatedByUsername(String admin);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.profiteer = ?1 And bge.grantee='user'")
    List<BucketGrantEntity> findAllGrantsAssignedToAUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.profiteer = ?1 And bge.grantee='group'")
    List<BucketGrantEntity> findAllGrantsAssignedToAUserGroup(String userGroup);

}
