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
package org.ow2.proactive.catalog.graphql.handler.catalogobject;

import java.util.Optional;

import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.graphql.bean.filter.CatalogObjectNameWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.filter.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.metamodel.CatalogObjectEntityMetaModelEnum;
import org.ow2.proactive.catalog.repository.specification.catalogobject.StringEqNeSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 14/06/2017
 */
@Component
public class CatalogObjectNameFilterHandler implements FilterHandler<CatalogObjectWhereArgs, CatalogObjectEntity> {

    @Override
    public Optional<Specification<CatalogObjectEntity>> handle(CatalogObjectWhereArgs whereArgs) {

        if (whereArgs.getNameArgs() != null) {
            CatalogObjectNameWhereArgs nameWhereArgs = whereArgs.getNameArgs();
            if (nameWhereArgs.getEq() != null) {
                return Optional.of(StringEqNeSpecification.builder()
                                                          .entityMetaModelEnum(CatalogObjectEntityMetaModelEnum.NAME)
                                                          .operations(Operations.EQ)
                                                          .value(nameWhereArgs.getEq())
                                                          .build());
            }
            if (nameWhereArgs.getNe() != null) {
                return Optional.of(StringEqNeSpecification.builder()
                                                          .entityMetaModelEnum(CatalogObjectEntityMetaModelEnum.NAME)
                                                          .operations(Operations.NE)
                                                          .value(nameWhereArgs.getEq())
                                                          .build());
            }
            if (nameWhereArgs.getLike() != null) {
                return Optional.of(StringEqNeSpecification.builder()
                                                          .entityMetaModelEnum(CatalogObjectEntityMetaModelEnum.NAME)
                                                          .operations(Operations.LIKE)
                                                          .value(nameWhereArgs.getEq())
                                                          .build());
            }
        }
        return Optional.empty();
    }
}
