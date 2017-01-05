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

import com.google.common.base.Objects;


/**
 * KeyValueLexicalClause depicts the structure of a key/value clause read
 * from a WCQL query (e.g. {@code generic_information(\"Infra*\", \"*\"}).
 *
 * @author ActiveEon Team
 */
public final class KeyValueLexicalClause {

    public enum PairType {
        GENERIC_INFORMATION,
        VARIABLE
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

    public boolean keyHasWildcards() {
        return keyHasWildcards;
    }

    public boolean valueHasWildcards() {
        return valueHasWildcards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyValueLexicalClause that = (KeyValueLexicalClause) o;

        return Objects.equal(keyHasWildcards, that.keyHasWildcards) &&
               Objects.equal(valueHasWildcards, that.valueHasWildcards) && Objects.equal(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, keyHasWildcards, valueHasWildcards);
    }

    @Override
    public String toString() {
        return "KeyValueLexicalClause{" + "type=" + type + ", keyHasWildcards=" + keyHasWildcards +
               ", valueHasWildcards=" + valueHasWildcards + '}';
    }

}
