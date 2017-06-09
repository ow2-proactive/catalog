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
package org.ow2.proactive.catalog.rest.util.parser;

import java.util.Objects;

import org.ow2.proactive.catalog.rest.entity.KeyValueMetadataEntity;

import com.google.common.collect.ImmutableList;


/**
 *
 * @see CatalogObjectParserInterface
 *
 * @author ActiveEon Team
 */
public final class CatalogObjectParserResult {

    private final String kind;

    private final String projectName;

    private final String name;

    private final ImmutableList<KeyValueMetadataEntity> keyValueList;

    public CatalogObjectParserResult(String kind, String projectName, String name,
            ImmutableList<KeyValueMetadataEntity> keyValueMap) {
        Objects.requireNonNull(keyValueMap);

        this.kind = kind;
        this.projectName = projectName;
        this.name = name;
        this.keyValueList = keyValueMap;
    }

    public String getKind() {
        return kind;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getJobName() {
        return name;
    }

    public ImmutableList<KeyValueMetadataEntity> getKeyValueList() {
        return keyValueList;
    }

}
