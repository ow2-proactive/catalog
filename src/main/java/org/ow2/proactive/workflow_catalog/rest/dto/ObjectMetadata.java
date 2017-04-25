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
package org.ow2.proactive.workflow_catalog.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.ow2.proactive.workflow_catalog.rest.entity.CatalogObjectRevision;
import org.ow2.proactive.workflow_catalog.rest.util.KeyValueEntityToDtoTransformer;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author ActiveEon Team
 */
public final class ObjectMetadata extends NamedMetadata {

    @JsonProperty("kind")
    public final String kind;

    @JsonProperty("revision_id")
    public final Long revisionId;

    @JsonProperty("bucket_id")
    public final Long bucketId;

    @JsonProperty("project_name")
    public final String projectName;

    @JsonProperty("layout")
    public final String layout;

    @JsonProperty("object_key_values")
    public final List<KeyValueMetadata> keyValueMetadataList;

    public ObjectMetadata(CatalogObjectRevision catalogObjectRevision) {
        this(catalogObjectRevision.getKind(),
             catalogObjectRevision.getBucketId(),
             catalogObjectRevision.getCatalogObject().getId(),
             catalogObjectRevision.getCreatedAt(),
             catalogObjectRevision.getName(),
             catalogObjectRevision.getProjectName(),
             catalogObjectRevision.getLayout(),
             catalogObjectRevision.getRevisionId(),
             KeyValueEntityToDtoTransformer.to(catalogObjectRevision.getKeyValueMetadataList()));
    }

    public ObjectMetadata(String kind, Long bucketId, Long id, LocalDateTime createdAt, String name,
                          String projectName, String layout, Long revisionId, List<KeyValueMetadata> keyValueMetadataList) {

        super(id, name, createdAt);
        this.kind = kind;
        this.layout = layout;
        this.revisionId = revisionId;
        this.bucketId = bucketId;
        this.projectName = projectName;
        this.keyValueMetadataList = keyValueMetadataList;
    }

}
