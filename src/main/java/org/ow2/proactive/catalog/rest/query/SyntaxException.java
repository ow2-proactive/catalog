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
package org.ow2.proactive.catalog.rest.query;

import java.util.List;

import com.google.common.base.Joiner;


/**
 * Exception thrown if one or more syntax errors are detected while
 * compiling a CatalogObject Catalog Query.
 *
 * @see CatalogQueryCompiler
 *
 * @author ActiveEon Team
 */
public class SyntaxException extends Exception {

    private List<SyntaxError> errors;

    public SyntaxException(List<SyntaxError> errors) {
        this.errors = errors;
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(errors);
    }

}
