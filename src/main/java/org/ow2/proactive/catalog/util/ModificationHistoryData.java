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

import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class ModificationHistoryData implements Serializable {

    private long modificationDate;

    private String username;

    private String oldValues;

    private String newValues;

    private String changes;

    public ModificationHistoryData(long modificationDate, String username) {
        this.modificationDate = modificationDate;
        this.username = username;
    }

    public void computeChanges(String granteeType, String oldValue, String newValue) {
        List<String> oldValues = Stream.of(oldValue.split("/")).collect(Collectors.toList());
        List<String> newValues = Stream.of(newValue.split("/")).collect(Collectors.toList());
        setChanges(granteeType, oldValues, newValues);
    }

    private void setChanges(String granteeType, List<String> oldValues, List<String> newValues) {
        List<String> values = Streams.zip(oldValues.stream(),
                                          newValues.stream(),
                                          (oldValue, newValue) -> oldValue + " => " + newValue)
                                     .collect(Collectors.toList());
        if (granteeType.equals(USER_GRANTEE_TYPE)) {
            this.changes = "Right: " + values.get(0);
        } else {
            this.changes = "Right: " + values.get(0) + ", Priority: " + values.get(1);
        }
    }
}
