/*
 *  ProActive Parallel Suite(TM): The Java(TM) library for
 *     Parallel, Distributed, Multi-Core Computing for
 *     Enterprise Grids & Clouds
 *
 *  Copyright (C) 1997-2016 INRIA/University of
 *                  Nice-Sophia Antipolis/ActiveEon
 *  Contact: proactive@ow2.org or contact@activeeon.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; version 3 of
 *  the License.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *  USA
 *
 *  If needed, contact us to obtain a release under GPL Version 2 or 3
 *  or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                          http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Unit tests associated to {@link ProActiveWorkflowParserResult}.
 *
 * @author ProActive Team
 */
public class ProActiveWorkflowParserResultTest {

    private ProActiveWorkflowParserResult proActiveWorkflowParserResult;

    @Before
    public void setUp() {
        proActiveWorkflowParserResult =
                new ProActiveWorkflowParserResult(
                        "projectName", "name",
                        ImmutableMap.of("g1", "gv1", "g2", "gv2"),
                        ImmutableMap.of("v1", "vv1", "v2", "vv2", "v3", "vv3")
                );
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
        ImmutableMap<String, String> genericInformation =
                proActiveWorkflowParserResult.getGenericInformation();
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
        new ProActiveWorkflowParserResult(
                "projectName", "name",
                null,
                ImmutableMap.of()
        );
    }

    @Test(expected = NullPointerException.class)
    public void testNullVariables() {
        new ProActiveWorkflowParserResult(
                "projectName", "name",
                ImmutableMap.of(),
                null
        );
    }

}