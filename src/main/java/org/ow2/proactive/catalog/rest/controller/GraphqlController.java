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
package org.ow2.proactive.catalog.rest.controller;

import java.io.IOException;
import java.util.Map;

import org.ow2.proactive.catalog.service.GraphqlService;
import org.ow2.proactive.catalog.service.RestApiAccessService;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 04/07/2017
 */
@Controller
@Log4j2
public class GraphqlController {

    private static final String DEFAULT_OPERATION_NAME = "operationName";

    private static final String DEFAULT_QUERY_KEY = "query";

    private static final String DEFAULT_VARIABLES_KEY = "variables";

    private static final String REQUEST_HEADER_NAME_SESSION_ID = "sessionid";

    @Autowired
    private GraphqlService graphqlService;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Value("${pa.catalog.security.required.sessionid}")
    private boolean sessionIdRequired;

    @RequestMapping(value = "/graphiql", method = RequestMethod.GET)
    public String graphiql() {
        return "/index.html";
    }

    /*
     * http://graphql.org/learn/serving-over-http/#post-request
     */
    @RequestMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> executeOperation(@RequestHeader(value = REQUEST_HEADER_NAME_SESSION_ID) String sessionId,
            @RequestBody Map<String, Object> body) throws IOException {

        log.debug("sessionId={}", sessionId);

        AuthenticatedUser user = null;
        if (sessionIdRequired) {
            user = restApiAccessService.getUserFromSessionId(sessionId);
        }

        String query = (String) body.get(DEFAULT_QUERY_KEY);
        String operationName = (String) body.get(DEFAULT_OPERATION_NAME);
        Map<String, Object> variables = (Map<String, Object>) body.get(DEFAULT_VARIABLES_KEY);

        log.debug("query={}, operationName={}, variables={}", query, operationName, variables);

        return graphqlService.executeQuery(query, operationName, (Object) null, variables, user);
    }

}
