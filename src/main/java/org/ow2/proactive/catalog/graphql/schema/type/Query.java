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
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import org.ow2.proactive.catalog.graphql.schema.common.Arguments;
import org.ow2.proactive.catalog.graphql.schema.type.filter.CatalogObjectWhereArgs;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import lombok.Data;


/**
 * @author ActiveEon Team
 * @since 03/07/2017
 */
@Data
public class Query {

    private final GraphQLObjectType type;

    private Query(GraphQLObjectType type) {
        this.type = type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DataFetcher catalogObjectFetcher;

        public Builder catalogObjectFetcher(DataFetcher catalogObjectFetcher) {
            this.catalogObjectFetcher = catalogObjectFetcher;
            return this;
        }

        public Query build() {
            GraphQLObjectType type = GraphQLObjectType.newObject()
                                                      .name("Query")
                                                      .field(newFieldDefinition().name("CatalogObjects")
                                                                                 .description("Catalog Object list, it will be empty if there is none.")
                                                                                 .type(CatalogObjectConnection.getInstance())
                                                                                 .argument(newArgument().name(Arguments.WHERE.getName())
                                                                                                        .type(CatalogObjectWhereArgs.getWhereArguments()))
                                                                                 .argument(CatalogObjectConnection.getConnectionFieldArguments())
                                                                                 .argument(newArgument().name(Arguments.FIRST.getName())
                                                                                                        .type(GraphQLInt)
                                                                                                        .defaultValue(50)
                                                                                                        .build())
                                                                                 .dataFetcher(catalogObjectFetcher))
                                                      .build();

            return new Query(type);
        }
    }

    public GraphQLObjectType getType() {
        return type;
    }

}
