package org.ow2.proactive.catalog.repository.entity;


import com.google.common.truth.Truth;
import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;


public class CatalogObjectRevisionEntityTestDspotDspotDspot {
    @Test(timeout = 10000)
    public void testAddKeyValue_mg4() throws Exception {
        CatalogObjectRevisionEntity __DSPOT_o_302 = new CatalogObjectRevisionEntity();
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        boolean o_testAddKeyValue_mg4__7 = catalogObjectRevision.equals(__DSPOT_o_302);
        Truth.assertThat(o_testAddKeyValue_mg4__7).isFalse();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
    }

    @Test(timeout = 10000)
    public void testAddKeyValue_mg1_mg19() throws Exception {
        KeyValueLabelMetadataEntity __DSPOT_o_305 = new KeyValueLabelMetadataEntity("Z(6r`Yq@^:zAh$EhroKO", ".7^Hdq9c%#gPN-lk(zs@", "cJk3*}f-((G+fP|@!d;|");
        KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_299 = new KeyValueLabelMetadataEntity("!3txyL2kZNwnLY}:.]il", "nyA-- l-w`f6YlYxeR|f", "3mBUdZo;EF|u,%buboy^");
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        catalogObjectRevision.addKeyValue(__DSPOT_keyValueMetadata_299);
        boolean o_testAddKeyValue_mg1_mg19__10 = __DSPOT_keyValueMetadata_299.equals(__DSPOT_o_305);
        Truth.assertThat(o_testAddKeyValue_mg1_mg19__10).isFalse();
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

