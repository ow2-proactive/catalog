package org.ow2.proactive.catalog.service;


@org.junit.runner.RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class KeyValueLabelMetadataHelperTestDspotDspotDspot {
    @org.mockito.InjectMocks
    private org.ow2.proactive.catalog.service.KeyValueLabelMetadataHelper keyValueLabelMetadataHelper;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.service.OwnerGroupStringHelper ownerGroupStringHelper;

    @org.junit.Test(timeout = 10000)
    public void testThatGetOnlyGenericInformationIsOnlyReturningGenericInformation_mg2() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_o_5720 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("n41{_SC<,)d;l]iGrO=:", "Z5P8sNtZ$D4>yNia#NVe", "fsh@Fr]$6Z:v^a^w:kIC")))));
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity keyValueLabelMetadataEntityGenericInfo = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("key", "valye", org.ow2.proactive.catalog.util.parser.WorkflowParser.ATTRIBUTE_GENERIC_INFORMATION_LABEL);
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity keyValueLabelMetadataEntityAnythingElse = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("key", "valye", "chat");
        java.util.List<org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity> onlyGenericInformation = keyValueLabelMetadataHelper.getOnlyGenericInformation(java.util.Arrays.asList(keyValueLabelMetadataEntityGenericInfo, keyValueLabelMetadataEntityAnythingElse));
        boolean o_testThatGetOnlyGenericInformationIsOnlyReturningGenericInformation_mg2__14 = keyValueLabelMetadataEntityAnythingElse.equals(__DSPOT_o_5720);
        com.google.common.truth.Truth.assertThat(o_testThatGetOnlyGenericInformationIsOnlyReturningGenericInformation_mg2__14).isFalse();
    }

    private void KeyValueLabelMetadataEntityListHasEntry(java.util.List<org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity> keyValueMetadataEntities, java.lang.String key, java.lang.String value) {
        for (org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity KeyValueLabelMetadataEntity : keyValueMetadataEntities) {
            if ((KeyValueLabelMetadataEntity.getKey().equals(key)) && (KeyValueLabelMetadataEntity.getValue().equals(value))) {
                return;
            }
        }
        com.google.common.truth.Truth.assertWithMessage(((((("Expected key: \"" + key) + "\" and value: \"") + value) + "\" inside keyValueMetadataEntities: ") + (keyValueMetadataEntities.toString()))).that(false).isTrue();
    }
}

