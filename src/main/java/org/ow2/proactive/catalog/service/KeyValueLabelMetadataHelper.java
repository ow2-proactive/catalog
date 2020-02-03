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
package org.ow2.proactive.catalog.service;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.model.GenericInfoBucketData;
import org.ow2.proactive.catalog.util.parser.AbstractCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.DefaultCatalogObjectParser;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author ActiveEon Team
 * @since 19/06/2017
 */
@Component
public class KeyValueLabelMetadataHelper {

    @SuppressWarnings("FieldCanBeLocal")
    public static final String GROUP_KEY = "group";

    @SuppressWarnings("FieldCanBeLocal")
    public static final String BUCKET_NAME_KEY = "bucketName";

    private final List<AbstractCatalogObjectParser> parsers;

    private final OwnerGroupStringHelper ownerGroupStringHelper;

    @Autowired
    public KeyValueLabelMetadataHelper(OwnerGroupStringHelper ownerGroupStringHelper,
            List<AbstractCatalogObjectParser> parsers) {
        this.ownerGroupStringHelper = ownerGroupStringHelper;
        this.parsers = parsers;
    }

    public static List<KeyValueLabelMetadataEntity> convertToEntity(List<Metadata> source) {
        return source.stream().map(KeyValueLabelMetadataEntity::new).collect(Collectors.toList());
    }

    public List<KeyValueLabelMetadataEntity>
            getOnlyGenericInformation(List<KeyValueLabelMetadataEntity> keyValueLabelMetadataEntities) {
        return keyValueLabelMetadataEntities.stream().filter(this::isGenericInformation).collect(Collectors.toList());
    }

    private boolean isGenericInformation(KeyValueLabelMetadataEntity keyValueLabelMetadataEntity) {
        return keyValueLabelMetadataEntity.getLabel().equals(WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
    }

    public List<KeyValueLabelMetadataEntity>
            getOnlyNotDuplicatedDependsOn(List<KeyValueLabelMetadataEntity> keyValueLabelMetadataEntities) {
        return new ArrayList<>(new HashSet<>(keyValueLabelMetadataEntities.stream()
                                                                          .filter(this::isDependsOn)
                                                                          .collect(Collectors.toList())));
    }

    private boolean isDependsOn(KeyValueLabelMetadataEntity keyValueLabelMetadataEntity) {
        return keyValueLabelMetadataEntity.getLabel().equals(WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL);
    }

    public List<KeyValueLabelMetadataEntity> extractKeyValuesFromRaw(String kind, byte[] rawObject) {
        AbstractCatalogObjectParser catalogObjectParser = parsers.stream()
                                                                 .filter(parser -> parser.isMyKind(kind))
                                                                 .findFirst()
                                                                 .orElse(new DefaultCatalogObjectParser());
        return catalogObjectParser.parse(new ByteArrayInputStream(rawObject));

    }

    public List<Metadata> convertFromEntity(List<KeyValueLabelMetadataEntity> source) {
        return source.stream().map(Metadata::new).collect(Collectors.toList());
    }

    public Map<String, String> toMap(final List<KeyValueLabelMetadataEntity> workflowKeyValueMetadataEntities) {
        return workflowKeyValueMetadataEntities.stream()
                                               .map(this::createSimpleImmutableEntry)
                                               .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey,
                                                                         AbstractMap.SimpleImmutableEntry::getValue,
                                                                         (duplicate1, duplicate2) -> duplicate2));
    }

    public List<KeyValueLabelMetadataEntity> replaceMetadataRelatedGenericInfoAndKeepOthers(
            final List<KeyValueLabelMetadataEntity> workflowMetadataEntities,
            final GenericInfoBucketData genericInfoBucketData) {
        if (workflowMetadataEntities == null) {
            return Collections.emptyList();
        }
        if (genericInfoBucketData == null) {
            return new ArrayList<>(workflowMetadataEntities);
        }

        List<KeyValueLabelMetadataEntity> metadataListWithGroup = replaceOrAddGenericInfoKey(workflowMetadataEntities,
                                                                                             GROUP_KEY,
                                                                                             ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString(genericInfoBucketData.getGroup()));

        List<KeyValueLabelMetadataEntity> metadataListWithGroupBucket = replaceOrAddGenericInfoKey(metadataListWithGroup,
                                                                                                   BUCKET_NAME_KEY,
                                                                                                   genericInfoBucketData.getBucketName());

        return new ArrayList<>(metadataListWithGroupBucket);
    }

    private List<KeyValueLabelMetadataEntity> replaceOrAddGenericInfoKey(
            final List<KeyValueLabelMetadataEntity> initialList, final String key, final String value) {
        if (key == null || value == null) {
            return initialList;
        }

        List<KeyValueLabelMetadataEntity> resultList = initialList.stream()
                                                                  .filter(item -> !item.getKey().equals(key))
                                                                  .collect(Collectors.toList());

        resultList.add(createKeyValueLabelMetadataEntity(key, value));

        return resultList;
    }

    private KeyValueLabelMetadataEntity createKeyValueLabelMetadataEntity(String key, String value) {
        return new KeyValueLabelMetadataEntity(key, value, WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
    }

    private AbstractMap.SimpleImmutableEntry<String, String>
            createSimpleImmutableEntry(final KeyValueLabelMetadataEntity KeyValueLabelMetadataEntity) {
        return new AbstractMap.SimpleImmutableEntry<>(KeyValueLabelMetadataEntity.getKey(),
                                                      KeyValueLabelMetadataEntity.getValue());

    }
}
