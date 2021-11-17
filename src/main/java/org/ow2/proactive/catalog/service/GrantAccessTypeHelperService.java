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
package org.ow2.proactive.catalog.service;

import static org.ow2.proactive.catalog.util.AccessType.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Service
@Transactional
public class GrantAccessTypeHelperService {

    public boolean compareGrantAccessType(String currentAccessType, String requiredAccessType) {
        // If user grant exists over this bucket, check the access type
        // else check the access type of the user group grant over this bucket
        // if both case are false ==> no grants assigned to this user over this bucket
        if (requiredAccessType.equals(admin.toString())) {
            return currentAccessType.equals(admin.toString());
        } else if (requiredAccessType.equals(write.toString())) {
            return currentAccessType.equals(write.toString()) || currentAccessType.equals(admin.toString());
        } else if (requiredAccessType.equals(read.toString())) {
            return currentAccessType.equals(read.toString()) || currentAccessType.equals(write.toString()) ||
                   currentAccessType.equals(admin.toString());
        } else {
            return false;
        }
    }

    /**
     *
     * @param accessType1 first access type
     * @param accessType2 second access type
     * @return 1 if the first access type is higher than the second and 2 in opposite case
     */
    public int getPriorityLevel(String accessType1, String accessType2) {
        int priority = 1;
        if (accessType1.equals(read.toString()) &&
            (accessType2.equals(admin.toString()) || accessType2.equals(write.toString()))) {
            priority = 2;
        } else if (accessType1.equals(write.toString()) && accessType2.equals(admin.toString())) {
            priority = 2;
        }
        return priority;
    }
}
