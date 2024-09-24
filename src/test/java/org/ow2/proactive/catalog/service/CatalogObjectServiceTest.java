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
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.DependsOnCatalogObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.*;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.name.validator.ObjectNameValidator;
import org.ow2.proactive.catalog.util.name.validator.TagsValidator;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;

import com.google.common.collect.ImmutableList;


/**
 * @author ActiveEon Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CatalogObjectServiceTest {

    public static final String COMMIT_MESSAGE = "commit message";

    public static final String UPDATE_COMMIT_MESSAGE = "The project name or/and tags metadata are updated";

    public static final String USERNAME = "username";

    public static final String APPLICATION_XML = "application/xml";

    public static final String OBJECT = "object";

    public static final String NAME = "catalog";

    public static final String BUCKET = "bucket";

    public static final long REVISION_COMMIT_TIME = 1551960076669L;

    public static final String PROJECT_NAME = "projectName";

    public static final String TAGS = "tag1,tag2";

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
    private WorkflowInfoAdder workflowInfoAdder;

    @Mock
    private KindAndContentTypeValidator kindAndContentTypeValidator;

    @Mock
    private TagsValidator tagsValidator;

    @Mock
    private ObjectNameValidator objectNameValidator;

    @Mock
    private SeparatorUtility separatorUtility;

    @Test(expected = BucketNotFoundException.class)
    public void testCreateCatalogObjectWithInvalidBucket() {
        when(objectNameValidator.isValid(anyString())).thenReturn(true);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        catalogObjectService.createCatalogObject("bucket",
                                                 NAME,
                                                 PROJECT_NAME,
                                                 TAGS,
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
    public void testGetObjectTags() {
        TreeSet<String> storedObjectTags = new TreeSet<>();
        storedObjectTags.add("objectTagA");
        storedObjectTags.add("objectTagB");
        storedObjectTags.add("objectTagB");
        when(catalogObjectRepository.findAllObjectTags()).thenReturn(storedObjectTags);
        Set<String> returnedObjectTags = catalogObjectService.getObjectTags();
        verify(catalogObjectRepository, times(1)).findAllObjectTags();
        assertThat(storedObjectTags).isEqualTo(returnedObjectTags);
    }

    @Test
    public void testCreateCatalogObject() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(objectNameValidator.isValid(anyString())).thenReturn(true);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity,
                                                                                         System.currentTimeMillis());
        when(catalogObjectRevisionRepository.save(any(CatalogObjectRevisionEntity.class))).thenReturn(catalogObjectEntity);
        when(workflowInfoAdder.addGenericInformationJobNameToRawObjectIfWorkflow(any(),
                                                                                 any(),
                                                                                 any(),
                                                                                 any())).thenReturn(new byte[] {});
        when(workflowInfoAdder.addAttributeToRawObjectIfWorkflow(any(), any(), any(), any())).thenReturn(new byte[] {});
        List<Metadata> keyValues = ImmutableList.of(new Metadata("key", "value", null));

        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject("bucket",
                                                                                       NAME,
                                                                                       PROJECT_NAME,
                                                                                       TAGS,
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
        assertThat(catalogObject.getProjectName()).isEqualTo(PROJECT_NAME);
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
                                                  Optional.empty(),
                                                  Optional.empty(),
                                                  Optional.empty(),
                                                  USERNAME);
    }

    @Test(expected = ObjectNameIsNotValidException.class)
    public void testCreateObjectWithInvalidName() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        CatalogObjectMetadata catalogObject = catalogObjectService.createCatalogObject("bucket",
                                                                                       "bad/name",
                                                                                       PROJECT_NAME,
                                                                                       TAGS,
                                                                                       OBJECT,
                                                                                       COMMIT_MESSAGE,
                                                                                       USERNAME,
                                                                                       APPLICATION_XML,
                                                                                       new LinkedList<>(),
                                                                                       null,
                                                                                       null);

    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testUpdateObjectMetadataWithInvalidBucket() {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        when(objectNameValidator.isValid(anyString())).thenReturn(true);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);
        catalogObjectService.updateObjectMetadata("wrong-bucket",
                                                  NAME,
                                                  Optional.of("some-kind"),
                                                  Optional.of("some-contentType"),
                                                  Optional.of("some-projectName"),
                                                  Optional.of("some-tags"),
                                                  USERNAME);
    }

    @Test(expected = CatalogObjectNotFoundException.class)
    public void testUpdateObjectMetadataWithInvalidObjectName() {
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(null);
        when(objectNameValidator.isValid(anyString())).thenReturn(true);
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);
        catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                  "wrong-name",
                                                  Optional.of("some-kind"),
                                                  Optional.of("some-contentType"),
                                                  Optional.of("some-projectName"),
                                                  Optional.of("some-tags"),
                                                  USERNAME);
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
                                                                                        Optional.of("updated-contentType"),
                                                                                        Optional.of("some-projectName"),
                                                                                        Optional.of("some-tags"),
                                                                                        USERNAME);
    }

    @Test(expected = BucketNotFoundException.class)
    public void testGetCatalogObjectWithInvalidBucket() {
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(null);
        catalogObjectService.listCatalogObjects(Arrays.asList("wrong-bucket"), 0, Integer.MAX_VALUE);
    }

    @Test
    public void testUpdateObjectMetadata() throws InterruptedException {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        when(keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(anyList(),
                                                                                        any())).thenReturn(Collections.emptyList());
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);

        tinyWait();

        CatalogObjectMetadata catalogObject = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                        NAME,
                                                                                        Optional.of("updated-kind"),
                                                                                        Optional.of("updated-contentType"),
                                                                                        Optional.of("updated-projectName"),
                                                                                        Optional.of("updated-tags"),
                                                                                        USERNAME);

        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getCommitMessage()).isEqualTo(UPDATE_COMMIT_MESSAGE);
        assertThat(catalogObject.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObject.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObject.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObject.getProjectName()).isEqualTo("updated-projectName");
        assertThat(catalogObject.getTags()).isEqualTo("updated-tags");
        assertThat(catalogObject.getName()).isEqualTo(NAME);
        assertThat(catalogObject.getMetadataList()).isNotEmpty();
        assertThat(catalogObject.getMetadataList()).hasSize(2);
        assertThat(catalogObject.getCommitTimeRaw()).isNotEqualTo(String.valueOf(now));
    }

    private void tinyWait() throws InterruptedException {
        // a necessary sleep to avoid that the new revision share the same commit time (and thus is discarded)
        Thread.sleep(2);
    }

    @Test
    public void testUpdateObjectMetadataOnlyKindOrOnlyContentTypeOrOnlyProjectNameOrTags() throws InterruptedException {
        long now = System.currentTimeMillis();
        BucketEntity bucketEntity = new BucketEntity("bucket", "toto");
        CatalogObjectRevisionEntity catalogObjectEntity = newCatalogObjectRevisionEntity(bucketEntity, now);
        when(bucketRepository.findOneByBucketName(anyString())).thenReturn(bucketEntity);
        when(catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(anyList(),
                                                                                    anyString())).thenReturn(catalogObjectEntity);
        when(keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(anyList(),
                                                                                        any())).thenReturn(Collections.emptyList());
        when(kindAndContentTypeValidator.isValid(anyString())).thenReturn(true);
        when(tagsValidator.isValid(anyString())).thenReturn(true);

        tinyWait();

        // only kind should be updated without changing contentType, projectName and tags
        CatalogObjectMetadata catalogObjectUpdatedKind = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                   NAME,
                                                                                                   Optional.of("updated-kind"),
                                                                                                   Optional.empty(),
                                                                                                   Optional.of(PROJECT_NAME),
                                                                                                   Optional.of(TAGS),
                                                                                                   USERNAME);

        assertThat(catalogObjectUpdatedKind).isNotNull();
        assertThat(catalogObjectUpdatedKind.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedKind.getUsername()).isEqualTo(USERNAME);
        assertThat(catalogObjectUpdatedKind.getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObjectUpdatedKind.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedKind.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(catalogObjectUpdatedKind.getTags()).isEqualTo(TAGS);
        assertThat(catalogObjectUpdatedKind.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedKind.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedKind.getMetadataList()).hasSize(1);
        assertThat(catalogObjectUpdatedKind.getCommitTimeRaw()).isEqualTo(String.valueOf(now));

        tinyWait();

        // only contentType should be updated without changing kind, projectName and tags
        CatalogObjectMetadata catalogObjectUpdatedContentType = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                          NAME,
                                                                                                          Optional.empty(),
                                                                                                          Optional.of("updated-contentType"),
                                                                                                          Optional.of(PROJECT_NAME),
                                                                                                          Optional.of(TAGS),
                                                                                                          USERNAME);

        assertThat(catalogObjectUpdatedContentType).isNotNull();
        assertThat(catalogObjectUpdatedContentType.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedContentType.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObjectUpdatedContentType.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectUpdatedContentType.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedContentType.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(catalogObjectUpdatedContentType.getTags()).isEqualTo(TAGS);
        assertThat(catalogObjectUpdatedContentType.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedContentType.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedContentType.getMetadataList()).hasSize(1);
        assertThat(catalogObjectUpdatedContentType.getCommitTimeRaw()).isEqualTo(String.valueOf(now));

        tinyWait();

        // only projectName should be updated without changing kind, contentType and tags
        CatalogObjectMetadata catalogObjectUpdatedProjectName = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                          NAME,
                                                                                                          Optional.of("updated-kind"),
                                                                                                          Optional.of("updated-contentType"),
                                                                                                          Optional.of("updated-projectName"),
                                                                                                          Optional.of(TAGS),
                                                                                                          USERNAME);

        assertThat(catalogObjectUpdatedProjectName).isNotNull();
        assertThat(catalogObjectUpdatedProjectName.getCommitMessage()).isEqualTo(UPDATE_COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedProjectName.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObjectUpdatedProjectName.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectUpdatedProjectName.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedProjectName.getProjectName()).isEqualTo("updated-projectName");
        assertThat(catalogObjectUpdatedProjectName.getTags()).isEqualTo(TAGS);
        assertThat(catalogObjectUpdatedProjectName.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedProjectName.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedProjectName.getMetadataList()).hasSize(3);
        assertThat(catalogObjectUpdatedProjectName.getCommitTimeRaw()).isNotEqualTo(String.valueOf(now));

        tinyWait();

        // only tags should be updated without changing kind and contentType and projectName
        CatalogObjectMetadata catalogObjectUpdatedTags = catalogObjectService.updateObjectMetadata(bucketEntity.getBucketName(),
                                                                                                   NAME,
                                                                                                   Optional.of("updated-kind"),
                                                                                                   Optional.of("updated-contentType"),
                                                                                                   Optional.of("updated-projectName"),
                                                                                                   Optional.of("updated-tags"),
                                                                                                   USERNAME);

        assertThat(catalogObjectUpdatedTags).isNotNull();
        assertThat(catalogObjectUpdatedTags.getCommitMessage()).isEqualTo(UPDATE_COMMIT_MESSAGE);
        assertThat(catalogObjectUpdatedTags.getUsername()).isEqualTo(USERNAME);

        assertThat(catalogObjectUpdatedTags.getContentType()).isEqualTo("updated-contentType");
        assertThat(catalogObjectUpdatedTags.getKind()).isEqualTo("updated-kind");
        assertThat(catalogObjectUpdatedTags.getProjectName()).isEqualTo("updated-projectName");
        assertThat(catalogObjectUpdatedTags.getTags()).isEqualTo("updated-tags");
        assertThat(catalogObjectUpdatedTags.getName()).isEqualTo(NAME);
        assertThat(catalogObjectUpdatedTags.getMetadataList()).isNotEmpty();
        assertThat(catalogObjectUpdatedTags.getMetadataList()).hasSize(2);
        assertThat(catalogObjectUpdatedTags.getCommitTimeRaw()).isNotEqualTo(String.valueOf(now));
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
                                                                                                                     .kindLower(kind)
                                                                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                                                                        dependency1Name))
                                                                                                                     .nameLower(dependency1Name)
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
                                                                     .nameLower("catalog")
                                                                     .kind("object")
                                                                     .kindLower("object")
                                                                     .contentType("application/xml")
                                                                     .contentTypeLower("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueLabelMetadataEntity> keyvalues = ImmutableList.of(new KeyValueLabelMetadataEntity("key",
                                                                                                       "value",
                                                                                                       null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .projectName(PROJECT_NAME)
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
                                                                     .nameLower("catalog")
                                                                     .kind("object")
                                                                     .kindLower("object")
                                                                     .bucket(bucketEntity)
                                                                     .contentType("application/xml")
                                                                     .contentTypeLower("application/xml")
                                                                     .lastCommitTime(now)
                                                                     .build();
        List<KeyValueLabelMetadataEntity> keyvalues = ImmutableList.of(new KeyValueLabelMetadataEntity("key1",
                                                                                                       "value2",
                                                                                                       null));
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .projectName(PROJECT_NAME)
                                                                                             .tags(TAGS)
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
        catalogObjectService.createCatalogObjectRevision("bucket",
                                                         NAME,
                                                         PROJECT_NAME,
                                                         TAGS,
                                                         COMMIT_MESSAGE,
                                                         USERNAME,
                                                         null);
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
                                                                                               PROJECT_NAME,
                                                                                               TAGS,
                                                                                               COMMIT_MESSAGE,
                                                                                               USERNAME,
                                                                                               keyvalues,
                                                                                               null);
        assertThat(catalogObject).isNotNull();
        assertThat(catalogObject.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(catalogObject.getTags()).isEqualTo(TAGS);
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
        assertThat(catalogObjectRevisionEntity.getProjectName()).isEqualTo(PROJECT_NAME);
        assertThat(catalogObjectRevisionEntity.getTags()).isEqualTo(TAGS);
        assertThat(catalogObjectRevisionEntity.getCommitMessage()).isEqualTo(COMMIT_MESSAGE);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getContentType()).isEqualTo(APPLICATION_XML);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getKind()).isEqualTo(OBJECT);
        assertThat(catalogObjectRevisionEntity.getCatalogObject().getId().getName()).isEqualTo(NAME);
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).isNotEmpty();
        assertThat(catalogObjectRevisionEntity.getKeyValueMetadataList()).hasSize(1);
    }
}
