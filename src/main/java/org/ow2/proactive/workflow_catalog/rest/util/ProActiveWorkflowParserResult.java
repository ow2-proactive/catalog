/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.util;

import com.google.common.collect.ImmutableMap;

import java.util.Objects;

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

    public ProActiveWorkflowParserResult(String projectName, String name, ImmutableMap<String, String> genericInformation, ImmutableMap<String, String> variables) {
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