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

import static com.google.common.truth.Truth.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;


/**
 * @author ActiveEon Team
 */
public class LocalDateTimeAttributeConverterTest {

    private LocalDateTimeAttributeConverter localDateTimeAttributeConverter;

    @Before
    public void setUp() {
        localDateTimeAttributeConverter = new LocalDateTimeAttributeConverter();
    }

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now();

        long localDateTimeEpoch = toEpoch(localDateTime);

        Timestamp timestamp = localDateTimeAttributeConverter.convertToDatabaseColumn(localDateTime);

        long timestampEpoch = timestamp.getTime() / 1000;

        assertThat(localDateTimeEpoch).isEqualTo(timestampEpoch);
    }

    @Test
    public void testConvertToDatabaseColumnShouldReturnNullIfNullParameter() {
        assertThat(localDateTimeAttributeConverter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        Instant now = Instant.now();

        Timestamp timestamp = Timestamp.from(now);
        long timestampEpoch = timestamp.getTime() / 1000;

        LocalDateTime localDateTime = localDateTimeAttributeConverter.convertToEntityAttribute(timestamp);

        long localDateTimeEpoch = toEpoch(localDateTime);

        assertThat(timestampEpoch).isEqualTo(localDateTimeEpoch);
    }

    @Test
    public void testConvertToEntityAttributeShouldReturnNullIfNullParameter() {
        assertThat(localDateTimeAttributeConverter.convertToEntityAttribute(null)).isNull();
    }

    private long toEpoch(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

}
