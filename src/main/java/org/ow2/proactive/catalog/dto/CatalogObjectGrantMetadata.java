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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Getter
@EqualsAndHashCode(callSuper = false)
public class CatalogObjectGrantMetadata extends ResourceSupport {

    @JsonProperty
    private final String grantee;

    @JsonProperty
    private final String creator;

    @JsonProperty
    private final String profiteer;

    @JsonProperty
    private final String accessType;

    @JsonProperty
    private final long catalogObjectId;

    @JsonProperty
    private final long catalogObjectBucketId;

    public CatalogObjectGrantMetadata(String grantee, String creator, String profiteer, String accessType,
                                      long catalogObjectId, long catalogObjectBucketId) {
        this.grantee = grantee;
        this.creator = creator;
        this.profiteer = profiteer;
        this.accessType = accessType;
        this.catalogObjectId = catalogObjectId;
        this.catalogObjectBucketId = catalogObjectBucketId;
    }

    public CatalogObjectGrantMetadata(CatalogObjectGrantEntity catalogObjectGrantEntity) {
        this.grantee = catalogObjectGrantEntity.getGrantee();
        this.creator = catalogObjectGrantEntity.getCreator();
        this.profiteer = catalogObjectGrantEntity.getProfiteer();
        this.accessType = catalogObjectGrantEntity.getAccessType();
        this.catalogObjectId = catalogObjectGrantEntity.getCatalogObjectRevisionEntity().getId();
        this.catalogObjectBucketId = catalogObjectGrantEntity.getBucketEntity().getId();
    }

}
