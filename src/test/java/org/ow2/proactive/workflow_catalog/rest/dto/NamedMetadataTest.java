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

package org.ow2.proactive.workflow_catalog.rest.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author ActiveEon Team
 */
public class NamedMetadataTest {

    @Test
    public void testEquality1() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());

        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testEquality2() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(2L, "name", LocalDateTime.now());

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testEquality3() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name1", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(1L, "name2", LocalDateTime.now());

        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testEquality4() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testEquality5() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(2L, "name", LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testEquality6() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());

        assertThat(a.equals(a)).isTrue();
    }

    @Test
    public void testEquality7() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());

        assertThat(a).isNotEqualTo(null);
    }

    @Test
    public void testEquality8() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());

        assertThat(a).isNotEqualTo(42);
    }

    @Test
    public void testHashCode1() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void testHashCode2() throws Exception {
        CustomNamedMetadata a =
                new CustomNamedMetadata(1L, "name", LocalDateTime.now());
        CustomNamedMetadata b =
                new CustomNamedMetadata(2L, "name", LocalDateTime.now());

        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    private static final class CustomNamedMetadata extends NamedMetadata {

        public CustomNamedMetadata(Long id, String name, LocalDateTime createdAt) {
            super(id, name, createdAt);
        }

    }

}