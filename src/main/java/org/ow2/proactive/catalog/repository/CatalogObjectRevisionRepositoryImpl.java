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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.springframework.stereotype.Repository;


@Repository
public class CatalogObjectRevisionRepositoryImpl implements CatalogObjectRevisionCustom {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<CatalogObjectRevisionEntity> findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameInBucket(
            List<String> bucketNames, List<String> kindList, String contentType, String objectName, int pageNo,
            int pageSize) {
        return em.createQuery(buildCriteriaQuery(bucketNames, kindList, contentType, objectName))
                 .setMaxResults(pageSize)
                 .setFirstResult(pageNo * pageSize)
                 .getResultList();
    }

    private CriteriaQuery<CatalogObjectRevisionEntity> buildCriteriaQuery(List<String> bucketNames,
            List<String> kindList, String contentType, String objectName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CatalogObjectRevisionEntity> cq = cb.createQuery(CatalogObjectRevisionEntity.class);
        Root<CatalogObjectRevisionEntity> catalogObjectRevisionEntityRoot = cq.from(CatalogObjectRevisionEntity.class);
        cq.orderBy(cb.asc(catalogObjectRevisionEntityRoot.get("projectName")));

        List<Predicate> allPredicates = new ArrayList<>();
        if (!kindList.isEmpty()) {
            Predicate kindPredicate = cb.or();
            for (String kind : kindList) {
                kindPredicate = cb.or(kindPredicate,
                                      cb.like(catalogObjectRevisionEntityRoot.get("catalogObject").get("kindLower"),
                                              kind.toLowerCase() + "%"));
            }
            allPredicates.add(kindPredicate);
        }

        if (contentType != null) {
            Predicate contentTypePredicate = cb.like(catalogObjectRevisionEntityRoot.get("catalogObject")
                                                                                    .get("contentTypeLower"),
                                                     contentType.toLowerCase() + "%");
            allPredicates.add(contentTypePredicate);
        }
        if (objectName != null) {
            Predicate objectNamePredicate = cb.like(catalogObjectRevisionEntityRoot.get("catalogObject")
                                                                                   .get("nameLower"),
                                                    "%" + objectName.toLowerCase() + "%");
            allPredicates.add(objectNamePredicate);
        }

        if (bucketNames != null) {
            Predicate bucketNamesPredicate = cb.in(catalogObjectRevisionEntityRoot.get("catalogObject")
                                                                                  .get("bucket")
                                                                                  .get("bucketName"))
                                               .value(bucketNames);
            allPredicates.add(bucketNamesPredicate);

        }

        Predicate lastCommitTimePredicate = cb.equal(catalogObjectRevisionEntityRoot.get("catalogObject")
                                                                                    .get("lastCommitTime"),
                                                     catalogObjectRevisionEntityRoot.get("commitTime"));
        allPredicates.add(lastCommitTimePredicate);

        Predicate filtersPredicate;
        if (allPredicates.size() == 1) {
            filtersPredicate = allPredicates.get(0);
        } else {
            filtersPredicate = cb.and(allPredicates.toArray(new Predicate[0]));
        }
        cq.where(filtersPredicate);

        cq.select(catalogObjectRevisionEntityRoot);
        return cq;
    }

}
