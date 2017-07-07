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
package org.ow2.proactive.catalog.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 13/06/2017
 */
@Log4j2
@Service
public class GraphqlService {

    private GraphQL graphql;

    @Autowired
    private DataFetcher catalogObjectFetcher;

    @PostConstruct
    public void init() throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        String pathToFile = "D:\\Workspace\\IDEA\\catalog-generic\\src\\main\\resources\\graphql-idl\\catalogObjectSchema.graphqls";
        String content = new String(Files.readAllBytes(Paths.get(pathToFile)));
        System.out.println("The content" + content);

        File schemaFile = new File(pathToFile);

        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schemaFile);
        RuntimeWiring wiring = buildRuntimeWiring();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);

        graphql = GraphQL.newGraphQL(graphQLSchema).build();

        //        graphql = GraphQL.newGraphQL(GraphQLSchema.newSchema()
        //                                                  .query(Query.builder()
        //                                                              .catalogObjectFetcher(catalogObjectFetcher)
        //                                                              .build()
        //                                                              .getType())
        //                                                  .build())
        //                         .build();
    }

    RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                            .type("Query", typeWiring -> typeWiring.dataFetcher("CatalogObjects", catalogObjectFetcher))
                            .build();
    }

    public Map<String, Object> executeQuery(String query, String operationName, Object graphqlContext,
            Map<String, Object> variables) {

        if (variables == null) {
            variables = ImmutableMap.of();
        }

        ExecutionResult executionResult = graphql.execute(query, operationName, graphqlContext, variables);

        Map<String, Object> result = new LinkedHashMap<>();

        if (!executionResult.getErrors().isEmpty()) {
            result.put("errors", executionResult.getErrors());
            log.error("Errors: {}", executionResult.getErrors());
        }

        result.put("data", executionResult.getData());

        return result;
    }
}
