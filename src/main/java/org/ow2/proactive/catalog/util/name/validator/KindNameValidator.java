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
package org.ow2.proactive.catalog.util.name.validator;

import org.springframework.stereotype.Component;


/**
 * According to this validator: the kind name can be between 1 and 61 characters long, and can contain only letters, numbers, underscores, dotes and dashes.
 A kind names can be separated by slash symbol.
 */
@Component
public class KindNameValidator extends NameValidator {
    protected static final String VALID_KIND_NAME_PATTERN = "^([a-zA-Z0-9-_\\. ]{1,61}\\/?)+$";

    public KindNameValidator() {
        super(VALID_KIND_NAME_PATTERN);
    }
}
