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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.rest.assembler.CatalogObjectRevisionResourceAssembler;
import org.ow2.proactive.catalog.rest.entity.Bucket;
import org.ow2.proactive.catalog.rest.service.CatalogObjectService;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectControllerTest {

    @InjectMocks
    private CatalogObjectController catalogObjectController;

    @Mock
    private CatalogObjectService catalogObjectService;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Mock
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Mock
    CatalogObjectRevisionResourceAssembler catalocObjectResourceAssembler;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreate() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(null);
        catalogObjectController.create(1L, "workflow", "name", "Commit message", Optional.empty(), file);
        verify(catalogObjectService, times(1)).createCatalogObject(1L,
                                                                   "workflow",
                                                                   "name",
                                                                   "Commit message",
                                                                   Optional.empty(),
                                                                   null);

        catalogObjectController.create(1L, "image", "name", "Commit message", Optional.empty(), file);
        verify(catalogObjectService, times(1)).createCatalogObject(1L,
                                                                   "image",
                                                                   "name",
                                                                   "Commit message",
                                                                   Optional.empty(),
                                                                   null);
    }

    @Test
    public void testList() throws Exception {
        Bucket bucket = mock(Bucket.class);
        when(bucketRepository.findOne(1L)).thenReturn(bucket);
        when(pagedResourcesAssembler.toResource(any(Page.class),
                                                any(CatalogObjectRevisionResourceAssembler.class))).thenReturn(mock(PagedResources.class));
        catalogObjectController.list(1L, null, pagedResourcesAssembler);
        verify(catalogObjectService, times(1)).listCatalogObjects(anyLong(),
                                                                  any(Optional.class),
                                                                  any(Pageable.class),
                                                                  any(PagedResourcesAssembler.class));
    }

    @Test
    public void testGet() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        catalogObjectController.get(1L, 2L, response);
        verify(catalogObjectService, times(1)).getCatalogObjectMetadata(1L, 2L);
    }

    @Test
    public void testDelete() throws Exception {
        catalogObjectController.delete(1L, 2L);
        verify(catalogObjectService, times(1)).delete(1L, 2L);
    }
}
