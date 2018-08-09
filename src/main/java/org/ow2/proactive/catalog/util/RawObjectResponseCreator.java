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

import java.io.ByteArrayInputStream;

import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.util.parser.SupportedParserKinds;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 11/23/2017
 */
@Log4j2
@Component
public class RawObjectResponseCreator {

    public final static String WORKFLOW_EXTENSION = ".xml";

    public ResponseEntity createRawObjectResponse(CatalogRawObject rawObject) {
        String name = rawObject.getName();

        byte[] bytes = rawObject.getRawObject();

        ResponseEntity.BodyBuilder responseBodyBuilder = ResponseEntity.ok().contentLength(bytes.length);

        try {
            String contentDispositionFileName = name;
            String fileExtension = rawObject.getExtension();

            if (fileExtension != null) {
                contentDispositionFileName += "." + fileExtension;
            }
            //add the .xml extension to contentDispositionFileName for workflow if the extension was not yet in name
            else if (rawObject.getKind() != null && rawObject.getContentType() != null &&
                     rawObject.getKind()
                              .toLowerCase()
                              .startsWith(SupportedParserKinds.WORKFLOW.toString().toLowerCase()) &&
                     !name.endsWith(WORKFLOW_EXTENSION)) {
                contentDispositionFileName += WORKFLOW_EXTENSION;
            }
            responseBodyBuilder.header(HttpHeaders.CONTENT_DISPOSITION,
                                       "attachment; filename=\"" + contentDispositionFileName + "\"");
        } catch (Exception e) {
            log.warn("The exception during creation of raw object response", e);
        }

        try {
            MediaType mediaType = MediaType.valueOf(rawObject.getContentType());
            responseBodyBuilder = responseBodyBuilder.contentType(mediaType);
        } catch (org.springframework.http.InvalidMediaTypeException mimeEx) {
            log.warn("The wrong content type for object: " + name + ", commitTime:" + rawObject.getCommitDateTime() +
                     ", the contentType: " + rawObject.getContentType(), mimeEx);
        }

        return responseBodyBuilder.body(new InputStreamResource(new ByteArrayInputStream(bytes)));
    }
}
