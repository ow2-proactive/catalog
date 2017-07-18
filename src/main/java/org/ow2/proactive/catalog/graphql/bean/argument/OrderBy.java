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

import java.util.Arrays;
import java.util.Optional;


/**
 * @author ActiveEon Team
 * @since 10/07/2017
 */
public enum OrderBy {
    CATALOG_OBJECT_KEY_ASC("catalogObjectKey_ASC"),
    CATALOG_OBJECT_KEY_DESC("catalogObjectKey_DESC"),
    KIND_ASC("kind_ASC"),
    KIND_DESC("kind_DESC");

    private String value;

    OrderBy(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderBy fromValue(String value) {
        Optional<OrderBy> first = Arrays.stream(OrderBy.values()).filter(e -> e.value.equals(value)).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        throw new IllegalArgumentException(value + " does not exist");
    }
}
