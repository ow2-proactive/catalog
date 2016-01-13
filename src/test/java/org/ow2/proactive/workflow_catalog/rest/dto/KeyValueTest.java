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

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author ActiveEon Team
 */
public class KeyValueTest {

    @Test
    public void testEquality1() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value");
        CustomKeyValue b = new CustomKeyValue("key", "value");

        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testEquality2() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key1", "value");
        CustomKeyValue b = new CustomKeyValue("key2", "value");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testEquality3() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value1");
        CustomKeyValue b = new CustomKeyValue("key", "value2");

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    public void testEquality4() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value");

        assertThat(a.equals(a)).isTrue();
    }

    @Test
    public void testEquality5() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value");

        assertThat(a).isNotEqualTo(null);
    }

    @Test
    public void testEquality6() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value");

        assertThat(a).isNotEqualTo(42);
    }

    @Test
    public void testHashCode1() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value");
        CustomKeyValue b = new CustomKeyValue("key", "value");

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void testHashCode2() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key1", "value");
        CustomKeyValue b = new CustomKeyValue("key2", "value");

        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    @Test
    public void testHashCode3() throws Exception {
        CustomKeyValue a = new CustomKeyValue("key", "value1");
        CustomKeyValue b = new CustomKeyValue("key", "value2");

        assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
    }

    private static final class CustomKeyValue extends KeyValue {

        public CustomKeyValue(String key, String value) {
            super(key, value);
        }

    }

}