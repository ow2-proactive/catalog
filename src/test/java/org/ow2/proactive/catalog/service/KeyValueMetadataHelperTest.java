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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.when;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.model.GenericInfoBucketData;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;


/**
 * @author ActiveEon Team
 * @since 16/08/2017
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyValueMetadataHelperTest {

    @InjectMocks
    private KeyValueMetadataHelper keyValueMetadataHelper;

    @Mock
    private OwnerGroupStringHelper ownerGroupStringHelper;

    @Test
    public void testThatMetadataIsCorrectlyConverted() {
        KeyValueLabelMetadataEntity entityToConvert = new KeyValueLabelMetadataEntity("key",
                                                                                      "value",
                                                                                      WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        List<Metadata> convertedMetadata = keyValueMetadataHelper.convertFromEntity(Collections.singletonList(entityToConvert));
        assertThat(convertedMetadata.get(0).getKey()).isEqualTo("key");
        assertThat(convertedMetadata.get(0).getValue()).isEqualTo("value");
        assertThat(convertedMetadata.get(0).getLabel()).isEqualTo(WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
    }

    @Test
    public void testThatMetadataEntityIsCorrectlyConvertedToMapEntry() {
        KeyValueLabelMetadataEntity entityToConvert = new KeyValueLabelMetadataEntity("key",
                                                                                      "value",
                                                                                      WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        List<AbstractMap.SimpleImmutableEntry<String, String>> convertedMetadata = keyValueMetadataHelper.toKeyValueEntry(Collections.singletonList(entityToConvert));
        assertThat(convertedMetadata.get(0).getKey()).isEqualTo("key");
        assertThat(convertedMetadata.get(0).getValue()).isEqualTo("value");
    }

    @Test
    public void testThatMetadataEntityIsReplaced() {
        List<KeyValueLabelMetadataEntity> metadataEntities = Collections.singletonList(new KeyValueLabelMetadataEntity("group",
                                                                                                                       "value",
                                                                                                                       WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL));

        GenericInfoBucketData genericInfoBucketData = GenericInfoBucketData.builder()
                                                                           .group("GROUP:coolGroup")
                                                                           .bucketName("coolBucket")
                                                                           .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString("GROUP:coolGroup")).thenReturn("coolGroup");
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                   genericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "group", "coolGroup");
    }

    @Test
    public void testThatMetadataEntityIsReplacedAndOthersAreKept() {
        List<KeyValueLabelMetadataEntity> metadataEntities = Arrays.asList(new KeyValueLabelMetadataEntity("group",
                                                                                                           "value",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL),
                                                                           new KeyValueLabelMetadataEntity("houses",
                                                                                                           "3",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL));

        GenericInfoBucketData genericInfoBucketData = GenericInfoBucketData.builder()
                                                                           .group("GROUP:coolGroup")
                                                                           .bucketName("coolBucket")
                                                                           .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString("GROUP:coolGroup")).thenReturn("coolGroup");
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                   genericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "group", "coolGroup");
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "houses", "3");
    }

    @Test
    public void testNullReturnsEmptyList() {
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(null,
                                                                                                                                   null);
        assertThat(replacedMetadata).isEmpty();
        assertThat(replacedMetadata).isNotNull();
    }

    @Test
    public void testEmptyListAndNullReturnsEmptyList() {
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(Collections.emptyList(),
                                                                                                                                   null);
        assertThat(replacedMetadata).isEmpty();
        assertThat(replacedMetadata).isNotNull();
    }

    @Test
    public void testThatEmptyBucketEntityDataReturnsNotAlteredList() {
        GenericInfoBucketData emptyGenericInfoBucketData = GenericInfoBucketData.builder().build();

        List<KeyValueLabelMetadataEntity> metadataEntities = Arrays.asList(new KeyValueLabelMetadataEntity("group",
                                                                                                           "value",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL),
                                                                           new KeyValueLabelMetadataEntity("houses",
                                                                                                           "3",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL));

        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                   emptyGenericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "group", "value");
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "houses", "3");
    }

    private void KeyValueLabelMetadataEntityListHasEntry(List<KeyValueLabelMetadataEntity> keyValueMetadataEntities,
            String key, String value) {
        for (KeyValueLabelMetadataEntity KeyValueLabelMetadataEntity : keyValueMetadataEntities) {
            if (KeyValueLabelMetadataEntity.getKey().equals(key) &&
                KeyValueLabelMetadataEntity.getValue().equals(value)) {
                return;
            }
        }
        assertWithMessage("Expected key: \"" + key + "\" and value: \"" + value +
                          "\" inside keyValueMetadataEntities: " + keyValueMetadataEntities.toString()).that(false)
                                                                                                       .isTrue();
    }

}
