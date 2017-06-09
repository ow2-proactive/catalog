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
package org.ow2.proactive.catalog.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.ow2.proactive.catalog.rest.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.rest.util.KeyValueEntityToDtoTransformer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author ActiveEon Team
 */
public final class CatalogObjectMetadata extends NamedMetadata {

    @JsonProperty("kind")
    public final String kind;

    @JsonProperty("bucket_id")
    public final Long bucketId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("commit_date")
    public final LocalDateTime commitDate;

    @JsonProperty("content_type")
    public final String contentType;

    @JsonProperty("commit_id")
    public final Long commitId;

    @JsonProperty("commit_message")
    public final String commitMessage;

    @JsonProperty("object_key_values")
    public final List<KeyValueMetadata> keyValueMetadataList;

    public CatalogObjectMetadata(CatalogObjectRevisionEntity catalogObjectRevision) {
        this(catalogObjectRevision.getKind(),
             catalogObjectRevision.getBucketId(),
             catalogObjectRevision.getCatalogObject().getId(),
             catalogObjectRevision.getCommitDate(),
             catalogObjectRevision.getName(),
             catalogObjectRevision.getContentType(),
             catalogObjectRevision.getCommitId(),
             catalogObjectRevision.getCommitMessage(),
             KeyValueEntityToDtoTransformer.to(catalogObjectRevision.getKeyValueMetadataList()));
    }

    public CatalogObjectMetadata(String kind, Long bucketId, Long id, LocalDateTime createdAt, String name,
            String contentType, Long commitId, String commitMessage, List<KeyValueMetadata> keyValueMetadataList) {

        super(id, name);
        this.kind = kind;
        this.contentType = contentType;
        this.commitId = commitId;
        this.commitDate = createdAt;
        this.bucketId = bucketId;
        this.commitMessage = commitMessage;
        this.keyValueMetadataList = keyValueMetadataList;
    }

}
