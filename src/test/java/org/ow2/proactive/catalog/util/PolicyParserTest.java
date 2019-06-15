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

import org.junit.Test;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.PolicyParser;
import org.ow2.proactive.catalog.util.parser.SupportedParserKinds;

import com.google.common.collect.Lists;


public class PolicyParserTest {

    private AbstractCatalogObjectParser parser = new PolicyParser();

    private SupportedParserKinds kind = SupportedParserKinds.POLICY;

    @Test
    public void testIsMyKind() {
        assertThat(parser.isMyKind("")).isFalse();
        assertThat(parser.isMyKind(kind.toString())).isTrue();
        assertThat(parser.isMyKind(kind.toString() + "/12343534563456346346")).isTrue();
        assertThat(parser.isMyKind("sfdfasfa")).isFalse();
    }

    @Test
    public void testGetIconPath() {
        assertThat(parser.getIconPath(Lists.newArrayList())).isEqualTo(kind.getDefaultIcon());

    }

}
