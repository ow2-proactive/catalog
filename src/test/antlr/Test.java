/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.ow2.proactive.workflow_catalog.rest.query.WorkflowCatalogQueryLanguageLexer;
import org.ow2.proactive.workflow_catalog.rest.query.WorkflowCatalogQueryLanguageListener;
import org.ow2.proactive.workflow_catalog.rest.query.WorkflowCatalogQueryLanguageParser;

public class Test {

    public static void main(String[] args) {
        // TODO: it seems there are some specific tools to write antlr tests
        // below is just an example for early testing

//        String input = "genericInformation.key=\"28 Rue de la chance\"";
//        String input = "TeSt=\"12\" AND generic_information.key=\"1\" OR project_name=\"1\"";
//        String input = "TeSt=\"12\" AND (generic_information.key=\"1\" OR project_name=\"1\")";

        // TODO next input is not handled as expected
        String input = "(genericInformation.key=\"28 Rue de la chance\" AND genericInformation.key=\"28 Rue de la chance\")";

        CharStream cs = new ANTLRInputStream(input);

        WorkflowCatalogQueryLanguageLexer lexer = new WorkflowCatalogQueryLanguageLexer(cs);

        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);

        WorkflowCatalogQueryLanguageParser parser = new WorkflowCatalogQueryLanguageParser(commonTokenStream);
        ParseTree tree = parser.statement();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new CustomWalker(), tree);
    }

    private static class CustomWalker implements WorkflowCatalogQueryLanguageListener {

        @Override
        public void enterClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx) {
            System.out.println("CustomWalker.enterClause");
        }

        @Override
        public void exitClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx) {
            System.out.println("CustomWalker.exitClause");
        }

        @Override
        public void enterClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx) {
            System.out.println("CustomWalker.enterClauses");
        }

        @Override
        public void exitClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx) {
            System.out.println("CustomWalker.exitClauses");
        }

        @Override
        public void enterStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx) {
            System.out.println("CustomWalker.enterStatement");
        }

        @Override
        public void exitStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx) {
            System.out.println("CustomWalker.exitStatement");
        }

        @Override
        public void visitTerminal(TerminalNode terminalNode) {
            System.out.println("CustomWalker.visitTerminal " + terminalNode);

        }

        @Override
        public void visitErrorNode(ErrorNode errorNode) {

        }

        @Override
        public void enterEveryRule(ParserRuleContext parserRuleContext) {

        }

        @Override
        public void exitEveryRule(ParserRuleContext parserRuleContext) {

        }
    }

}
