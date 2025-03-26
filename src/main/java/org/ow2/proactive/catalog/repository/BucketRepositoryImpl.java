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

import static org.ow2.proactive.catalog.service.BucketService.DEFAULT_BUCKET_OWNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.common.base.Strings;


@Repository
public class BucketRepositoryImpl implements BucketRepositoryCustom {

    @PersistenceContext
    EntityManager em;

    @Value("${pa.catalog.tenant.filtering}")
    private boolean isTenantFiltering;

    @Override
    public List<Object[]> findBucketContainingKindListAndContentTypeAndObjectName(List<String> kindList,
            String contentType, String objectName) {

        return em.createQuery(buildCriteriaQuery(null, kindList, contentType, objectName, null, null, null))
                 .getResultList();
    }

    @Override
    public List<Object[]> findBucketByOwnerContainingKindListAndContentTypeAndObjectNameAndLastCommittedTimeInterval(
            List<String> owners, List<String> kindList, String contentType, String objectName,
            Long committedTimeGreater, Long committedTimeLessThan, String tenant, AuthenticatedUser user) {

        return em.createQuery(buildCriteriaQuery(owners,
                                                 kindList,
                                                 contentType,
                                                 objectName,
                                                 committedTimeGreater,
                                                 committedTimeLessThan,
                                                 tenant,
                                                 user))
                 .getResultList();
    }

    @Override
    public List<Object[]> findBucketByOwnerContainingKindList(List<String> owners, List<String> kindList) {
        return em.createQuery(buildCriteriaQuery(owners, kindList, null, null, null, null, null)).getResultList();
    }

    @Override
    public List<Object[]> findBucketContainingKindList(List<String> kindList) {
        return em.createQuery(buildCriteriaQuery(null, kindList, null, null, null, null, null)).getResultList();
    }

    private CriteriaQuery<Object[]> buildCriteriaQuery(List<String> owners, List<String> kindList, String contentType,
            String objectName, Long committedTimeGreater, Long committedTimeLessThan, String tenant,
            AuthenticatedUser user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<BucketEntity> bucketEntityRoot = cq.from(BucketEntity.class);
        cq.orderBy(cb.asc(bucketEntityRoot.get("id")));
        Join<BucketEntity, CatalogObjectEntity> catalogObjectsJoin = bucketEntityRoot.join("catalogObjects",
                                                                                           JoinType.LEFT);
        List<Predicate> allPredicates = new ArrayList<>();

        if (kindList != null && !kindList.isEmpty()) {
            List<Predicate> kindPredicatesList = new ArrayList<>();
            for (String kind : kindList) {
                kindPredicatesList.add(cb.like(catalogObjectsJoin.get("kindLower"),
                                               toRightSidePredicatePattern(kind.toLowerCase())));
            }
            Predicate kindPredicate;
            if (kindList.size() == 1) {
                kindPredicate = kindPredicatesList.get(0);
            } else {
                kindPredicate = cb.or(kindPredicatesList.toArray(new Predicate[0]));
            }
            allPredicates.add(kindPredicate);
        }

        if (!Strings.isNullOrEmpty(contentType)) {
            Predicate contentTypePredicate = cb.like(catalogObjectsJoin.get("contentTypeLower"),
                                                     toRightSidePredicatePattern(contentType.toLowerCase()));
            allPredicates.add(contentTypePredicate);
        }

        if (!Strings.isNullOrEmpty(objectName)) {
            Predicate objectNamePredicate = cb.like(catalogObjectsJoin.get("nameLower"),
                                                    toBothSidesPredicatePattern(objectName.toLowerCase()));
            allPredicates.add(objectNamePredicate);
        }

        if (owners != null && !owners.isEmpty()) {
            Predicate ownerPredicate = cb.in(bucketEntityRoot.get("owner")).value(owners);
            allPredicates.add(ownerPredicate);
        }
        if (!Strings.isNullOrEmpty(tenant)) {
            Predicate tenantPredicate = cb.equal(bucketEntityRoot.get("tenant"), tenant);
            allPredicates.add(tenantPredicate);
        }
        if (user != null && isTenantFiltering && !user.isAllTenantAccess()) {
            String userTenant = user.getTenant();
            Predicate tenantPredicate;
            Predicate nullTenantPredicate = cb.isNull(bucketEntityRoot.get("tenant"));
            if (!Strings.isNullOrEmpty(userTenant)) {
                Predicate userTenantPredicate = cb.equal(bucketEntityRoot.get("tenant"), userTenant);
                if (!Strings.isNullOrEmpty(tenant)) {
                    Predicate filteredTenantPredicate = cb.equal(bucketEntityRoot.get("tenant"), tenant);
                    tenantPredicate = cb.or(userTenantPredicate, filteredTenantPredicate, nullTenantPredicate);
                } else {
                    tenantPredicate = cb.or(userTenantPredicate, nullTenantPredicate);
                }
            } else {
                if (!Strings.isNullOrEmpty(tenant)) {
                    Predicate filteredTenantPredicate = cb.equal(bucketEntityRoot.get("tenant"), tenant);
                    tenantPredicate = cb.or(filteredTenantPredicate, nullTenantPredicate);
                } else {
                    tenantPredicate = cb.isNull(bucketEntityRoot.get("tenant"));
                }
            }
            allPredicates.add(tenantPredicate);
        }

        if (committedTimeGreater > 0) {
            allPredicates.add(cb.ge(catalogObjectsJoin.get("lastCommitTime"), committedTimeGreater));
        }
        if (committedTimeLessThan > 0) {
            allPredicates.add(cb.lessThan(catalogObjectsJoin.get("lastCommitTime"), committedTimeLessThan));
        }

        if (allPredicates.size() > 0) {
            Predicate finalPredicate;
            if (allPredicates.size() == 1) {
                finalPredicate = allPredicates.get(0);
            } else {
                finalPredicate = cb.and(allPredicates.toArray(new Predicate[0]));
            }

            cq.where(finalPredicate);
        }
        cq.multiselect(bucketEntityRoot.get("bucketName"),
                       bucketEntityRoot.get("owner"),
                       cb.count(catalogObjectsJoin.get("id").get("name")),
                       bucketEntityRoot.get("tenant"),
                       bucketEntityRoot.get("id"))
          .groupBy(bucketEntityRoot.get("bucketName"), bucketEntityRoot.get("owner"), bucketEntityRoot.get("id"));
        return cq;
    }

    private CriteriaQuery<Object[]> buildCriteriaQuery(List<String> owners, List<String> kindList, String contentType,
            String objectName, Long committedTimeGreater, Long committedTimeLessThan, String tenant) {
        return buildCriteriaQuery(owners,
                                  kindList,
                                  contentType,
                                  objectName,
                                  committedTimeGreater,
                                  committedTimeLessThan,
                                  tenant,
                                  null);
    }

    private String toBothSidesPredicatePattern(String pattern) {
        return pattern.contains("%") ? pattern.toLowerCase() : "%" + pattern.toLowerCase() + "%";
    }

    private String toRightSidePredicatePattern(String pattern) {
        return pattern.contains("%") ? pattern.toLowerCase() : pattern.toLowerCase() + "%";
    }
}
