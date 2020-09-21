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

import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectContentTypeWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.specification.catalogobject.ContentTypeSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 05/07/2017
 */
@Component
public class CatalogObjectContentTypeFilterHandler
        implements FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> {

    @Override
    public Optional<Specification<CatalogObjectRevisionEntity>> handle(CatalogObjectWhereArgs whereArgs) {

        if (whereArgs.getContentTypeArg() != null) {
            CatalogObjectContentTypeWhereArgs contentTypeArg = whereArgs.getContentTypeArg();
            if (contentTypeArg.getEq() != null) {
                return Optional.of(ContentTypeSpecification.builder()
                                                           .operations(Operations.EQ)
                                                           .value(contentTypeArg.getEq())
                                                           .build());
            }
            if (contentTypeArg.getNe() != null) {
                return Optional.of(ContentTypeSpecification.builder()
                                                           .operations(Operations.NE)
                                                           .value(contentTypeArg.getNe())
                                                           .build());
            }
            if (contentTypeArg.getLike() != null) {
                return Optional.of(ContentTypeSpecification.builder()
                                                           .operations(Operations.LIKE)
                                                           .value(contentTypeArg.getLike())
                                                           .build());
            }
            if (contentTypeArg.getNotLike() != null) {
                return Optional.of(ContentTypeSpecification.builder()
                                                           .operations(Operations.NOT_LIKE)
                                                           .value(contentTypeArg.getNotLike())
                                                           .build());
            }
        }
        return Optional.empty();
    }
}
