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
package org.ow2.proactive.workflow_catalog.rest.util;

import java.util.Objects;

import com.google.common.collect.ImmutableMap;


/**
 *
 * @see ProActiveWorkflowParser
 *
 * @author ActiveEon Team
 */
public final class ProActiveWorkflowParserResult {

    private final String projectName;

    private final String name;

    private final ImmutableMap<String, String> genericInformation;

    private final ImmutableMap<String, String> variables;

    public ProActiveWorkflowParserResult(String projectName, String name,
            ImmutableMap<String, String> genericInformation, ImmutableMap<String, String> variables) {
        Objects.requireNonNull(genericInformation);
        Objects.requireNonNull(variables);

        this.projectName = projectName;
        this.name = name;
        this.genericInformation = genericInformation;
        this.variables = variables;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getJobName() {
        return name;
    }

    public ImmutableMap<String, String> getGenericInformation() {
        return genericInformation;
    }

    public ImmutableMap<String, String> getVariables() {
        return variables;
    }

}
