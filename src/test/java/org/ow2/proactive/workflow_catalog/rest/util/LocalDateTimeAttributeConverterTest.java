/*
 *  ProActive Parallel Suite(TM): The Java(TM) library for
 *     Parallel, Distributed, Multi-Core Computing for
 *     Enterprise Grids & Clouds
 *
 *  Copyright (C) 1997-2016 INRIA/University of
 *                  Nice-Sophia Antipolis/ActiveEon
 *  Contact: proactive@ow2.org or contact@activeeon.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; version 3 of
 *  the License.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *  USA
 *
 *  If needed, contact us to obtain a release under GPL Version 2 or 3
 *  or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                          http://proactive.inria.fr/team_members.htm
 */

package org.ow2.proactive.workflow_catalog.rest.util;

import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.google.common.truth.Truth.assertThat;

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

        Timestamp timestamp =
                localDateTimeAttributeConverter.convertToDatabaseColumn(
                        localDateTime);

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
        return localDateTime.atZone(
                ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

}