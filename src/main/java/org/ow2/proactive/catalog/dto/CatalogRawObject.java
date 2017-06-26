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

import java.util.List;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;

import lombok.EqualsAndHashCode;


/**
 * @author ActiveEon Team
 * @since 19/06/2017
 */
@EqualsAndHashCode(callSuper = true)
public class CatalogRawObject extends CatalogObjectMetadata {

    private final byte[] rawObject;

    public CatalogRawObject(CatalogObjectEntity catalogObject) {
        super(catalogObject);
        this.rawObject = catalogObject.getRevisions().first().getRawObject();
    }

    public CatalogRawObject(CatalogObjectRevisionEntity catalogObject) {
        super(catalogObject);
        this.rawObject = catalogObject.getRawObject();
    }

    public CatalogRawObject(Long bucketId, String name, String kind, String contentType, long createdAt,
            String commitMessage, List<KeyValueMetadata> keyValueMetadataList, byte[] rawObject) {
        super(bucketId, name, kind, contentType, createdAt, commitMessage, keyValueMetadataList);
        this.rawObject = rawObject;
    }

    public byte[] getRawObject() {
        return rawObject;
    }
}
