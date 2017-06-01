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
package org.ow2.proactive.catalog.rest.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.query.QueryExpressionBuilderException;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectServiceTest {

    @InjectMocks
    private CatalogObjectService catalogObjectService;

    @Mock
    private CatalogObjectRevisionService catalogObjectRevisionService;

    private final static Long DUMMY_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCatalogObject() {
        catalogObjectService.createCatalogObject(DUMMY_ID, "object", "name", "commit message", Optional.empty(), null);
        verify(catalogObjectRevisionService, times(1)).createCatalogObjectRevision(DUMMY_ID,
                                                                                   "object",
                                                                                   "name",
                                                                                   "commit message",
                                                                                   Optional.empty(),
                                                                                   Optional.empty(),
                                                                                   null);
    }

    @Test
    public void testGetCatalogObjectMetadata() {
        catalogObjectService.getCatalogObjectMetadata(DUMMY_ID, DUMMY_ID);
        verify(catalogObjectRevisionService, times(1)).getCatalogObject(DUMMY_ID, DUMMY_ID, Optional.empty());
    }

    @Test
    public void testListCatalogObjects() throws QueryExpressionBuilderException {
        catalogObjectService.listCatalogObjects(DUMMY_ID, Optional.empty(), null, null);
        verify(catalogObjectRevisionService, times(1)).listCatalogObjects(DUMMY_ID,
                                                                          Optional.empty(),
                                                                          Optional.empty(),
                                                                          null,
                                                                          null);
    }

    @Test
    public void testDelete() throws Exception {
        catalogObjectService.delete(1L, 2L);
        verify(catalogObjectRevisionService, times(1)).delete(1L, 2L, Optional.empty());
    }
}
