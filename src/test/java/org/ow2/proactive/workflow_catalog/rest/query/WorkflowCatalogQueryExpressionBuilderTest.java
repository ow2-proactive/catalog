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

import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Create the BooleanExpression that will be used to generate the final QueryDSL query.
 *
 * @author ActiveEon Team
 */
public class WorkflowCatalogQueryExpressionBuilderTest {

    @Mock
    private WorkflowCatalogQueryCompiler queryCompiler;

    @Mock
    protected WorkflowCatalogQueryLanguageListener queryListener;

    @Mock
    protected ParseTreeWalker walker;

    private WorkflowCatalogQueryExpressionBuilder expressionBuilder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        WorkflowCatalogQueryLanguageParser.StartContext context =
                mock(WorkflowCatalogQueryLanguageParser.StartContext.class);
        when(queryCompiler.compile(anyString())).thenReturn(context);
    }

    @Test
    public void testBuildWithEmptyQuery() throws Exception {
        expressionBuilder = new WorkflowCatalogQueryExpressionBuilder("", queryCompiler,
                queryListener, walker);
        assertNotNull(expressionBuilder.build());
    }

    @Test
    public void testBuildWithQuery() throws Exception {
        expressionBuilder = new WorkflowCatalogQueryExpressionBuilder("name=\"NonExistingWorkflow\"",
                queryCompiler, queryListener, walker);
        QueryExpressionContext context = expressionBuilder.build();
        verify(queryCompiler, times(1)).compile(anyString());
        verify(walker, times(1)).walk(any(ParseTreeListener.class), any(ParseTree.class));
        assertNotNull(context);
    }

    @Test(expected = QueryExpressionBuilderException.class)
    public void testBuildWithQueryException() throws Exception {
        expressionBuilder = new WorkflowCatalogQueryExpressionBuilder("invalidAttr=\"InvalidValue\"");
        expressionBuilder.build();
    }
}