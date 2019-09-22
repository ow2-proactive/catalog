package org.ow2.proactive.catalog.repository.entity;


public class BucketEntityTestDspotDspot {
    private final java.lang.String DEFAULT_BUCKET_NAME = "test";

    private final java.lang.String DEFAULT_BUCKET_USER = "BucketTestUser";

    private org.ow2.proactive.catalog.repository.entity.BucketEntity bucket;

    @org.junit.Before
    public void setUp() {
        bucket = new org.ow2.proactive.catalog.repository.entity.BucketEntity(DEFAULT_BUCKET_NAME, DEFAULT_BUCKET_USER);
    }

    @org.junit.Test(timeout = 10000)
    public void testAddWorkflow_mg2() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity __DSPOT_o_192 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
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
        boolean o_testAddWorkflow_mg2__9 = catalogObject.equals(__DSPOT_o_192);
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
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_o_200 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_catalogObjectRevision_191 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
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
        catalogObject.addRevision(__DSPOT_catalogObjectRevision_191);
        boolean o_testAddWorkflow_mg1_mg16__12 = __DSPOT_catalogObjectRevision_191.equals(__DSPOT_o_200);
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
    public void testAddWorkflow_mg1_mg11_mg51() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_o_213 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata("t4ZcWnP&.ZuHnJBZ}O[]", "Ip/#WP}M8a6@]?8h8whM", "ovI#I|mK8#fwjn]5N[9_"));
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_195 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata("k5aw4Z#8{}YbgCZ5GeAZ", "BXWZ9Li]$FA^|2]&v3A9", "Bd%$(Yc4+914v7{bG0ev"));
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_catalogObjectRevision_191 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
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
        catalogObject.addRevision(__DSPOT_catalogObjectRevision_191);
        __DSPOT_catalogObjectRevision_191.addKeyValue(__DSPOT_keyValueMetadata_195);
        boolean o_testAddWorkflow_mg1_mg11_mg51__17 = __DSPOT_keyValueMetadata_195.equals(__DSPOT_o_213);
        com.google.common.truth.Truth.assertThat(o_testAddWorkflow_mg1_mg11_mg51__17).isFalse();
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

