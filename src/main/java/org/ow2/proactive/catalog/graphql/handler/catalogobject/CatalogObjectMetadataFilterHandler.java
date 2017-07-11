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

import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectMetadataArgs;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.specification.catalogobject.KeyValueSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 05/07/2017
 */
@Component
public class CatalogObjectMetadataFilterHandler implements FilterHandler<CatalogObjectWhereArgs, CatalogObjectEntity> {

    @Override
    public Optional<Specification<CatalogObjectEntity>> handle(CatalogObjectWhereArgs catalogObjectWhereArgs) {
        if (catalogObjectWhereArgs.getMetadataArg() != null) {
            CatalogObjectMetadataArgs metadataArgs = catalogObjectWhereArgs.getMetadataArg();
            if (metadataArgs.getValue().getEq() != null) {
                return Optional.of(KeyValueSpecification.builder()
                                                        .operations(Operations.EQ)
                                                        .key(metadataArgs.getKey())
                                                        .value(metadataArgs.getValue().getEq())
                                                        .build());
            }
            if (metadataArgs.getValue().getNe() != null) {
                return Optional.of(KeyValueSpecification.builder()
                                                        .operations(Operations.NE)
                                                        .key(metadataArgs.getKey())
                                                        .value(metadataArgs.getValue().getEq())
                                                        .build());
            }
            if (metadataArgs.getValue().getLike() != null) {
                return Optional.of(KeyValueSpecification.builder()
                                                        .operations(Operations.LIKE)
                                                        .key(metadataArgs.getKey())
                                                        .value(metadataArgs.getValue().getEq())
                                                        .build());
            }
        }
        return Optional.empty();
    }
}
