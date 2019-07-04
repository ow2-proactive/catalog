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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.ow2.proactive.catalog.dto.CatalogObjectDependencies;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectNameReference;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.dto.DependsOnCatalogObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectAlreadyExistingException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.KindOrContentTypeIsNotValidException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.service.exception.UnprocessableEntityException;
import org.ow2.proactive.catalog.service.exception.WrongParametersException;
import org.ow2.proactive.catalog.service.model.GenericInfoBucketData;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.FileNameAndContent;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.RevisionCommitMessageBuilder;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.annotations.VisibleForTesting;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Service
@Transactional
public class CatalogObjectService {
    @Autowired
    private CatalogObjectRepository catalogObjectRepository;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private ArchiveManagerHelper archiveManager;

    @Autowired
    private KeyValueLabelMetadataHelper keyValueLabelMetadataHelper;

    @Autowired
    private GenericInformationAdder genericInformationAdder;

    @Autowired
    private RevisionCommitMessageBuilder revisionCommitMessageBuilder;

    @Autowired
    private KindAndContentTypeValidator kindAndContentTypeValidator;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private SeparatorUtility separatorUtility;

    @Value("${kind.separator}")
    protected String kindSeparator;

    private AutoDetectParser mediaTypeFileParser = new AutoDetectParser();

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String kind, String commitMessage,
            String username, String contentType, byte[] rawObject, String extension) {
        return this.createCatalogObject(bucketName,
                                        name,
                                        kind,
                                        commitMessage,
                                        username,
                                        contentType,
                                        Collections.emptyList(),
                                        rawObject,
                                        extension);
    }

    public List<CatalogObjectMetadata> createCatalogObjects(String bucketName, String kind, String commitMessage,
            String username, byte[] zipArchive) {

        List<FileNameAndContent> filesContainedInArchive = archiveManager.extractZIP(zipArchive);

        if (filesContainedInArchive.isEmpty()) {
            throw new UnprocessableEntityException("Malformed archive");
        }
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);

        return filesContainedInArchive.stream().map(file -> {
            String objectName = file.getName();
            CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                               objectName));
            if (catalogObject == null) {
                String contentTypeOfFile = getFileMimeType(file);
                return this.createCatalogObject(bucketName,
                                                objectName,
                                                kind,
                                                commitMessage,
                                                username,
                                                contentTypeOfFile,
                                                Collections.emptyList(),
                                                file.getContent(),
                                                FilenameUtils.getExtension(file.getFileNameWithExtension()));
            } else {
                return this.createCatalogObjectRevision(bucketName,
                                                        objectName,
                                                        commitMessage,
                                                        username,
                                                        file.getContent());
            }
        }).collect(Collectors.toList());
    }

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String kind, String commitMessage,
            String username, String contentType, List<Metadata> metadataList, byte[] rawObject, String extension) {
        if (!kindAndContentTypeValidator.isValid(kind)) {
            throw new KindOrContentTypeIsNotValidException(kind, "kind");
        }
        if (!kindAndContentTypeValidator.isValid(contentType)) {
            throw new KindOrContentTypeIsNotValidException(contentType, "Content-Type");
        }

        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);

        CatalogObjectRevisionEntity catalogObjectEntityCheck = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                                      name);
        if (catalogObjectEntityCheck != null) {
            throw new CatalogObjectAlreadyExistingException(bucketName, name);
        }

        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .bucket(bucketEntity)
                                                                     .contentType(contentType)
                                                                     .kind(kind)
                                                                     .extension(extension)
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                        name))
                                                                     .build();
        bucketEntity.getCatalogObjects().add(catalogObjectEntity);

        CatalogObjectRevisionEntity result = buildCatalogObjectRevisionEntity(commitMessage,
                                                                              username,
                                                                              metadataList,
                                                                              rawObject,
                                                                              catalogObjectEntity);

        return new CatalogObjectMetadata(result);
    }

    private String getFileMimeType(FileNameAndContent file) {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(file.getContent()));
        Detector detector = mediaTypeFileParser.getDetector();
        org.apache.tika.metadata.Metadata md = new org.apache.tika.metadata.Metadata();
        md.set(org.apache.tika.metadata.Metadata.RESOURCE_NAME_KEY, file.getFileNameWithExtension());
        MediaType mediaType = MediaType.OCTET_STREAM;
        try {
            mediaType = detector.detect(is, md);
        } catch (IOException e) {
            log.warn("there is a problem of identifying mime type for the file from archive : " + file.getName(), e);
        }
        return mediaType.toString();
    }

    public CatalogObjectMetadata updateObjectMetadata(String bucketName, String name, Optional<String> kind,
            Optional<String> contentType) {
        findBucketByNameAndCheck(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = findCatalogObjectByNameAndBucketAndCheck(bucketName,
                                                                                                           name);
        if (!kind.isPresent() && !contentType.isPresent()) {
            throw new WrongParametersException("at least one parameter should be present");
        }
        if (kind.isPresent() && !kindAndContentTypeValidator.isValid(kind.get())) {
            throw new KindOrContentTypeIsNotValidException(kind.get(), "kind");
        }
        if (contentType.isPresent() && !kindAndContentTypeValidator.isValid(contentType.get())) {
            throw new KindOrContentTypeIsNotValidException(contentType.get(), "Content-Type");
        }
        CatalogObjectEntity catalogObjectEntity = catalogObjectRevisionEntity.getCatalogObject();
        kind.ifPresent(catalogObjectEntity::setKind);
        contentType.ifPresent(catalogObjectEntity::setContentType);
        catalogObjectRepository.save(catalogObjectEntity);
        return new CatalogObjectMetadata(catalogObjectEntity);
    }

    /**
     * This methods computes the successor(s) (depends_on) and predecessor(s) (called_by) of a given catalog object
     *
     * @param bucketName
     * @param name
     * @param revisionCommitTime
     * @return the dependencies (dependsOn and calledBy) of a catalog object
     */

    protected CatalogObjectDependencies processObjectDependencies(String bucketName, String name,
            long revisionCommitTime) {
        List<String> dependsOnCatalogObjectsList = catalogObjectRevisionRepository.findDependsOnCatalogObjectNamesFromKeyValueMetadata(bucketName,
                                                                                                                                       name,
                                                                                                                                       revisionCommitTime);
        List<DependsOnCatalogObject> dependsOnBucketAndObjectNameList = new ArrayList<>();
        for (String dependOnCatalogObject : dependsOnCatalogObjectsList) {
            String revisionCommitTimeOfDependsOnObject = catalogObjectRevisionRepository.findRevisionOfDependsOnCatalogObjectFromKeyLabelMetadata(bucketName,
                                                                                                                                                  name,
                                                                                                                                                  revisionCommitTime,
                                                                                                                                                  dependOnCatalogObject);
            dependsOnBucketAndObjectNameList.add(new DependsOnCatalogObject(dependOnCatalogObject,
                                                                            String.valueOf(revisionCommitTime),
                                                                            isDependsOnObjectExistInCatalog(separatorUtility.getSplitBySeparator(dependOnCatalogObject)
                                                                                                                            .get(0),
                                                                                                            separatorUtility.getSplitBySeparator(dependOnCatalogObject)
                                                                                                                            .get(1),
                                                                                                            revisionCommitTimeOfDependsOnObject)));

        }
        List<CatalogObjectRevisionEntity> calledByCatalogObjectList = catalogObjectRevisionRepository.findCalledByCatalogObjectsFromKeyValueMetadata(separatorUtility.getConcatWithSeparator(bucketName,
                                                                                                                                                                                             name));
        List<String> calledByBucketAndObjectNameList = calledByCatalogObjectList.stream()
                                                                                .map(revisionEntity -> separatorUtility.getConcatWithSeparator(revisionEntity.getCatalogObject()
                                                                                                                                                             .getBucket()
                                                                                                                                                             .getBucketName(),
                                                                                                                                               revisionEntity.getCatalogObject()
                                                                                                                                                             .getId()
                                                                                                                                                             .getName()))
                                                                                .collect(Collectors.toList());

        return new CatalogObjectDependencies(dependsOnBucketAndObjectNameList, calledByBucketAndObjectNameList);
    }

    public boolean isDependsOnObjectExistInCatalog(String bucketName, String name,
            String revisionCommitTimeOfDependsOnObject) {
        CatalogObjectRevisionEntity catalogObjectRevisionEntity;
        if (revisionCommitTimeOfDependsOnObject.equals(WorkflowParser.LATEST_VERSION)) {
            catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                 name);
        } else {
            catalogObjectRevisionEntity = catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(Collections.singletonList(bucketName),
                                                                                                                name,
                                                                                                                Long.parseLong(revisionCommitTimeOfDependsOnObject));
        }

        return catalogObjectRevisionEntity != null;
    }

    /**
     *
     * @param bucketName
     * @param name
     * @param revisionCommitTime
     * @return  the dependencies (dependsOn and calledBy) of a catalog object
     */

    public CatalogObjectDependencies getObjectDependencies(String bucketName, String name, long revisionCommitTime) {
        // Check that the bucketName/name object exists in the catalog
        findCatalogObjectByNameAndBucketAndCheck(bucketName, name);
        return processObjectDependencies(bucketName, name, revisionCommitTime);
    }

    public CatalogObjectDependencies getObjectDependencies(String bucketName, String name) {
        // Check that the bucketName/name object exists in the catalog and retrieve the commit time
        CatalogObjectRevisionEntity catalogObject = findCatalogObjectByNameAndBucketAndCheck(bucketName, name);
        return processObjectDependencies(bucketName, name, catalogObject.getCommitTime());
    }

    private BucketEntity findBucketByNameAndCheck(String bucketName) {
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity == null) {
            throw new BucketNotFoundException(bucketName);
        }
        return bucketEntity;
    }

    private CatalogObjectRevisionEntity findCatalogObjectByNameAndBucketAndCheck(String bucketName, String name) {
        CatalogObjectRevisionEntity catalogObject = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                           name);
        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException(bucketName, name);
        }
        return catalogObject;
    }

    private CatalogObjectRevisionEntity buildCatalogObjectRevisionEntity(final String commitMessage,
            final String username, final List<org.ow2.proactive.catalog.dto.Metadata> metadataList,
            final byte[] rawObject, final CatalogObjectEntity catalogObjectEntity) {

        List<KeyValueLabelMetadataEntity> keyValueMetadataEntities = KeyValueLabelMetadataHelper.convertToEntity(metadataList);

        if (keyValueMetadataEntities == null) {
            throw new NullPointerException("Cannot build catalog object!");
        }

        List<KeyValueLabelMetadataEntity> keyValues = CollectionUtils.isEmpty(metadataList) ? keyValueLabelMetadataHelper.extractKeyValuesFromRaw(catalogObjectEntity.getKind(),
                                                                                                                                                  rawObject)
                                                                                            : keyValueMetadataEntities;

        GenericInfoBucketData genericInfoBucketData = createGenericInfoBucketData(catalogObjectEntity.getBucket());

        if (genericInfoBucketData == null) {
            throw new NullPointerException("Cannot build catalog object!");
        }

        List<KeyValueLabelMetadataEntity> genericInformationWithBucketDataList = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(keyValues,
                                                                                                                                                            genericInfoBucketData);
        byte[] workflowWithReplacedGenericInfo = genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(rawObject,
                                                                                                                    catalogObjectEntity.getKind(),
                                                                                                                    keyValueLabelMetadataHelper.toMap(keyValueLabelMetadataHelper.getOnlyGenericInformation(genericInformationWithBucketDataList)));

        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(commitMessage)
                                                                                             .username(username)
                                                                                             .commitTime(LocalDateTime.now()
                                                                                                                      .atZone(ZoneId.systemDefault())
                                                                                                                      .toInstant()
                                                                                                                      .toEpochMilli())
                                                                                             .keyValueMetadataList(genericInformationWithBucketDataList)
                                                                                             .rawObject(workflowWithReplacedGenericInfo)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();

        genericInformationWithBucketDataList.forEach(keyValue -> keyValue.setCatalogObjectRevision(catalogObjectRevisionEntity));

        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);

        return catalogObjectRevisionRepository.save(catalogObjectRevisionEntity);
    }

    private GenericInfoBucketData createGenericInfoBucketData(BucketEntity bucket) {
        if (bucket == null) {
            return GenericInfoBucketData.EMPTY;
        }
        return GenericInfoBucketData.builder().bucketName(bucket.getBucketName()).group(bucket.getOwner()).build();
    }

    public List<CatalogObjectMetadata> listCatalogObjects(List<String> bucketNames) {
        bucketNames.forEach(this::findBucketByNameAndCheck);
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsInBucket(bucketNames);

        return buildMetadataWithLink(result);
    }

    public List<CatalogObjectMetadata> listCatalogObjects(List<String> bucketsNames, Optional<String> kind,
            Optional<String> contentType) {
        if (bucketsNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<CatalogObjectMetadata> metadataList;

        if (kind.isPresent() || contentType.isPresent()) {
            metadataList = listCatalogObjectsByKindAndContentType(bucketsNames,
                                                                  kind.orElse(""),
                                                                  contentType.orElse(""));
        } else {
            metadataList = listCatalogObjects(bucketsNames);
        }
        return metadataList;
    }

    private List<CatalogObjectMetadata> buildMetadataWithLink(List<CatalogObjectRevisionEntity> result) {
        return result.stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    // find catalog objects by kind and Content-Type
    public List<CatalogObjectMetadata> listCatalogObjectsByKindAndContentType(List<String> bucketNames, String kind,
            String contentType) {
        bucketNames.forEach(this::findBucketByNameAndCheck);
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindAndContentTypeInBucket(bucketNames,
                                                                                                                                         kind,
                                                                                                                                         contentType);
        return buildMetadataWithLink(result);
    }

    public ZipArchiveContent getCatalogObjectsAsZipArchive(String bucketName, List<String> catalogObjectsNames) {
        List<CatalogObjectRevisionEntity> revisions = getCatalogObjects(bucketName, catalogObjectsNames);

        return archiveManager.compressZIP(revisions);
    }

    public List<CatalogObjectMetadata> listSelectedCatalogObjects(String bucketName, List<String> catalogObjectsNames) {
        List<CatalogObjectRevisionEntity> result = getCatalogObjects(bucketName, catalogObjectsNames);
        return buildMetadataWithLink(result);
    }

    private List<CatalogObjectRevisionEntity> getCatalogObjects(String bucketName, List<String> catalogObjectsNames) {
        findBucketByNameAndCheck(bucketName);
        List<CatalogObjectRevisionEntity> revisions = catalogObjectsNames.stream()
                                                                         .map(name -> catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                                                             name))
                                                                         .collect(Collectors.toList());
        return revisions;
    }

    public CatalogObjectMetadata delete(String bucketName, String name) throws CatalogObjectNotFoundException {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        CatalogObjectMetadata catalogObjectMetadata = getCatalogObjectMetadata(bucketName, name);
        try {
            catalogObjectRepository.delete(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(), name));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            log.warn("CatalogObject {} does not exist in bucket {}", name, bucketName);
            throw new CatalogObjectNotFoundException(bucketName, name);
        }
        return catalogObjectMetadata;
    }

    public CatalogObjectMetadata getCatalogObjectMetadata(String bucketName, String name) {
        return new CatalogObjectMetadata(findCatalogObjectByNameAndBucketAndCheck(bucketName, name));
    }

    public CatalogRawObject getCatalogRawObject(String bucketName, String name) {
        return new CatalogRawObject(findCatalogObjectByNameAndBucketAndCheck(bucketName, name));
    }

    /**
     * ####################  Revision Operations ###################
     **/

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String commitMessage,
            String username, byte[] rawObject) {
        return this.createCatalogObjectRevision(bucketName,
                                                name,
                                                commitMessage,
                                                username,
                                                Collections.emptyList(),
                                                rawObject);
    }

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String commitMessage,
            String username, List<Metadata> metadataListParsed, byte[] rawObject) {

        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                           name));

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException(bucketName, name);
        }

        CatalogObjectRevisionEntity revisionEntity = buildCatalogObjectRevisionEntity(commitMessage,
                                                                                      username,
                                                                                      metadataListParsed,
                                                                                      rawObject,
                                                                                      catalogObject);

        return new CatalogObjectMetadata(revisionEntity);
    }

    public List<CatalogObjectMetadata> listCatalogObjectRevisions(String bucketName, String name) {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        findCatalogObjectByNameAndBucketAndCheck(bucketName, name);
        CatalogObjectEntity list = catalogObjectRepository.readCatalogObjectRevisionsById(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                                         name));

        return list.getRevisions().stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    public CatalogObjectMetadata getCatalogObjectRevision(String bucketName, String name, long commitTime)
            throws UnsupportedEncodingException {
        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketName,
                                                                                                name,
                                                                                                commitTime);

        return new CatalogObjectMetadata(revisionEntity);
    }

    public CatalogRawObject getCatalogObjectRevisionRaw(String bucketName, String name, long commitTime)
            throws UnsupportedEncodingException {
        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketName,
                                                                                                name,
                                                                                                commitTime);

        return new CatalogRawObject(revisionEntity);

    }

    public CatalogObjectMetadata restore(String bucketName, String name, Long commitTime) {
        CatalogObjectRevisionEntity catalogObjectRevision = catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(Collections.singletonList(bucketName),
                                                                                                                                  name,
                                                                                                                                  commitTime);

        if (catalogObjectRevision == null) {
            throw new RevisionNotFoundException(bucketName, name, commitTime);
        }

        String restoreCommitMessage = revisionCommitMessageBuilder.build(catalogObjectRevision.getCommitMessage(),
                                                                         commitTime);

        CatalogObjectRevisionEntity restoredRevision = buildCatalogObjectRevisionEntity(restoreCommitMessage,
                                                                                        catalogObjectRevision.getUsername(),
                                                                                        keyValueLabelMetadataHelper.convertFromEntity(catalogObjectRevision.getKeyValueMetadataList()),
                                                                                        catalogObjectRevision.getRawObject(),
                                                                                        catalogObjectRevision.getCatalogObject());

        return new CatalogObjectMetadata(restoredRevision);
    }

    /**
     * Method returns all ordered stored kinds for all objects in catalog
     *
     * @return unique set of kinds with root kind
     * for example kinds: a/b, a/c, d/f/g
     * should return a, a/b, a/c, d, d/f, d/f/g
     */
    public TreeSet<String> getKinds() {
        Set<String> allStoredKinds = catalogObjectRepository.findAllKinds();
        TreeSet<String> resultKinds = new TreeSet<>();

        allStoredKinds.forEach(kind -> {
            String[] splittedKinds = kind.split(kindSeparator);
            StringBuilder rootKinds = new StringBuilder();
            for (int i = 0; i < splittedKinds.length - 1; i++) {
                rootKinds.append(splittedKinds[i]);
                resultKinds.add(rootKinds.toString());
                rootKinds.append(kindSeparator);
            }
            resultKinds.add(kind);
        });
        return resultKinds;
    }

    /**
     * @return all ordered Content-Types for all objects in catalog
     */
    public TreeSet<String> getContentTypes() {
        return new TreeSet<>(catalogObjectRepository.findAllContentTypes());
    }

    public List<CatalogObjectNameReference> getAccessibleCatalogObjectsNameReferenceByKindAndContentType(
            boolean sessionIdRequired, String sessionId, Optional<String> kind, Optional<String> contentType) {
        List<CatalogObjectNameReference> catalogObjectsNameReferenceByKindAndContentType = generateCatalogObjectsNameReferenceByKind(catalogObjectRepository.findCatalogObjectNameReferenceByKindAndContentType(kind.orElse(""),
                                                                                                                                                                                                                contentType.orElse("")));

        Map<String, List<CatalogObjectNameReference>> catalogObjectsGroupedByBucket = groupCatalogObjectsNameReferencePerBucket(catalogObjectsNameReferenceByKindAndContentType);
        List<BucketEntity> buckets = bucketRepository.findAll();

        return buckets.stream()
                      .filter(bucketEntity -> restApiAccessService.isBucketAccessibleByUser(sessionIdRequired,
                                                                                            sessionId,
                                                                                            bucketEntity.getBucketName()))
                      .map(bucketEntity -> catalogObjectsGroupedByBucket.get(bucketEntity.getBucketName()))
                      .filter(Objects::nonNull)
                      .flatMap(Collection::stream)
                      .collect(Collectors.toList());
    }

    private Map<String, List<CatalogObjectNameReference>>
            groupCatalogObjectsNameReferencePerBucket(List<CatalogObjectNameReference> catalogObjectsNameReferences) {

        return catalogObjectsNameReferences.stream()
                                           .collect(Collectors.groupingBy(CatalogObjectNameReference::getBucketName));
    }

    private List<CatalogObjectNameReference>
            generateCatalogObjectsNameReferenceByKind(List<Object[]> catalogObjectsNameReferenceByKind) {
        return catalogObjectsNameReferenceByKind.stream()
                                                .map(item -> new CatalogObjectNameReference(String.valueOf(item[0]),
                                                                                            String.valueOf(item[1])))
                                                .collect(Collectors.toList());

    }

    @VisibleForTesting
    protected CatalogObjectRevisionEntity getCatalogObjectRevisionEntityByCommitTime(String bucketName, String name,
            long commitTime) {
        CatalogObjectRevisionEntity revisionEntity = catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(Collections.singletonList(bucketName),
                                                                                                                           name,
                                                                                                                           commitTime);
        if (revisionEntity == null) {
            throw new RevisionNotFoundException(bucketName, name, commitTime);
        }
        return revisionEntity;
    }

}
