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
package org.ow2.proactive.catalog.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;


/**
 * @author ActiveEon Team
 * @since 4/25/2017
 */
public final class KeyValueEntityToDtoTransformer {

    public static List<Metadata> to(Collection<? extends KeyValueMetadataEntity> from) {
        return from.stream()
                   .map(entity -> new Metadata(entity.getKey(), entity.getValue(), entity.getLabel()))
                   .collect(Collectors.toList());
    }
}
