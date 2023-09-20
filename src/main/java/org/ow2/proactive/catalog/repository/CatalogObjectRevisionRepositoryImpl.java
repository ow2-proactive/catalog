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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;


@Repository
public class CatalogObjectRevisionRepositoryImpl implements CatalogObjectRevisionCustom {

    @PersistenceContext
    EntityManager em;

    @Value("${pa.catalog.db.items.max.size}")
    private Integer dbItemsMaxSize;

    @Override
    public List<CatalogObjectRevisionEntity> findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameInBucket(
            List<String> bucketNames, List<String> kindList, String contentType, String objectName, String projectName,
            String lastCommitBy, Long lastCommitTimeGreater, Long lastCommitTimeLessThan, int pageNo, int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize cannot be negative");
        }
        return bucketNames != null && bucketNames.isEmpty() ? new ArrayList<>()
                                                            : em.createQuery(buildCriteriaQuery(bucketNames,
                                                                                                kindList,
                                                                                                contentType,
                                                                                                objectName,
                                                                                                projectName,
                                                                                                lastCommitBy,
                                                                                                lastCommitTimeGreater,
                                                                                                lastCommitTimeLessThan))
                                                                .setMaxResults(pageSize)
                                                                .setFirstResult(pageNo * pageSize)
                                                                .getResultList();
    }

    @Override
    public List<CatalogObjectRevisionEntity>
            findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameAndTagInBucket(List<String> bucketNames,
                    List<String> objectNames, List<String> kindList, String contentType, String objectName,
                    String projectName, String lastCommitBy, String tag, Long lastCommitTimeGreater,
                    Long lastCommitTimeLessThan, int pageNo, int pageSize) {
        if (pageSize < 0) {
            throw new IllegalArgumentException("pageSize cannot be negative");
        }
        if (bucketNames != null) {
            if (bucketNames.isEmpty()) {
                return new ArrayList<>();
            } else if (bucketNames.size() > dbItemsMaxSize) {
                // if the number of buckets to filter is greater than the configurable max items, we consider not to filter by bucket names.
                // doing otherwise would be very complex
                bucketNames = null;
            }
        }
        if (objectNames != null && objectNames.isEmpty()) {
            return new ArrayList<>();
        } else if (objectNames != null && objectNames.size() > dbItemsMaxSize) {
            // we create a split partition for a big size of object name list
            List<CatalogObjectRevisionEntity> answer = new ArrayList<>();
            List<List<String>> partition = Lists.partition(objectNames, dbItemsMaxSize);
            for (List<String> objectNamesSubList : partition) {
                List<CatalogObjectRevisionEntity> subAnswer = em.createQuery(buildCriteriaQuery(bucketNames,
                                                                                                objectNamesSubList,
                                                                                                kindList,
                                                                                                contentType,
                                                                                                objectName,
                                                                                                projectName,
                                                                                                lastCommitBy,
                                                                                                lastCommitTimeGreater,
                                                                                                lastCommitTimeLessThan,
                                                                                                tag))
                                                                .setMaxResults(pageSize)
                                                                .setFirstResult(pageNo * pageSize)
                                                                .getResultList()
                                                                .stream()
                                                                .distinct()
                                                                .collect(Collectors.toList());
                answer.addAll(subAnswer);
            }
            return answer;
        } else {
            return em.createQuery(buildCriteriaQuery(bucketNames,
                                                     objectNames,
                                                     kindList,
                                                     contentType,
                                                     objectName,
                                                     projectName,
                                                     lastCommitBy,
                                                     lastCommitTimeGreater,
                                                     lastCommitTimeLessThan,
                                                     tag))
                     .setMaxResults(pageSize)
                     .setFirstResult(pageNo * pageSize)
                     .getResultList()
                     .stream()
                     .distinct()
                     .collect(Collectors.toList());
        }
    }

    private String toRightSidePredicatePattern(String pattern) {
        return pattern.contains("%") ? pattern.toLowerCase() : pattern.toLowerCase() + "%";
    }

    private String toBothSidesPredicatePattern(String pattern) {
        return pattern.contains("%") ? pattern.toLowerCase() : "%" + pattern.toLowerCase() + "%";
    }

    private CriteriaQuery<CatalogObjectRevisionEntity> buildCriteriaQuery(List<String> bucketNames,
            List<String> objectNames, List<String> kindList, String contentType, String objectName, String projectName,
            String lastCommitBy, Long lastCommitTimeGreater, Long lastCommitTimeLessThan, String tag) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CatalogObjectRevisionEntity> cq = cb.createQuery(CatalogObjectRevisionEntity.class);
        Root<CatalogObjectRevisionEntity> root = cq.from(CatalogObjectRevisionEntity.class);

        ListJoin<CatalogObjectRevisionEntity, KeyValueLabelMetadataEntity> metadata = null;

        if (tag != null) {
            metadata = root.joinList("keyValueMetadataList");
        }

        List<Predicate> allPredicates = getCommonPredicates(kindList,
                                                            cb,
                                                            root,
                                                            contentType,
                                                            objectName,
                                                            projectName,
                                                            lastCommitBy,
                                                            lastCommitTimeGreater,
                                                            lastCommitTimeLessThan,
                                                            bucketNames,
                                                            objectNames);

        if (tag != null) {
            allPredicates.add(cb.equal(metadata.get("label"), WorkflowParser.OBJECT_TAG_LABEL));
            allPredicates.add(cb.like(cb.lower(metadata.get("key")), toBothSidesPredicatePattern(tag)));
        }

        cq.where(allPredicates.toArray(new Predicate[0]));

        cq.orderBy(cb.asc(root.get("projectName")));

        cq.select(root);
        // we avoid using distinct as it creates an issue in Oracle
        // distinct is performed in java on the returned result
        return cq;
    }

    private List<Predicate> getCommonPredicates(List<String> kindList, CriteriaBuilder cb,
            Root<CatalogObjectRevisionEntity> root, String contentType, String objectName, String projectName,
            String lastCommitBy, Long lastCommitTimeGreater, Long lastCommitTimeLessThan, List<String> bucketNames,
            List<String> objectNames) {
        List<Predicate> allPredicates = new ArrayList<>();
        if (!kindList.isEmpty()) {
            List<Predicate> kindPredicates = new ArrayList<>();
            for (String kind : kindList) {
                kindPredicates.add(cb.like(root.get("catalogObject").get("kindLower"),
                                           toRightSidePredicatePattern(kind)));
            }
            Predicate orKindPredicate = cb.or(kindPredicates.toArray(new Predicate[0]));
            allPredicates.add(orKindPredicate);
        }

        if (contentType != null) {
            Predicate contentTypePredicate = cb.like(root.get("catalogObject").get("contentTypeLower"),
                                                     toRightSidePredicatePattern(contentType));
            allPredicates.add(contentTypePredicate);
        }
        if (objectName != null) {
            Predicate objectNamePredicate = cb.like(root.get("catalogObject").get("nameLower"),
                                                    toBothSidesPredicatePattern(objectName));
            allPredicates.add(objectNamePredicate);
        }

        if (!Strings.isNullOrEmpty(projectName)) {
            Predicate projectNamePredicate = cb.like(cb.lower(root.get("projectName")),
                                                     toBothSidesPredicatePattern(projectName));
            allPredicates.add(projectNamePredicate);
        }

        if (!Strings.isNullOrEmpty(lastCommitBy)) {
            Predicate projectNamePredicate = cb.equal(root.get("username"), lastCommitBy);
            allPredicates.add(projectNamePredicate);
        }

        if (lastCommitTimeGreater > 0) {
            allPredicates.add(cb.ge(root.get("catalogObject").get("lastCommitTime"), lastCommitTimeGreater));
        }

        if (lastCommitTimeLessThan > 0) {
            allPredicates.add(cb.lessThan(root.get("catalogObject").get("lastCommitTime"), lastCommitTimeLessThan));
        }

        if (bucketNames != null && bucketNames.size() <= dbItemsMaxSize) {
            Predicate bucketNamesPredicate = cb.in(root.get("catalogObject").get("bucket").get("bucketName"))
                                               .value(bucketNames);
            allPredicates.add(bucketNamesPredicate);

        }

        if (objectNames != null && objectNames.size() <= dbItemsMaxSize) {
            Predicate objectNamesPredicate = cb.in(root.get("catalogObject").get("nameLower")).value(objectNames);
            allPredicates.add(objectNamesPredicate);

        }

        Predicate lastCommitTimePredicate = cb.equal(root.get("catalogObject").get("lastCommitTime"),
                                                     root.get("commitTime"));
        allPredicates.add(lastCommitTimePredicate);
        return allPredicates;
    }

    private CriteriaQuery<CatalogObjectRevisionEntity> buildCriteriaQuery(List<String> bucketNames,
            List<String> kindList, String contentType, String objectName, String projectName, String lastCommitBy,
            Long lastCommitTimeGreater, Long lastCommitTimeLessThan) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CatalogObjectRevisionEntity> cq = cb.createQuery(CatalogObjectRevisionEntity.class);
        Root<CatalogObjectRevisionEntity> root = cq.from(CatalogObjectRevisionEntity.class);

        List<Predicate> allPredicates = getCommonPredicates(kindList,
                                                            cb,
                                                            root,
                                                            contentType,
                                                            objectName,
                                                            projectName,
                                                            lastCommitBy,
                                                            lastCommitTimeGreater,
                                                            lastCommitTimeLessThan,
                                                            bucketNames,
                                                            null);

        cq.where(allPredicates.toArray(new Predicate[0]));

        cq.orderBy(cb.asc(root.get("projectName")));
        cq.select(root);
        return cq;
    }

}
