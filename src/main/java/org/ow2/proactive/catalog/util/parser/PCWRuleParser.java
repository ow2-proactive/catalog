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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.util.parser.pcw.rule.model.PollConfiguration;
import org.ow2.proactive.catalog.util.parser.pcw.rule.model.Rule;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;


/**
 * DefaultObjectParser is the default parser for object.
 *
 * @author ActiveEon Team
 */
@Log4j2
@Component

public final class PCWRuleParser extends AbstractCatalogObjectParser {

    private static final String POLL_CONFIGURATION_LABEL = "PollConfiguration";

    private static final ObjectMapper mapper = new ObjectMapper();

    public List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) {

        List<KeyValueLabelMetadataEntity> keyValueMetadataEntities = new ArrayList<>();

        Rule pcwRule = parseToPCWRuleContent(inputStream);

        PollConfiguration pollConfiguration = checkAndGetPollConfiguration(pcwRule);

        String pcwRuleName = ((pcwRule.getName() == null) ? "" : pcwRule.getName());
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("name", pcwRuleName, GENERAL_LABEL));

        String pcwPollType = ((pollConfiguration.getPollType() == null) ? "" : pollConfiguration.getPollType());
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("PollType",
                                                                     pcwPollType,
                                                                     POLL_CONFIGURATION_LABEL));
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("pollingPeriodInSeconds",
                                                                     String.valueOf(pollConfiguration.getPollingPeriodInSeconds()),
                                                                     POLL_CONFIGURATION_LABEL));
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("calmPeriodInSeconds",
                                                                     String.valueOf(pollConfiguration.getCalmPeriodInSeconds()),
                                                                     POLL_CONFIGURATION_LABEL));

        String nodeUrlsListAsJson = getNodeUrlsAsJsonListToString(pollConfiguration);
        String kpisListAsJson = getKpisAsJsonListToString(pollConfiguration);
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("kpis", kpisListAsJson, POLL_CONFIGURATION_LABEL));
        keyValueMetadataEntities.add(new KeyValueLabelMetadataEntity("NodeUrls",
                                                                     nodeUrlsListAsJson,
                                                                     POLL_CONFIGURATION_LABEL));

        return keyValueMetadataEntities;
    }

    @Override
    public boolean isMyKind(String kind) {
        return kind.toLowerCase().startsWith(SupportedParserKinds.PCW_RULE.toString().toLowerCase());
    }

    @Override
    public String getIconPath(List<KeyValueLabelMetadataEntity> keyValueMetadataEntities) {
        return SupportedParserKinds.PCW_RULE.getDefaultIcon();
    }

    private Rule parseToPCWRuleContent(InputStream inputStream) {
        try {
            return mapper.readValue(inputStream, Rule.class);
        } catch (IOException e) {
            throw new RuntimeException("Problem to parse the pcw-rule", e);
        }
    }

    private PollConfiguration checkAndGetPollConfiguration(Rule rule) {
        PollConfiguration pollConfiguration = rule.getPollConfiguration();
        if (pollConfiguration == null) {
            throw new RuntimeException("Poll Configuration is missing in the pcw-rule: " + rule.getName());
        }
        return pollConfiguration;
    }

    private String getNodeUrlsAsJsonListToString(PollConfiguration pollConfiguration) {
        List<String> nodeUrlsList = new LinkedList<>();
        pollConfiguration.getNodeInformations()
                         .stream()
                         .forEach(nodeInformation -> nodeUrlsList.add(nodeInformation.getUrl()));

        String nodeUrlsListAsJson = "[]";
        try {
            nodeUrlsListAsJson = mapper.writeValueAsString(nodeUrlsList);
        } catch (JsonProcessingException e) {
            log.error("Unable to process node urls pcw-rule parameters to json string", e);
        }
        return nodeUrlsListAsJson;
    }

    private String getKpisAsJsonListToString(PollConfiguration pollConfiguration) {
        String kpisListAsJson = "[]";
        try {
            kpisListAsJson = mapper.writeValueAsString(pollConfiguration.getKpis());
        } catch (JsonProcessingException e) {
            log.error("Unable to process kpis pcw-rule parameters to json string", e);
        }
        return kpisListAsJson;
    }

}
