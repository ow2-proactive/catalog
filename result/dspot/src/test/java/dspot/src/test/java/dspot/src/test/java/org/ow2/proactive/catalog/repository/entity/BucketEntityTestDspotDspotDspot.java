package org.ow2.proactive.catalog.repository.entity;


public class BucketEntityTestDspotDspotDspot {
    private final java.lang.String DEFAULT_BUCKET_NAME = "test";

    private final java.lang.String DEFAULT_BUCKET_USER = "BucketTestUser";

    private org.ow2.proactive.catalog.repository.entity.BucketEntity bucket;

    @org.junit.Before
    public void setUp() {
        bucket = new org.ow2.proactive.catalog.repository.entity.BucketEntity(DEFAULT_BUCKET_NAME, DEFAULT_BUCKET_USER);
    }

    @org.junit.Test(timeout = 10000)
    public void testAddWorkflow_mg2() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity __DSPOT_o_211 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObject = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=null, bucket=null, contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
        catalogObject.setBucket(bucket);
        catalogObject.setId(new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey(null, "name"));
        bucket.addCatalogObject(catalogObject);
        boolean o_testAddWorkflow_mg2__9 = catalogObject.equals(__DSPOT_o_211);
        com.google.common.truth.Truth.assertThat(o_testAddWorkflow_mg2__9).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getCatalogObjects())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getBucketName()).isEqualTo("test");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).toString()).isEqualTo("BucketEntity(id=null, bucketName=test, owner=BucketTestUser)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getOwner()).isEqualTo("BucketTestUser");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name), bucket=BucketEntity(id=null, bucketName=test, owner=BucketTestUser), contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getBucketId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).toString()).isEqualTo("CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getName()).isEqualTo("name");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
    }

    @org.junit.Test(timeout = 10000)
    public void testAddWorkflow_mg1_mg16() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_o_219 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_catalogObjectRevision_210 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObject = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=null, bucket=null, contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
        catalogObject.setBucket(bucket);
        catalogObject.setId(new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey(null, "name"));
        bucket.addCatalogObject(catalogObject);
        catalogObject.addRevision(__DSPOT_catalogObjectRevision_210);
        boolean o_testAddWorkflow_mg1_mg16__12 = __DSPOT_catalogObjectRevision_210.equals(__DSPOT_o_219);
        com.google.common.truth.Truth.assertThat(o_testAddWorkflow_mg1_mg16__12).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getCatalogObjects())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getBucketName()).isEqualTo("test");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).toString()).isEqualTo("BucketEntity(id=null, bucketName=test, owner=BucketTestUser)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getOwner()).isEqualTo("BucketTestUser");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name), bucket=BucketEntity(id=null, bucketName=test, owner=BucketTestUser), contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getBucketId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).toString()).isEqualTo("CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getName()).isEqualTo("name");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
    }

    @org.junit.Test(timeout = 10000)
    public void testAddWorkflow_mg1_mg11_mg74() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_o_249 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata("`8U^L|U2^w&]RipC8T[d", "Xo/B!IcY}7hT0[e!2k?W", "P/q=qf@xV4RP-Rlv;5Zd"));
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_214 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata("#fwjn]5N[9_#G7nLKEf<", "rz={[YT&^U6Y]Kg1e/q[", "NKD[/hFAO[^U-(XuVkZS"))));
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_catalogObjectRevision_210 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObject = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=null, bucket=null, contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
        catalogObject.setBucket(bucket);
        catalogObject.setId(new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey(null, "name"));
        bucket.addCatalogObject(catalogObject);
        catalogObject.addRevision(__DSPOT_catalogObjectRevision_210);
        __DSPOT_catalogObjectRevision_210.addKeyValue(__DSPOT_keyValueMetadata_214);
        boolean o_testAddWorkflow_mg1_mg11_mg74__19 = __DSPOT_keyValueMetadata_214.equals(__DSPOT_o_249);
        com.google.common.truth.Truth.assertThat(o_testAddWorkflow_mg1_mg11_mg74__19).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getKind()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getRevisions())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((long) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getLastCommitTime()))).isEqualTo(0L);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getContentType()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getCatalogObjects())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getBucketName()).isEqualTo("test");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).toString()).isEqualTo("BucketEntity(id=null, bucketName=test, owner=BucketTestUser)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.BucketEntity) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getBucket())).getOwner()).isEqualTo("BucketTestUser");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).toString()).isEqualTo("CatalogObjectEntity{id=CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name), bucket=BucketEntity(id=null, bucketName=test, owner=BucketTestUser), contentType=\'null\', kind=\'null\', lastCommitTime=0}");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getBucketId()).isNull();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).toString()).isEqualTo("CatalogObjectEntity.CatalogObjectEntityKey(bucketId=null, name=name)");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey) (((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getId())).getName()).isEqualTo("name");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity) (catalogObject)).getExtension()).isNull();
    }
}

