package org.ow2.proactive.catalog.repository.entity;


import com.google.common.truth.Truth;
import java.util.Collection;
import org.junit.Test;
import org.mockito.Mockito;


public class CatalogObjectRevisionEntityTestDspotDspot {
    @Test(timeout = 10000)
    public void testAddKeyValue_mg4() throws Exception {
        CatalogObjectRevisionEntity __DSPOT_o_296 = new CatalogObjectRevisionEntity();
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        boolean o_testAddKeyValue_mg4__7 = catalogObjectRevision.equals(__DSPOT_o_296);
        Truth.assertThat(o_testAddKeyValue_mg4__7).isFalse();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
    }

    @Test(timeout = 10000)
    public void testAddKeyValue_mg1_mg18() throws Exception {
        KeyValueLabelMetadataEntity __DSPOT_o_299 = new KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new KeyValueLabelMetadataEntity("D[Hn8JlB^Q()<6Hrg^$.", "b?8Mq[Zg@S@DLF4dxe9d", "3L<Q[o@+ YZf@=7TNK8v")))));
        KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_293 = new KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new KeyValueLabelMetadataEntity("-w`f6YlYxeR|f3mBUdZo", ";EF|u,%buboy^`qH;&N0", "t<Q&DDg$-,/&`OigT`JE")));
        CatalogObjectRevisionEntity catalogObjectRevision = createCatalogObjectRevision();
        Truth.assertThat(((Collection) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getKeyValueMetadataList())).isEmpty()).isFalse();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitMessage()).isNull();
        Truth.assertThat(((long) (((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCommitTime()))).isEqualTo(0L);
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getCatalogObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getRawObject()).isNull();
        Truth.assertThat(((CatalogObjectRevisionEntity) (catalogObjectRevision)).getId()).isNull();
        catalogObjectRevision.addKeyValue(Mockito.mock(KeyValueLabelMetadataEntity.class));
        catalogObjectRevision.addKeyValue(__DSPOT_keyValueMetadata_293);
        boolean o_testAddKeyValue_mg1_mg18__16 = __DSPOT_keyValueMetadata_293.equals(__DSPOT_o_299);
        Truth.assertThat(o_testAddKeyValue_mg1_mg18__16).isFalse();
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

