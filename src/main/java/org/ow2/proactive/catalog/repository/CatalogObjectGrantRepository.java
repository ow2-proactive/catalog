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

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.bucket.bucketName = ?1 AND  coge.catalogObject.id.name = ?2")
    List<CatalogObjectGrantEntity> findCatalogObjectGrantsByBucketNameAndCatalogObjectName(String bucketName,
            String objectName);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.bucket.id = ?1")
    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByBucketEntityId(long bucketId);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.bucket.id = ?1 AND coge.grantee = ?2 And coge.granteeType='user'")
    List<CatalogObjectGrantEntity> findCatalogObjectsGrantsInABucketAssignedToAUser(long bucketId, String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.bucket.id = ?1 AND coge.grantee in ?2 And coge.granteeType='group'")
    List<CatalogObjectGrantEntity> findCatalogObjectsGrantsInABucketAssignedToAUserGroup(long bucketId,
            List<String> userGroup);

    //TODO to test
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.bucket.bucketName = ?1 AND  coge.catalogObject.id.name = ?2 AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findGrantsAssignedToAnObject(String bucketName, String catalogObjectName);

    //TODO to test
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.id.name = ?1 AND coge.grantee = ?2 AND coge.catalogObject.bucket.id = ?3 AND coge.granteeType='user'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUsernameForUpdate(String catalogObjectId, String username,
            long bucketId);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.id.name = ?1 AND coge.grantee = ?2 AND coge.catalogObject.bucket.id = ?3 AND coge.granteeType='tenant'")
    CatalogObjectGrantEntity findCatalogObjectGrantByTenantForUpdate(String catalogObjectId, String tenant,
            long bucketId);

    //TODO to test
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObject.id.name = ?1 AND coge.grantee = ?2 AND coge.catalogObject.bucket.id = ?3 AND coge.granteeType='group'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUserGroupForUpdate(String catalogObjectName, String userGroup,
            long bucketId);

    @Query(value = "SELECT coge.catalogObject.bucket.id FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType<>'noAccess'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUsername(String username);

    @Query(value = "SELECT coge.catalogObject.bucket.id FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='tenant' AND coge.accessType<>'noAccess'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToATenant(String tenant);

    @Query(value = "SELECT coge.catalogObject.bucket.id FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType<>'noAccess'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUserGroup(List<String> userGroups);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType='noAccess'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsWithNoAccessRightsAndAssignedToAUsername(String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='tenant' AND coge.accessType='noAccess'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsWithNoAccessRightsAndAssignedToATenant(String tenant);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType='noAccess'")
    List<CatalogObjectGrantEntity>
            findAllObjectGrantsWithNoAccessRightsAndAssignedToAUserGroup(List<String> userGroups);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user' AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllAccessibleObjectGrantsAssignedToAUser(String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='tenant' AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllAccessibleObjectGrantsAssignedToATenant(String tenant);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group' AND coge.accessType<>'noAccess'")
    List<CatalogObjectGrantEntity> findAllAccessibleObjectGrantsAssignedToUserGroups(List<String> userGroup);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='user'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsAssignedToAUser(String username);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee = ?1 AND coge.granteeType='tenant'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsAssignedToATenant(String tenant);

    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.grantee in ?1 AND coge.granteeType='group'")
    List<CatalogObjectGrantEntity> findAllObjectGrantsAssignedToUserGroups(List<String> userGroup);
}
