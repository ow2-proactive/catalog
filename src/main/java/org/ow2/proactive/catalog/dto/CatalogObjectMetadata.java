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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.KeyValueEntityToDtoTransformer;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author ActiveEon Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CatalogObjectMetadata extends ResourceSupport {

    @JsonProperty("commit_time_raw")
    protected final String commitTimeRaw;

    @JsonProperty("kind")
    protected final String kind;

    @JsonProperty("bucket_name")
    protected final String bucketName;

    @JsonProperty
    protected final String name;

    @JsonProperty("project_name")
    protected final String projectName;

    @JsonProperty
    protected final String extension;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("commit_time")
    protected final LocalDateTime commitDateTime;

    @JsonProperty("content_type")
    protected final String contentType;

    @JsonProperty("commit_message")
    protected final String commitMessage;

    @JsonProperty("username")
    protected final String username;

    @JsonProperty("object_key_values")
    protected final List<Metadata> metadataList;

    public CatalogObjectMetadata(CatalogObjectEntity catalogObject) {
        this(catalogObject.getBucket().getBucketName(),
             catalogObject.getId().getName(),
             catalogObject.getRevisions().first().getProjectName(),
             catalogObject.getKind(),
             catalogObject.getContentType(),
             catalogObject.getRevisions().first().getCommitTime(),
             catalogObject.getRevisions().first().getCommitMessage(),
             catalogObject.getRevisions().first().getUsername(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getRevisions().first().getKeyValueMetadataList()),
             catalogObject.getExtension());
    }

    public CatalogObjectMetadata(CatalogObjectRevisionEntity catalogObject) {
        this(catalogObject.getCatalogObject().getBucket().getBucketName(),
             catalogObject.getCatalogObject().getId().getName(),
             catalogObject.getProjectName(),
             catalogObject.getCatalogObject().getKind(),
             catalogObject.getCatalogObject().getContentType(),
             catalogObject.getCommitTime(),
             catalogObject.getCommitMessage(),
             catalogObject.getUsername(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getKeyValueMetadataList()),
             catalogObject.getCatalogObject().getExtension());
    }

    public CatalogObjectMetadata(String bucketName, String name, String projectName, String kind, String contentType,
            long commitTime, String commitMessage, String username, List<Metadata> metadataList, String extension) {
        this.bucketName = bucketName;
        this.name = name;
        this.kind = kind;
        this.contentType = contentType;
        this.commitTimeRaw = String.valueOf(commitTime);
        this.commitDateTime = Instant.ofEpochMilli(commitTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.commitMessage = commitMessage;
        this.username = username;
        if (metadataList == null) {
            this.metadataList = new ArrayList<>();
        } else {
            this.metadataList = metadataList;
        }
        if (projectName != null && !projectName.isEmpty()) {
            this.projectName = projectName;
        } else {
            this.projectName = getProjectNameIfExistsOrEmptyString();
        }
        this.extension = extension;

    }

    private String getProjectNameIfExistsOrEmptyString() {
        Optional<Metadata> projectNameIfExists = metadataList.stream()
                                                             .filter(property -> property.getKey()
                                                                                         .equals("project_name"))
                                                             .findAny();
        return projectNameIfExists.map(Metadata::getValue).orElse("");
    }

}
