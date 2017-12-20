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
package org.ow2.proactive.catalog.util;

import static com.google.common.truth.Truth.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


/**
 * @author ActiveEon Team
 * @since 11/23/2017
 */
public class RawObjectResponseCreatorTest {

    private RawObjectResponseCreator rawObjectResponseCreator = new RawObjectResponseCreator();

    @Test
    public void testCreateRawObjectResponseGeneralKindRightContentType() {
        String objectName = "object name";
        String contentDispositionFileName = "attachment; filename=\"" + objectName + "\"";
        CatalogRawObject rawObject = new CatalogRawObject("bucket-name",
                                                          objectName,
                                                          "object",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        ResponseEntity responseEntity = rawObjectResponseCreator.createRawObjectResponse(rawObject);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);
        assertThat(responseEntity.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION)).isTrue();
        assertThat(responseEntity.getHeaders()
                                 .getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo(contentDispositionFileName);
        assertThat(responseEntity.getHeaders()
                                 .getFirst(HttpHeaders.CONTENT_LENGTH)).isEqualTo(String.valueOf(rawObject.getRawObject().length));
    }

    @Test
    public void testCreateRawObjectResponseWorkflowKindWithoutExtension() {
        String objectName = "object name";
        String contentDispositionFileName = "attachment; filename=\"" + objectName +
                                            RawObjectResponseCreator.WORKFLOW_EXTENSION + "\"";
        CatalogRawObject rawObject = new CatalogRawObject("bucket-name",
                                                          objectName,
                                                          "workflow",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        ResponseEntity responseEntity = rawObjectResponseCreator.createRawObjectResponse(rawObject);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);
        assertThat(responseEntity.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION)).isTrue();
        assertThat(responseEntity.getHeaders()
                                 .getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo(contentDispositionFileName);
    }

    @Test
    public void testCreateRawObjectResponseWorkflowKindWithExtension() {
        String objectName = "object name.xml";
        String contentDispositionFileName = "attachment; filename=\"" + objectName + "\"";
        CatalogRawObject rawObject = new CatalogRawObject("bucket-name",
                                                          objectName,
                                                          "workflow",
                                                          "application/xml",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        ResponseEntity responseEntity = rawObjectResponseCreator.createRawObjectResponse(rawObject);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_XML);
        assertThat(responseEntity.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION)).isTrue();
        assertThat(responseEntity.getHeaders()
                                 .getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo(contentDispositionFileName);
    }

    @Test
    public void testCreateRawObjectResponseWrongContentType() {
        CatalogRawObject rawObject = new CatalogRawObject("bucket-name",
                                                          "name",
                                                          "object",
                                                          "testContentType",
                                                          1400343L,
                                                          "commit message",
                                                          Collections.emptyList(),
                                                          new byte[0]);
        ResponseEntity responseEntity = rawObjectResponseCreator.createRawObjectResponse(rawObject);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getHeaders().getContentType()).isNull();
    }

}
