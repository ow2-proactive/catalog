/*
 *  ProActive Parallel Suite(TM): The Java(TM) library for
 *     Parallel, Distributed, Multi-Core Computing for
 *     Enterprise Grids & Clouds
 *
 *  Copyright (C) 1997-2016 INRIA/University of
 *                  Nice-Sophia Antipolis/ActiveEon
 *  Contact: proactive@ow2.org or contact@activeeon.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; version 3 of
 *  the License.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *  USA
 *
 *  If needed, contact us to obtain a release under GPL Version 2 or 3
 *  or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                          http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.query;

import java.util.Set;

import org.ow2.proactive.workflow_catalog.rest.entity.QGenericInformation;
import org.ow2.proactive.workflow_catalog.rest.entity.QVariable;
import com.google.common.base.MoreObjects;
import com.mysema.query.types.Predicate;

/**
 * PredicateContext keeps the context associated to a query predicate.
 * Mainly the QueryDSL predicate and the aliases used.
 *
 * @author ActiveEon Team
 */
public final class PredicateContext {

    private final Predicate predicate;

    private final Set<QGenericInformation> qGenericInformation;

    private final Set<QVariable> qVariables;

    public PredicateContext(Predicate predicate, Set<QGenericInformation> qGenericInformation, Set<QVariable> qVariables) {
        this.predicate = predicate;
        this.qGenericInformation = qGenericInformation;
        this.qVariables = qVariables;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Set<QGenericInformation> getGenericInformationAliases() {
        return qGenericInformation;
    }

    public Set<QVariable> getVariableAliases() {
        return qVariables;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("predicate", predicate)
                .add("qGenericInformation", qGenericInformation)
                .add("qVariables", qVariables).toString();
    }

}