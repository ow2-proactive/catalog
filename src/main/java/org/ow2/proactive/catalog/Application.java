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

import java.io.File;

import javax.sql.DataSource;

import org.ow2.proactive.catalog.util.EntityScanRoot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;


/**
 * @author ActiveEon Team
 */
@SpringBootApplication(scanBasePackages = { "org.ow2.proactive.catalog" })
@EnableAutoConfiguration(exclude = { MultipartAutoConfiguration.class })
@EnableTransactionManagement
@EnableEncryptableProperties
@EntityScan(basePackages = "org.ow2.proactive.catalog.repository.entity")
@EntityScanRoot("classpath:/org/ow2/proactive/catalog/repository/entity")
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

    @Bean
    public OpenAPI workflowCatalogOpenAPI() {
        return new OpenAPI().info(new Info().title("CatalogObjectEntity Catalog API")
                                            .description("The purpose of the catalog is to store ProActive objects.\n" +
                                                         "\n" +
                                                         "A catalog is subdivided into buckets.\n\n Each bucket manages zero, one or more\n" +
                                                         "versioned ProActive objects.")
                                            .version("1.0\"")
                                            .license(new License().name("GNU Affero General Public License v3.0")
                                                                  .url("https://github.com/ow2-proactive/catalog/blob/master/LICENSE.txt")));
    }

}
