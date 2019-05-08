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
package org.ow2.proactive.catalog.callgraph;

import java.io.Serializable;
import java.util.Objects;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * @author ActiveEon Team
 * @since 2019-03-22
 */

@PublicAPI
public final class GraphNode implements Serializable {

    public static class Builder {

        private String bucketName;

        private String objectName;

        private String objectKind;

        private boolean isInCatalog;

        public Builder bucketName(String bucketName) {
            this.bucketName = bucketName;
            return this;
        }

        public Builder objectName(String objectName) {
            this.objectName = objectName;
            return this;
        }

        public Builder objectKind(String objectKind) {
            this.objectKind = objectKind;
            return this;
        }

        public Builder isInCatalog(boolean isInCatalog) {
            this.isInCatalog = isInCatalog;
            return this;
        }

        public GraphNode build() {
            return new GraphNode(bucketName, objectName, objectKind, isInCatalog);
        }
    }

    private final String bucketName;

    private final String objectName;

    private final String objectKind;

    private final boolean isInCatalog;

    public GraphNode(String bucketName, String objectName, String objectKind, boolean isInCatalog) {
        this.bucketName = Objects.requireNonNull(bucketName, "bucketName");
        this.objectName = Objects.requireNonNull(objectName, "objectName");
        this.objectKind = Objects.requireNonNull(objectKind, "objectKind");
        this.isInCatalog = isInCatalog;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GraphNode graphNode = (GraphNode) o;
        return Objects.equals(bucketName, graphNode.bucketName) && Objects.equals(objectName, graphNode.objectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucketName, objectName);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getObjectKind() {
        return objectKind;
    }

    public boolean isInCatalog() {
        return isInCatalog;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(bucketName).append('/').append(objectName);
        return sb.toString();
    }
}
