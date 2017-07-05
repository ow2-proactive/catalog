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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ow2.proactive.catalog.graphql.handler.Handler;
import org.ow2.proactive.catalog.graphql.schema.common.Arguments;
import org.ow2.proactive.catalog.graphql.schema.type.CatalogObject;
import org.ow2.proactive.catalog.graphql.schema.type.CatalogObjectConnection;
import org.ow2.proactive.catalog.graphql.schema.type.MetaData;
import org.ow2.proactive.catalog.graphql.schema.type.filter.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
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
public class CatalogObjectFetcher implements DataFetcher<CatalogObjectConnection> {

    @Autowired
    private List<Handler<CatalogObjectWhereArgs, CatalogObjectEntity>> catalogObjectHandlers;

    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Override
    public CatalogObjectConnection get(DataFetchingEnvironment environment) {
        CatalogObjectWhereArgs argument = environment.getArgument(Arguments.WHERE.getName());

        List<Specification<CatalogObjectEntity>> specificationList = catalogObjectHandlers.stream()
                                                                                          .map(handler -> handler.handle(argument))
                                                                                          .filter(specificationOptional -> specificationOptional.isPresent())
                                                                                          .map(optional -> optional.get())
                                                                                          .collect(Collectors.toList());

        Specification<CatalogObjectEntity> specifications = specificationList.get(0);
        for (int i = 1; i < specificationList.size(); i++) {
            specifications = Specifications.where(specifications).and(specificationList.get(i));
        }

        List<CatalogObjectEntity> catalogObjectEntities = catalogObjectRepository.findAll(specifications);
        return null;
    }

    /**
     * See https://facebook.github.io/relay/graphql/connections.htm#HasPreviousPage()
     *
     * @param nbEntriesBeforeSlicing The number of entries before slicing.
     * @param last                   the number of last entries requested.
     * @return {@code true} if entries have been sliced and another page is available, {@code false}
     * otherwise.
     */
    protected boolean hasPreviousPage(int nbEntriesBeforeSlicing, Integer last) {

        if (last == null) {
            return false;
        }

        if (nbEntriesBeforeSlicing > last) {
            return true;
        }

        return false;
    }

    /**
     * See https://facebook.github.io/relay/graphql/connections.htm#HasNextPage()
     *
     * @param nbEntriesBeforeSlicing The number of entries before slicing.
     * @param first                  the number of first entries requested.
     * @return {@code true} if entries have been sliced and another page is available, {@code false}
     * otherwise.
     */
    protected boolean hasNextPage(int nbEntriesBeforeSlicing, Integer first) {

        if (first == null) {
            return false;
        }

        if (nbEntriesBeforeSlicing > first) {
            return true;
        }

        return false;
    }

    public static class CatalogObjectMapper implements Function<Stream<CatalogObjectEntity>, Stream<CatalogObject>> {

        @Override
        public Stream<CatalogObject> apply(Stream<CatalogObjectEntity> catalogObjectEntityStream) {
            return catalogObjectEntityStream.map(entity -> {
                List<MetaData> metaData = entity.getRevisions()
                                                .first()
                                                .getKeyValueMetadataList()
                                                .stream()
                                                .map(MetaData::new)
                                                .collect(Collectors.toList());

                return CatalogObject.builder()
                                    .bucketId(entity.getId().getBucketId())
                                    .commitDateTime(entity.getRevisions().first().getCommitTime())
                                    .commitMessage(entity.getRevisions().first().getCommitMessage())
                                    .contentType(entity.getContentType())
                                    .kind(entity.getKind())
                                    .name(entity.getId().getName())
                                    .metaData(metaData)
                                    .build();
            });
        }
    }
}
