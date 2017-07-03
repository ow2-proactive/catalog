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
package org.ow2.proactive.catalog.graphql.schema.type;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Data;


/**
 * @author ActiveEon Team
 * @since 08/06/2017
 */
@Builder
@Data
public class KeyValue {

    private final String key;

    private final String value;

    private final static GraphQLObjectType TYPE;

    static {
        TYPE = GraphQLObjectType.newObject()
                                .name("metadata")
                                .description("Key value metadata as a map.")
                                .field(newFieldDefinition().name("key")
                                                           .description("Key as the key in a map.")
                                                           .type(GraphQLString))
                                .field(newFieldDefinition().name("value")
                                                           .description("Value as the value in a map.")
                                                           .type(GraphQLString))
                                .build();
    }

    public final static GraphQLObjectType getTypeInstance() {
        return TYPE;
    }

}
