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
package org.ow2.proactive.catalog.graphql.bean;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.catalog.dto.KeyValueMetadata;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.KeyValueEntityToDtoTransformer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


/**
 * @author ActiveEon Team
 */
@Data
public class CatalogObject {

    @JsonProperty("kind")
    private final String kind;

    @JsonProperty("bucket_id")
    private final Long bucketId;

    @JsonProperty
    private final String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("commit_time")
    private final LocalDateTime commitDateTime;

    @JsonProperty("content_type")
    private final String contentType;

    @JsonProperty("commit_message")
    private final String commitMessage;

    @JsonProperty("object_key_values")
    private final List<KeyValueMetadata> keyValueMetadataList;

    @JsonProperty("content_link")
    private String contentLink;

    public CatalogObject(CatalogObjectEntity catalogObject) {
        this(catalogObject.getId().getBucketId(),
             catalogObject.getId().getName(),
             catalogObject.getKind(),
             catalogObject.getContentType(),
             catalogObject.getRevisions().first().getCommitTime(),
             catalogObject.getRevisions().first().getCommitMessage(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getRevisions().first().getKeyValueMetadataList()));
    }

    public CatalogObject(CatalogObjectRevisionEntity catalogObject) {
        this(catalogObject.getCatalogObject().getId().getBucketId(),
             catalogObject.getCatalogObject().getId().getName(),
             catalogObject.getCatalogObject().getKind(),
             catalogObject.getCatalogObject().getContentType(),
             catalogObject.getCommitTime(),
             catalogObject.getCommitMessage(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getKeyValueMetadataList()));
    }

    public CatalogObject(Long bucketId, String name, String kind, String contentType, long createdAt,
            String commitMessage, List<KeyValueMetadata> keyValueMetadataList) {
        this.bucketId = bucketId;
        this.name = name;
        this.kind = kind;
        this.contentType = contentType;
        this.commitDateTime = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.commitMessage = commitMessage;
        if (keyValueMetadataList == null) {
            this.keyValueMetadataList = new ArrayList<>();
        } else {
            this.keyValueMetadataList = keyValueMetadataList;
        }

    }

}