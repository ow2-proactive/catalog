package org.ow2.proactive.catalog.repository.entity;


import com.google.common.truth.Truth;
import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;


public class CatalogObjectRevisionEntityTestDspot {
    @Test(timeout = 10000)
    public void testAddKeyValue_mg4() throws Exception {
        Object __DSPOT_o_284 = new Object();
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        boolean o_testAddKeyValue_mg4__7 = catalogObjectRevision.equals(__DSPOT_o_284);
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
        KeyValueLabelMetadataEntity __DSPOT_o_287 = new KeyValueLabelMetadataEntity("Gw_1nfNsEocWj+V2ku8d", "[w[zx>weSco;ly|yz)r_", " 8Q72!4y|o3LHg@h[Y&Q");
        KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_281 = new KeyValueLabelMetadataEntity("Hv!Lb^R/Cg|3RC!0fPw#", "b@ByP.bRrDAE !rxKQ)s", "(Bqv|9>B^t$vWL?9&8ga");
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        catalogObjectRevision.addKeyValue(__DSPOT_keyValueMetadata_281);
        boolean o_testAddKeyValue_mg1_mg18__10 = __DSPOT_keyValueMetadata_281.equals(__DSPOT_o_287);
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

