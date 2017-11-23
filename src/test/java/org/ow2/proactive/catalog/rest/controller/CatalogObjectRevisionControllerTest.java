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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.springframework.http.ResponseEntity;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectRevisionControllerTest {

    @InjectMocks
    private CatalogObjectRevisionController catalogObjectRevisionController;

    @Mock
    private CatalogObjectService catalogObjectService;

    @Mock
    private RawObjectResponseCreator rawObjectResponseCreator;

    private static final Long BUCKET_ID = 1L;

    private static final long COMMIT_TIME = System.currentTimeMillis();

    @Test
    public void testList() throws Exception {
        catalogObjectRevisionController.list("", BUCKET_ID, "name");
        verify(catalogObjectService, times(1)).listCatalogObjectRevisions(BUCKET_ID, "name");
    }

    @Test
    public void testGetRevisionRaw() throws Exception {
        CatalogRawObject rawObject = new CatalogRawObject(1L,
                                                          "name",
                                                          "object",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        ResponseEntity responseEntity = ResponseEntity.ok().body(1);
        when(catalogObjectService.getCatalogObjectRevisionRaw(anyLong(), anyString(), anyLong())).thenReturn(rawObject);
        when(rawObjectResponseCreator.createRawObjectResponse(rawObject)).thenReturn(responseEntity);
        ResponseEntity responseEntityFromController = catalogObjectRevisionController.getRaw("",
                                                                                             BUCKET_ID,
                                                                                             "name",
                                                                                             System.currentTimeMillis());
        verify(catalogObjectService, times(1)).getCatalogObjectRevisionRaw(anyLong(), anyString(), anyLong());
        assertThat(responseEntityFromController).isNotNull();
        assertThat(responseEntityFromController).isEqualTo(responseEntity);
    }
}
