package org.ow2.proactive.catalog.repository.entity;


import com.google.common.truth.Truth;
import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;


public class CatalogObjectRevisionEntityTestDspotDspot {
    @Test(timeout = 10000)
    public void testAddKeyValue_mg4() throws Exception {
        Object __DSPOT_o_281 = new Object();
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        boolean o_testAddKeyValue_mg4__7 = catalogObjectRevision.equals(__DSPOT_o_281);
        Truth.assertThat(o_testAddKeyValue_mg4__7).isFalse();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
    }

    @Test(timeout = 10000)
    public void testAddKeyValue_mg1_mg18() throws Exception {
        KeyValueLabelMetadataEntity __DSPOT_o_284 = new KeyValueLabelMetadataEntity("?g<xEWJnf[-.[<#:3[|_", ".,7KcmZhyus|yGP%Hl%#", "Z|p`+5or+0F]JZNr 8m{");
        KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_278 = new KeyValueLabelMetadataEntity("8-[lOV9/j.?.Rx[_@Zl,", "@8|><yr4]?#7Z;#Ib/>Z", "JL:BhN+R,ViYi7x0NUxf");
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        catalogObjectRevision.addKeyValue(__DSPOT_keyValueMetadata_278);
        boolean o_testAddKeyValue_mg1_mg18__10 = __DSPOT_keyValueMetadata_278.equals(__DSPOT_o_284);
        Truth.assertThat(o_testAddKeyValue_mg1_mg18__10).isFalse();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
    }

    private CatalogObjectRevisionEntity createCatalogObjectRevision() {
        KeyValueLabelMetadataEntity variableMock = Mockito.mock(KeyValueLabelMetadataEntity.class);
        CatalogObjectRevisionEntity catalogObjectRevision = new CatalogObjectRevisionEntity();
        catalogObjectRevision.addKeyValue(variableMock);
        return catalogObjectRevision;
    }
}

