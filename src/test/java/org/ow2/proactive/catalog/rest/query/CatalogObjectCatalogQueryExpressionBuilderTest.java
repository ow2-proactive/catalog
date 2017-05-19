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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.query.CatalogQueryCompiler;
import org.ow2.proactive.catalog.rest.query.CatalogQueryExpressionBuilder;
import org.ow2.proactive.catalog.rest.query.CatalogQueryLanguageListener;
import org.ow2.proactive.catalog.rest.query.QueryExpressionBuilderException;
import org.ow2.proactive.catalog.rest.query.QueryExpressionContext;
import org.ow2.proactive.catalog.rest.query.parser.CatalogQueryLanguageParser;


/**
 * Create the BooleanExpression that will be used to generate the final QueryDSL query.
 *
 * @author ActiveEon Team
 */
public class CatalogObjectCatalogQueryExpressionBuilderTest {

    @Mock
    private CatalogQueryCompiler queryCompiler;

    @Mock
    protected CatalogQueryLanguageListener queryListener;

    @Mock
    protected ParseTreeWalker walker;

    private CatalogQueryExpressionBuilder expressionBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        CatalogQueryLanguageParser.StartContext context = mock(CatalogQueryLanguageParser.StartContext.class);
        when(queryCompiler.compile(anyString())).thenReturn(context);
    }

    @Test
    public void testBuildWithEmptyQuery() throws Exception {
        expressionBuilder = new CatalogQueryExpressionBuilder("", queryCompiler, queryListener, walker);
        assertNotNull(expressionBuilder.build());
    }

    @Test
    public void testBuildWithQuery() throws Exception {
        expressionBuilder = new CatalogQueryExpressionBuilder("name=\"NonExistingWorkflow\"",
                                                              queryCompiler,
                                                              queryListener,
                                                              walker);
        QueryExpressionContext context = expressionBuilder.build();
        verify(queryCompiler, times(1)).compile(anyString());
        verify(walker, times(1)).walk(any(ParseTreeListener.class), any(ParseTree.class));
        assertNotNull(context);
    }

    @Test(expected = QueryExpressionBuilderException.class)
    public void testBuildWithQueryException() throws Exception {
        expressionBuilder = new CatalogQueryExpressionBuilder("invalidAttr=\"InvalidValue\"");
        expressionBuilder.build();
    }
}
