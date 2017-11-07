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
package org.ow2.proactive.catalog.repository.specification.catalogobject;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.springframework.data.jpa.domain.Specification;

import lombok.Builder;
import lombok.Getter;


/**
 * @author ActiveEon Team
 * @since 06/07/2017
 */
@Getter
public class OrSpecification extends AndOrSpecification {

    @Builder
    public OrSpecification(Operations operations, Object value, Join catalogObjectJoin, Join metadataJoin,
            Join bucketJoin, List<Specification<CatalogObjectRevisionEntity>> fieldSpecifications) {
        super(operations, value, catalogObjectJoin, metadataJoin, bucketJoin, fieldSpecifications);
    }

    @Override
    protected Predicate predicate(CriteriaBuilder cb, Predicate[] predicates) {
        return cb.or(predicates);
    }
}
