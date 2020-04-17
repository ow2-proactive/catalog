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
package org.ow2.proactive.catalog.service.exception;

import org.ow2.proactive.microservices.common.exception.ClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * This Exception is thrown when a CREATE request for object has been
 * received but the object name is not valid, according to next rules:
 * tthe object name should not be empty, should start and finish with non-whitespace character
 * and can contain all characters except forward slash and comma.
 * The HTTP status is 400, "Bad Request"
 *
 * @author ActiveEon Team
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ObjectNameIsNotValidException extends ClientException {

    public ObjectNameIsNotValidException(String objectName) {
        super("The object name: '" + objectName + "' is not valid. " +
              "\n The object name should not be empty, should start and finish with non-whitespace character and can contain all characters except forward slash and comma.");
    }

    public ObjectNameIsNotValidException(Throwable cause) {
        super(cause);
    }

}
