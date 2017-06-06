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
package org.ow2.proactive.catalog.rest.service.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.catalog.rest.entity.QCatalogObjectRevision;
import org.ow2.proactive.catalog.rest.query.QueryExpressionContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.stereotype.Repository;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.query.ListSubQuery;


/**
 * @author ActiveEon Team
 */
@Repository
public class QueryDslCatalogObjectRevisionRepository extends QueryDslRepositorySupport {

    @PersistenceContext
    private EntityManager entityManager;

    private QCatalogObjectRevision qCatalogObjectRevision = QCatalogObjectRevision.catalogObjectRevision;

    public QueryDslCatalogObjectRevisionRepository() {
        super(CatalogObjectRevision.class);
    }

    public Page<CatalogObjectRevision> findAllCatalogObjectRevisions(long bucketId, Long catalogObjectId,
            QueryExpressionContext context, Pageable pageable) {
        ListSubQuery<Long> allCatalogObjectRevisions = createFindAllCatalogObjectRevisionsSubQuery(bucketId,
                                                                                                   catalogObjectId);

        return findCatalogObjectRevisions(allCatalogObjectRevisions, context, pageable);
    }

    private ListSubQuery<Long> createFindAllCatalogObjectRevisionsSubQuery(long bucketId, Long catalogObjectId) {
        return new JPASubQuery().from(QCatalogObjectRevision.catalogObjectRevision)
                                .join(qCatalogObjectRevision.catalogObject)
                                .where(qCatalogObjectRevision.catalogObject.id.eq(catalogObjectId)
                                                                              .and(qCatalogObjectRevision.bucketId.eq(bucketId)))
                                .list(qCatalogObjectRevision.commitId);
    }

    public Page<CatalogObjectRevision> findMostRecentCatalogObjectRevisions(long bucketId,
            QueryExpressionContext context, Pageable pageable) {

        ListSubQuery<Long> allMostRecentRevisions = createFindMostRecentRevisionsSubQuery(bucketId);

        return findCatalogObjectRevisions(allMostRecentRevisions, context, pageable);
    }

    private ListSubQuery<Long> createFindMostRecentRevisionsSubQuery(long bucketId) {
        return new JPASubQuery().from(QCatalogObjectRevision.catalogObjectRevision)
                                .join(qCatalogObjectRevision.catalogObject)
                                .where(qCatalogObjectRevision.catalogObject.lastCommitId.eq(qCatalogObjectRevision.commitId)
                                                                                        .and(qCatalogObjectRevision.bucketId.eq(bucketId)))
                                .list(qCatalogObjectRevision.commitId);
    }

    private Page<CatalogObjectRevision> findCatalogObjectRevisions(ListSubQuery<Long> catalogObjectRevisionIds,
            QueryExpressionContext context, Pageable pageable) {
        JPAQuery query = new JPAQuery(entityManager).from(QCatalogObjectRevision.catalogObjectRevision);

        query = query.where(qCatalogObjectRevision.commitId.in(catalogObjectRevisionIds).and(context.getExpression()))
                     .distinct();

        long count = query.count();

        JPQLQuery paginatedQuery = super.getQuerydsl().applyPagination(pageable, query);

        return new PageImpl<>(paginatedQuery.list(qCatalogObjectRevision), pageable, count);
    }

}
