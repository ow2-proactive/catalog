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
import java.util.*;
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
import org.ow2.proactive.catalog.service.exception.*;
import org.ow2.proactive.catalog.service.model.GenericInfoBucketData;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.FileNameAndContent;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.RevisionCommitMessageBuilder;
import org.ow2.proactive.catalog.util.SeparatorUtility;
import org.ow2.proactive.catalog.util.name.validator.KindAndContentTypeValidator;
import org.ow2.proactive.catalog.util.name.validator.ObjectNameValidator;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private WorkflowInfoAdder workflowInfoAdder;

    @Autowired
    private RevisionCommitMessageBuilder revisionCommitMessageBuilder;

    @Autowired
    private KindAndContentTypeValidator kindAndContentTypeValidator;

    @Autowired
    private ObjectNameValidator objectNameValidator;

    @Autowired
    private RestApiAccessService restApiAccessService;

    @Autowired
    private SeparatorUtility separatorUtility;

    @Autowired
    CatalogObjectGrantService catalogObjectGrantService;

    @Value("${kind.separator}")
    protected String kindSeparator;

    @VisibleForTesting
    static final String KIND_NOT_FOUND = "N/A";

    static final String PROJECT_NAME = "project_name";

    private AutoDetectParser mediaTypeFileParser = new AutoDetectParser();

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String projectName, String kind,
            String commitMessage, String username, String contentType, byte[] rawObject, String extension) {
        return this.createCatalogObject(bucketName,
                                        name,
                                        projectName,
                                        kind,
                                        commitMessage,
                                        username,
                                        contentType,
                                        Collections.emptyList(),
                                        rawObject,
                                        extension);
    }

    public List<CatalogObjectMetadata> createCatalogObjects(String bucketName, String projectName, String kind,
            String commitMessage, String username, byte[] zipArchive) {

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
                                                projectName,
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
                                                        projectName,
                                                        commitMessage,
                                                        username,
                                                        file.getContent());
            }
        }).collect(Collectors.toList());
    }

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String projectName, String kind,
            String commitMessage, String username, String contentType, List<Metadata> metadataList, byte[] rawObject,
            String extension) {
        if (!objectNameValidator.isValid(name)) {
            throw new ObjectNameIsNotValidException(name);
        }
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
                                                                     .contentTypeLower(contentType)
                                                                     .kind(kind)
                                                                     .kindLower(kind)
                                                                     .extension(extension)
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                        name))
                                                                     .nameLower(name)
                                                                     .build();
        bucketEntity.getCatalogObjects().add(catalogObjectEntity);
        CatalogObjectRevisionEntity result = buildCatalogObjectRevisionEntity(commitMessage,
                                                                              username,
                                                                              projectName,
                                                                              rawObject,
                                                                              catalogObjectEntity,
                                                                              metadataList);
        return new CatalogObjectMetadata(result);
    }

    private String getFileMimeType(FileNameAndContent file) {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(file.getContent()));
        Detector detector = mediaTypeFileParser.getDetector();
        org.apache.tika.metadata.Metadata md = new org.apache.tika.metadata.Metadata();
        md.set(org.apache.tika.metadata.TikaMetadataKeys.RESOURCE_NAME_KEY, file.getFileNameWithExtension());
        MediaType mediaType = MediaType.OCTET_STREAM;
        try {
            mediaType = detector.detect(is, md);
        } catch (IOException e) {
            log.warn("there is a problem of identifying mime type for the file from archive : " + file.getName(), e);
        }
        return mediaType.toString();
    }

    public CatalogObjectMetadata updateObjectMetadata(String bucketName, String name, Optional<String> kind,
            Optional<String> contentType, Optional<String> projectName) {
        findBucketByNameAndCheck(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = findCatalogObjectByNameAndBucketAndCheck(bucketName,
                                                                                                           name);

        if (!kind.isPresent() && !contentType.isPresent() && !projectName.isPresent()) {
            throw new WrongParametersException("at least one parameter should be present");
        }
        if (kind.isPresent() && !kindAndContentTypeValidator.isValid(kind.get())) {
            throw new KindOrContentTypeIsNotValidException(kind.get(), "kind");
        }
        if (contentType.isPresent() && !kindAndContentTypeValidator.isValid(contentType.get())) {
            throw new KindOrContentTypeIsNotValidException(contentType.get(), "Content-Type");
        }

        //synchronize project name values
        for (KeyValueLabelMetadataEntity metadata : catalogObjectRevisionEntity.getKeyValueMetadataList()) {
            if (metadata.getKey().equals(PROJECT_NAME)) {
                metadata.setValue(projectName.orElse(""));
            }
        }

        byte[] workflowWithSynchronizedProjectName = workflowInfoAdder.addProjectNameToRawObjectIfWorkflow(catalogObjectRevisionEntity.getRawObject(),
                                                                                                           kind.orElse(""),
                                                                                                           projectName.orElse(""));
        catalogObjectRevisionEntity.setProjectName(projectName.orElse(""));
        catalogObjectRevisionEntity.setRawObject(workflowWithSynchronizedProjectName);
        catalogObjectRevisionRepository.save(catalogObjectRevisionEntity);
        CatalogObjectEntity catalogObjectEntity = catalogObjectRevisionEntity.getCatalogObject();
        kind.ifPresent(catalogObjectEntity::setKind);
        kind.ifPresent(catalogObjectEntity::setKindLower);
        catalogObjectEntity.setNameLower(name);
        contentType.ifPresent(catalogObjectEntity::setContentType);
        contentType.ifPresent(catalogObjectEntity::setContentTypeLower);
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

        String dependOnBucketName;
        String dependOnObjectName;
        String dependOnKind;
        for (String dependOnBucketAndObjectName : dependsOnCatalogObjectsList) {
            String revisionCommitTimeOfDependsOnObject = catalogObjectRevisionRepository.findRevisionOfDependsOnCatalogObjectFromKeyLabelMetadata(bucketName,
                                                                                                                                                  name,
                                                                                                                                                  revisionCommitTime,
                                                                                                                                                  dependOnBucketAndObjectName);
            dependOnBucketName = separatorUtility.getSplitBySeparator(dependOnBucketAndObjectName).get(0);
            dependOnObjectName = separatorUtility.getSplitBySeparator(dependOnBucketAndObjectName).get(1);
            boolean isCatalogObjectExist = isDependsOnObjectExistInCatalog(dependOnBucketName,
                                                                           dependOnObjectName,
                                                                           revisionCommitTimeOfDependsOnObject);
            dependOnKind = isCatalogObjectExist ? getCatalogObjectMetadata(dependOnBucketName, dependOnObjectName)
                                                                                                                  .getKind()
                                                : KIND_NOT_FOUND;
            dependsOnBucketAndObjectNameList.add(new DependsOnCatalogObject(dependOnBucketAndObjectName,
                                                                            dependOnKind,
                                                                            String.valueOf(revisionCommitTime),
                                                                            isCatalogObjectExist));

        }
        String input = separatorUtility.getConcatWithSeparator(bucketName, name);
        List<CatalogObjectRevisionEntity> calledByCatalogObjectList = catalogObjectRevisionRepository.findCalledByCatalogObjectsFromKeyValueMetadata(input);
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

    protected CatalogObjectRevisionEntity findCatalogObjectByNameAndBucketAndCheck(String bucketName, String name) {
        CatalogObjectRevisionEntity catalogObject = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                           name);
        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException(bucketName, name);
        }
        return catalogObject;
    }

    private CatalogObjectRevisionEntity buildCatalogObjectRevisionEntity(final String commitMessage,
            final String username, final String projectName, final byte[] rawObject,
            final CatalogObjectEntity catalogObjectEntity, final List<Metadata> metadataList) {

        List<KeyValueLabelMetadataEntity> keyValueMetadataEntities = KeyValueLabelMetadataHelper.convertToEntity(metadataList);

        if (keyValueMetadataEntities == null) {
            throw new NullPointerException("Cannot build catalog object!");
        }

        List<KeyValueLabelMetadataEntity> catalogObjectMetadataEntities = new ArrayList<>();
        if (rawObject != null) {
            catalogObjectMetadataEntities = keyValueLabelMetadataHelper.extractKeyValuesFromRaw(catalogObjectEntity.getKind(),
                                                                                                rawObject);
        }

        //here the priority is given to the metadataList provided as a query param
        List<KeyValueLabelMetadataEntity> keyValues = CollectionUtils.isEmpty(metadataList) ? catalogObjectMetadataEntities
                                                                                            : keyValueMetadataEntities;
        GenericInfoBucketData genericInfoBucketData = createGenericInfoBucketData(catalogObjectEntity.getBucket());

        if (genericInfoBucketData == null) {
            throw new NullPointerException("Cannot build catalog object!");
        }

        List<KeyValueLabelMetadataEntity> genericInformationWithBucketDataList = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(keyValues,
                                                                                                                                                            genericInfoBucketData);

        String metadataProjectName = getProjectNameIfExistsOrEmptyString(metadataList);
        String workflowXmlProjectName = getProjectNameIfExistsOrEmptyString(KeyValueLabelMetadataHelper.convertFromEntity(catalogObjectMetadataEntities));

        //the project name is synchronized in the following order projectName > metadataProjectName > workflowXmlProjectName
        String synchronizedProjectName = "";
        if (!projectName.isEmpty()) {
            synchronizedProjectName = projectName;
        } else if (!metadataProjectName.isEmpty()) {
            synchronizedProjectName = metadataProjectName;
        } else {
            synchronizedProjectName = workflowXmlProjectName;
        }
        //synchronize project name values
        for (KeyValueLabelMetadataEntity metadata : genericInformationWithBucketDataList) {
            if (metadata.getKey().equals(PROJECT_NAME)) {
                metadata.setValue(synchronizedProjectName);
            }
        }

        byte[] workflowWithReplacedGenericInfo = workflowInfoAdder.addGenericInformationJobNameToRawObjectIfWorkflow(rawObject,
                                                                                                                     catalogObjectEntity.getKind(),
                                                                                                                     keyValueLabelMetadataHelper.toMap(keyValueLabelMetadataHelper.getOnlyGenericInformation(genericInformationWithBucketDataList)),
                                                                                                                     catalogObjectEntity.getId()
                                                                                                                                        .getName());
        byte[] workflowWithSynchronizedProjectName = workflowInfoAdder.addProjectNameToRawObjectIfWorkflow(workflowWithReplacedGenericInfo,
                                                                                                           catalogObjectEntity.getKind(),
                                                                                                           synchronizedProjectName);

        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(commitMessage)
                                                                                             .username(username)
                                                                                             .projectName(synchronizedProjectName)
                                                                                             .commitTime(LocalDateTime.now()
                                                                                                                      .atZone(ZoneId.systemDefault())
                                                                                                                      .toInstant()
                                                                                                                      .toEpochMilli())
                                                                                             .keyValueMetadataList(genericInformationWithBucketDataList)
                                                                                             .rawObject(workflowWithSynchronizedProjectName)
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

    public List<CatalogObjectMetadata> listCatalogObjects(List<String> bucketNames, int pageNo, int pageSize) {
        bucketNames.forEach(this::findBucketByNameAndCheck);
        List<CatalogObjectRevisionEntity> result = listCatalogObjectsEntities(bucketNames, pageNo, pageSize);

        return buildMetadataWithLink(result);
    }

    public List<CatalogObjectRevisionEntity> listCatalogObjectsEntities(List<String> bucketNames, int pageNo,
            int pageSize) {
        Pageable paging = new PageRequest(pageNo, pageSize);
        Page<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsInBucket(bucketNames,
                                                                                                                     paging);
        return result.getContent();
    }

    public List<CatalogObjectMetadata> listCatalogObjects(List<String> bucketsNames, Optional<String> kind,
            Optional<String> contentType) {
        return listCatalogObjects(bucketsNames, kind, contentType, Optional.empty(), 0, Integer.MAX_VALUE);
    }

    public List<CatalogObjectMetadata> listCatalogObjects(List<String> bucketsNames, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectNameFilter, int pageNo, int pageSize) {
        List<CatalogObjectMetadata> metadataList;
        if (kind.isPresent() || contentType.isPresent() || objectNameFilter.isPresent()) {
            metadataList = listCatalogObjectsByKindListAndContentTypeAndObjectName(bucketsNames,
                                                                                   kind.orElse(""),
                                                                                   contentType.orElse(""),
                                                                                   objectNameFilter.orElse(""),
                                                                                   pageNo,
                                                                                   pageSize);
        } else {
            metadataList = listCatalogObjects(bucketsNames, pageNo, pageSize);
        }
        return metadataList;
    }

    private List<CatalogObjectMetadata> buildMetadataWithLink(List<CatalogObjectRevisionEntity> result) {
        return result.stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    // find pageable catalog objects by kind(s) and Content-Type and objectName
    public List<CatalogObjectMetadata> listCatalogObjectsByKindListAndContentTypeAndObjectName(List<String> bucketNames,
            String kind, String contentType, String objectName, int pageNo, int pageSize) {
        bucketNames.forEach(this::findBucketByNameAndCheck);
        List<String> kindList = new ArrayList<>();
        if (!kind.isEmpty()) {
            kindList = Arrays.asList(kind.toLowerCase().split(","));
        } else {
            kindList.add("");
        }
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameInBucket(bucketNames,
                                                                                                                                                          kindList,
                                                                                                                                                          contentType,
                                                                                                                                                          objectName,
                                                                                                                                                          pageNo,
                                                                                                                                                          pageSize);

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
        return catalogObjectsNames.stream()
                                  .map(name -> catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                      name))
                                  .collect(Collectors.toList());
    }

    public CatalogObjectMetadata delete(String bucketName, String name) throws CatalogObjectNotFoundException {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        CatalogObjectMetadata catalogObjectMetadata = getCatalogObjectMetadata(bucketName, name);
        try {
            // Get the bucketId
            long bucketId = bucketEntity.getId();

            // Find the catalog object id
            List<String> bucketsName = new LinkedList<>();
            bucketsName.add(bucketName);
            CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                             name);
            long catalogObjectId = catalogObjectRevisionEntity.getId();

            //Delete all object grants
            catalogObjectGrantService.deleteAllCatalogObjectGrantsByBucketIdAndObjectId(bucketId, catalogObjectId);

            // Delete the catalog Object
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

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String projectName,
            String commitMessage, String username, byte[] rawObject) {
        return this.createCatalogObjectRevision(bucketName,
                                                name,
                                                projectName,
                                                commitMessage,
                                                username,
                                                Collections.emptyList(),
                                                rawObject);
    }

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String projectName,
            String commitMessage, String username, List<Metadata> metadataListParsed, byte[] rawObject) {

        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                           name));

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException(bucketName, name);
        }

        CatalogObjectRevisionEntity revisionEntity = buildCatalogObjectRevisionEntity(commitMessage,
                                                                                      username,
                                                                                      projectName,
                                                                                      rawObject,
                                                                                      catalogObject,
                                                                                      metadataListParsed);

        return new CatalogObjectMetadata(revisionEntity);
    }

    protected String getProjectNameIfExistsOrEmptyString(List<Metadata> metadataListParsed) {
        Optional<Metadata> projectNameIfExists = metadataListParsed.stream()
                                                                   .filter(property -> property.getKey()
                                                                                               .equals(PROJECT_NAME))
                                                                   .findAny();
        return projectNameIfExists.map(Metadata::getValue).orElse("");
    }

    public CatalogObjectMetadata createCatalogObjectRevision(CatalogObjectRevisionEntity catalogObjectRevision,
            String commitMessage) {
        return createCatalogObjectRevision(catalogObjectRevision.getCatalogObject().getBucket().getBucketName(),
                                           catalogObjectRevision.getCatalogObject().getId().getName(),
                                           catalogObjectRevision.getProjectName(),
                                           commitMessage,
                                           catalogObjectRevision.getUsername(),
                                           KeyValueLabelMetadataHelper.convertFromEntity(catalogObjectRevision.getKeyValueMetadataList()),
                                           catalogObjectRevision.getRawObject());
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

    public CatalogObjectMetadata restoreCatalogObject(String bucketName, String name, Long commitTime) {
        CatalogObjectRevisionEntity catalogObjectRevision = catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(Collections.singletonList(bucketName),
                                                                                                                                  name,
                                                                                                                                  commitTime);

        if (catalogObjectRevision == null) {
            throw new RevisionNotFoundException(bucketName, name, commitTime);
        }

        String restoreCommitMessage = revisionCommitMessageBuilder.build(catalogObjectRevision.getCommitMessage(),
                                                                         commitTime);

        List<Metadata> metadataList = KeyValueLabelMetadataHelper.convertFromEntity(catalogObjectRevision.getKeyValueMetadataList());

        CatalogObjectRevisionEntity restoredRevision = buildCatalogObjectRevisionEntity(restoreCommitMessage,
                                                                                        catalogObjectRevision.getUsername(),
                                                                                        catalogObjectRevision.getProjectName(),
                                                                                        catalogObjectRevision.getRawObject(),
                                                                                        catalogObjectRevision.getCatalogObject(),
                                                                                        metadataList);

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

        List<CatalogObjectNameReference> catalogObjectsNameReferenceByKindAndContentType = generateCatalogObjectsNameReferenceByKind(catalogObjectRevisionRepository.findCatalogObjectNameReferenceByKindAndContentType(kind.orElse(""),
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

    private List<CatalogObjectNameReference> generateCatalogObjectsNameReferenceByKind(
            List<CatalogObjectRevisionEntity> catalogObjectsRevisionEntityList) {

        return buildMetadataWithLink(catalogObjectsRevisionEntityList).stream()
                                                                      .map(item -> new CatalogObjectNameReference(item.getBucketName(),
                                                                                                                  item.getProjectName(),
                                                                                                                  item.getName()))
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
