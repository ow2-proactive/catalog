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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectBucketIdWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectNameWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification;
import org.ow2.proactive.catalog.repository.specification.catalogobject.BucketIdEqNeSpecification;
import org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification;
import org.ow2.proactive.catalog.repository.specification.generic.CompositeKeyEqNeSpecification;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.collect.ImmutableList;


/**
 * @author ActiveEon Team
 * @since 06/07/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectAndOrGroupFilterHandlerTest {

    @InjectMocks
    private CatalogObjectAndOrGroupFilterHandler andFilterHandler;

    @Mock
    private CatalogObjectBucketIdFilterHandler bucketIdHandler;

    @Mock
    private CatalogObjectKindFilterHandler kindHandler;

    @Mock
    private CatalogObjectNameFilterHandler nameHandler;

    @Mock
    private CatalogObjectMetadataFilterHandler metadataHandler;

    private CatalogObjectWhereArgs whereArgs;

    @Before
    public void setUp() throws Exception {

        when(bucketIdHandler.handle(any(CatalogObjectWhereArgs.class))).thenCallRealMethod();
        when(nameHandler.handle(any(CatalogObjectWhereArgs.class))).thenCallRealMethod();
        when(kindHandler.handle(any(CatalogObjectWhereArgs.class))).thenReturn(Optional.empty());
        when(metadataHandler.handle(any(CatalogObjectWhereArgs.class))).thenReturn(Optional.empty());

        CatalogObjectWhereArgs bucketid = CatalogObjectWhereArgs.builder()
                                                                .bucketIdArg(CatalogObjectBucketIdWhereArgs.builder()
                                                                                                           .eq(1L)
                                                                                                           .build())
                                                                .build();
        CatalogObjectWhereArgs name = CatalogObjectWhereArgs.builder()
                                                            .nameArg(CatalogObjectNameWhereArgs.builder()
                                                                                               .eq("name")
                                                                                               .build())
                                                            .build();

        CatalogObjectWhereArgs bucketid2 = CatalogObjectWhereArgs.builder()
                                                                 .bucketIdArg(CatalogObjectBucketIdWhereArgs.builder()
                                                                                                            .eq(2L)
                                                                                                            .build())
                                                                 .build();

        CatalogObjectWhereArgs name2 = CatalogObjectWhereArgs.builder()
                                                             .nameArg(CatalogObjectNameWhereArgs.builder()
                                                                                                .eq("name2")
                                                                                                .build())
                                                             .build();

        CatalogObjectWhereArgs name3 = CatalogObjectWhereArgs.builder()
                                                             .nameArg(CatalogObjectNameWhereArgs.builder()
                                                                                                .eq("name3")
                                                                                                .build())
                                                             .build();

        CatalogObjectWhereArgs orwhere3 = CatalogObjectWhereArgs.builder()
                                                                .orArg(ImmutableList.of(name2, name3))
                                                                .build();

        CatalogObjectWhereArgs andwhere1 = CatalogObjectWhereArgs.builder()
                                                                 .andArg(ImmutableList.of(bucketid, name))
                                                                 .build();
        CatalogObjectWhereArgs andwhere2 = CatalogObjectWhereArgs.builder()
                                                                 .andArg(ImmutableList.of(bucketid2, orwhere3))
                                                                 .build();
        whereArgs = CatalogObjectWhereArgs.builder().orArg(ImmutableList.of(andwhere1, andwhere2)).build();
        andFilterHandler.init();
    }

    @Test
    public void testHandleMethod() throws Exception {
        Optional<Specification<CatalogObjectEntity>> specification = andFilterHandler.handle(whereArgs);
        assertThat(specification).isNotNull();
        assertThat(specification.get() instanceof OrSpecification).isTrue();
        OrSpecification orSpecification = (OrSpecification) specification.get();

        assertThat(orSpecification.getFieldSpcifications()).hasSize(2);
        assertThat(orSpecification.getFieldSpcifications().get(0) instanceof AndSpecification).isTrue();
        assertThat(orSpecification.getFieldSpcifications().get(1) instanceof AndSpecification).isTrue();

        AndSpecification leftAnd = (AndSpecification) orSpecification.getFieldSpcifications().get(0);
        AndSpecification rightAnd = (AndSpecification) orSpecification.getFieldSpcifications().get(1);

        assertThat(leftAnd.getFieldSpcifications()).hasSize(2);
        assertThat(leftAnd.getFieldSpcifications().get(0) instanceof BucketIdEqNeSpecification).isTrue();
        assertThat(leftAnd.getFieldSpcifications().get(1) instanceof CompositeKeyEqNeSpecification).isTrue();

        assertThat(rightAnd.getFieldSpcifications()).hasSize(2);
        assertThat(rightAnd.getFieldSpcifications().get(0) instanceof BucketIdEqNeSpecification).isTrue();
        assertThat(rightAnd.getFieldSpcifications().get(1) instanceof OrSpecification).isTrue();

        OrSpecification rightAndChildOr = (OrSpecification) rightAnd.getFieldSpcifications().get(1);
        assertThat(rightAndChildOr.getFieldSpcifications()).hasSize(2);
        assertThat(rightAndChildOr.getFieldSpcifications().get(0) instanceof CompositeKeyEqNeSpecification).isTrue();
        assertThat(rightAndChildOr.getFieldSpcifications().get(1) instanceof CompositeKeyEqNeSpecification).isTrue();

    }

}
