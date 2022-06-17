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
import static org.ow2.proactive.catalog.service.KeyValueLabelMetadataHelper.GROUP_KEY;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
public class KeyValueLabelMetadataHelperTest {

    @InjectMocks
    private KeyValueLabelMetadataHelper keyValueLabelMetadataHelper;

    @Mock
    private OwnerGroupStringHelper ownerGroupStringHelper;

    @Test
    public void testThatMetadataIsCorrectlyConverted() {
        KeyValueLabelMetadataEntity entityToConvert = new KeyValueLabelMetadataEntity("key",
                                                                                      "value",
                                                                                      WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        List<Metadata> convertedMetadata = keyValueLabelMetadataHelper.convertFromEntity(Collections.singletonList(entityToConvert));
        assertThat(convertedMetadata.get(0).getKey()).isEqualTo("key");
        assertThat(convertedMetadata.get(0).getValue()).isEqualTo("value");
        assertThat(convertedMetadata.get(0).getLabel()).isEqualTo(WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
    }

    @Test
    public void testThatMetadataEntityIsCorrectlyConvertedToMapEntry() {
        KeyValueLabelMetadataEntity entityToConvert = new KeyValueLabelMetadataEntity("key",
                                                                                      "value",
                                                                                      WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        Map<String, String> convertedMetadata = keyValueLabelMetadataHelper.toMap(Collections.singletonList(entityToConvert));
        assertThat(convertedMetadata.containsKey("key")).isTrue();
        assertThat(convertedMetadata.containsValue("value")).isTrue();
    }

    @Test
    public void testThatGetOnlyGenericInformationIsOnlyReturningGenericInformation() {
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityGenericInfo = new KeyValueLabelMetadataEntity("key",
                                                                                                             "valye",
                                                                                                             WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse = new KeyValueLabelMetadataEntity("key",
                                                                                                              "valye",
                                                                                                              "chat");

        List<KeyValueLabelMetadataEntity> onlyGenericInformation = keyValueLabelMetadataHelper.getOnlyGenericInformation(Arrays.asList(keyValueLabelMetadataEntityGenericInfo,
                                                                                                                                       keyValueLabelMetadataEntityAnythingElse));
        assertThat(onlyGenericInformation.size()).isEqualTo(1);
    }

    @Test
    public void testThatGetOnlyGenericInformationIsReturningEmptyListIfNoGenericInfo() {
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse1 = new KeyValueLabelMetadataEntity("key",
                                                                                                               "valye",
                                                                                                               "dogs");
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse2 = new KeyValueLabelMetadataEntity("key",
                                                                                                               "valye",
                                                                                                               "something");

        List<KeyValueLabelMetadataEntity> onlyGenericInformation = keyValueLabelMetadataHelper.getOnlyGenericInformation(Arrays.asList(keyValueLabelMetadataEntityAnythingElse1,
                                                                                                                                       keyValueLabelMetadataEntityAnythingElse2));
        assertThat(onlyGenericInformation.size()).isEqualTo(0);
    }

    @Test
    public void testThatGetOnlyNotDuplicatedDependsOnIsReturningEmptyListIfNoDependsOn() {
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse1 = new KeyValueLabelMetadataEntity("key",
                                                                                                               "value",
                                                                                                               WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse2 = new KeyValueLabelMetadataEntity("key",
                                                                                                               "value",
                                                                                                               "something");

        List<KeyValueLabelMetadataEntity> onlyNotDuplicatedDependsOn = keyValueLabelMetadataHelper.getOnlyNotDuplicatedDependsOn(Arrays.asList(keyValueLabelMetadataEntityAnythingElse1,
                                                                                                                                               keyValueLabelMetadataEntityAnythingElse2));
        assertThat(onlyNotDuplicatedDependsOn.size()).isEqualTo(0);
    }

    @Test
    public void testThatGetOnlyNotDuplicatedDependsOnIsOnlyReturningDependsOn() {
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse1 = new KeyValueLabelMetadataEntity("key",
                                                                                                               "value",
                                                                                                               WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityDependsOn1 = new KeyValueLabelMetadataEntity("key",
                                                                                                            "value",
                                                                                                            WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL);

        KeyValueLabelMetadataEntity keyValueLabelMetadataEntityDependsOn2 = new KeyValueLabelMetadataEntity("key",
                                                                                                            "value",
                                                                                                            WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL);

        List<KeyValueLabelMetadataEntity> onlyNotDuplicatedDependsOn = keyValueLabelMetadataHelper.getOnlyNotDuplicatedDependsOn(Arrays.asList(keyValueLabelMetadataEntityAnythingElse1,
                                                                                                                                               keyValueLabelMetadataEntityDependsOn1,
                                                                                                                                               keyValueLabelMetadataEntityDependsOn2));
        assertThat(onlyNotDuplicatedDependsOn.size()).isEqualTo(1);
    }

    @Test
    public void testThatMetadataEntityIsReplaced() {
        List<KeyValueLabelMetadataEntity> metadataEntities = Collections.singletonList(new KeyValueLabelMetadataEntity(GROUP_KEY,
                                                                                                                       "value",
                                                                                                                       WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL));

        GenericInfoBucketData genericInfoBucketData = GenericInfoBucketData.builder()
                                                                           .group("GROUP:coolGroup")
                                                                           .bucketName("coolBucket")
                                                                           .build();

        when(ownerGroupStringHelper.extractGroupFromBucketOwnerOrGroupString("GROUP:coolGroup")).thenReturn("coolGroup");
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                        genericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, GROUP_KEY, "coolGroup");
    }

    @Test
    public void testThatToMapTakesAlwaysTheLastKeyDuplicateInList() {
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntity1 = new KeyValueLabelMetadataEntity("key",
                                                                                                   "thisShouldBeOverwritten",
                                                                                                   WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        KeyValueLabelMetadataEntity keyValueLabelMetadataEntity2 = new KeyValueLabelMetadataEntity("key",
                                                                                                   "hello",
                                                                                                   "chat");

        Map<String, String> shouldContainOnlyOne = keyValueLabelMetadataHelper.toMap(Arrays.asList(keyValueLabelMetadataEntity1,
                                                                                                   keyValueLabelMetadataEntity2));

        assertThat(shouldContainOnlyOne.size()).isEqualTo(1);
        assertThat(shouldContainOnlyOne.containsValue("hello")).isTrue();
        assertThat(shouldContainOnlyOne.containsValue("thisShouldBeOverwritten")).isFalse();
    }

    @Test
    public void testThatMetadataEntityIsReplacedAndOthersAreKept() {
        List<KeyValueLabelMetadataEntity> metadataEntities = Arrays.asList(new KeyValueLabelMetadataEntity(GROUP_KEY,
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
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                        genericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, GROUP_KEY, "coolGroup");
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, "houses", "3");
    }

    @Test
    public void testNullReturnsEmptyList() {
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(null,
                                                                                                                                        null);
        assertThat(replacedMetadata).isEmpty();
        assertThat(replacedMetadata).isNotNull();
    }

    @Test
    public void testEmptyListAndNullReturnsEmptyList() {
        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(Collections.emptyList(),
                                                                                                                                        null);
        assertThat(replacedMetadata).isEmpty();
        assertThat(replacedMetadata).isNotNull();
    }

    @Test
    public void testThatEmptyBucketEntityDataReturnsNotAlteredList() {
        GenericInfoBucketData emptyGenericInfoBucketData = GenericInfoBucketData.builder().build();

        List<KeyValueLabelMetadataEntity> metadataEntities = Arrays.asList(new KeyValueLabelMetadataEntity(GROUP_KEY,
                                                                                                           "value",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL),
                                                                           new KeyValueLabelMetadataEntity("houses",
                                                                                                           "3",
                                                                                                           WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL));

        List<KeyValueLabelMetadataEntity> replacedMetadata = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(metadataEntities,
                                                                                                                                        emptyGenericInfoBucketData);
        KeyValueLabelMetadataEntityListHasEntry(replacedMetadata, GROUP_KEY, "value");
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
