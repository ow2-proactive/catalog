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
 * AtomicLexicalClause depicts the structure of an atomic clause read
 * from a WCQL query (e.g. {@code project_name=\"*Project\"}).
 *
 * @author ActiveEon Team
 */
public class AtomicLexicalClause {

    public enum FieldType {
        NAME, PROJECT_NAME;
    }

    private final FieldType type;

    private final boolean hasWildcards;

    public enum Operator {EQUAL, NOT_EQUAL}

    private final Operator operator;

    public AtomicLexicalClause(FieldType type, Operator operator, boolean hasWildcards) {
        this.operator = operator;
        this.type = type;
        this.hasWildcards = hasWildcards;
    }

    public boolean hasWildcards() {
        return hasWildcards;
    }

    public FieldType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtomicLexicalClause that = (AtomicLexicalClause) o;

        if (hasWildcards != that.hasWildcards) return false;
        if (type != that.type) return false;
        return operator == that.operator;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (hasWildcards ? 1 : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AtomicLexicalClause{" +
                "type=" + type +
                ", operator=" + operator +
                ", hasWildcards=" + hasWildcards +
                '}';
    }

}
