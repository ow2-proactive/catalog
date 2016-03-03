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

import org.ow2.proactive.workflow_catalog.rest.query.AtomicLexicalClause.FieldType;
import org.ow2.proactive.workflow_catalog.rest.query.AtomicLexicalClause.Operator;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Unit tests associated to {@link AtomicLexicalClause}.
 *
 * @author ActiveEon Team
 */
public class AtomicLexicalClauseTest {

    @Test
    public void testEqualsContract() {
        EqualsVerifier.forClass(AtomicLexicalClause.class).verify();
    }

    @Test
    public void testGetOperator1() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, false);

        assertThat(atomicLexicalClause.getOperator()).isEqualTo(Operator.EQUAL);
    }

    @Test
    public void testGetOperator2() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.NAME, Operator.NOT_EQUAL, false);

        assertThat(atomicLexicalClause.getOperator()).isEqualTo(Operator.NOT_EQUAL);
    }

    @Test
    public void testGetType1() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, false);

        assertThat(atomicLexicalClause.getType()).isEqualTo(FieldType.NAME);
    }

    @Test
    public void testGetType2() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.PROJECT_NAME, Operator.EQUAL, false);

        assertThat(atomicLexicalClause.getType()).isEqualTo(FieldType.PROJECT_NAME);
    }

    @Test
    public void testHasWildcards1() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, true);

        assertThat(atomicLexicalClause.hasWildcards()).isTrue();
    }

    @Test
    public void testHasWildcards2() {
        AtomicLexicalClause atomicLexicalClause =
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, false);

        assertThat(atomicLexicalClause.hasWildcards()).isFalse();
    }

}
