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
package org.ow2.proactive.catalog.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;
import org.springframework.util.Assert;

import lombok.extern.log4j.Log4j2;


/**
 * {@link ImportBeanDefinitionRegistrar} used by {@link EntityScanRoot}.
 *
 */
@Log4j2
class EntityScanRootRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String BEAN_NAME = "entityScanRootBeanPostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String entityScanRoot = getEntityScanRoot(importingClassMetadata);
        if (!registry.containsBeanDefinition(BEAN_NAME)) {
            addEntityScanRootBeanPostProcessor(registry, entityScanRoot);
        } else {
            updateEntityScanRootBeanPostProcessor(registry, entityScanRoot);
        }
    }

    private String getEntityScanRoot(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(EntityScanRoot.class.getName()));
        String scanRoot = attributes.getString("value");
        Assert.state(!scanRoot.isEmpty(), "@EntityScanRoot value attribute cannot be empty");
        return scanRoot;
    }

    private void addEntityScanRootBeanPostProcessor(BeanDefinitionRegistry registry, String scanRoot) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(EntityScanRootBeanPostProcessor.class);
        beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(scanRoot);
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        // We don't need this one to be post processed otherwise it can cause a
        // cascade of bean instantiation that we would rather avoid.
        beanDefinition.setSynthetic(true);
        registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
    }

    private void updateEntityScanRootBeanPostProcessor(BeanDefinitionRegistry registry, String scanRoot) {
        BeanDefinition definition = registry.getBeanDefinition(BEAN_NAME);
        ValueHolder constructorArguments = definition.getConstructorArgumentValues()
                                                     .getGenericArgumentValue(String.class);
        constructorArguments.setValue(scanRoot);
    }

    /**
     * {@link BeanPostProcessor} to set
     * {@link MutablePersistenceUnitInfo#setPersistenceUnitRootUrl(java.net.URL)} based
     * on an {@link EntityScanRoot} annotation.
     */
    static class EntityScanRootBeanPostProcessor implements BeanPostProcessor, SmartInitializingSingleton, Ordered {

        private final String entityScanRoot;

        private boolean processed;

        EntityScanRootBeanPostProcessor(String entityScanRoot) {
            this.entityScanRoot = entityScanRoot;
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof LocalContainerEntityManagerFactoryBean) {
                LocalContainerEntityManagerFactoryBean factoryBean = (LocalContainerEntityManagerFactoryBean) bean;
                factoryBean.setPersistenceUnitPostProcessors(new PersistenceUnitPostProcessor() {
                    @Override
                    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
                        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
                        try {
                            if (entityScanRoot.startsWith("classpath*:")) {
                                Resource[] matchingResources = resourcePatternResolver.getResources(entityScanRoot);
                                if (matchingResources.length == 0) {
                                    log.error("No matching resources for " + entityScanRoot + ", aborting");
                                } else if (matchingResources.length > 1) {
                                    log.error("Multiple matching resources for " + entityScanRoot + ": " +
                                              Arrays.asList(matchingResources).stream().map(resource -> {
                                                  try {
                                                      return resource.getURL().toExternalForm();
                                                  } catch (IOException e) {
                                                      return "";
                                                  }
                                              }).collect(Collectors.joining(",")));
                                } else {
                                    pui.setPersistenceUnitRootUrl(matchingResources[0].getURL());
                                }
                            } else {
                                pui.setPersistenceUnitRootUrl(resourcePatternResolver.getResource(entityScanRoot)
                                                                                     .getURL());
                            }
                        } catch (IOException e) {
                            log.error("Error when setting persistence unit root url for " + entityScanRoot, e);
                        }
                    }
                });
                this.processed = true;
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            return bean;
        }

        @Override
        public void afterSingletonsInstantiated() {
            Assert.state(this.processed,
                         "Unable to configure " + "LocalContainerEntityManagerFactoryBean from @EntityScanRoot, " +
                                         "ensure an appropriate bean is registered.");
        }

        @Override
        public int getOrder() {
            return 0;
        }

    }

}
