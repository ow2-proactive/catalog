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
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.stereotype.Repository;


@Repository
public class CatalogObjectRevisionRepositoryImpl implements CatalogObjectRevisionCustom {

    @PersistenceContext
    EntityManager em;

    @Override
    public List<CatalogObjectRevisionEntity> findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameInBucket(
            List<String> bucketNames, List<String> kindList, String contentType, String objectName, int pageNo,
            int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize cannot be negative");
        }
        return em.createQuery(buildCriteriaQuery(bucketNames, kindList, contentType, objectName))
                 .setMaxResults(pageSize)
                 .setFirstResult(pageNo * pageSize)
                 .getResultList();
    }

    @Override
    public List<CatalogObjectRevisionEntity>
            findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameAndTagInBucket(List<String> bucketNames,
                    List<String> kindList, String contentType, String objectName, String tag, int pageNo,
                    int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize cannot be negative");
        }
        return em.createQuery(buildCriteriaQuery(bucketNames, kindList, contentType, objectName, tag))
                 .setMaxResults(pageSize)
                 .setFirstResult(pageNo * pageSize)
                 .getResultList()
                 .stream()
                 .distinct()
                 .collect(Collectors.toList());
    }

    private CriteriaQuery<CatalogObjectRevisionEntity> buildCriteriaQuery(List<String> bucketNames,
            List<String> kindList, String contentType, String objectName, String tag) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CatalogObjectRevisionEntity> cq = cb.createQuery(CatalogObjectRevisionEntity.class);
        Root<CatalogObjectRevisionEntity> root = cq.from(CatalogObjectRevisionEntity.class);

        ListJoin<CatalogObjectRevisionEntity, KeyValueLabelMetadataEntity> metadata = root.joinList("keyValueMetadataList");

        List<Predicate> allPredicates = getCommonPredicates(kindList, cb, root, contentType, objectName, bucketNames);

        allPredicates.add(cb.equal(metadata.get("label"), WorkflowParser.OBJECT_TAG_LABEL));
        allPredicates.add(cb.like(cb.lower(metadata.get("key")), "%" + tag.toLowerCase() + "%"));

        cq.where(allPredicates.toArray(new Predicate[0]));

        cq.orderBy(cb.asc(root.get("projectName")));

        cq.select(root);
        // we avoid using distinct as it creates an issue in Oracle
        // distinct is performed in java on the returned result
        return cq;
    }

    private List<Predicate> getCommonPredicates(List<String> kindList, CriteriaBuilder cb,
            Root<CatalogObjectRevisionEntity> root, String contentType, String objectName, List<String> bucketNames) {
        List<Predicate> allPredicates = new ArrayList<>();
        if (!kindList.isEmpty()) {
            List<Predicate> kindPredicates = new ArrayList<>();
            for (String kind : kindList) {
                kindPredicates.add(cb.like(root.get("catalogObject").get("kindLower"), kind.toLowerCase() + "%"));
            }
            Predicate orKindPredicate = cb.or(kindPredicates.toArray(new Predicate[0]));
            allPredicates.add(orKindPredicate);
        }

        if (contentType != null) {
            Predicate contentTypePredicate = cb.like(root.get("catalogObject").get("contentTypeLower"),
                                                     contentType.toLowerCase() + "%");
            allPredicates.add(contentTypePredicate);
        }
        if (objectName != null) {
            Predicate objectNamePredicate = cb.like(root.get("catalogObject").get("nameLower"),
                                                    "%" + objectName.toLowerCase() + "%");
            allPredicates.add(objectNamePredicate);
        }

        if (bucketNames != null) {
            Predicate bucketNamesPredicate = cb.in(root.get("catalogObject").get("bucket").get("bucketName"))
                                               .value(bucketNames);
            allPredicates.add(bucketNamesPredicate);

        }

        Predicate lastCommitTimePredicate = cb.equal(root.get("catalogObject").get("lastCommitTime"),
                                                     root.get("commitTime"));
        allPredicates.add(lastCommitTimePredicate);
        return allPredicates;
    }

    private CriteriaQuery<CatalogObjectRevisionEntity> buildCriteriaQuery(List<String> bucketNames,
            List<String> kindList, String contentType, String objectName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CatalogObjectRevisionEntity> cq = cb.createQuery(CatalogObjectRevisionEntity.class);
        Root<CatalogObjectRevisionEntity> root = cq.from(CatalogObjectRevisionEntity.class);

        List<Predicate> allPredicates = getCommonPredicates(kindList, cb, root, contentType, objectName, bucketNames);

        cq.where(allPredicates.toArray(new Predicate[0]));

        cq.orderBy(cb.asc(root.get("projectName")));

        cq.select(root);
        return cq;
    }

}
