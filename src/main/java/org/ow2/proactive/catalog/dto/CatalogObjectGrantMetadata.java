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

import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.ow2.proactive.catalog.util.CatalogObjectID;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Data
@Getter
@EqualsAndHashCode(callSuper = false)
public class CatalogObjectGrantMetadata extends ResourceSupport {

    @JsonProperty
    private final String granteeType;

    @JsonProperty
    private final String creator;

    @JsonProperty
    private final String grantee;

    @JsonProperty
    private final String accessType;

    @JsonProperty
    private final int priority;

    @JsonProperty
    private final CatalogObjectID catalogObjectId;

    @JsonProperty
    private final String catalogObjectName;

    @JsonProperty
    private final long catalogObjectBucketId;

    @JsonProperty
    private final String bucketName;

    public CatalogObjectGrantMetadata(String granteeType, String creator, String grantee, String accessType,
            int priority, CatalogObjectID catalogObjectId, String catalogObjectName, long catalogObjectBucketId,
            String bucketName) {
        this.granteeType = granteeType;
        this.creator = creator;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.catalogObjectId = catalogObjectId;
        this.catalogObjectName = catalogObjectName;
        this.catalogObjectBucketId = catalogObjectBucketId;
        this.bucketName = bucketName;
    }

    public CatalogObjectGrantMetadata(CatalogObjectGrantEntity catalogObjectGrantEntity) {
        this.granteeType = catalogObjectGrantEntity.getGranteeType();
        this.creator = catalogObjectGrantEntity.getCreator();
        this.grantee = catalogObjectGrantEntity.getGrantee();
        this.accessType = catalogObjectGrantEntity.getAccessType();
        this.priority = catalogObjectGrantEntity.getPriority();
        if (catalogObjectGrantEntity.getCatalogObject() != null) {
            this.catalogObjectName = catalogObjectGrantEntity.getCatalogObject().getId().getName();
            this.catalogObjectId = new CatalogObjectID(catalogObjectGrantEntity.getCatalogObject()
                                                                               .getId()
                                                                               .getBucketId(),
                                                       catalogObjectGrantEntity.getCatalogObject().getId().getName());
            this.catalogObjectBucketId = catalogObjectGrantEntity.getCatalogObject().getBucket().getId();
            this.bucketName = catalogObjectGrantEntity.getCatalogObject().getBucket().getBucketName();
        } else {
            this.catalogObjectName = "";
            this.catalogObjectId = new CatalogObjectID(1L, "");
            this.catalogObjectBucketId = 1L;
            this.bucketName = "";
        }
    }

}
