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
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.specification.AbstractSpecification;
import org.springframework.data.jpa.domain.Specification;


/**
 * @author ActiveEon Team
 * @since 13/07/2017
 */
public abstract class AndOrSpecification extends AbstractSpecification {

    protected List<Specification<CatalogObjectRevisionEntity>> fieldSpecifications;

    public AndOrSpecification(Operations operations, Object value, Join catalogObjectJoin, Join metadataJoin,
            Join bucketJoin, List<Specification<CatalogObjectRevisionEntity>> fieldSpecifications) {
        super(operations, value, catalogObjectJoin, metadataJoin, bucketJoin);
        this.fieldSpecifications = fieldSpecifications;
    }

    @Override
    protected Predicate buildPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
        initMetadataJoin(root, query, cb);
        List<Predicate> predicates = fieldSpecifications.stream().map(spec -> {
            AbstractSpecification abstractSpecification = (AbstractSpecification) spec;
            abstractSpecification.setCatalogObjectJoin(catalogObjectJoin);
            abstractSpecification.setMetadataJoin(metadataJoin);
            return abstractSpecification.toPredicate(root, query, cb);
        }).collect(Collectors.toList());

        return predicate(cb, predicates.toArray(new Predicate[predicates.size()]));
    }

    protected abstract Predicate predicate(CriteriaBuilder cb, Predicate[] predicates);

    public List<Specification<CatalogObjectRevisionEntity>> getFieldSpecifications() {
        return fieldSpecifications;
    }
}
