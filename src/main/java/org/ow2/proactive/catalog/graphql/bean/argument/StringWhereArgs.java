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
package org.ow2.proactive.catalog.graphql.bean.argument;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author ActiveEon Team
 * @since 04/07/2017
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StringWhereArgs extends WhereArgs<String> {

    protected final String like;

    protected final String notLike;

    public StringWhereArgs(String eq, String ne, String gt, String gte, String lt, String lte, String like,
            String notLike) {
        super(eq, ne, gt, gte, lt, lte);
        this.like = like;
        this.notLike = notLike;
    }

    public String getLike() {
        return like;
    }

    public String getNotLike() {
        return notLike;
    }
}