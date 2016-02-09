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

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * @author ActiveEon Team
 */
public class ClauseKey {

    public enum TABLE {VARIABLE, GENERIC_INFORMATION, NAME, PROJECT_NAME}

    public enum OPERATION {EQUAL, NOT_EQUAL}

    public enum CLAUSE_TYPE {KEY, VALUE, NOT_APPLICABLE}

    private final TABLE table;
    private final OPERATION operation;
    private final CLAUSE_TYPE clauseType;
    private final boolean hasWildcards;

    public ClauseKey(TABLE table, OPERATION operation, CLAUSE_TYPE clauseType, boolean hasWildcards) {
        this.table = table;
        this.operation = operation;
        this.clauseType = clauseType;
        this.hasWildcards = hasWildcards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClauseKey clauseKey = (ClauseKey) o;

        if (hasWildcards != clauseKey.hasWildcards) return false;
        if (table != clauseKey.table) return false;
        if (operation != clauseKey.operation) return false;
        return clauseType == clauseKey.clauseType;

    }

    @Override
    public int hashCode() {
        return Objects.hash(table, operation, clauseType, hasWildcards);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("table", table)
                .add("operation", operation)
                .add("clauseType", clauseType)
                .add("hasWildcards", hasWildcards).toString();
    }

}
