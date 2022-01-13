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

import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


public interface BucketGrantRepository extends JpaRepository<BucketGrantEntity, Long>,
        JpaSpecificationExecutor<BucketGrantEntity>, QueryDslPredicateExecutor<BucketGrantEntity> {

    List<BucketGrantEntity> findBucketGrantEntitiesByBucketEntityId(long bucketId);

    List<BucketGrantEntity> findBucketGrantEntitiesByCreator(String creatorName);

    List<BucketGrantEntity> deleteAllByBucketEntityId(long bucketId);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.grantee = ?2 AND bge.granteeType='user' AND bge.accessType<>'noAccess'")
    BucketGrantEntity findBucketGrantByUsername(long bucketId, String username);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.grantee = ?2 AND bge.granteeType='user'")
    BucketGrantEntity findBucketGrantByUsernameForUpdate(long bucketId, String username);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.grantee = ?2 AND bge.granteeType='group' AND bge.accessType<>'noAccess'")
    BucketGrantEntity findBucketGrantByUserGroup(long bucketId, String userGroup);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.bucketEntity.id = ?1 AND bge.grantee = ?2 AND bge.granteeType='group'")
    BucketGrantEntity findBucketGrantByUserGroupForUpdate(long bucketId, String userGroup);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.grantee = ?1 And bge.granteeType='user' AND bge.accessType<>'noAccess'")
    List<BucketGrantEntity> findAllGrantsAssignedToAUsername(String username);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.grantee in ?1 And bge.granteeType='group' AND bge.accessType<>'noAccess'")
    List<BucketGrantEntity> findAllGrantsAssignedToTheUserGroups(List<String> userGroup);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.grantee = ?1 AND bge.bucketEntity.id=?2 And bge.granteeType='user' AND bge.accessType<>'noAccess'")
    List<BucketGrantEntity> findAllGrantsAssignedToAUsernameInsideABucket(String username, long bucketId);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.grantee in ?1 And bge.granteeType='group' AND bge.bucketEntity.id=?2 AND bge.accessType<>'noAccess'")
    List<BucketGrantEntity> findAllGrantsAssignedToTheUserGroupsInsideABucket(List<String> userGroup, long bucketId);

    @Query(value = "SELECT bge FROM BucketGrantEntity bge WHERE bge.accessType='noAccess'")
    List<BucketGrantEntity> findAllNoAccessGrants();

}
