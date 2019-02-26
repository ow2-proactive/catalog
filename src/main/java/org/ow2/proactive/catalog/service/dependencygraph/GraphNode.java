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
package org.ow2.proactive.catalog.service.dependencygraph;

import java.io.Serializable;
import java.util.Objects;

import lombok.EqualsAndHashCode;

/**
 * @author ActiveEon Team
 * @since 2019-02-25
 */
@EqualsAndHashCode(of = { "bucketName", "objectName"})
public final class GraphNode implements Serializable {
    private final String bucketName;
    private final String objectName;

    public GraphNode(String bucketName, String objectName) {
        this.bucketName = Objects.requireNonNull(bucketName, "bucketName");
        this.objectName =  Objects.requireNonNull(objectName, "objectName");
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GraphNode{");
        sb.append("bucketName='").append(bucketName).append('\'');
        sb.append(", objectName='").append(objectName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
