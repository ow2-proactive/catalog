///*
// *  *
// * ProActive Parallel Suite(TM): The Java(TM) library for
// *    Parallel, Distributed, Multi-Core Computing for
// *    Enterprise Grids & Clouds
// *
// * Copyright (C) 1997-2015 INRIA/University of
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
// *  Initial developer(s):               The ProActive Team
// *                        http://proactive.inria.fr/team_members.htm
// *  Contributor(s):
// *
// *  * $$ACTIVEEON_INITIAL_DEV$$
// */
//package org.ow2.proactive.workflow_catalog.rest.query;
//
//import org.ow2.proactive.workflow_catalog.rest.Application;
//import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.WebIntegrationTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
//
//@ActiveProfiles("test")
//@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = { Application.class })
//@WebIntegrationTest
//public class WorkflowCatalogQueryLanguageVisitorTest2 {
//
//    @Test
//    public void test() throws SyntaxException {
////        String query1 = "(generic_information.name = \"I\" OR generic_information.name=\"C\") AND generic_information.value=\"E\"";
//        String query2 = "generic_information.name = \"I\" AND generic_information.name=\"C\" OR variable.name=\"A\" AND generic_information.value=\"B\"";
//        String query3 = "generic_information.name = \"A\" AND generic_information.name=\"B\" " +
//                "AND generic_information.value=\"C\" OR variable.name=\"D\" AND generic_information.value=\"E\"";
//        String query4 = "(generic_information.name = \"A\" OR generic_information.name=\"B\") AND generic_information.name=\"C\"";
//
//        WorkflowCatalogQueryCompiler compiler = new WorkflowCatalogQueryCompiler();
//        final WorkflowCatalogQueryLanguageParser.ExpressionContext compile = compiler.compile(query4);
//
//        System.out.println("WorkflowCatalogQueryLanguageVisitorTest2.test VISIT");
//
////        WorkflowCatalogQueryLanguageVisitor visitor = new WorkflowCatalogQueryLanguageVisitor();
////        final BooleanExpression booleanBuilder = visitor.visitExpression(compile);
//
//
//
//    }
//
//
//}
