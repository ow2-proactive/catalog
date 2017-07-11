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
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.util.parser.CatalogObjectParserFactory;
import org.ow2.proactive.catalog.util.parser.CatalogObjectParserInterface;


/**
 * Unit tests associated to {@link org.ow2.proactive.catalog.util.parser.PCWRuleParser}.
 *
 * @author ActiveEon Team
 */
public final class PCWRuleParserTest {
    @Test
    public void testParseCpuRule() throws XMLStreamException {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleExample.json");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "ruleNodeIsUpMetric", "General");
        assertKeyValueDataAre(result.get(1), "PollType", "Ping", "PollConfiguration");
        assertKeyValueDataAre(result.get(2), "pollingPeriodInSeconds", "100", "PollConfiguration");
        assertKeyValueDataAre(result.get(3), "calmPeriodInSeconds", "50", "PollConfiguration");
        assertKeyValueDataAre(result.get(4),
                              "kpis",
                              "[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]",
                              "PollConfiguration");
        assertKeyValueDataAre(result.get(5),
                              "NodeUrls",
                              "[\"localhost\",\"service:jmx:rmi:///jndi/rmi://192.168.1.122:52304/rmnode\"]",
                              "PollConfiguration");

    }

    private static void assertKeyValueDataAre(KeyValueMetadataEntity data, String key, String value, String type) {
        assertTrue(data.getKey().equals(key) && data.getValue().equals(value) && data.getType().equals(type));
    }

    @Test
    public void testParseRuleContainingNoName() throws Exception {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleNoName.json");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "", "General");
        assertKeyValueDataAre(result.get(1), "PollType", "Ping", "PollConfiguration");
        assertKeyValueDataAre(result.get(2), "pollingPeriodInSeconds", "100", "PollConfiguration");
        assertKeyValueDataAre(result.get(3), "calmPeriodInSeconds", "50", "PollConfiguration");
        assertKeyValueDataAre(result.get(4),
                              "kpis",
                              "[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]",
                              "PollConfiguration");
        assertKeyValueDataAre(result.get(5),
                              "NodeUrls",
                              "[\"localhost\",\"service:jmx:rmi:///jndi/rmi://192.168.1.122:52304/rmnode\"]",
                              "PollConfiguration");
    }

    @Test
    public void testParseRuleContainingNoType() throws Exception {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleNoType.json");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "ruleNodeIsUpMetric", "General");
        assertKeyValueDataAre(result.get(1), "PollType", "", "PollConfiguration");
        assertKeyValueDataAre(result.get(2), "pollingPeriodInSeconds", "100", "PollConfiguration");
        assertKeyValueDataAre(result.get(3), "calmPeriodInSeconds", "50", "PollConfiguration");
        assertKeyValueDataAre(result.get(4),
                              "kpis",
                              "[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]",
                              "PollConfiguration");
        assertKeyValueDataAre(result.get(5),
                              "NodeUrls",
                              "[\"localhost\",\"service:jmx:rmi:///jndi/rmi://192.168.1.122:52304/rmnode\"]",
                              "PollConfiguration");
    }

    @Test
    public void testParseRuleContainingNoPollingPeriod() throws Exception {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleNoPollingPeriod.json");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "ruleNodeIsUpMetric", "General");
        assertKeyValueDataAre(result.get(1), "PollType", "Ping", "PollConfiguration");
        assertKeyValueDataAre(result.get(2), "pollingPeriodInSeconds", "0", "PollConfiguration");
        assertKeyValueDataAre(result.get(3), "calmPeriodInSeconds", "50", "PollConfiguration");
        assertKeyValueDataAre(result.get(4),
                              "kpis",
                              "[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]",
                              "PollConfiguration");
        assertKeyValueDataAre(result.get(5),
                              "NodeUrls",
                              "[\"localhost\",\"service:jmx:rmi:///jndi/rmi://192.168.1.122:52304/rmnode\"]",
                              "PollConfiguration");
    }

    @Test
    public void testParseRuleContainingNoUrl() throws Exception {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleNoNodeUrl.json");

        assertThat(result).hasSize(6);
        assertKeyValueDataAre(result.get(0), "name", "ruleNodeIsUpMetric", "General");
        assertKeyValueDataAre(result.get(1), "PollType", "Ping", "PollConfiguration");
        assertKeyValueDataAre(result.get(2), "pollingPeriodInSeconds", "100", "PollConfiguration");
        assertKeyValueDataAre(result.get(3), "calmPeriodInSeconds", "50", "PollConfiguration");
        assertKeyValueDataAre(result.get(4),
                              "kpis",
                              "[\"upAndRunning\",\"sigar:Type=Cpu Total\",\"sigar:Type=FileSystem,Name=/ Free\"]",
                              "PollConfiguration");
        assertKeyValueDataAre(result.get(5), "NodeUrls", "[]", "PollConfiguration");
    }

    @Test(expected = RuntimeException.class)
    public void testParseRuleContainingNoPollingConfiguration() throws Exception {
        List<KeyValueMetadataEntity> result = parseRule("pcwRuleNoPollingConfiguration.json");
    }

    @Test(expected = RuntimeException.class)
    public void testLoadFromNotValidFolderException() throws XMLStreamException {
        parseRule("WronRuleToParse.json");
    }

    private List<KeyValueMetadataEntity> parseRule(String filename) throws XMLStreamException {
        CatalogObjectParserInterface parser = CatalogObjectParserFactory.get().getParser("pcw-rule");

        return parser.parse(ProActiveCatalogObjectParserTest.class.getResourceAsStream("/pcw-rules/" + filename));
    }
}
