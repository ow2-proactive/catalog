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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author ActiveEon Team
 */
@RunWith(Parameterized.class)
public class WorkflowCatalogQueryCompilerTest {

    private final WorkflowCatalogQueryCompiler compiler;

    private final TestInput testInput;

    @Parameterized.Parameters(name = "testQuery{index}")
    public static Collection<TestInput> queriesUnderTest() {
        return Arrays.asList(new TestInput[]{

                // Queries that are syntactically correct

                createValidTestInput("variable.name=\"name\""),
                createValidTestInput("variable.name= \"name\""),
                createValidTestInput("variable.name =\"name\""),
                createValidTestInput("variable.name = \"name\""),
                createValidTestInput("variable.name != \"name\""),
                createValidTestInput("variable.name != \"name with \\\" inside\""),
                createValidTestInput("variable.name = \" abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ\""),
                createValidTestInput("generic_information.name=\"name\""),
                createValidTestInput("generic_information.value=\"value\""),
                createValidTestInput("generic_information.name = \"name\" AND generic_information.value=\"value\""),
                createValidTestInput("generic_information.name = \"name\" && generic_information.value=\"value\""),
                createValidTestInput("generic_information.name = \"name\" || generic_information.value=\"value\""),
                createValidTestInput("generic_information.name = \"name\" AND generic_information.name=\"name\""),
                createValidTestInput("generic_information.name = \"name1\" OR generic_information.name=\"name2\""),
                createValidTestInput("(generic_information.name = \"name1\" OR generic_information.name=\"name2\")"),
                createValidTestInput("(generic_information.name = \"name1\") OR (generic_information.name=\"name2\")"),
                createValidTestInput("((generic_information.name = \"name1\") OR (generic_information.name=\"name2\"))"),
                createValidTestInput("(((generic_information.name = \"name1\") OR (generic_information.name=\"name2\")))"),

                createValidTestInput("generic_information.value = \"value1\" " +
                        "AND generic_information.value=\"value2\"" +
                        "OR generic_information.value=\"value3\"" +
                        "AND generic_information.value=\"value4\"" +
                        "OR generic_information.value=\"value5\"" +
                        "AND generic_information.value=\"value6\"" +
                        "OR generic_information.value=\"value7\"" +
                        "AND generic_information.value=\"value8\"" +
                        "OR generic_information.value=\"value9\""),

                createValidTestInput("very_long_attribute_name.with.underscore.and.points=\"name\""),

                // Parentheses have been introduced in the language mainly for the following case
                // where precedence should be given to OR operator whereas AND usually has precedence over OR
                createValidTestInput("(generic_information.name = \"name1\" OR generic_information.name=\"name2\") AND variable.name=\"name3\""),
                createValidTestInput("((generic_information.name = \"name1\" OR generic_information.name=\"name2\") AND variable.name=\"name3\")"),
                createValidTestInput("((generic_information.name = \"name1\" OR generic_information.name=\"name2\") AND variable.name=\"name3\")"),

                // Queries that must raise an exception due to syntax error

                createInvalidTestInput("_invalidAttribute = \"value\""),
                createInvalidTestInput("0invalidAttribute = \"value\""),
                createInvalidTestInput("generic_information.value <> \"value\""),
                createInvalidTestInput("generic_information.value == \"value\""),
                createInvalidTestInput("generic_information.value == \"value\""),
                createInvalidTestInput("generic_information.value = \"value\" AND"),
                createInvalidTestInput("generic_information.value = \"value\" OR"),
                createInvalidTestInput("generic_information.name = \"name\" | generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" & generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" AND AND generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" OR OR generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" AND OR AND generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" OR AND OR generic_information.value = \"value\""),
                createInvalidTestInput("(generic_information.name = \"name\" OR generic_information.value = \"value\""),
                createInvalidTestInput("(generic_information.name = \"name\" OR ) generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" ( OR ) generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" ( OR generic_information.value = \"value\" )"),
                createInvalidTestInput("( generic_information.name ) = \"name\" OR generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = ( \"name\" ) OR generic_information.value = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" OR ( generic_information.value ) = \"value\""),
                createInvalidTestInput("generic_information.name = \"name\" OR generic_information.value = ( \"value\" ) "),

        });
    }

    public WorkflowCatalogQueryCompilerTest(TestInput testInput) {
        this.compiler = new WorkflowCatalogQueryCompiler();
        this.testInput = testInput;
    }

    @Test
    public void testQuery() {
        try {
            compiler.compile(testInput.getQuery());

            if (!testInput.shouldPass) {
                Assert.fail(getMessageDump(
                        testInput, "Query is detected as syntactically correct but it should not"));
            }
        } catch (SyntaxException e) {
            if (testInput.shouldPass) {
                Assert.fail(getMessageDump(
                        testInput, e, "Query is detected as syntactically incorrect but it should not"));
            }
        }
    }

    private String getMessageDump(TestInput testInput, SyntaxException syntaxException, String firstLine) {
        String message = getMessageDump(testInput, firstLine);
        message += "    Error: " + syntaxException.getMessage() + "\n";

        return message;
    }

    private String getMessageDump(TestInput testInput, String firstLine) {
        String message = firstLine + ": \n";
        message += "    Query: " + testInput.getQuery() + "\n";

        return message;
    }

    public static TestInput createValidTestInput(String query) {
        return new TestInput(query, true);
    }

    public static TestInput createInvalidTestInput(String query) {
        return new TestInput(query, false);
    }

    private static final class TestInput {

        private final String query;

        private final boolean shouldPass;

        public TestInput(String query, boolean shouldPass) {
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
