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
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import lombok.Data;
import lombok.ToString;


/**
 * @author ActiveEon Team
 * @since 08/06/2017
 */
@Data
@ToString
public class Bucket {

    public final static TypeSingleton<GraphQLObjectType> TYPE = new TypeSingleton<GraphQLObjectType>() {
        @Override
        public GraphQLObjectType buildType(DataFetcher... dataFetchers) {

            DataFetcher genericInformationDataFetcher = dataFetchers[0];
            DataFetcher taskDataFetcher = dataFetchers[1];
            DataFetcher variableDataFetcher = dataFetchers[2];

            return GraphQLObjectType.newObject()
                                    .name(Types.JOB.getName())
                                    .description("Job managed by a ProActive Scheduler instance. A Job is made of one or more Tasks.")
                                    .withInterface(JobTaskCommon.TYPE.getInstance(genericInformationDataFetcher,
                                                                                  variableDataFetcher))
                                    .field(newFieldDefinition().name(DATA_MANAGEMENT.getName())
                                                               .description("User configuration for data spaces.")
                                                               .type(DataManagement.TYPE.getInstance()))
                                    .field(newFieldDefinition().name(DESCRIPTION.getName())
                                                               .description("The description of the job.")
                                                               .type(GraphQLString))
                                    .field(newFieldDefinition().name(FINISHED_TIME.getName())
                                                               .description("The timestamp at which the Job has finished its execution.")
                                                               .type(GraphQLLong))
                                    .field(newFieldDefinition().name(GENERIC_INFORMATION.getName())
                                                               .description("Generic information list, empty if there is none.")
                                                               .type(new GraphQLList(GenericInformation.TYPE.getInstance(dataFetchers)))
                                                               .argument(newArgument().name(FILTER.getName())
                                                                                      .description("Generic information input filter.")
                                                                                      .type(new GraphQLList(KeyValueInput.TYPE.getInstance()))
                                                                                      .build())
                                                               .dataFetcher(genericInformationDataFetcher))
                                    .build();
        }
    };

    private DataManagement dataManagement;

    private long lastUpdatedTime;

    private int numberOfFailedTasks;

    private int numberOfFaultyTasks;
}
