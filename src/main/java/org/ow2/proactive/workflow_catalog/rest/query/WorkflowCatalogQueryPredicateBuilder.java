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

import com.google.common.collect.ImmutableSet;
import com.mysema.query.BooleanBuilder;
import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;


/**
 * Create the BooleanBuilder that will be used to generate the WCQL query
 *
 * @author ActiveEon Team
 */
public class WorkflowCatalogQueryPredicateBuilder {

    private final String workflowCatalogQuery;

    protected WorkflowCatalogQueryCompiler queryCompiler;

    protected WorkflowCatalogQueryLanguageVisitor wcqlVisitor;

    public WorkflowCatalogQueryPredicateBuilder(String workflowCatalogQuery) {
        this.workflowCatalogQuery = workflowCatalogQuery;
        this.queryCompiler = new WorkflowCatalogQueryCompiler();
        this.wcqlVisitor = new WorkflowCatalogQueryLanguageVisitor();
    }

    public PredicateContext build() throws QueryPredicateBuilderException {
        // empty query must throw no exception and be valid
        if (workflowCatalogQuery.trim().isEmpty()) {
            return new PredicateContext(
                    new BooleanBuilder(), ImmutableSet.of(), ImmutableSet.of());
        }

        try {
            WorkflowCatalogQueryLanguageParser.ExpressionContext context =
                    queryCompiler.compile(workflowCatalogQuery);

            BooleanBuilder booleanBuilder = wcqlVisitor.visitExpression(context);

            WorkflowCatalogQueryLanguageVisitor.QGenerator generator = wcqlVisitor.getGenerator();

            return new PredicateContext(
                    booleanBuilder,
                    generator.getGenericInformationAliases(),
                    generator.getVariableAliases());
        } catch (Exception e) {
            throw new QueryPredicateBuilderException(e.getMessage());
        }
    }

    protected void setQueryCompiler(WorkflowCatalogQueryCompiler queryCompiler) {
        this.queryCompiler = queryCompiler;
    }

    protected void setWcqlVisitor(WorkflowCatalogQueryLanguageVisitor wcqlVisitor) {
        this.wcqlVisitor = wcqlVisitor;
    }

}
