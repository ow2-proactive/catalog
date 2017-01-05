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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


/**
 * Unit tests associated to {@link ProActiveWorkflowParserResult}.
 *
 * @author ProActive Team
 */
public class ProActiveWorkflowParserResultTest {

    private ProActiveWorkflowParserResult proActiveWorkflowParserResult;

    @Before
    public void setUp() {
        proActiveWorkflowParserResult = new ProActiveWorkflowParserResult("projectName",
                                                                          "name",
                                                                          ImmutableMap.of("g1", "gv1", "g2", "gv2"),
                                                                          ImmutableMap.of("v1",
                                                                                          "vv1",
                                                                                          "v2",
                                                                                          "vv2",
                                                                                          "v3",
                                                                                          "vv3"));
    }

    @Test
    public void testGetProjectName() {
        assertThat(proActiveWorkflowParserResult.getProjectName()).isEqualTo("projectName");
    }

    @Test
    public void testGetJobName() {
        assertThat(proActiveWorkflowParserResult.getJobName()).isEqualTo("name");
    }

    @Test
    public void testGetGenericInformation() {
        ImmutableMap<String, String> genericInformation = proActiveWorkflowParserResult.getGenericInformation();
        assertThat(genericInformation).hasSize(2);
        assertThat(genericInformation).containsExactly("g1", "gv1", "g2", "gv2");
    }

    @Test
    public void testGetVariables() {
        ImmutableMap<String, String> variables = proActiveWorkflowParserResult.getVariables();
        assertThat(variables).hasSize(3);
        assertThat(variables).containsExactly("v1", "vv1", "v2", "vv2", "v3", "vv3");
    }

    @Test(expected = NullPointerException.class)
    public void testNullGenericInformation() {
        new ProActiveWorkflowParserResult("projectName", "name", null, ImmutableMap.of());
    }

    @Test(expected = NullPointerException.class)
    public void testNullVariables() {
        new ProActiveWorkflowParserResult("projectName", "name", ImmutableMap.of(), null);
    }

}
