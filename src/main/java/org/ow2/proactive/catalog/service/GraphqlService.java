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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessType;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 13/06/2017
 */
@Log4j2
@Service
public class GraphqlService {

    @Getter
    private GraphQL graphql;

    @Autowired
    private DataFetcher catalogObjectFetcher;

    @Autowired
    private GrantRightsService grantRightsService;

    @PostConstruct
    public void init() throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        File schemaFile = new ClassPathResource("graphql-idl/catalogObjectSchema.graphqls").getFile();

        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schemaFile);
        RuntimeWiring wiring = buildRuntimeWiring();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);

        graphql = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                            .type("Query",
                                  typeWiring -> typeWiring.dataFetcher("allCatalogObjects", catalogObjectFetcher))
                            .build();
    }

    public Map<String, Object> executeQuery(String query, String operationName, Object graphqlContext,
            Map<String, Object> variables, AuthenticatedUser user) {

        if (variables == null) {
            variables = ImmutableMap.of();
        }

        ExecutionResult executionResult = graphql.execute(ExecutionInput.newExecutionInput()
                                                                        .query(query)
                                                                        .operationName(operationName)
                                                                        .context(graphqlContext)
                                                                        .variables(variables)
                                                                        .build());

        Map<String, Object> result = new LinkedHashMap<>();

        if (!executionResult.getErrors().isEmpty()) {
            result.put("errors", executionResult.getErrors());
            log.error("Errors: {}", executionResult.getErrors());
        }
        Object data = executionResult.getData();

        // the following block is used to remove buckets and objects for which the current user does not have read access
        if (data != null && user != null) {
            try {
                Map<String, Object> allCatalogObjects = (Map<String, Object>) ((Map<String, Object>) data).get("allCatalogObjects");
                List<Map<String, Object>> edges = (List<Map<String, Object>>) allCatalogObjects.get("edges");
                for (Iterator<Map<String, Object>> it = edges.iterator(); it.hasNext();) {
                    Map<String, Object> edge = it.next();
                    String bucketName = (String) edge.get("bucketName");
                    String objectName = (String) edge.get("name");
                    if (!AccessTypeHelper.satisfy(grantRightsService.getCatalogObjectRights(user,
                                                                                            bucketName,
                                                                                            objectName),
                                                  AccessType.read)) {
                        it.remove();
                    }
                }
            } catch (Exception e) {
                log.error("Unexpected exception when parsing graphql result: {}", e);
            }
        }
        result.put("data", data);

        return result;
    }
}
