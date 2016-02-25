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
package org.ow2.proactive.workflow_catalog.rest.query;

import org.ow2.proactive.workflow_catalog.rest.entity.QWorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
import com.mysema.query.jpa.JPASubQuery;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


/**
 * Create the BooleanExpression that will be used to generate the final QueryDSL query.
 *
 * @author ActiveEon Team
 */
public class WorkflowCatalogQueryExpressionBuilder {

    private final String workflowCatalogQuery;

    protected WorkflowCatalogQueryCompiler queryCompiler;

    protected WorkflowCatalogQueryLanguageListener queryListener;

    private ParseTreeWalker walker;

    public WorkflowCatalogQueryExpressionBuilder(String workflowCatalogQuery) {
        this(workflowCatalogQuery, new WorkflowCatalogQueryCompiler(),
                new WorkflowCatalogQueryLanguageListener(),
                new ParseTreeWalker());
    }

    protected WorkflowCatalogQueryExpressionBuilder(String workflowCatalogQuery,
            WorkflowCatalogQueryCompiler queryCompiler,
            WorkflowCatalogQueryLanguageListener queryListener,
            ParseTreeWalker walker) {
        this.queryCompiler = queryCompiler;
        this.queryListener = queryListener;
        this.workflowCatalogQuery = workflowCatalogQuery;
        this.walker = walker;
    }

    public QueryExpressionContext build() throws QueryExpressionBuilderException {
        // empty query must throw no exception and apply no filter
        if (workflowCatalogQuery.trim().isEmpty()) {
            return new QueryExpressionContext(
                    QWorkflowRevision.workflowRevision.in(
                            new JPASubQuery()
                                    .from(QWorkflowRevision.workflowRevision)
                                    .list(QWorkflowRevision.workflowRevision)));
        }

        try {
            WorkflowCatalogQueryLanguageParser.StartContext context =
                    queryCompiler.compile(workflowCatalogQuery);
            walker.walk(queryListener, context);
            return new QueryExpressionContext(queryListener.getBooleanExpression());
        } catch (Exception e) {
            throw new QueryExpressionBuilderException(e.getMessage());
        }
    }

}
