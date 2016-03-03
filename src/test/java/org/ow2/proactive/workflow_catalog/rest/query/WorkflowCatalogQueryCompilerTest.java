/*
 *  ProActive Parallel Suite(TM): The Java(TM) library for
 *     Parallel, Distributed, Multi-Core Computing for
 *     Enterprise Grids & Clouds
 *
 *  Copyright (C) 1997-2016 INRIA/University of
 *                  Nice-Sophia Antipolis/ActiveEon
 *  Contact: proactive@ow2.org or contact@activeeon.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; version 3 of
 *  the License.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *  USA
 *
 *  If needed, contact us to obtain a release under GPL Version 2 or 3
 *  or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                          http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.query;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Unit tests associated to {@link WorkflowCatalogQueryCompiler}.
 * <p>
 * Statically define some queries (written in WCQL) that should
 * be syntactically valid or invalid.
 *
 * @author ActiveEon Team
 * @see WorkflowCatalogQueryCompiler
 */
@RunWith(Parameterized.class)
public class WorkflowCatalogQueryCompilerTest {

    private final WorkflowCatalogQueryCompiler compiler;

    private final Assertion assertion;

    @Parameterized.Parameters(name = "testQuery{index}")
    public static Collection<Assertion> queriesUnderTest() {
        return Arrays.asList(new Assertion[] {

                // Queries that are syntactically correct

                validTestInput("name=\"\""),
                validTestInput("name=\"value\""),
                validTestInput("name=\"I love you\""),
                validTestInput("name=\"Escape \\\" character\""),
                validTestInput("name=\"Escape \\* character\""),
                validTestInput("name=\"Escape \\\\ character\""),
                validTestInput("name != \"value\""),
                validTestInput(" \t name  \r \n = \"\"  "),
                validTestInput("name=\"*suffix\""),
                validTestInput("name=\"prefix*\""),
                validTestInput("name=\"*contains*\""),

                validTestInput("project_name=\"\""),
                validTestInput("project_name=\"value\""),
                validTestInput("project_name=\"I love you\""),
                validTestInput("project_name=\"Escape \\\" character\""),
                validTestInput("project_name=\"Escape \\* character\""),
                validTestInput("project_name=\"Escape \\\\ character\""),
                validTestInput("project_name != \"value\""),
                validTestInput(" \t project_name  \r \n = \"\"  "),
                validTestInput("project_name=\"*suffix\""),
                validTestInput("project_name=\"prefix*\""),
                validTestInput("project_name=\"*contains*\""),

                validTestInput("generic_information(\"\", \"\")"),
                validTestInput("generic_information(\"name\", \"\")"),
                validTestInput("generic_information(\"\", \"value\")"),
                validTestInput("generic_information(\"name\", \"value\")"),
                validTestInput("generic_information(\"name\", \"*\")"),
                validTestInput("generic_information(\"*\", \"value\")"),
                validTestInput("generic_information(\"prefix*\", \"value\")"),
                validTestInput("generic_information(\"*suffix\", \"value\")"),
                validTestInput("generic_information(\"*contains*\", \"value\")"),
                validTestInput("generic_information(\"name\", \"prefix*\")"),
                validTestInput("generic_information(\"name\", \"*suffix\")"),
                validTestInput("generic_information(\"name\", \"*contains*\")"),
                validTestInput("generic_information(\"My Long Name\", \"value\")"),
                validTestInput("generic_information(\"name\", \"My Long Value\")"),
                validTestInput("generic_information(\"My Long Name\", \"My Long Value\")"),

                validTestInput("variable(\"\", \"\")"),
                validTestInput("variable(\"name\", \"\")"),
                validTestInput("variable(\"\", \"value\")"),
                validTestInput("variable(\"name\", \"value\")"),
                validTestInput("variable(\"name\", \"*\")"),
                validTestInput("variable(\"*\", \"value\")"),
                validTestInput("variable(\"prefix*\", \"value\")"),
                validTestInput("variable(\"*suffix\", \"value\")"),
                validTestInput("variable(\"*contains*\", \"value\")"),
                validTestInput("variable(\"name\", \"prefix*\")"),
                validTestInput("variable(\"name\", \"*suffix\")"),
                validTestInput("variable(\"name\", \"*contains*\")"),
                validTestInput("variable(\"My Long Name\", \"value\")"),
                validTestInput("variable(\"name\", \"My Long Value\")"),
                validTestInput("variable(\"My Long Name\", \"My Long Value\")"),

                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\") " +
                                "AND generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\") " +
                                "OR generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\") " +
                                "AND generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\") " +
                                "OR generic_information(\"name3\", \"value3\")"),

                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\") " +
                                "AND variable(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\") " +
                                "OR variable(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\") " +
                                "AND variable(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\") " +
                                "OR variable(\"name3\", \"value3\")"),

                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\") " +
                                "AND generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\") " +
                                "AND variable(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\") " +
                                "OR generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\") " +
                                "OR variable(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "OR variable(\"name2\", \"value2\") " +
                                "AND generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "OR generic_information(\"name2\", \"value2\") " +
                                "AND variable(\"name3\", \"value3\")"),
                validTestInput(
                        "generic_information(\"name1\", \"value1\") " +
                                "AND variable(\"name2\", \"value2\") " +
                                "OR generic_information(\"name3\", \"value3\")"),
                validTestInput(
                        "variable(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\") " +
                                "OR variable(\"name3\", \"value3\")"),
                validTestInput(
                        "name=\"name1\" AND variable(\"name1\", \"value1\") " +
                                "AND generic_information(\"name2\", \"value2\") " +
                                "OR variable(\"name3\", \"value3\")"),

                // Queries that are syntactically incorrect

                invalidTestInput(""),

                invalidTestInput("name=\'value\'"),
                invalidTestInput("name==\"value\""),
                invalidTestInput("name<>\"value\""),
                invalidTestInput("name=value"),
                invalidTestInput("name=(value)"),
                invalidTestInput("name(value)"),

                invalidTestInput("project_name=\'value\'"),
                invalidTestInput("project_name==\"value\""),
                invalidTestInput("project_name<>\"value\""),
                invalidTestInput("project_name=value"),
                invalidTestInput("project_name=(value)"),
                invalidTestInput("project_name(value)"),

                invalidTestInput("generic_information.name=\"name\""),
                invalidTestInput("generic_information.value=\"value\""),
                invalidTestInput("generic_information(name, value)"),
                invalidTestInput("generic_information(\'name\', \'value\')"),
                invalidTestInput("generic_information=(\"name\", \"value\")"),
                invalidTestInput("generic_information(\"name\"=\"value\")"),
                invalidTestInput("generic_information<>(\"name\", \"value\")"),

                invalidTestInput("variable.name=\"name\""),
                invalidTestInput("variable.value=\"value\""),
                invalidTestInput("variable(name, value)"),
                invalidTestInput("variable(\'name\', \'value\')"),
                invalidTestInput("variable=(\"name\", \"value\")"),
                invalidTestInput("variable(\"name\"=\"value\")"),
                invalidTestInput("variable<>(\"name\", \"value\")"),

                invalidTestInput("name!=\"name1\" and variable(\"name2\", \"value\")"),
                invalidTestInput("generic_information(\"name1\", \"value\") or project_name!=\"name2\""),
        });
    }

    public WorkflowCatalogQueryCompilerTest(Assertion assertion) {
        this.compiler = new WorkflowCatalogQueryCompiler();
        this.assertion = assertion;
    }

    @Test
    public void testQuery() {
        try {
            compiler.compile(assertion.getQuery());

            if (!assertion.shouldPass) {
                Assert.fail(getMessageDump(
                        assertion, "Query is detected as syntactically correct but it should not"));
            }
        } catch (SyntaxException e) {
            if (assertion.shouldPass) {
                Assert.fail(getMessageDump(
                        assertion, e, "Query is detected as syntactically incorrect but it should not"));
            }
        }
    }

    private String getMessageDump(Assertion assertion, SyntaxException syntaxException, String firstLine) {
        String message = getMessageDump(assertion, firstLine);
        message += "    Error: " + syntaxException.getMessage() + "\n";

        return message;
    }

    private String getMessageDump(Assertion assertion, String firstLine) {
        String message = firstLine + ": \n";
        message += "    Query: " + assertion.getQuery() + "\n";

        return message;
    }

    public static Assertion invalidTestInput(String query) {
        return new Assertion(query, false);
    }

    public static Assertion validTestInput(String query) {
        return new Assertion(query, true);
    }

    private static final class Assertion {

        private final String query;

        private final boolean shouldPass;

        public Assertion(String query, boolean shouldPass) {
            this.query = query;
            this.shouldPass = shouldPass;
        }

        public String getQuery() {
            return query;
        }

        public boolean shouldPass() {
            return shouldPass;
        }

    }

}
