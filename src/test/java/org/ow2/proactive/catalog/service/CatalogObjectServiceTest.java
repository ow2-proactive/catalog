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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.catalog.service.CatalogObjectService.KIND_NOT_FOUND;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.DependsOnCatalogObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.graphql.bean.CatalogObject;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.KindOrContentTypeIsNotValidException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.service.exception.WrongParametersException;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;

import com.google.common.collect.ImmutableList;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectServiceTest {

    public static final String COMMIT_MESSAGE = "commit message";

    public static final String USERNAME = "username";

    public static final String APPLICATION_XML = "application/xml";

    public static final String OBJECT = "object";

    public static final String NAME = "catalog";

    public static final String BUCKET = "bucket";

    public static final long REVISION_COMMIT_TIME = 1551960076669L;

    @InjectMocks
    private CatalogObjectService catalogObjectService;

    @Mock
    private CatalogObjectRepository catalogObjectRepository;

    @Mock
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Mock
    private BucketRepository bucketRepository;

    @Mock
    private KeyValueLabelMetadataHelper keyValueLabelMetadataHelper;

    @Mock
    private GenericInformationAdder genericInformationAdder;

    @Mock
    private KindAndContentTypeValidator kindAndContentTypeValidator;

    @Mock
    private SeparatorUtility separatorUtility;

    @Test(expected = BucketNotFoundException.class)
    public void testCreateCatalogObjectWithInvalidBucket() {
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        catalogObjectService.createCatalogObject("bucket",
                                                 NAME,
                                                 OBJECT,
                                                 COMMIT_MESSAGE,
                                                 USERNAME,
                                                 APPLICATION_XML,
                                                 null,
                                                 null);
    }

    /**
     * for example kinds: a/b, a/c, d/f/g
     * should return a, a/b, a/c, d, d/f, d/f/g
     */
    @Test
    public void testGetKinds() {
        TreeSet<String> storedKinds = new TreeSet<>();
        storedKinds.add("a/b");
        storedKinds.add("a/c");
        storedKinds.add("d/f/g");
        when(catalogObjectRepository.findAllKinds()).thenReturn(storedKinds);
        catalogObjectService.kindSeparator = "/";
        Set<String> returnedKinds = catalogObjectService.getKinds();
        verify(catalogObjectRepository, times(1)).findAllKinds();
        storedKinds.add("a");
        storedKinds.add("d");
        storedKinds.add("d/f");
        assertThat(storedKinds).isEqualTo(returnedKinds);
    }

    @Test
    public void testGetContentTypes() {
        TreeSet<String> storedContentTypes = new TreeSet<>();
        storedContentTypes.add("application/xml");
        storedContentTypes.add("application/json");
        storedContentTypes.add("text");
        when(catalogObjectRepository.findAllContentTypes()).thenReturn(storedContentTypes);
        Set<String> returnedContentTypes = catalogObjectService.getContentTypes();
        verify(catalogObjectRepository, times(1)).findAllContentTypes();
        assertThat(storedContentTypes).isEqualTo(returnedContentTypes);
    }

    @Test
    public void testCreateCatalogObject() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity,
                                                                                         System.currentTimeMillis());
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
        when(genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(any(),
                                                                                any(),
                                                                                any())).thenReturn(new byte[] {});
        List<Metadata> keyValues = ImmutableList.of(new Metadata("key", "value", null));

        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject("bucket",
                                                                                       NAME,
                                                                                       OBJECT,
                                                                                       COMMIT_MESSAGE,
                                                                                       USERNAME,
                                                                                       APPLICATION_XML,
                                                                                       keyValues,
                                                                                       null,
                                                                                       null);

        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObject.getUsername()).isEqualTo(USERNAME);
        assertThat(catalogObject.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObject.getKind()).isEqualTo(OBJECT);
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getMetadataList()).isNotEmpty();
        assertThat(catalogObject.getMetadataList()).hasSize(1);
    }

    @Test(expected = WrongParametersException.class)
    public void testUpdateObjectMetadataWithoutGivenParameters() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);

        catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                  NAME,
                                                  Optional.empty(),
                                                  Optional.empty());
    }

    @Test(expected = BucketNotFoundException.class)
    public void testUpdateObjectMetadataWithInvalidBucket() {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        catalogObjectService.updateObjectMetadata("wrong-bucket",
                                                  NAME,
                                                  Optional.of("some-kind"),
                                                  Optional.of("some-contentType"));
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testUpdateObjectMetadataWithInvalidObjectName() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(null);
        catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                  "wrong-name",
                                                  Optional.of("some-kind"),
                                                  Optional.of("some-contentType"));
    }

    @Test(expected = KindOrContentTypeIsNotValidException.class)
    public void testUpdateObjectWithInvalidKindName() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(false);

        CatalogObjectMetadata catalogObject = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                        NAME,
                                                                                        Optional.of("some-kind//fgf' g"),
                                                                                        Optional.of("updated-contentType"));
    }

    @Test(expected = BucketNotFoundException.class)
    public void testGetCatalogObjectWithInvalidBucket() {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        catalogObjectService.listCatalogObjects(Arrays.asList("wrong-bucket"));
    }

    @Test
    public void testUpdateObjectMetadata() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);

        CatalogObjectMetadata catalogObject = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                        NAME,
                                                                                        Optional.of("updated-kind"),
                                                                                        Optional.of("updated-contentType"));

        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObject.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObject.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObject.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getMetadataList()).isNotEmpty();
        assertThat(catalogObject.getMetadataList()).hasSize(1);
        assertThat(catalogObject.getCommitTimeRaw()).isEqualTo(String.valueOf(now));
    }

    @Test
    public void testUpdateObjectMetadataOnlyKindOrOnlyContentType() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);

        // only kind should be updated without changing contentType
        CatalogObjectMetadata catalogObjectUpdatedKind = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                   NAME,
                                                                                                   Optional.of("updated-kind"),
                                                                                                   Optional.empty());

        assertThat(catalogObjectUpdatedKind).isNotNull();
        assertThat(catalogObjectUpdatedKind.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedKind.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObjectUpdatedKind.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObjectUpdatedKind.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedKind.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedKind.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedKind.getMetadataList()).hasSize(1);
        assertThat(catalogObjectUpdatedKind.getCommitTimeRaw()).isEqualTo(String.valueOf(now));

        // only contentType should be updated without changing kind
        CatalogObjectMetadata catalogObjectUpdatedContentType = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                          NAME,
                                                                                                          Optional.empty(),
                                                                                                          Optional.of("updated-contentType"));

        assertThat(catalogObjectUpdatedContentType).isNotNull();
        assertThat(catalogObjectUpdatedContentType.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedContentType.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObjectUpdatedContentType.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectUpdatedContentType.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedContentType.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedContentType.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedContentType.getMetadataList()).hasSize(1);
        assertThat(catalogObjectUpdatedContentType.getCommitTimeRaw()).isEqualTo(String.valueOf(now));
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testCatalogObjectNotFound() {
        catalogObjectService.getObjectDependencies(BUCKET, OBJECT);

    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testCatalogObjectWithRevisionCommitTimeNotFound() {
        catalogObjectService.getObjectDependencies(BUCKET, OBJECT, REVISION_COMMIT_TIME);

    }

    @Test
    public void testGetObjectDependencies() {

        when(separatorUtility.getSplitBySeparator(anyString())).thenCallRealMethod();
        when(separatorUtility.getConcatWithSeparator(anyString(), anyString())).thenCallRealMethod();
        SeparatorUtility sep = new SeparatorUtility();

        long commitTime = 1L;
        String revisionCommitInString = String.format("%d", commitTime);
        long bucketId = 2L;
        String dependency1Name = "dep1Name";
        String dependency1 = sep.getConcatWithSeparator(BUCKET, dependency1Name);
        String dependency2 = sep.getConcatWithSeparator(BUCKET, "dep2");
        String kind = "kind";
        List<String> dependencies = Arrays.asList(dependency1, dependency2);

        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitTime(commitTime)
                                                                                             .build();
        CatalogObjectRevisionEntity objectDependency1 = CatalogObjectRevisionEntity.builder()
                                                                                   .catalogObject(CatalogObjectEntity.builder()
                                                                                                                     .bucket(new BucketEntity(BUCKET,
                                                                                                                                              "owner"))
                                                                                                                     .kind(kind)
                                                                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                                                                        dependency1Name))
                                                                                                                     .build())
                                                                                   .build();
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(BUCKET),
                                                                                    OBJECT)).thenReturn(catalogObjectRevisionEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(BUCKET),
                                                                                    dependency1Name)).thenReturn(objectDependency1);
        when(catalogObjectRevisionRepository.findDependsOnCatalogObjectNamesFromKeyValueMetadata(BUCKET,
                                                                                                 OBJECT,
                                                                                                 commitTime)).thenReturn(dependencies);
        when(catalogObjectRevisionRepository.findRevisionOfDependsOnCatalogObjectFromKeyLabelMetadata(BUCKET,
                                                                                                      OBJECT,
                                                                                                      commitTime,
                                                                                                      dependency1)).thenReturn(WorkflowParser.LATEST_VERSION);
        when(catalogObjectRevisionRepository.findRevisionOfDependsOnCatalogObjectFromKeyLabelMetadata(BUCKET,
                                                                                                      OBJECT,
                                                                                                      commitTime,
                                                                                                      dependency2)).thenReturn(revisionCommitInString);
        when(catalogObjectRevisionRepository.findCalledByCatalogObjectsFromKeyValueMetadata(sep.getConcatWithSeparator(BUCKET,
                                                                                                                       OBJECT))).thenReturn(Collections.singletonList(objectDependency1));

        CatalogObjectDependencies catalogObjectDependencies = catalogObjectService.getObjectDependencies(BUCKET,
                                                                                                         OBJECT);
        assertThat(catalogObjectDependencies.getDependsOnList()).containsExactly(new DependsOnCatalogObject(dependency1,
                                                                                                            kind,
                                                                                                            revisionCommitInString,
                                                                                                            true),
                                                                                 new DependsOnCatalogObject(dependency2,
                                                                                                            KIND_NOT_FOUND,
                                                                                                            revisionCommitInString,
                                                                                                            false));
        assertThat(catalogObjectDependencies.getCalledByList()).containsExactly(sep.getConcatWithSeparator(BUCKET,
                                                                                                           dependency1Name));

    }

    @Test
    public void testGetCatalogObjectMetadata() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        CatalogObjectMetadata objectMetadata = catalogObjectService.getCatalogObjectMetadata("bucket", "name");

        verify(catalogObjectRevisionRepository, times(1)).findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                                 anyString());
        assertThat(objectMetadata).isNotNull();
        assertThat(objectMetadata.getName()).isEqualTo(NAME);
        assertThat(objectMetadata.getKind()).isEqualTo(OBJECT);
        assertThat(objectMetadata.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(objectMetadata.getCommitDateTime()
                                 .atZone(ZoneId.systemDefault())
                                 .toInstant()
                                 .toEpochMilli() == now).isTrue();
        assertThat(objectMetadata.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(objectMetadata.getUsername()).isEqualTo(USERNAME);

    }

    private CatalogObjectEntity newCatalogObjectEntity(long now) {
        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(1L,
                                                                                                                        "catalog"))
                                                                     .kind("object")
                                                                     .contentType("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueLabelMetadataEntity> keyvalues = ImmutableList.of(new KeyValueLabelMetadataEntity("key",
                                                                                                       "value",
                                                                                                       null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(COMMIT_MESSAGE)
                                                                                             .commitTime(now)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectEntity;
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevisionEntity(BucketEntity bucketEntity, long now) {
        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(1L,
                                                                                                                        "catalog"))
                                                                     .kind("object")
                                                                     .bucket(bucketEntity)
                                                                     .contentType("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueLabelMetadataEntity> keyvalues = ImmutableList.of(new KeyValueLabelMetadataEntity("key",
                                                                                                       "value",
                                                                                                       null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(COMMIT_MESSAGE)
                                                                                             .commitTime(now)
                                                                                             .username(USERNAME)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();
        catalogObjectRevisionEntity.addKeyValueList(keyvalues);
        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);
        return catalogObjectRevisionEntity;
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testFindWorkflowInvalidId() throws Exception {
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(null);
        catalogObjectService.getCatalogObjectMetadata("bucket", "name");
    }

    @Test(expected = BucketNotFoundException.class)
    public void testCreateCatalogObjectRevisionNotFound() {
        when(catalogObjectRepository.findOne(any(CatalogObjectEntity.CatalogObjectEntityKey.class))).thenReturn(null);
        List<KeyValueLabelMetadataEntity> keyvalues = ImmutableList.of(new KeyValueLabelMetadataEntity("key",
                                                                                                       "value",
                                                                                                       null));
        catalogObjectService.createCatalogObjectRevision("bucket", NAME, COMMIT_MESSAGE, USERNAME, null);
    }

    @Test
    public void testCreateCatalogObjectRevision() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "owner");
        CatalogObjectEntity catalogObjectEntity = newCatalogObjectEntity(System.currentTimeMillis());
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = newCatalogObjectRevisionEntity(bucketEntity,
                                                                                                 System.currentTimeMillis());
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRepository.findOne(any(CatalogObjectEntity.CatalogObjectEntityKey.class))).thenReturn(catalogObjectEntity);
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectRevisionEntity);
        List<Metadata> keyvalues = ImmutableList.of(new Metadata("key", "value", null));
        when(keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(any(),
                                                                                        any())).thenReturn(Collections.emptyList());
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObjectRevision("bucket",
                                                                                               NAME,
                                                                                               COMMIT_MESSAGE,
                                                                                               USERNAME,
                                                                                               keyvalues,
                                                                                               null);
        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObject.getUsername()).isEqualTo(USERNAME);
        assertThat(catalogObject.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObject.getKind()).isEqualTo(OBJECT);
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getMetadataList()).isNotEmpty();
        assertThat(catalogObject.getMetadataList()).hasSize(1);
    }

    @Test(expected = RevisionNotFoundException.class)
    public void testGetCatalogObjectRevisionNotFound() {
        long now = System.currentTimeMillis();
        when(catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(anyList(),
                                                                                   anyString(),
                                                                                   anyLong())).thenReturn(null);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectService.getCatalogObjectRevisionEntityByCommitTime("bucket",
                                                                                                                                  NAME,
                                                                                                                                  now);

    }

    @Test
    public void testGetCatalogObjectRevision() {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "owner");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(anyList(),
                                                                                   anyString(),
                                                                                   anyLong())).thenReturn(catalogObjectEntity);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectService.getCatalogObjectRevisionEntityByCommitTime("bucket",
                                                                                                                                  NAME,
                                                                                                                                  now);

        assertThat(catalogObjectRevisionEntity).isNotNull();
        assertThat(catalogObjectRevisionEntity.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getKind()).isEqualTo(OBJECT);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getId().getName()).isEqualTo(NAME);
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).isNotEmpty();
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).hasSize(1);
    }
}
