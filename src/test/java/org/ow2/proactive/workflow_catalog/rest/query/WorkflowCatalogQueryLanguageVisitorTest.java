///*
// *  ProActive Parallel Suite(TM): The Java(TM) library for
// *     Parallel, Distributed, Multi-Core Computing for
// *     Enterprise Grids & Clouds
// *
// *  Copyright (C) 1997-2016 INRIA/University of
// *                  Nice-Sophia Antipolis/ActiveEon
// *  Contact: proactive@ow2.org or contact@activeeon.com
// *
// *  This library is free software; you can redistribute it and/or
// *  modify it under the terms of the GNU Affero General Public License
// *  as published by the Free Software Foundation; version 3 of
// *  the License.
// *
// *  This library is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// *  Affero General Public License for more details.
// *
// *  You should have received a copy of the GNU Affero General Public License
// *  along with this library; if not, write to the Free Software
// *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
// *  USA
// *
// *  If needed, contact us to obtain a release under GPL Version 2 or 3
// *  or a different license than the AGPL.
// *
// *  Initial developer(s):               The ProActive Team
// *                          http://proactive.inria.fr/team_members.htm
// */
//package org.ow2.proactive.workflow_catalog.rest.query;
//
//import com.google.common.collect.ImmutableList;
//import org.antlr.v4.runtime.tree.TerminalNode;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
//import java.util.List;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
//
///**
// * @author ActiveEon Team
// */
//public class WorkflowCatalogQueryLanguageVisitorTest {
//
//    private WorkflowCatalogQueryLanguageVisitor queryLanguageVisitor;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.ExpressionContext ctx;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.And_expressionContext mockedAnd;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.Or_expressionContext mockedOr;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.FinalClauseContext mockedFinalClause;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.ParenthesedClauseContext mockedParenthesedClause;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.And_expressionContext mockedParAnd;
//
//    @Mock
//    WorkflowCatalogQueryLanguageParser.Or_expressionContext mockedParOr;
//
//    @Mock
//    TerminalNode mockedAttributeLiteral;
//
//    @Mock
//    TerminalNode mockedStringLiteral;
//
//    @Mock
//    TerminalNode mockedOperation;
//
//    private List<WorkflowCatalogQueryLanguageParser.And_expressionContext> lAndExpressions;
//    private List<WorkflowCatalogQueryLanguageParser.ClauseContext> lClause;
//    private static final String attributeLiteralString = "variable.name";
//    private static final String stringLiteralString = "\"toto\"";
//
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//        lAndExpressions = ImmutableList.of(mockedAnd);
//        lClause = ImmutableList.of(mockedFinalClause, mockedParenthesedClause);
//        initMocks();
//        queryLanguageVisitor = new WorkflowCatalogQueryLanguageVisitor();
//    }
//
//    @Test
//    public void testVisitExpression() throws Exception {
//        queryLanguageVisitor.visitExpression(ctx);
//        verify(ctx, times(1)).or_expression();
//    }
//
//    @Test
//    public void testVisitAnd_expression() throws Exception {
//        queryLanguageVisitor.visitAnd_expression(mockedAnd);
//        verify(mockedAnd, times(1)).clause();
//    }
//
//    @Test
//    public void testVisitOr_expression() throws Exception {
//        queryLanguageVisitor.visitOr_expression(mockedOr);
//        verify(mockedOr, times(1)).and_expression();
//    }
//
//    @Test
//    public void testVisitFinalClause() throws Exception {
//        queryLanguageVisitor.visitFinalClause(mockedFinalClause);
//        verify(mockedFinalClause, times(1)).AttributeLiteral();
//        verify(mockedAttributeLiteral, times(1)).getText();
//        verify(mockedStringLiteral, times(1)).getText();
//        verify(mockedOperation, times(1)).getText();
//    }
//
//    @Test
//    public void testVisitParenthesedClause() throws Exception {
//        queryLanguageVisitor.visitParenthesedClause(mockedParenthesedClause);
//        verify(mockedParenthesedClause, times(1)).or_expression();
//    }
//
//    @Test
//    public void testGetTableVariable() throws Exception {
//        assertEquals(ClauseKey.TABLE.VARIABLE, queryLanguageVisitor.getFieldType("variable.name"));
//        assertEquals(ClauseKey.TABLE.VARIABLE, queryLanguageVisitor.getFieldType("VARIABLE.NAME"));
//    }
//
//    @Test
//    public void testGetTableGI() throws Exception {
//        assertEquals(ClauseKey.TABLE.GENERIC_INFORMATION, queryLanguageVisitor.getFieldType("generic_information.name"));
//        assertEquals(ClauseKey.TABLE.GENERIC_INFORMATION, queryLanguageVisitor.getFieldType("GENERIC_INFORMATION.NAME"));
//    }
//
//    @Test
//    public void testGetTableName() throws Exception {
//        assertEquals(ClauseKey.TABLE.NAME, queryLanguageVisitor.getFieldType("name"));
//        assertEquals(ClauseKey.TABLE.NAME, queryLanguageVisitor.getFieldType("NAME"));
//    }
//
//    @Test
//    public void testGetTableProjectName() throws Exception {
//        assertEquals(ClauseKey.TABLE.PROJECT_NAME, queryLanguageVisitor.getFieldType("project_name"));
//        assertEquals(ClauseKey.TABLE.PROJECT_NAME, queryLanguageVisitor.getFieldType("PROJECT_NAME"));
//    }
//
//    @Test
//    public void testGetOperation() throws Exception {
//        assertEquals(ClauseKey.OPERATION.EQUAL, queryLanguageVisitor.getOperator("="));
//        assertEquals(ClauseKey.OPERATION.NOT_EQUAL, queryLanguageVisitor.getOperator("!="));
//    }
//
//    @Test
//    public void testGetClauseType() throws Exception {
//        assertEquals(ClauseKey.CLAUSE_TYPE.KEY, queryLanguageVisitor.getClauseType("variable.name"));
//        assertEquals(ClauseKey.CLAUSE_TYPE.KEY, queryLanguageVisitor.getClauseType("VARIABLE.NAME"));
//        assertEquals(ClauseKey.CLAUSE_TYPE.VALUE, queryLanguageVisitor.getClauseType("variable.value"));
//        assertEquals(ClauseKey.CLAUSE_TYPE.VALUE, queryLanguageVisitor.getClauseType("VARIABLE.VALUE"));
//    }
//
//    private void initMocks() {
//        // for the regular case
//        when(ctx.or_expression()).thenReturn(mockedOr);
//        when(mockedOr.and_expression()).thenReturn(lAndExpressions);
//        when(mockedAnd.clause()).thenReturn(lClause);
//        when(mockedFinalClause.AttributeLiteral()).thenReturn(mockedAttributeLiteral);
//        when(mockedFinalClause.StringLiteral()).thenReturn(mockedStringLiteral);
//        when(mockedFinalClause.COMPARE_OPERATOR()).thenReturn(mockedOperation);
//        when(mockedAttributeLiteral.getText()).thenReturn(attributeLiteralString);
//        when(mockedStringLiteral.getText()).thenReturn(stringLiteralString);
//        when(mockedOperation.getText()).thenReturn("=");
//
//        // for the recursive case
//        List<WorkflowCatalogQueryLanguageParser.And_expressionContext> lParAndExpressions;
//        List<WorkflowCatalogQueryLanguageParser.ClauseContext> lParClause;
//        lParAndExpressions = ImmutableList.of(mockedParAnd);
//        lParClause = ImmutableList.of(mockedFinalClause);
//        when(mockedParenthesedClause.or_expression()).thenReturn(mockedParOr);
//        when(mockedParOr.and_expression()).thenReturn(lParAndExpressions);
//        when(mockedParAnd.clause()).thenReturn(lParClause);
//    }
//}