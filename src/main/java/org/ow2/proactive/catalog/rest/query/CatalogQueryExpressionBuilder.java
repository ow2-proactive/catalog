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
package org.ow2.proactive.catalog.rest.query;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ow2.proactive.catalog.rest.entity.QCatalogObjectRevision;
import org.ow2.proactive.catalog.rest.query.parser.CatalogQueryLanguageParser;

import com.mysema.query.jpa.JPASubQuery;


/**
 * Create the BooleanExpression that will be used to generate the final QueryDSL query.
 *
 * @author ActiveEon Team
 */
public class CatalogQueryExpressionBuilder {

    private final String catalogQuery;

    protected CatalogQueryCompiler queryCompiler;

    protected CatalogQueryLanguageListener queryListener;

    private ParseTreeWalker walker;

    public CatalogQueryExpressionBuilder(String catalogQuery) {
        this(catalogQuery, new CatalogQueryCompiler(), new CatalogQueryLanguageListener(), new ParseTreeWalker());
    }

    protected CatalogQueryExpressionBuilder(String catalogQuery, CatalogQueryCompiler queryCompiler,
            CatalogQueryLanguageListener queryListener, ParseTreeWalker walker) {
        this.queryCompiler = queryCompiler;
        this.queryListener = queryListener;
        this.catalogQuery = catalogQuery;
        this.walker = walker;
    }

    public QueryExpressionContext build() throws QueryExpressionBuilderException {
        // empty query must throw no exception and apply no filter
        if (catalogQuery.trim().isEmpty()) {
            return new QueryExpressionContext(QCatalogObjectRevision.catalogObjectRevision.in(new JPASubQuery().from(QCatalogObjectRevision.catalogObjectRevision)
                                                                                                               .list(QCatalogObjectRevision.catalogObjectRevision)));
        }

        try {
            CatalogQueryLanguageParser.StartContext context = queryCompiler.compile(catalogQuery);
            walker.walk(queryListener, context);
            return new QueryExpressionContext(queryListener.getBooleanExpression());
        } catch (Exception e) {
            throw new QueryExpressionBuilderException(e.getMessage());
        }
    }

}
