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

public class AccessTypeHelper {

    /**
     * Check whether currentAccessType satisfies requiredAccessType, i.e., whether the current access type is larger than required access type
     * @param currentAccessType the access type to check
     * @param requiredAccessType the target access type
     * @return true when the current access type satisfies the required access type
     */
    public static boolean satisfy(String currentAccessType, String requiredAccessType) {
        // currentAccessType satisfies requiredAccessType when currentAccessType > requiredAccessType (Note: as defined by the enum AccessType order, admin > write > read > noAccess)
        return AccessType.valueOf(currentAccessType).compareTo(AccessType.valueOf(requiredAccessType)) > 0;
    }

    /**
     * Compare two access type (as defined by the enum AccessType order: admin > write > read > noAccess)
     * @param accessType1 first access type
     * @param accessType2 second access type
     * @return 1 if the first access type is higher than the second, otherwise return -1
     */
    public static int compare(AccessType accessType1, AccessType accessType2) {
        return accessType1.compareTo(accessType2);
    }

    /**
     * Compare two access type (as defined by the enum AccessType order: admin > write > read > noAccess)
     * @param accessType1 first access type as String
     * @param accessType2 second access type as String
     * @return 1 if the first access type is higher than the second, otherwise return -1
     */
    public static int compare(String accessType1, String accessType2) {
        return AccessType.valueOf(accessType1).compareTo(AccessType.valueOf(accessType2));
    }
}
