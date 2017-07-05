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

import static graphql.Scalars.GraphQLLong;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;

import org.ow2.proactive.catalog.graphql.schema.common.Operations;

import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
public class CatalogObjectBucketIdWhereArgs extends WhereArgs<Long> {

    protected final Long[] in;

    protected final Long[] notIn;

    public CatalogObjectBucketIdWhereArgs(Long eq, Long ne, Long gt, Long gte, Long lt, Long lte, Long[] in,
            Long[] notIn) {
        super(eq, ne, gt, gte, lt, lte);
        this.in = in;
        this.notIn = notIn;
    }

    public Long[] getIn() {
        return in;
    }

    public Long[] getNotIn() {
        return notIn;
    }

    private final static GraphQLInputType TYPE;

    static {
        TYPE = newInputObject().name("bucketIdArgs")
                               .description("Bucket ID argument")
                               .field(newInputObjectField().name(Operations.EQ.getName())
                                                           .description("equal")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.NE.getName())
                                                           .description("not equal")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.GT.getName())
                                                           .description("great than")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.GTE.getName())
                                                           .description("equal or great than")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.LT.getName())
                                                           .description("less than")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.LTE.getName())
                                                           .description("equal or less than")
                                                           .type(GraphQLLong)
                                                           .build())
                               .field(newInputObjectField().name(Operations.IN.getName())
                                                           .description("in the list")
                                                           .type(new GraphQLList(GraphQLLong))
                                                           .build())
                               .field(newInputObjectField().name(Operations.NOT_IN.getName())
                                                           .description("not in the list")
                                                           .type(new GraphQLList(GraphQLLong))
                                                           .build())
                               .build();
    }

    public static GraphQLInputType getTypeInstance() {
        return TYPE;
    }
}
