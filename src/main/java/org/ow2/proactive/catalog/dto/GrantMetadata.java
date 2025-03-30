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

import static org.ow2.proactive.catalog.util.GrantHelper.TENANT_GRANTEE_TYPE;
import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

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
public class GrantMetadata extends ResourceSupport implements Comparable<GrantMetadata> {

    @JsonProperty
    protected String granteeType = "";

    @JsonProperty
    protected String grantee = "";

    @JsonProperty
    protected String accessType = "";

    @JsonProperty
    protected int priority = 0;

    @JsonProperty
    protected String bucketName;

    @JsonProperty
    protected String creator = "";

    @JsonProperty
    protected long creationDate;

    @JsonProperty
    protected Deque<ModificationHistoryData> modificationHistory;

    public static final int TENANT_PRIORITY = -100;

    public static final int USER_PRIORITY = 100;

    public GrantMetadata() {
    }

    public GrantMetadata(String granteeType, String grantee, String accessType, int priority, String bucketName,
            String creator) {
        this.granteeType = granteeType;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.bucketName = bucketName;
        this.creator = creator;
    }

    public GrantMetadata(String granteeType, String grantee, String accessType, int priority, String creator) {
        this.granteeType = granteeType;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.creator = creator;
    }

    public GrantMetadata(String granteeType, String grantee, String accessType, int priority, String bucketName,
            String creator, long creationDate, Deque<ModificationHistoryData> modificationHistory) {
        this.granteeType = granteeType;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.bucketName = bucketName;
        this.creator = creator;
        this.creationDate = creationDate;
        this.modificationHistory = modificationHistory;
    }

    @Override
    public int compareTo(GrantMetadata o) {
        return this.getComputedPriority() - o.getComputedPriority();
    }

    /**
     * Return a grant priority which takes into consideration the Grantee type
     */
    public int getComputedPriority() {
        switch (this.accessType) {
            case USER_GRANTEE_TYPE:
                return USER_PRIORITY;
            case TENANT_GRANTEE_TYPE:
                return TENANT_PRIORITY;
            default:
                return priority;
        }
    }
}
