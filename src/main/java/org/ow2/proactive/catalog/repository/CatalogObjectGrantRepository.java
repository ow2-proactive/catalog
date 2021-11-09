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

import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


public interface CatalogObjectGrantRepository extends JpaRepository<CatalogObjectGrantEntity, Long>,
        JpaSpecificationExecutor<CatalogObjectGrantEntity>, QueryDslPredicateExecutor<CatalogObjectGrantEntity> {

    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByCatalogObjectRevisionEntityIdAndBucketEntityId(long catalogObjectId, long bucketId);

    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByCreatorAndBucketEntityId(String admin, long bucketId);

    List<CatalogObjectGrantEntity> findCatalogObjectGrantEntitiesByBucketEntityId(long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObjectRevisionEntity.id = ?1 AND coge.profiteer = ?2 AND coge.bucketEntity.id=?3 AND coge.grantee='user'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUsername(long catalogObjectId, String username, long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.catalogObjectRevisionEntity.id = ?1 AND coge.profiteer = ?2 AND coge.bucketEntity.id=?3 AND coge.grantee='group'")
    CatalogObjectGrantEntity findCatalogObjectGrantByUserGroup(long catalogObjectId, String userGroup, long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.profiteer = ?1 AND coge.catalogObjectRevisionEntity.id = ?2 AND coge.bucketEntity.id=?3 And coge.grantee='user'")
    List<CatalogObjectGrantEntity> findAllCatalogObjectGrantsAssignedToAUsername(String username, long catalogObjectId,
            long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge FROM CatalogObjectGrantEntity coge WHERE coge.profiteer = ?1 AND coge.catalogObjectRevisionEntity.id = ?2 AND coge.bucketEntity.id=?3 And coge.grantee='group'")
    List<CatalogObjectGrantEntity> findAllCatalogObjectGrantsAssignedToAUserGroup(String userGroup,
            long catalogObjectId, long bucketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge.bucketEntity.id FROM CatalogObjectGrantEntity coge WHERE coge.profiteer = ?1 AND coge.grantee='user'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT coge.bucketEntity.id FROM CatalogObjectGrantEntity coge WHERE coge.profiteer = ?1 AND coge.grantee='group'")
    List<Long> findAllBucketsIdFromCatalogObjectGrantsAssignedToAUserGroup(String userGroup);

}
