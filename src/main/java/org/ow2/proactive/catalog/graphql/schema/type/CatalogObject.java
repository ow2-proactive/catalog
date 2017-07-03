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

import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import java.util.Map;

import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import lombok.Builder;
import lombok.Data;


/**
 * @author ActiveEon Team
 * @since 08/06/2017
 */
@Builder
@Data
public class CatalogObject {

    private final Long bucketId;

    private final long commitDateTime;

    private final String commitMessage;

    private final String contentType;

    private final Map<String, String> keyValueMetadata;

    private final String kind;

    private final String link;

    private final String name;

    private final static GraphQLObjectType TYPE;

    static {
        TYPE = GraphQLObjectType.newObject()
                                .name("CatalogObject")
                                .description("Catalog Object")
                                .field(newFieldDefinition().name("bucketId")
                                                           .description("Bucket Identifier")
                                                           .type(GraphQLLong))
                                .field(newFieldDefinition().name("commitDateTime")
                                                           .description("Commit Datetime")
                                                           .type(GraphQLLong))
                                .field(newFieldDefinition().name("commitMessage")
                                                           .description("Commit Message")
                                                           .type(GraphQLString))
                                .field(newFieldDefinition().name("keyValueMetadata")
                                                           .description("Catalog Object key valu metadata list")
                                                           .type(new GraphQLList(KeyValue.getTypeInstance()))
                                                           .build())
                                .field(newFieldDefinition().name("kind")
                                                           .description("Catalog Object kind")
                                                           .type(GraphQLString))
                                .field(newFieldDefinition().name("name")
                                                           .description("Catalog Object name")
                                                           .type(GraphQLString))
                                .field(newFieldDefinition().name("link")
                                                           .description("Catalog Object link")
                                                           .type(GraphQLString))
                                .build();
    }

    public final static GraphQLObjectType getTypeInstance() {
        return TYPE;
    }

}
