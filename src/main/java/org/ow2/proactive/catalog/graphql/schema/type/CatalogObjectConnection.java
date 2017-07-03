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

import static graphql.Scalars.GraphQLInt;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.util.List;

import com.google.common.collect.ImmutableList;

import graphql.relay.Relay;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;


/**
 * @author ActiveEon Team
 * @since 08/06/2017
 */
public class CatalogObjectConnection {
    private final static Relay RELAY = new Relay();

    private final static GraphQLObjectType TYPE;

    static {
        List<GraphQLFieldDefinition> extendedFields = ImmutableList.of(newFieldDefinition().name("totalCount")
                                                                                           .description("Total number of the items.")
                                                                                           .type(GraphQLInt)
                                                                                           .build());

        TYPE = RELAY.connectionType("CatalogObjects", CatalogObjectEdge.getInstance(), extendedFields);
    }

    public final static GraphQLObjectType getInstance() {
        return TYPE;
    }

    public final static List<GraphQLArgument> getConnectionFieldArguments() {
        return RELAY.getConnectionFieldArguments();
    }

    private CatalogObjectConnection() {
    }

    /**
     *
     */
    public final static class CatalogObjectEdge {
        private final static GraphQLObjectType TYPE = RELAY.edgeType("Jobs",
                                                                     CatalogObject.getTypeInstance(),
                                                                     null,
                                                                     ImmutableList.of());

        private CatalogObjectEdge() {
        }

        public final static GraphQLObjectType getInstance() {
            return TYPE;
        }
    }
}
