///*
// * ProActive Parallel Suite(TM): The Java(TM) library for
// *    Parallel, Distributed, Multi-Core Computing for
// *    Enterprise Grids & Clouds
// *
// * Copyright (C) 1997-2016 INRIA/University of
// *                 Nice-Sophia Antipolis/ActiveEon
// * Contact: proactive@ow2.org or contact@activeeon.com
// *
// * This library is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Affero General Public License
// * as published by the Free Software Foundation; version 3 of
// * the License.
// *
// * This library is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// * Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with this library; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
// * USA
// *
// * If needed, contact us to obtain a release under GPL Version 2 or 3
// * or a different license than the AGPL.
// *
// * Initial developer(s):               The ProActive Team
// *                         http://proactive.inria.fr/team_members.htm
// */
//package org.ow2.proactive.workflow_catalog.rest.query;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.Spy;
//import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
//
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
///**
// * Create the BooleanBuilder that will be used to generate the WCQL query
// *
// * @author ActiveEon Team
// */
//public class WorkflowCatalogQueryPredicateBuilderTest {
//
//    @Spy
//    private WorkflowCatalogQueryCompiler workflowCatalogQueryCompiler;
//
//    @Spy
//    private WorkflowCatalogQueryLanguageVisitor queryListener;
//
//    @Before
//    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    public void testBuild() throws Exception {
//        String wcqlQuery = "variable.name=\"toto\"";
//
//        WorkflowCatalogQueryExpressionBuilder queryBuilder = new WorkflowCatalogQueryExpressionBuilder(wcqlQuery);
//        queryBuilder.setQueryCompiler(workflowCatalogQueryCompiler);
//        queryBuilder.setWcqlVisitor(queryListener);
//        queryBuilder.build();
//
//        verify(workflowCatalogQueryCompiler, times(1)).compile(wcqlQuery);
//        verify(queryListener, times(1)).visitExpression(any(WorkflowCatalogQueryLanguageParser.ExpressionContext.class));
//    }
//
//}