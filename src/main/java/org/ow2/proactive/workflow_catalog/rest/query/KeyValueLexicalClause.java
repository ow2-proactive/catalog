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
package org.ow2.proactive.workflow_catalog.rest.query;

/**
 * @author ActiveEon Team
 */
public class KeyValueLexicalClause {

    public enum PairType {
        GENERIC_INFORMATION, VARIABLE
    }

    private final PairType type;

    private final boolean keyHasWildcards;

    private final boolean valueHasWildcards;

    public KeyValueLexicalClause(PairType type, boolean keyHasWildcards, boolean valueHasWildcards) {
        this.type = type;
        this.keyHasWildcards = keyHasWildcards;
        this.valueHasWildcards = valueHasWildcards;
    }

    public PairType getType() {
        return type;
    }

    public boolean isKeyHasWildcards() {
        return keyHasWildcards;
    }

    public boolean isValueHasWildcards() {
        return valueHasWildcards;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValueLexicalClause that = (KeyValueLexicalClause) o;

        if (keyHasWildcards != that.keyHasWildcards) return false;
        if (valueHasWildcards != that.valueHasWildcards) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (keyHasWildcards ? 1 : 0);
        result = 31 * result + (valueHasWildcards ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValueLexicalClause{" +
                "type=" + type +
                ", keyHasWildcards=" + keyHasWildcards +
                ", valueHasWildcards=" + valueHasWildcards +
                '}';
    }
}
