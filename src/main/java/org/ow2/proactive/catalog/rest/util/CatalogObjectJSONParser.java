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
package org.ow2.proactive.catalog.rest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;


/**
 * This class parses a JSON describing a catalog object
 * @author ActiveEon Team
 * @since 4/25/2017
 */
public final class CatalogObjectJSONParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CatalogObjectData parseJSONFile(File fobject) throws IOException {
        FileInputStream fisobject = new FileInputStream(fobject);
        byte[] bObject = ByteStreams.toByteArray(fisobject);

        return MAPPER.readValue(bObject, CatalogObjectData.class);
    }

    public static class CatalogObjectData {

        private String name;

        private String kind;

        private String commitMessage;

        private String objectFileName;

        private String contentType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getCommitMessage() {
            return commitMessage;
        }

        public void setCommitMessage(String commitMessage) {
            this.commitMessage = commitMessage;
        }

        public String getObjectFileName() {
            return objectFileName;
        }

        public void setObjectFileName(String objectFileName) {
            this.objectFileName = objectFileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

    }
}
