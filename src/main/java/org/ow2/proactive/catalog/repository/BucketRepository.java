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

import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;


/**
 * @author ActiveEon Team
 */
public interface BucketRepository extends JpaRepository<BucketEntity, Long>, JpaSpecificationExecutor<BucketEntity>,
        QueryDslPredicateExecutor<BucketEntity> {

    BucketEntity findOneByBucketName(String bucketName);

    List<BucketEntity> findByOwner(@Param("owner") String owner);

    List<BucketEntity> findByOwnerIn(List<String> owners);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bk FROM BucketEntity bk WHERE SIZE(bk.catalogObjects) = 0")
    List<BucketEntity> findEmptyBucketsForUpdate();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "javax.persistence.lock.timeout", value = "5000") })
    @Query(value = "SELECT bk FROM BucketEntity bk WHERE bk.bucketName = ?1")
    BucketEntity findBucketForUpdate(String bucketName);

    @Query(value = "SELECT DISTINCT bk FROM BucketEntity bk LEFT JOIN bk.catalogObjects cos" +
                   " WHERE lower(cos.kind) LIKE lower(concat(?1, '%')) AND lower(cos.contentType) LIKE lower(concat(?2, '%'))" +
                   " AND lower(cos.id.name) LIKE lower(concat('%', ?3, '%')) OR bk.catalogObjects IS EMPTY")
    List<BucketEntity> findContainingKindAndContentTypeAndObjectName(String kind, String contentType,
            String objectName);

    @Query(value = "SELECT DISTINCT bk FROM BucketEntity bk LEFT JOIN bk.catalogObjects cos" +
                   " WHERE bk.owner in ?1 AND (lower(cos.kind) LIKE lower(concat(?2, '%'))" +
                   " AND (lower(cos.contentType) LIKE lower(concat(?3, '%')) AND lower(cos.id.name) LIKE lower(concat('%', ?4, '%')))" +
                   " OR bk.catalogObjects IS EMPTY)")
    List<BucketEntity> findByOwnerIsInContainingKindAndContentTypeAndObjectName(List<String> owners, String kind,
            String contentType, String objectName);

}
