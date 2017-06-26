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
package org.ow2.proactive.catalog.rest.controller.validator;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * @author ActiveEon Team
 * @since 19/06/2017
 */
@Component
public class CatalogObjectNameEncodingValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogObjectNamePathParam.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CatalogObjectNamePathParam pathParam = (CatalogObjectNamePathParam) target;
        try {
            String decodedName = URLDecoder.decode(pathParam.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            errors.rejectValue("name path parameter", "name", "name path parameter cannot be decoded in UTF-8");
        }
    }
}
