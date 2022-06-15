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

import java.util.Deque;

import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.ow2.proactive.catalog.util.ModificationHistoryData;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Data
@Getter
@EqualsAndHashCode(callSuper = false)
public class BucketGrantMetadata extends GrantMetadata {

    @JsonProperty
    private final long bucketId;

    public BucketGrantMetadata(BucketGrantEntity bucketGrantEntity) {
        super(bucketGrantEntity.getGranteeType(),
              bucketGrantEntity.getGrantee(),
              bucketGrantEntity.getAccessType(),
              bucketGrantEntity.getPriority(),
              bucketGrantEntity.getBucketEntity().getBucketName(),
              bucketGrantEntity.getCreator(),
              bucketGrantEntity.getCreationDate(),
              bucketGrantEntity.getModificationHistory());

        this.bucketId = bucketGrantEntity.getBucketEntity().getId();
    }

    public BucketGrantMetadata(String granteeType, String creator, String grantee, String accessType, int priority,
            long bucketId, String bucketName) {
        super(granteeType, grantee, accessType, priority, bucketName, creator);
        this.bucketId = bucketId;
    }
}
