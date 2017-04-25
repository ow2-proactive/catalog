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
package org.ow2.proactive.workflow_catalog.rest.entity;

import static com.google.common.truth.Truth.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectRevisionTest {

    @Test
    public void testAddGenericInformation() throws Exception {
        CatalogObjectRevision catalogObjectRevision = createWorkflowRevision();

        assertThat(catalogObjectRevision.getGenericInformation()).hasSize(1);

        catalogObjectRevision.addGenericInformation(Mockito.mock(GenericInformation.class));

        assertThat(catalogObjectRevision.getGenericInformation()).hasSize(2);
    }

    @Test
    public void testAddVariable() throws Exception {
        CatalogObjectRevision catalogObjectRevision = createWorkflowRevision();

        assertThat(catalogObjectRevision.getVariables()).hasSize(1);

        catalogObjectRevision.addVariable(Mockito.mock(Variable.class));

        assertThat(catalogObjectRevision.getVariables()).hasSize(2);
    }

    private CatalogObjectRevision createWorkflowRevision() {
        GenericInformation genericInformationMock = Mockito.mock(GenericInformation.class);
        Variable variableMock = Mockito.mock(Variable.class);

        CatalogObjectRevision catalogObjectRevision = new CatalogObjectRevision(1L,
                                                                 1L,
                                                                 "test",
                                                                 "test",
                                                                 LocalDateTime.now(),
                                                                 null,
                                                                 Lists.newArrayList(genericInformationMock),
                                                                 Lists.newArrayList(variableMock),
                                                                 new byte[0]);

        return catalogObjectRevision;
    }

}
