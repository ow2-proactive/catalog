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
package org.ow2.proactive.catalog.rest.entity;

import static com.google.common.truth.Truth.assertThat;

import java.time.LocalDateTime;

import org.junit.Test;
import org.mockito.Mockito;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevision;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectRevisionTest {

    @Test
    public void testAddKeyValue() throws Exception {
        CatalogObjectRevision catalogObjectRevision = createWorkflowRevision();
        //TODO
        //        assertThat(catalogObjectRevision.getGenericInformation()).hasSize(1);
        //
        //        catalogObjectRevision.addGenericInformation(Mockito.mock(GenericInformation.class));
        //
        //        assertThat(catalogObjectRevision.getGenericInformation()).hasSize(2);

        //        CatalogObjectRevision catalogObjectRevision = createWorkflowRevision();
        //
        //        assertThat(catalogObjectRevision.getVariables()).hasSize(1);
        //
        //        catalogObjectRevision.addVariable(Mockito.mock(Variable.class));
        //
        //        assertThat(catalogObjectRevision.getVariables()).hasSize(2);
    }

    private CatalogObjectRevision createWorkflowRevision() {
        KeyValueMetadata variableMock = Mockito.mock(KeyValueMetadata.class);

        CatalogObjectRevision catalogObjectRevision = new CatalogObjectRevision("workflow",
                                                                                1L,
                                                                                1L,
                                                                                "test",
                                                                                "test",
                                                                                LocalDateTime.now(),
                                                                                null,
                                                                                Lists.newArrayList(variableMock),
                                                                                new byte[0]);

        return catalogObjectRevision;
    }

}
