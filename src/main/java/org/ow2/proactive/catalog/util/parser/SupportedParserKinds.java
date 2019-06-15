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

/**
 * @author ActiveEon Team
 * @since 7/11/2017
 */
public enum SupportedParserKinds {
    WORKFLOW("workflow", "/automation-dashboard/styles/patterns/img/wf-icons/wf-default-icon.png"),
    CALENDAR("calendar", "/automation-dashboard/styles/patterns/img/objects-icons/calendar_icon.png"),
    SCRIPT("script", "/automation-dashboard/styles/patterns/img/objects-icons/script_icon.png"),
    NODE_SOURCE("nodesource", "/automation-dashboard/styles/patterns/img/objects-icons/node_source_icon.png"),
    POLICY("policynodesource", "/automation-dashboard/styles/patterns/img/objects-icons/node_source_policy_icon.png"),
    INFRASTRUCTURE(
            "infrastructurenodesource",
            "/automation-dashboard/styles/patterns/img/objects-icons/node_source_infrastructure_icon.png"),
    PCW_RULE("rule", "/automation-dashboard/styles/patterns/img/objects-icons/rule_icon.png");

    private final String kind;

    private final String defaultIcon;

    SupportedParserKinds(final String kind, final String defaultIcon) {
        this.kind = kind;
        this.defaultIcon = defaultIcon;
    }

    @Override
    public String toString() {
        return kind;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

}
