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
package org.ow2.proactive.catalog;

import static org.mockito.Mockito.spy;

import javax.sql.DataSource;

import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.fetcher.CatalogObjectFetcher;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectAndOrGroupFilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectBucketIdFilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectKindFilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectMetadataFilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.CatalogObjectNameFilterHandler;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.ow2.proactive.catalog.service.GenericInformationAdder;
import org.ow2.proactive.catalog.service.GraphqlService;
import org.ow2.proactive.catalog.service.KeyValueLabelMetadataHelper;
import org.ow2.proactive.catalog.service.OwnerGroupStringHelper;
import org.ow2.proactive.catalog.service.WorkflowXmlManipulator;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import graphql.schema.DataFetcher;


/**
 * @author ActiveEon Team
 * @since 23/06/2017
 */
@EnableAutoConfiguration
@EntityScan(basePackages = { "org.ow2.proactive.catalog" })
@PropertySource("classpath:application-test.properties")
@Profile("test")
public class IntegrationTestConfig {

    @Bean
    public DataSource testDataSource() {
        return createMemDataSource();
    }

    private DataSource createMemDataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase db = builder.setType(EmbeddedDatabaseType.HSQL).build();

        return db;
    }

    @Bean
    public BucketService bucketService() {
        return new BucketService();
    }

    @Bean
    public CatalogObjectService catalogObjectService() {
        return new CatalogObjectService();
    }

    @Bean
    public GraphqlService graphqlService() {
        return new GraphqlService();
    }

    @Bean
    public DataFetcher dataFetcher() {
        return new CatalogObjectFetcher();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectBucketIdFilterHandler() {
        return new CatalogObjectBucketIdFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectKindFilterHandler() {
        return new CatalogObjectKindFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectNameFilterHandler() {
        return new CatalogObjectNameFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectMetadataFilterHandler() {
        return new CatalogObjectMetadataFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectAndOrGroupFilterHandler() {
        return new CatalogObjectAndOrGroupFilterHandler();
    }

    @Bean
    public CatalogObjectFetcher.CatalogObjectMapper catalogObjectMapper() {
        return spy(new CatalogObjectFetcher.CatalogObjectMapper());
    }

    @Bean
    public ArchiveManagerHelper archiveManager() {
        return new ArchiveManagerHelper();
    }

    @Bean
    public KeyValueLabelMetadataHelper keyValueMetadataHelper() {
        return new KeyValueLabelMetadataHelper(new OwnerGroupStringHelper());
    }

    @Bean
    public GenericInformationAdder genericInformationAdder() {
        return new GenericInformationAdder();
    }

    @Bean
    public WorkflowXmlManipulator workflowXmlManipulator() {
        return new WorkflowXmlManipulator();
    }

}
