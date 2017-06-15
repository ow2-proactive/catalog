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
package org.ow2.proactive.catalog.graphql.fetchers;

import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.graphql.handler.Handler;
import org.ow2.proactive.catalog.graphql.schema.common.Arguments;
import org.ow2.proactive.catalog.graphql.schema.type.filter.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.rest.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
@Component
@Transactional(readOnly = true)
public class CatalogObjectFetcher implements DataFetcher<CatalogObjectEntity> {

    @Autowired
    private List<Handler<CatalogObjectWhereArgs, CatalogObjectEntity>> catalogObjectHandlers;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Override
    public CatalogObjectEntity get(DataFetchingEnvironment environment) {
        CatalogObjectWhereArgs argument = environment.getArgument(Arguments.WHERE.getName());

        List<Specification<CatalogObjectEntity>> specificationList = catalogObjectHandlers.stream()
                                                                                          .map(handler -> handler.handle(argument))
                                                                                          .filter(specificationOptional -> specificationOptional.isPresent())
                                                                                          .map(optional -> optional.get())
                                                                                          .collect(Collectors.toList());

        Specification<CatalogObjectEntity> result = specificationList.get(0);
        for (int i = 1; i < specificationList.size(); i++) {
            result = Specifications.where(result).and(specificationList.get(i));
        }

        catalogObjectRepository.findAll(result);
        return null;
    }
}
