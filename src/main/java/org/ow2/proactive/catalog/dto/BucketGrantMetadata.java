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

import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;


public class BucketGrantMetadata extends ResourceSupport {

    @JsonProperty
    private final String grantee;

    @JsonProperty
    private final String creator;

    @JsonProperty
    private final String profiteer;

    @JsonProperty
    private final String accessType;

    @JsonProperty
    private final long bucketId;

    public BucketGrantMetadata(BucketGrantEntity bucketGrantEntity) {
        this.grantee = bucketGrantEntity.getGrantee();
        this.creator = bucketGrantEntity.getCreator();
        this.profiteer = bucketGrantEntity.getProfiteer();
        this.accessType = bucketGrantEntity.getAccessType();
        this.bucketId = bucketGrantEntity.getBucketEntity().getId();
    }

    public BucketGrantMetadata(String grantee, String creator, String profiteer, String accessType, long bucketId) {
        this.grantee = grantee;
        this.creator = creator;
        this.profiteer = profiteer;
        this.accessType = accessType;
        this.bucketId = bucketId;
    }

    public String getGrantee() {
        return this.grantee;
    }

    public String getProfiteer() {
        return profiteer;
    }

    public String getCreator() {
        return creator;
    }

    public String getAccessType() {
        return accessType;
    }

    public long getBucketId() {
        return bucketId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BucketGrantMetadata that = (BucketGrantMetadata) o;
        return Objects.equals(this.getBucketId(), that.getBucketId()) &&
               Objects.equals(this.getProfiteer(), that.getProfiteer()) &&
               Objects.equals(this.getAccessType(), that.getAccessType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), grantee, profiteer, accessType, bucketId);
    }
}
