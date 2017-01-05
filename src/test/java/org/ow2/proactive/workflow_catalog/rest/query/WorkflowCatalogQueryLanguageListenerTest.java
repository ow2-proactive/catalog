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
package org.ow2.proactive.workflow_catalog.rest.query;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;


/**
 * Unit and functional tests associated to {@link WorkflowCatalogQueryLanguageListener}.
 *
 * @author ActiveEon Team
 * @see WorkflowCatalogQueryCompiler
 */
public class WorkflowCatalogQueryLanguageListenerTest {

    private WorkflowCatalogQueryLanguageListener listener;

    @Before
    public void setUp() {
        listener = new WorkflowCatalogQueryLanguageListener();
    }

    @Test
    public void testInitialization() {
        assertThat(listener.atomicClausesToFuncMap).isNotEmpty();
        assertThat(listener.keyValueClausesToFuncMap).isNotEmpty();

        assertThat(listener.stackOfClauses).isEmpty();
        assertThat(listener.stackOfContexts).isEmpty();
        assertThat(listener.stackOfSubQueries).isEmpty();

        assertThat(listener.booleanExpression).isNull();
    }

    /**
     * Walk on a Tree representation of a valid WCQL query
     */
    @Test
    public void testWalkOnValidQuery() throws SyntaxException {
        WorkflowCatalogQueryCompiler queryCompiler = new WorkflowCatalogQueryCompiler();

        String query = "generic_information(\"Infrastructure\", \"Amazon EC2\") " +
                       "AND generic_information(\"Type\", \"Public\") " +
                       "AND variable(\"CPU\", \"*\") OR generic_information(\"Cloud\", \"Amazon EC2\") " +
                       "AND variable(\"CPU\", \"*\") OR name=\"Amazon\"";

        WorkflowCatalogQueryLanguageParser.StartContext context = queryCompiler.compile(query);

        ParseTreeWalker walker = new ParseTreeWalker();

        listener = Mockito.spy(listener);

        walker.walk(listener, context);

        verify(listener).enterStart(Mockito.any());
        verify(listener, times(3)).enterAndExpression(Mockito.any());
        verify(listener, times(2)).enterOrExpression(Mockito.any());
        verify(listener, times(1)).enterAtomicClause(Mockito.any());
        verify(listener, times(5)).enterKeyValueClause(Mockito.any());

        verify(listener).exitStart(Mockito.any());
        verify(listener, times(3)).exitAndExpression(Mockito.any());
        verify(listener, times(2)).exitOrExpression(Mockito.any());
        verify(listener, times(1)).exitAtomicClause(Mockito.any());
        verify(listener, times(5)).exitKeyValueClause(Mockito.any());

        assertThat(listener.stackOfClauses).isEmpty();
        assertThat(listener.stackOfContexts).isEmpty();
        assertThat(listener.stackOfSubQueries).isEmpty();

        assertThat(listener.booleanExpression).isNotNull();
    }

    @Test
    public void testSanitizeLiteral() {
        assertEquals("CPU", listener.sanitizeLiteral("\"CPU\""));
        assertEquals("%PU", listener.sanitizeLiteral("\"*PU\""));
        assertEquals("CP%", listener.sanitizeLiteral("\"CP*\""));
        assertEquals("%P%", listener.sanitizeLiteral("\"*P*\""));
        assertEquals("CPU*", listener.sanitizeLiteral("\"CPU\\\\*\""));
        assertEquals("CPU\\\\%", listener.sanitizeLiteral("\"CPU%\""));
    }

}
