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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * This class parses a JSON describing a catalog package
 * @author ActiveEon Team
 * @since 4/25/2017
 */
public final class PackageMetadataJSONParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PackageMetadataJSONParser() {
        throw new IllegalStateException("Utility class");
    }

    protected static byte[] writeJSONFile(PackageData data) throws IOException {
        return MAPPER.writeValueAsBytes(data);
    }

    protected static PackageData createPackageMetadata(String bucketName, String userGroup) {
        return new PackageData(bucketName, userGroup);
    }

    @Getter
    @Setter
    protected static class PackageData {

        private PackageMetaData metadata;

        private BucketData catalog;

        public PackageData(String bucketName, String userGroup) {
            this.metadata = new PackageMetaData(bucketName);

            this.catalog = new BucketData(bucketName, userGroup, new ArrayList<CatalogObjectData>());
        }

    }

    @Getter
    @Setter
    private static class PackageMetaData {

        private String name;

        private String slug;

        private String short_description;

        private String author;

        private String version;

        public PackageMetaData(String name) {
            this.name = name;
            this.slug = name;
            this.author = "";
            this.version = "";
            this.short_description = "";
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    protected static class BucketData {

        private String bucket;

        private String userGroup;

        private List<CatalogObjectData> objects;

    }

    @Getter
    @Setter
    protected static class CatalogObjectData {

        private String name;

        private CatalogObjectMetaData metadata;

        private String file;

        public CatalogObjectData(String name, String kind, String commitMessage, String contentType, String file) {
            this.name = name;
            this.file = file;
            this.metadata = new CatalogObjectMetaData(kind, commitMessage, contentType);
        }

    }

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    private static class CatalogObjectMetaData {

        private String kind;

        private String commitMessage;

        private String contentType;

    }
}
