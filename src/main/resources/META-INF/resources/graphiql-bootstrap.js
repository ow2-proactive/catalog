/*
 * ProActive Parallel Suite(TM):
 * The Java(TM) library for Parallel, Distributed,
 * Multi-Core Computing for Enterprise Grids & Clouds
 *
 * Copyright (c) 2016 ActiveEon
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

// Parse the search string to get url parameters.
var search = window.location.search;
var parameters = {};
search.substr(1).split('&').forEach(function (entry) {
    var eq = entry.indexOf('=');
    if (eq >= 0) {
        parameters[decodeURIComponent(entry.slice(0, eq))] =
            decodeURIComponent(entry.slice(eq + 1));
    }
});
// if variables was provided, try to format it.
if (parameters.variables) {
    try {
        parameters.variables =
            JSON.stringify(JSON.parse(parameters.variables), null, 2);
    } catch (e) {
        // Do nothing, we want to display the invalid JSON as a string, rather
        // than present an error.
    }
}
// When the query and variables string is edited, update the URL bar so
// that it can be easily shared
function onEditQuery(newQuery) {
    parameters.query = newQuery;
    updateURL();
}
function onEditVariables(newVariables) {
    parameters.variables = newVariables;
    updateURL();
}
function onEditOperationName(newOperationName) {
    parameters.operationName = newOperationName;
    updateURL();
}
function updateURL() {
    var newSearch = '?' + Object.keys(parameters).filter(function (key) {
            return Boolean(parameters[key]);
        }).map(function (key) {
            return encodeURIComponent(key) + '=' +
                encodeURIComponent(parameters[key]);
        }).join('&');
    history.replaceState(null, null, newSearch);
}
// Defines a GraphQL fetcher using the fetch API.
function graphQLFetcher(graphQLParams) {
    var location = window.location.origin + window.location.pathname;

    location = location.replace('graphiql/', '');
    location = location.replace('graphiql', '');

    if (!location.endsWith('/')) {
        location = location + '/';
    }

    return fetch(location + "graphql", {
        method: 'post',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'sessionid': localStorage['pa.session']
        },
        body: JSON.stringify(graphQLParams),
        credentials: 'include',
    }).then(function (response) {
        return response.text();
    }).then(function (responseBody) {
        try {
            return JSON.parse(responseBody);
        } catch (error) {
            return responseBody;
        }
    });
}
// Render <GraphiQL /> into the body.
ReactDOM.render(
    React.createElement(GraphiQL, {
        fetcher: graphQLFetcher,
        query: parameters.query,
        variables: parameters.variables,
        operationName: parameters.operationName,
        onEditQuery: onEditQuery,
        onEditVariables: onEditVariables,
        onEditOperationName: onEditOperationName
    }),
    document.getElementById('graphiql')
);
