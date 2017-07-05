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
package org.ow2.proactive.catalog.graphql.schema.type.filter;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import graphql.schema.GraphQLInputType;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author ActiveEon Team
 * @since 12/06/2017
 */
@AllArgsConstructor
@Data
public class CatalogObjectWhereArgs {

    private final AndOrArgs andOrArgs;

    private final CatalogObjectHavingMetadataWhereArgs havingArgs;

    private final CatalogObjectBucketIdWhereArgs idArgs;

    private final CatalogObjectKindWhereArgs kindArgs;

    private final CatalogObjectNameWhereArgs nameArgs;

    private final CatalogObjectRevisionWhereArgs revisionArgs;

    private final static GraphQLInputType args;

    static {
        args = newInputObject().field(newInputObjectField().name("havingMetadataArg")
                                                           .description("CatalogObject metadata argument")
                                                           .type(CatalogObjectHavingMetadataWhereArgs.getTypeInstance())
                                                           .build())
                               .field(newInputObjectField().name("bucketIdArg")
                                                           .description("Bucket id argument")
                                                           .description("CatalogObject bucket id argument")
                                                           .type(CatalogObjectBucketIdWhereArgs.getTypeInstance())
                                                           .build())
                               .field(newInputObjectField().name("kindArg")
                                                           .description("CatalogObject kind argument")
                                                           .type(CatalogObjectKindWhereArgs.getTypeInstance())
                                                           .build())
                               .field(newInputObjectField().name("nameArg")
                                                           .description("CatalogObject name argument")
                                                           .type(CatalogObjectNameWhereArgs.getTypeInstance())
                                                           .build())
                               .build();

    }

    public static GraphQLInputType getWhereArguments() {
        return args;
    }
}
