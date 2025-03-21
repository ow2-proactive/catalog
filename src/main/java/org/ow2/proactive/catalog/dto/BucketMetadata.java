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
package org.ow2.proactive.catalog.dto;

import java.util.Objects;

import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


/**
 * @author ActiveEon Team
 */
@Data
public class BucketMetadata extends ResourceSupport implements Comparable<BucketMetadata> {

    @JsonProperty
    private final String owner;

    @JsonProperty
    private final String name;

    @JsonProperty
    private int objectCount;

    @JsonProperty
    private String rights;

    @JsonProperty
    private final String tenant;

    public BucketMetadata(BucketEntity bucket) {
        this.name = bucket.getBucketName();
        this.owner = bucket.getOwner();
        this.objectCount = 0;
        this.tenant = bucket.getTenant();
    }

    public BucketMetadata(BucketEntity bucket, int objectCount) {
        this.name = bucket.getBucketName();
        this.owner = bucket.getOwner();
        this.objectCount = objectCount;
        this.tenant = bucket.getTenant();
    }

    public BucketMetadata(String name, String owner, int objectCount, String tenant) {
        this.name = name;
        this.owner = owner;
        this.objectCount = objectCount;
        this.tenant = tenant;
    }

    public BucketMetadata(String name, String owner) {
        this.name = name;
        this.owner = owner;
        this.objectCount = 0;
        this.tenant = "";
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getTenant() {
        return tenant;
    }

    public void setObjectCount(int objectCount) {
        this.objectCount = objectCount;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return Objects.equals(this.getName(), ((BucketMetadata) o).getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(BucketMetadata bucketMetadata) {
        return getName().compareTo(bucketMetadata.getName());
    }
}
