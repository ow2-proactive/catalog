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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 11/13/2017
 */
@Component
public class BucketNameValidator {
    protected final static String VALID_BUCKET_NAME_PATTERN = "[a-z][a-z0-9-]{1,61}[a-z0-9]";

    private final static Pattern validBucketNamePattern = Pattern.compile(VALID_BUCKET_NAME_PATTERN);

    /**
     * According to this check: the bucket name can be between 3 and 63 characters long, and can contain only lower-case characters, numbers, and dashes.
     A bucket name must start with a lowercase letter and cannot terminate with a dash.
     *
     * @param bucketNameForCheck
     * @return true result if bucket name is valid
     */
    public boolean checkBucketName(String bucketNameForCheck) {
        Matcher matcher = validBucketNamePattern.matcher(bucketNameForCheck);
        return matcher.matches();
    }
}
