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

import java.io.File;
import java.util.List;

import javax.sql.DataSource;

import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.fetcher.CatalogObjectFetcher;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.graphql.handler.catalogobject.*;
import org.ow2.proactive.catalog.mocks.BucketGrantServiceMock;
import org.ow2.proactive.catalog.mocks.CatalogObjectGrantServiceMock;
import org.ow2.proactive.catalog.mocks.RestApiAccessServiceMock;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.helper.CommonRestTemplate;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.RawObjectResponseCreator;
import org.ow2.proactive.catalog.util.RevisionCommitMessageBuilder;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.name.validator.ObjectNameValidator;
import org.ow2.proactive.catalog.util.name.validator.TagsValidator;
import org.ow2.proactive.catalog.util.parser.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import com.google.common.collect.Lists;

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

    @Value("${spring.datasource.driverClassName:}")
    private String dataSourceDriverClassName;

    @Value("${spring.datasource.url:}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username:}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password:}")
    private String dataSourcePassword;

    @Bean
    public DataSource testDataSource() {
        return createDataSource();
    }

    private DataSource createDataSource() {
        return DataSourceBuilder.create()
                                .username(dataSourceUsername)
                                .password(dataSourcePassword)
                                .url(dataSourceUrl)
                                .driverClassName(dataSourceDriverClassName)
                                .build();
    }

    private String getDatabaseDirectory() {
        String proactiveHome = System.getProperty("proactive.home");

        if (proactiveHome == null) {
            return System.getProperty("java.io.tmpdir") + File.separator + "proactive" + File.separator + "catalog";
        }

        return proactiveHome + File.separator + "data" + File.separator + "db" + File.separator + "catalog" +
               File.separator + "wc";
    }

    @Bean
    public OwnerGroupStringHelper ownerGroupStringHelper() {
        return new OwnerGroupStringHelper();
    }

    @Bean
    public GrantRightsService grantRightsService() {
        return new GrantRightsService();
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
    public JobPlannerService jobPlannerService() {
        return new JobPlannerService();
    }

    @Bean
    public DataFetcher dataFetcher() {
        return new CatalogObjectFetcher();
    }

    @Bean
    public SeparatorUtility separatorUtility() {
        return new SeparatorUtility();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectBucketNameFilterHandler() {
        return new CatalogObjectBucketNameFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectKindFilterHandler() {
        return new CatalogObjectKindFilterHandler();
    }

    @Bean
    public FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> catalogObjectContentTypeFilterHandler() {
        return new CatalogObjectContentTypeFilterHandler();
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
    public WorkflowParser workflowParser() {
        return new WorkflowParser();
    }

    @Bean
    public PCWRuleParser pcwRuleParser() {
        return new PCWRuleParser();
    }

    @Bean
    public PolicyParser policyParser() {
        return new PolicyParser();
    }

    @Bean
    public InfrastructureParser infrastructureParser() {
        return new InfrastructureParser();
    }

    @Bean
    public NodeSourceParser nodeSourceParser() {
        return new NodeSourceParser();
    }

    @Bean
    public ScriptParser scriptParser() {
        return new ScriptParser();
    }

    @Bean
    public TextParser textParser() {
        return new TextParser();
    }

    @Bean
    public CalendarDefinitionParser calendarDefinitionParser() {
        return new CalendarDefinitionParser();
    }

    @Bean
    public KeyValueLabelMetadataHelper keyValueMetadataHelper() {
        List<AbstractCatalogObjectParser> parsers = Lists.newArrayList(workflowParser(),
                                                                       pcwRuleParser(),
                                                                       policyParser(),
                                                                       calendarDefinitionParser(),
                                                                       infrastructureParser(),
                                                                       nodeSourceParser(),
                                                                       scriptParser(),
                                                                       textParser());
        return new KeyValueLabelMetadataHelper(new OwnerGroupStringHelper(), parsers);
    }

    @Bean
    public WorkflowInfoAdder genericInformationAdder() {
        return new WorkflowInfoAdder();
    }

    @Bean
    public WorkflowXmlManipulator workflowXmlManipulator() {
        return new WorkflowXmlManipulator();
    }

    @Bean
    public RawObjectResponseCreator rawObjectResponseCreator() {
        return new RawObjectResponseCreator();
    }

    @Bean
    public BucketNameValidator bucketNameValidator() {
        return new BucketNameValidator();
    }

    @Bean
    public KindAndContentTypeValidator kindAndContentTypeValidator() {
        return new KindAndContentTypeValidator();
    }

    @Bean
    public TagsValidator tagsValidator() {
        return new TagsValidator();
    }

    @Bean
    public ObjectNameValidator objectNameValidator() {
        return new ObjectNameValidator();
    }

    @Bean
    public RevisionCommitMessageBuilder revisionCommitMessageBuilder() {
        return new RevisionCommitMessageBuilder();
    }

    @Bean
    @Primary
    public RestApiAccessService restApiAccessService() {
        return new RestApiAccessServiceMock();
    }

    @Bean
    @Primary
    public BucketGrantService bucketGrantService() {
        return new BucketGrantServiceMock();
    }

    @Bean
    @Primary
    public CatalogObjectGrantService catalogObjectGrantService() {
        return new CatalogObjectGrantServiceMock();
    }

}
