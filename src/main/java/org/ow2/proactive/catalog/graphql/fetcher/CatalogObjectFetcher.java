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
package org.ow2.proactive.catalog.graphql.fetcher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ow2.proactive.catalog.graphql.bean.CatalogObject;
import org.ow2.proactive.catalog.graphql.bean.CatalogObjectConnection;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.argument.OrderBy;
import org.ow2.proactive.catalog.graphql.bean.argument.PageInfo;
import org.ow2.proactive.catalog.graphql.bean.common.Arguments;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.metamodel.CatalogObjectEntityMetaModelEnum;
import org.ow2.proactive.catalog.repository.specification.catalogobject.DefaultSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;


/**
 * @author ActiveEon Team
 * @since 09/06/2017
 */
@Component
@Transactional(readOnly = true)
public class CatalogObjectFetcher implements DataFetcher {

    @Autowired
    private List<FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity>> catalogObjectFilterHandlers;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object get(DataFetchingEnvironment environment) {

        Pageable pageable = createPageable(environment);

        CatalogObjectWhereArgs argument = objectMapper.convertValue(environment.getArgument(Arguments.WHERE.getName()),
                                                                    CatalogObjectWhereArgs.class);

        Optional<Specification<CatalogObjectRevisionEntity>> specificationOptional = argument == null ? Optional.empty()
                                                                                                      : catalogObjectFilterHandlers.stream()
                                                                                                                                   .map(handler -> handler.handle(argument))
                                                                                                                                   .filter(spec -> spec.isPresent())
                                                                                                                                   .map(optional -> optional.get())
                                                                                                                                   .findFirst();

        Page<CatalogObjectRevisionEntity> catalogObjectEntitiesPage = specificationOptional.isPresent() ? catalogObjectRevisionRepository.findAll(specificationOptional.get(),
                                                                                                                                                  pageable)
                                                                                                        : catalogObjectRevisionRepository.findAll(new DefaultSpecification(),
                                                                                                                                                  pageable);

        CatalogObjectMapper mapper = new CatalogObjectMapper();
        return CatalogObjectConnection.builder()
                                      .edges(mapper.apply(catalogObjectEntitiesPage.getContent().stream())
                                                   .collect(Collectors.toList()))
                                      .page(catalogObjectEntitiesPage.getNumber())
                                      .size(catalogObjectEntitiesPage.getSize())
                                      .hasNext(catalogObjectEntitiesPage.hasNext())
                                      .hasPrevious(catalogObjectEntitiesPage.hasPrevious())
                                      .totalPage(catalogObjectEntitiesPage.getTotalPages())
                                      .totalCount(Long.valueOf(catalogObjectEntitiesPage.getTotalElements()).intValue())
                                      .build();
    }

    private Pageable createPageable(DataFetchingEnvironment environment) {
        String orderByString = objectMapper.convertValue(environment.getArgument(Arguments.ORDER_BY.getName()),
                                                         String.class);

        OrderBy orderBy;
        if (orderByString == null) {
            orderBy = OrderBy.CATALOG_OBJECT_KEY_ASC;
        } else {
            orderBy = OrderBy.fromValue(orderByString);
        }

        PageInfo pageInfo = objectMapper.convertValue(environment.getArgument(Arguments.PAGE_INFO.getName()),
                                                      PageInfo.class);

        if (pageInfo == null) {
            pageInfo = new PageInfo(0, 50);
        }

        switch (orderBy) {
            case CATALOG_OBJECT_KEY_ASC:
                return new PageRequest(pageInfo.getPage(), pageInfo.getSize(), Sort.Direction.ASC, "catalogObject.id");
            case CATALOG_OBJECT_KEY_DESC:
                return new PageRequest(pageInfo.getPage(),
                                       pageInfo.getSize(),
                                       Sort.Direction.DESC,
                                       CatalogObjectEntityMetaModelEnum.ID.getName());
            case KIND_ASC:
                return new PageRequest(pageInfo.getPage(),
                                       pageInfo.getSize(),
                                       Sort.Direction.ASC,
                                       CatalogObjectEntityMetaModelEnum.KIND.getName());
            case KIND_DESC:
                return new PageRequest(pageInfo.getPage(),
                                       pageInfo.getSize(),
                                       Sort.Direction.DESC,
                                       CatalogObjectEntityMetaModelEnum.KIND.getName());
            default:
                throw new IllegalArgumentException(orderBy + " does not exist");
        }

    }

    public static class CatalogObjectMapper
            implements Function<Stream<CatalogObjectRevisionEntity>, Stream<CatalogObject>> {

        @Override
        public Stream<CatalogObject> apply(Stream<CatalogObjectRevisionEntity> catalogObjectEntityStream) {
            return catalogObjectEntityStream.map(CatalogObject::new);
        }
    }
}
