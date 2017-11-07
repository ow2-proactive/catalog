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
package org.ow2.proactive.catalog.repository.specification;

import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.repository.entity.metamodel.CatalogObjectEntityMetaModelEnum;
import org.springframework.data.jpa.domain.Specification;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author ActiveEon Team
 * @since 13/07/2017
 */
@AllArgsConstructor
@Data
public abstract class AbstractSpecification<T> implements Specification<CatalogObjectRevisionEntity> {

    protected Operations operations;

    protected T value;

    protected Join<CatalogObjectRevisionEntity, CatalogObjectEntity> catalogObjectJoin;

    protected Join<CatalogObjectRevisionEntity, KeyValueLabelMetadataEntity> metadataJoin;

    protected Join<Join, BucketEntity> bucketEntityJoin;

    protected void initCatalogObjectJoin(Root<CatalogObjectRevisionEntity> root, CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        if (root.getJoins().size() == 0) {
            catalogObjectJoin = getOrCreateJoin(root, "catalogObject");
            Predicate revisionPredicate = cb.equal(root.get(CatalogObjectEntityMetaModelEnum.COMMIT_TIME.getName()),
                                                   catalogObjectJoin.get(CatalogObjectEntityMetaModelEnum.LAST_COMMIT_TIME.getName()));

            catalogObjectJoin.on(revisionPredicate);
            query.distinct(true);
        }
    }

    protected void initMetadataJoin(Root<CatalogObjectRevisionEntity> root, CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        catalogObjectJoin = getOrCreateJoin(root, "catalogObject");
        metadataJoin = getOrCreateJoin(root, "keyValueMetadataList");
    }

    protected void initBucketJoin(Root<CatalogObjectRevisionEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        catalogObjectJoin = getOrCreateJoin(root, "catalogObject");
        bucketEntityJoin = getOrCreateJoin(catalogObjectJoin, "bucket");
    }

    private Join getOrCreateJoin(From from, String joinName) {
        Optional<Join<CatalogObjectRevisionEntity, ?>> joinOptional = from.getJoins()
                                                                          .stream()
                                                                          .filter(join -> ((Join) join).getAttribute()
                                                                                                       .getName()
                                                                                                       .equals(joinName))
                                                                          .findFirst();
        if (joinOptional.isPresent()) {
            return joinOptional.get();
        }
        return from.join(joinName, JoinType.INNER);
    }

    @Override
    public Predicate toPredicate(Root<CatalogObjectRevisionEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        initCatalogObjectJoin(root, query, cb);
        return buildPredicate(root, query, cb);
    }

    protected abstract Predicate buildPredicate(Root<CatalogObjectRevisionEntity> root, CriteriaQuery<?> query,
            CriteriaBuilder cb);
}
