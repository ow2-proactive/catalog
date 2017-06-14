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
import org.ow2.proactive.catalog.service.CatalogObjectRevisionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectRevisionControllerTest {

    @InjectMocks
    private CatalogObjectRevisionController catalogObjectRevisionController;

    @Mock
    private CatalogObjectRevisionService catalogObjectRevisionService;

    private static final Long BUCKET_ID = 1L;

    private static final Long CO_ID = 2L;

    private static final Long REV_ID = 3L;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws Exception {
        MultipartFile mockedFile = mock(MultipartFile.class);
        when(mockedFile.getBytes()).thenReturn(null);
        catalogObjectRevisionController.create(BUCKET_ID,
                                               CO_ID,
                                               "workflow",
                                               "name",
                                               "Commit message",
                                               "application/xml",
                                               mockedFile);
        verify(catalogObjectRevisionService, times(1)).createCatalogObjectRevision(BUCKET_ID,
                                                                                   "workflow",
                                                                                   "name",
                                                                                   "Commit message",
                                                                                   Optional.of(CO_ID),
                                                                                   "application/xml",
                                                                                   null);

        catalogObjectRevisionController.create(BUCKET_ID,
                                               CO_ID,
                                               "image",
                                               "name",
                                               "Commit message",
                                               "image/gif",
                                               mockedFile);
        verify(catalogObjectRevisionService, times(1)).createCatalogObjectRevision(BUCKET_ID,
                                                                                   "image",
                                                                                   "name",
                                                                                   "Commit message",
                                                                                   Optional.of(CO_ID),
                                                                                   "image/gif",
                                                                                   null);
    }

    @Test
    public void testList() throws Exception {
        Pageable mockedPageable = mock(Pageable.class);
        PagedResourcesAssembler mockedAssembler = mock(PagedResourcesAssembler.class);
        catalogObjectRevisionController.list(BUCKET_ID, CO_ID, mockedPageable, mockedAssembler);
        verify(catalogObjectRevisionService, times(1)).listCatalogObjectRevisions(BUCKET_ID,
                                                                                  CO_ID,
                                                                                  mockedPageable,
                                                                                  mockedAssembler);
    }

    @Test
    public void testGet() throws Exception {
        catalogObjectRevisionController.get(BUCKET_ID, CO_ID, REV_ID);
        verify(catalogObjectRevisionService, times(1)).getCatalogObject(BUCKET_ID, CO_ID, Optional.of(REV_ID));
    }

    @Test
    public void testGetRevisionRaw() throws Exception {
        catalogObjectRevisionController.getRaw(BUCKET_ID, CO_ID, REV_ID);
        verify(catalogObjectRevisionService, times(1)).getCatalogObjectRaw(BUCKET_ID, CO_ID, Optional.of(REV_ID));
    }

    @Test
    public void testDelete() throws Exception {
        catalogObjectRevisionController.delete(BUCKET_ID, CO_ID, REV_ID);
        verify(catalogObjectRevisionService, times(1)).delete(BUCKET_ID, CO_ID, Optional.of(REV_ID));
    }
}
