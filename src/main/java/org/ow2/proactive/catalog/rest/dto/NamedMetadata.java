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
import java.util.Objects;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author ActiveEon Team
 */
public abstract class NamedMetadata extends ResourceSupport {

    @JsonProperty
    public final Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("created_at")
    public final LocalDateTime createdAt;

    public final String name;

    public NamedMetadata(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return Objects.equals(this.id, ((NamedMetadata) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
