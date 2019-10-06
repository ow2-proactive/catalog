package org.ow2.proactive.catalog.service;


@org.junit.runner.RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class CatalogObjectServiceTestDspotDspot {
    @org.junit.Test(timeout = 10000)
    public void testCreateCatalogObject_mg2() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.BucketEntity __DSPOT_o_1158 = new org.ow2.proactive.catalog.repository.entity.BucketEntity("QnAG-Xw9pruU|6?EW!B)", "p&(V/p*f@Km$*c!!6gDF");
        org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity = new org.ow2.proactive.catalog.repository.entity.BucketEntity("bucket", "toto");
        org.mockito.Mockito.when(this.kindAndContentTypeValidator.isValid(org.mockito.Matchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(this.bucketRepository.findOneByBucketName(org.mockito.Matchers.anyString())).thenReturn(bucketEntity);
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, java.lang.System.currentTimeMillis());
        org.mockito.Mockito.when(this.catalogObjectRevisionRepository.save(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
        org.mockito.Mockito.when(this.genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(org.mockito.Matchers.any(), org.mockito.Matchers.any(), org.mockito.Matchers.any())).thenReturn(new byte[]{  });
        java.util.List<org.ow2.proactive.catalog.dto.Metadata> keyValues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.dto.Metadata("key", "value", null));
        org.ow2.proactive.catalog.dto.CatalogObjectMetadata catalogObject = this.catalogObjectService.createCatalogObject("bucket", org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.NAME, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.OBJECT, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.APPLICATION_XML, keyValues, null, null);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
        boolean o_testCreateCatalogObject_mg2__31 = bucketEntity.equals(__DSPOT_o_1158);
        com.google.common.truth.Truth.assertThat(o_testCreateCatalogObject_mg2__31).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
    }

    public static final java.lang.String COMMIT_MESSAGE = "commit message";

    public static final java.lang.String APPLICATION_XML = "application/xml";

    public static final java.lang.String OBJECT = "object";

    public static final java.lang.String NAME = "catalog";

    @org.mockito.InjectMocks
    private org.ow2.proactive.catalog.service.CatalogObjectService catalogObjectService;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.repository.CatalogObjectRepository catalogObjectRepository;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.repository.BucketRepository bucketRepository;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.service.KeyValueLabelMetadataHelper keyValueLabelMetadataHelper;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.service.GenericInformationAdder genericInformationAdder;

    @org.mockito.Mock
    private org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator kindAndContentTypeValidator;

    @org.junit.Test(timeout = 10000)
    public void testCreateCatalogObjectnull29006_failAssert0() throws java.lang.Exception {
        try {
            org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity = new org.ow2.proactive.catalog.repository.entity.BucketEntity("bucket", "toto");
            org.mockito.Mockito.when(kindAndContentTypeValidator.isValid(org.mockito.Matchers.anyString())).thenReturn(true);
            org.mockito.Mockito.when(bucketRepository.findOneByBucketName(org.mockito.Matchers.anyString())).thenReturn(bucketEntity);
            org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, java.lang.System.currentTimeMillis());
            org.mockito.Mockito.when(catalogObjectRevisionRepository.save(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
            org.mockito.Mockito.when(genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(org.mockito.Matchers.any(), org.mockito.Matchers.any(), org.mockito.Matchers.any())).thenReturn(new byte[]{  });
            java.util.List<org.ow2.proactive.catalog.dto.Metadata> keyValues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.dto.Metadata("key", "value", null));
            org.ow2.proactive.catalog.dto.CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject("bucket", org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.NAME, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.OBJECT, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.APPLICATION_XML, null, null, null);
            org.junit.Assert.fail("testCreateCatalogObjectnull29006 should have thrown NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            org.junit.Assert.assertEquals(null, expected.getMessage());
        }
    }

    @org.junit.Test(timeout = 10000)
    public void testCreateCatalogObject_mg6() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity __DSPOT_o_1162 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity();
        org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity = new org.ow2.proactive.catalog.repository.entity.BucketEntity("bucket", "toto");
        org.mockito.Mockito.when(this.kindAndContentTypeValidator.isValid(org.mockito.Matchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(this.bucketRepository.findOneByBucketName(org.mockito.Matchers.anyString())).thenReturn(bucketEntity);
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, java.lang.System.currentTimeMillis());
        org.mockito.Mockito.when(this.catalogObjectRevisionRepository.save(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
        org.mockito.Mockito.when(this.genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(org.mockito.Matchers.any(), org.mockito.Matchers.any(), org.mockito.Matchers.any())).thenReturn(new byte[]{  });
        java.util.List<org.ow2.proactive.catalog.dto.Metadata> keyValues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.dto.Metadata("key", "value", null));
        org.ow2.proactive.catalog.dto.CatalogObjectMetadata catalogObject = this.catalogObjectService.createCatalogObject("bucket", org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.NAME, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.OBJECT, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.APPLICATION_XML, keyValues, null, null);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
        boolean o_testCreateCatalogObject_mg6__31 = catalogObjectEntity.equals(__DSPOT_o_1162);
        com.google.common.truth.Truth.assertThat(o_testCreateCatalogObject_mg6__31).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
    }

    @org.junit.Test(timeout = 10000)
    public void testCreateCatalogObject_mg3_mg27() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_o_1168 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity(new org.ow2.proactive.catalog.dto.Metadata(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("0aRTRID9KfO*{85e7YBb", "&j?hx1If127X{/-{ZqYo", "kA58I(@>B`d(z%:.GY5]")));
        org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity __DSPOT_keyValueMetadata_1159 = new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("C`[<C#tJK[lI!Z<e1w!5", "U(r6E[K} #WrfN(26H2T", "(euHZ!FWY !F:>4y$Ph>");
        org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity = new org.ow2.proactive.catalog.repository.entity.BucketEntity("bucket", "toto");
        org.mockito.Mockito.when(this.kindAndContentTypeValidator.isValid(org.mockito.Matchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(this.bucketRepository.findOneByBucketName(org.mockito.Matchers.anyString())).thenReturn(bucketEntity);
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, java.lang.System.currentTimeMillis());
        org.mockito.Mockito.when(this.catalogObjectRevisionRepository.save(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
        org.mockito.Mockito.when(this.genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(org.mockito.Matchers.any(), org.mockito.Matchers.any(), org.mockito.Matchers.any())).thenReturn(new byte[]{  });
        java.util.List<org.ow2.proactive.catalog.dto.Metadata> keyValues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.dto.Metadata("key", "value", null));
        org.ow2.proactive.catalog.dto.CatalogObjectMetadata catalogObject = this.catalogObjectService.createCatalogObject("bucket", org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.NAME, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.OBJECT, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.APPLICATION_XML, keyValues, null, null);
        catalogObjectEntity.addKeyValue(__DSPOT_keyValueMetadata_1159);
        boolean o_testCreateCatalogObject_mg3_mg27__36 = __DSPOT_keyValueMetadata_1159.equals(__DSPOT_o_1168);
        com.google.common.truth.Truth.assertThat(o_testCreateCatalogObject_mg3_mg27__36).isFalse();
    }

    @org.junit.Test(timeout = 10000)
    public void testCreateCatalogObjectRevision_mg880() throws java.lang.Exception {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity __DSPOT_o_1526 = new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity();
        org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity = new org.ow2.proactive.catalog.repository.entity.BucketEntity("bucket", "owner");
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObjectEntity = newCatalogObjectEntity(java.lang.System.currentTimeMillis());
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectRevisionEntity = newCatalogObjectRevisionEntity(bucketEntity, java.lang.System.currentTimeMillis());
        org.mockito.Mockito.when(this.bucketRepository.findOneByBucketName(org.mockito.Matchers.anyString())).thenReturn(bucketEntity);
        org.mockito.Mockito.when(this.catalogObjectRepository.findOne(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey.class))).thenReturn(catalogObjectEntity);
        org.mockito.Mockito.when(this.catalogObjectRevisionRepository.save(org.mockito.Matchers.any(org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectRevisionEntity);
        java.util.List<org.ow2.proactive.catalog.dto.Metadata> keyvalues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.dto.Metadata("key", "value", null));
        org.mockito.Mockito.when(this.keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(org.mockito.Matchers.any(), org.mockito.Matchers.any())).thenReturn(java.util.Collections.emptyList());
        org.ow2.proactive.catalog.dto.CatalogObjectMetadata catalogObject = this.catalogObjectService.createCatalogObjectRevision("bucket", org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.NAME, org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE, keyvalues, null);
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
        boolean o_testCreateCatalogObjectRevision_mg880__34 = catalogObjectEntity.equals(__DSPOT_o_1526);
        com.google.common.truth.Truth.assertThat(o_testCreateCatalogObjectRevision_mg880__34).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getCommitMessage()).isEqualTo("commit message");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getKind()).isEqualTo("object");
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getMetadataList())).isEmpty()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getBucketName()).isEqualTo("bucket");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getProjectName()).isEqualTo("");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getName()).isEqualTo("catalog");
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getExtension()).isNull();
        com.google.common.truth.Truth.assertThat(((java.util.Collection) (((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getLinks())).isEmpty()).isTrue();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).hasLinks()).isFalse();
        com.google.common.truth.Truth.assertThat(((org.ow2.proactive.catalog.dto.CatalogObjectMetadata) (catalogObject)).getId()).isNull();
    }

    private org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity newCatalogObjectEntity(long now) {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObjectEntity = org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.builder().id(new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey(1L, "catalog")).kind("object").contentType("application/xml").lastCommitTime(now).build();
        java.util.List<org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity> keyvalues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("key", "value", null));
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectRevisionEntity = org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.builder().commitMessage(org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE).commitTime(now).catalogObject(catalogObjectEntity).build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectEntity;
    }

    private org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity newCatalogObjectRevisionEntity(org.ow2.proactive.catalog.repository.entity.BucketEntity bucketEntity, long now) {
        org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity catalogObjectEntity = org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.builder().id(new org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey(1L, "catalog")).kind("object").bucket(bucketEntity).contentType("application/xml").lastCommitTime(now).build();
        java.util.List<org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity> keyvalues = com.google.common.collect.ImmutableList.of(new org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity("key", "value", null));
        org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity catalogObjectRevisionEntity = org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity.builder().commitMessage(org.ow2.proactive.catalog.service.CatalogObjectServiceTestDspotDspot.COMMIT_MESSAGE).commitTime(now).catalogObject(catalogObjectEntity).build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectRevisionEntity;
    }
}

