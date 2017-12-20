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

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.KeyValueEntityToDtoTransformer;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author ActiveEon Team
 */
@Data
@NoArgsConstructor
public class CatalogObject {

    private String kind;

    private String bucketName;

    private String name;

    private long commitDateTime;

    private String contentType;

    private String commitMessage;

    private List<Metadata> metadata;

    private String link;

    public CatalogObject(CatalogObjectEntity catalogObject) {
        this(catalogObject.getBucket().getBucketName(),
             catalogObject.getId().getName(),
             catalogObject.getKind(),
             catalogObject.getContentType(),
             catalogObject.getRevisions().first().getCommitTime(),
             catalogObject.getRevisions().first().getCommitMessage(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getRevisions().first().getKeyValueMetadataList()));
    }

    public CatalogObject(CatalogObjectRevisionEntity catalogObject) {
        this(catalogObject.getCatalogObject().getBucket().getBucketName(),
             catalogObject.getCatalogObject().getId().getName(),
             catalogObject.getCatalogObject().getKind(),
             catalogObject.getCatalogObject().getContentType(),
             catalogObject.getCommitTime(),
             catalogObject.getCommitMessage(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getKeyValueMetadataList()));
    }

    public CatalogObject(String bucketName, String name, String kind, String contentType, long createdAt,
            String commitMessage, List<Metadata> metadataList) {
        this.bucketName = bucketName;
        this.name = name;
        this.kind = kind;
        this.contentType = contentType;
        this.commitDateTime = createdAt;
        this.commitMessage = commitMessage;
        if (metadataList == null) {
            this.metadata = new ArrayList<>();
        } else {
            this.metadata = metadataList;
        }
    }
}
