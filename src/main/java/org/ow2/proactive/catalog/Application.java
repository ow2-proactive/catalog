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

import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @author ActiveEon Team
 */
@SpringBootApplication(scanBasePackages = { "org.ow2.proactive.catalog" })
@EnableAutoConfiguration(exclude = { MultipartAutoConfiguration.class })
@EnableSwagger2
@EnableTransactionManagement
@EntityScan(basePackages = "org.ow2.proactive.catalog.repository.entity")
@PropertySource("classpath:application.properties")
public class Application extends WebMvcConfigurerAdapter {

    @Value("${spring.datasource.driverClassName:}")
    private String dataSourceDriverClassName;

    @Value("${spring.datasource.url:}")
    private String dataSourceUrl;

    @Value("${spring.datasource.username:}")
    private String dataSourceUsername;

    @Value("${spring.datasource.password:}")
    private String dataSourcePassword;

    public static void main(String[] args) {
        //Important notice when using PDFBox with Java 8  :  https://pdfbox.apache.org/2.0/getting-started.html
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false)
                  .favorParameter(true)
                  .parameterName("format")
                  .ignoreAcceptHeader(true)
                  .useJaf(false)
                  .defaultContentType(MediaType.APPLICATION_JSON)
                  .mediaType("json", MediaType.APPLICATION_JSON);
    }

    @Bean
    @Profile("default")
    public DataSource defaultDataSource() {
        String jdbcUrl = dataSourceUrl;

        if (jdbcUrl.isEmpty()) {
            jdbcUrl = "jdbc:hsqldb:file:" + getDatabaseDirectory() +
                      ";create=true;hsqldb.tx=mvcc;hsqldb.applog=1;hsqldb.sqllog=0;hsqldb.write_delay=false";
        }

        return DataSourceBuilder.create()
                                .username(dataSourceUsername)
                                .password(dataSourcePassword)
                                .url(jdbcUrl)
                                .driverClassName(dataSourceDriverClassName)
                                .build();
    }

    @Bean
    @Profile("test")
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
    public MultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket workflowCatalogApi() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
                                                      .groupName("CatalogObjectEntity Catalog")
                                                      .ignoredParameterTypes(Pageable.class,
                                                                             PagedResourcesAssembler.class)
                                                      .alternateTypeRules(newRule(typeResolver.resolve(InputStreamResource.class),
                                                                                  typeResolver.resolve(MultipartFile.class)))
                                                      .select()
                                                      .paths(allowedPaths())
                                                      .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("CatalogObjectEntity Catalog API")
                                   .description("The purpose of the catalog is to store ProActive objects.\n" + "\n" +
                                                "A catalog is subdivided into buckets.\n\n Each bucket manages zero, one or more\n" +
                                                "versioned ProActive objects.")
                                   .license("GNU Affero General Public License v3.0")
                                   .licenseUrl("https://github.com/ow2-proactive/catalog/blob/master/LICENSE.txt")
                                   .version("1.0")
                                   .build();
    }

    private Predicate<String> allowedPaths() {
        return PathSelectors.regex("/buckets.*");
    }

}
