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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.rest.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectServiceTest {

    @InjectMocks
    private CatalogObjectService catalogObjectService;

    @Mock
    private CatalogObjectRevisionService catalogObjectRevisionService;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Mock
    private BucketRepository bucketRepository;

    private static final Long DUMMY_ID = 0L;

    private static final Long EXISTING_ID = 1L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateCatalogObject() {
        catalogObjectService.createCatalogObject(DUMMY_ID, "object", "name", "commit message", "application/xml", null);
        verify(catalogObjectRevisionService, times(1)).createCatalogObjectRevision(DUMMY_ID,
                                                                                   "object",
                                                                                   "name",
                                                                                   "commit message",
                                                                                   Optional.empty(),
                                                                                   "application/xml",
                                                                                   null);
    }

    @Test
    public void testGetCatalogObjectMetadata() {
        catalogObjectService.getCatalogObjectMetadata(DUMMY_ID, DUMMY_ID);
        verify(catalogObjectRevisionService, times(1)).getCatalogObject(DUMMY_ID, DUMMY_ID, Optional.empty());
    }

    @Test
    public void testListCatalogObjects() {
        catalogObjectService.listCatalogObjects(DUMMY_ID, Optional.empty());
        verify(catalogObjectRevisionService, times(1)).listCatalogObjects(DUMMY_ID, Optional.empty());
    }

    @Test
    public void testDelete() throws Exception {
        catalogObjectService.delete(1L, 2L);
        verify(catalogObjectRevisionService, times(1)).delete(1L, 2L, Optional.empty());
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testFindWorkflowInvalidId() throws Exception {
        when(catalogObjectRepository.findOne(anyLong())).thenReturn(null);
        catalogObjectService.findObjectById(DUMMY_ID);
    }

    @Test
    public void testFindCatalogObject() throws Exception {
        when(catalogObjectRepository.findOne(EXISTING_ID)).thenReturn(mock(CatalogObjectEntity.class));
        catalogObjectService.findObjectById(EXISTING_ID);
        verify(catalogObjectRepository, times(1)).findOne(EXISTING_ID);
    }
}
