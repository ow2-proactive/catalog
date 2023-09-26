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
 * received but the tags list is not valid, according to next rules:
 * The tags string is a comma separated list and cannot terminate with a comma.
 * A single tag can contain only letters, numbers and set of next characters _.-/,
 * A single tag cannot start with a number and shorter than 2 characters.
 * The HTTP status is 400, "Bad Request"
 *
 * @author ActiveEon Team
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TagsIsNotValidException extends ClientException {

    public TagsIsNotValidException(String parameterName, String parameterType) {
        super("The " + parameterType + " name: '" + parameterName +
              "' is not valid, please check the specification of the " + parameterType + " parameter naming");
    }

    public TagsIsNotValidException(Throwable cause) {
        super(cause);
    }

}
