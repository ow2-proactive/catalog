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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author ActiveEon Team
 * @since 8/2/2018
 */
public abstract class NameValidator {

    protected String namePattern;

    private Pattern validNamePattern;

    public NameValidator(String namePattern) {
        this.namePattern = namePattern;
        validNamePattern = Pattern.compile(namePattern);
    }

    /**
    * Please check the validator pattern in the specific class implementation of this abstract class
     * @param nameForCheck
     * @return true result if name is valid
     */
    public boolean isValid(String nameForCheck) {
        Matcher matcher = validNamePattern.matcher(nameForCheck);
        return matcher.matches();
    }
}
