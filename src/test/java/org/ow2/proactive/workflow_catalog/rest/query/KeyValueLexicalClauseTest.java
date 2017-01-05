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
import static org.ow2.proactive.workflow_catalog.rest.query.KeyValueLexicalClause.PairType.GENERIC_INFORMATION;
import static org.ow2.proactive.workflow_catalog.rest.query.KeyValueLexicalClause.PairType.VARIABLE;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;


/**
 * Unit tests associated to {@link KeyValueLexicalClause}.
 *
 * @author ActiveEon Team
 */
public class KeyValueLexicalClauseTest {

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(KeyValueLexicalClause.class).verify();
    }

    @Test
    public void testGetType1() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(GENERIC_INFORMATION, false, false);

        assertThat(clause.getType()).isEqualTo(GENERIC_INFORMATION);
    }

    @Test
    public void testGetType2() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(VARIABLE, false, false);

        assertThat(clause.getType()).isEqualTo(VARIABLE);
    }

    @Test
    public void testKeyHasWildcards1() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(VARIABLE, false, true);

        assertThat(clause.keyHasWildcards()).isFalse();
    }

    @Test
    public void testKeyHasWildcards2() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(VARIABLE, true, false);

        assertThat(clause.keyHasWildcards()).isTrue();
    }

    @Test
    public void testValueHasWildcards1() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(VARIABLE, true, false);

        assertThat(clause.valueHasWildcards()).isFalse();
    }

    @Test
    public void testValueHasWildcards2() {
        KeyValueLexicalClause clause = new KeyValueLexicalClause(VARIABLE, false, true);

        assertThat(clause.valueHasWildcards()).isTrue();
    }

}
