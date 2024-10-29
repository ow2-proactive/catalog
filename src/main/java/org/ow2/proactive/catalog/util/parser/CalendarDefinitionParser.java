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
package org.ow2.proactive.catalog.util.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;


/**
 * DefaultObjectParser is the default parser for object.
 *
 * @author ActiveEon Team
 */
@Log4j2
@Component

public final class CalendarDefinitionParser extends AbstractCatalogObjectParser {

    public static final String DESCRIPTION_KEY = "description";

    @Override
    public boolean isMyKind(String kind) {
        return kind.toLowerCase().startsWith(SupportedParserKinds.CALENDAR.toString().toLowerCase());
    }

    @Override
    public String getIconPath(List<KeyValueLabelMetadataEntity> keyValueMetadataEntities) {
        return SupportedParserKinds.CALENDAR.getDefaultIcon();
    }

    @Override
    List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<KeyValueLabelMetadataEntity> result = new ArrayList<>(1);
        try {
            Map<String, String> parse = objectMapper.readValue(inputStream, Map.class);
            if (parse.containsKey(DESCRIPTION_KEY)) {
                result.add(new KeyValueLabelMetadataEntity(DESCRIPTION_KEY, parse.get(DESCRIPTION_KEY), "General"));
            }
        } catch (Exception e) {
            log.warn("Error when reading calendar's raw object (JSON). Returning object without description");
        }
        return result;
    }
}
