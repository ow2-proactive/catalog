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
import com.mysema.query.types.expr.BooleanExpression;

/**
 * QueryExpressionContext keeps the context associated to a query expression.
 * Mainly the QueryDSL expression and possibly some other fields in the future.
 *
 * @author ActiveEon Team
 */
public final class QueryExpressionContext {

    private final BooleanExpression booleanExpression;

    public QueryExpressionContext(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }

    public BooleanExpression getExpression() {
        return booleanExpression;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("expression", booleanExpression).toString();
    }

}