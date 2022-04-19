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

import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


public interface CatalogObjectGrantRepository extends JpaRepository<CatalogObjectGrantEntity, Long>,
        JpaSpecificationExecutor<CatalogObjectGrantEntity>, QueryDslPredicateExecutor<CatalogObjectGrantEntity> {

    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByBucketEntityIdAndCatalogObjectRevisionEntityId(
            long bucketId, long catalogObjectId);

    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByBucketEntityId(long bucketId);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.bucketEntity.bucketName = ?1 AND coge.catalogObjectRevisionEntity.catalogObject.id.name = ?2 AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllGrantsAssignedToAnObjectInsideABucket(String bucketName,
            String catalogObjectName);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObjectRevisionEntity.id = ?1 AND coge.grantee = ?2 AND coge.bucketEntity.id=?3 AND coge.granteeType='user'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUsernameForUpdate(long catalogObjectId, String username,
            long bucketId);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObjectRevisionEntity.id = ?1 AND coge.grantee = ?2 AND coge.bucketEntity.id=?3 AND coge.granteeType='group'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUserGroupForUpdate(long catalogObjectId, String userGroup,
            long bucketId);

    @Query(value = "SELECT coge.bucketEntity.id FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType<>'noAccess'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUsername(String username);

    @Query(value = "SELECT coge.bucketEntity.id FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType<>'noAccess'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUserGroup(List<String> userGroups);

    List<CatalogObjectGrantEntity> deleteAllByBucketEntityIdAndCatalogObjectRevisionEntityId(long bucketId,
            long catalogObjectId);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType='noAccess'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsWithNoAccessRightsAndAssignedToAUsername(String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType='noAccess'")
    List<CatalogObjectGrantEntity>
            findAllObjectGrantsWithNoAccessRightsAndAssignedToAUserGroup(List<String> userGroups);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsAssignedToAUser(String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsAssignedToUserGroups(List<String> userGroup);
}
