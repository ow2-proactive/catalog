/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */

package org.ow2.proactive.workflow_catalog.rest.service.repository;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Predicate;
import org.ow2.proactive.workflow_catalog.rest.entity.QGenericInformation;
import org.ow2.proactive.workflow_catalog.rest.entity.QVariable;
import org.ow2.proactive.workflow_catalog.rest.entity.QWorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


/**
 * @author ActiveEon Team
 */
@Repository
public class QueryDslWorkflowRevisionRepository extends QueryDslRepositorySupport {

    @PersistenceContext
    private EntityManager entityManager;

    private QWorkflowRevision qWorkflowRevision = QWorkflowRevision.workflowRevision;

    private QGenericInformation qGenericInformation = QGenericInformation.genericInformation;

    private QVariable qVariable = QVariable.variable;

    public QueryDslWorkflowRevisionRepository() {
        super(WorkflowRevision.class);
    }

    public Page<WorkflowRevision> findAllWorkflowRevisions(Predicate predicate, Pageable pageable) {
        return findWorkflowRevisions(
                new JPAQuery(entityManager)
                        .from(qWorkflowRevision)
                        .leftJoin(qWorkflowRevision.variables, qVariable)
                        .leftJoin(qWorkflowRevision.genericInformation, qGenericInformation)
                        .where(predicate)
                        .distinct(), pageable);
    }

    public Page<WorkflowRevision> findMostRecentWorkflowRevisions(Predicate predicate, Pageable pageable) {
        return findWorkflowRevisions(new JPAQuery(entityManager)
                .from(qWorkflowRevision)
                .join(qWorkflowRevision.workflow)
                .leftJoin(qWorkflowRevision.variables, qVariable)
                .leftJoin(qWorkflowRevision.genericInformation, qGenericInformation)
                .where(qWorkflowRevision.workflow.lastRevisionId.eq(qWorkflowRevision.revisionId))
                .where(predicate)
                .distinct(), pageable);
    }

    private Page<WorkflowRevision> findWorkflowRevisions(JPQLQuery query, Pageable pageable) {
        QWorkflowRevision qWorkflowRevision = QWorkflowRevision.workflowRevision;

        long count = query.count();

        JPQLQuery paginatedQuery = super.getQuerydsl().applyPagination(pageable, query);

        return new PageImpl<>(paginatedQuery.list(qWorkflowRevision), pageable, count);
    }

}
