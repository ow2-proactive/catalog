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

import java.util.Objects;

import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;

import lombok.Data;


/**
 * @author ActiveEon Team
 */
@Data
public class KeyValueMetadata {

    private final String key;

    private final String value;

    private final String label;

    public KeyValueMetadata(String key, String value, String label) {
        this.key = key;
        this.value = value;
        this.label = label;
    }

    public KeyValueMetadata(KeyValueMetadataEntity entity) {
        this.key = entity.getKey();
        this.value = entity.getValue();
        this.label = entity.getType();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof KeyValueMetadata)) {
            return false;
        }

        KeyValueMetadata that = (KeyValueMetadata) other;

        if (!Objects.equals(key, that.key)) {
            return false;
        }

        return Objects.equals(value, that.value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(key, value);
    }

}
