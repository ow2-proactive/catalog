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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.tika.detect.Detector;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
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
import org.ow2.proactive.catalog.service.exception.KindNameIsNotValidException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.service.exception.UnprocessableEntityException;
import org.ow2.proactive.catalog.service.exception.WrongParametersException;
import org.ow2.proactive.catalog.service.model.GenericInfoBucketData;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.FileNameAndContent;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;
import org.ow2.proactive.catalog.util.RevisionCommitMessageBuilder;
import org.ow2.proactive.catalog.util.name.validator.KindNameValidator;
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
    private KindNameValidator kindNameValidator;

    @Value("${kind.separator}")
    protected String kindSeparator;

    private AutoDetectParser mediaTypeFileParser = new AutoDetectParser();

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String kind, String commitMessage,
            String contentType, byte[] rawObject) {
        return this.createCatalogObject(bucketName,
                                        name,
                                        kind,
                                        commitMessage,
                                        contentType,
                                        Collections.emptyList(),
                                        rawObject);
    }

    public List<CatalogObjectMetadata> createCatalogObjects(String bucketName, String kind, String commitMessage,
            byte[] zipArchive) {

        List<FileNameAndContent> filesContainedInArchive = archiveManager.extractZIP(zipArchive);

        if (filesContainedInArchive.isEmpty()) {
            throw new UnprocessableEntityException("Malformed archive");
        }
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);

        return filesContainedInArchive.stream().map(file -> {
            CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                               file.getName()));
            if (catalogObject == null) {
                String contentTypeOfFile = getFileMimeType(file);
                return this.createCatalogObject(bucketName,
                                                file.getName(),
                                                kind,
                                                commitMessage,
                                                contentTypeOfFile,
                                                Collections.emptyList(),
                                                file.getContent());
            } else {
                return this.createCatalogObjectRevision(bucketName, file.getName(), commitMessage, file.getContent());
            }
        }).collect(Collectors.toList());
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
        if (kind.isPresent() && !kindNameValidator.isValid(kind.get())) {
            throw new KindNameIsNotValidException(kind.get());
        }
        CatalogObjectEntity catalogObjectEntity = catalogObjectRevisionEntity.getCatalogObject();
        kind.ifPresent(catalogObjectEntity::setKind);
        contentType.ifPresent(catalogObjectEntity::setContentType);
        catalogObjectRepository.save(catalogObjectEntity);
        return new CatalogObjectMetadata(catalogObjectEntity);
    }

    public CatalogObjectMetadata createCatalogObject(String bucketName, String name, String kind, String commitMessage,
            String contentType, List<Metadata> metadataList, byte[] rawObject) {
        if (!kindNameValidator.isValid(kind)) {
            throw new KindNameIsNotValidException(kind);
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
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                        name))
                                                                     .build();
        bucketEntity.getCatalogObjects().add(catalogObjectEntity);

        CatalogObjectRevisionEntity result = buildCatalogObjectRevisionEntity(commitMessage,
                                                                              metadataList,
                                                                              rawObject,
                                                                              catalogObjectEntity);

        return new CatalogObjectMetadata(result);
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
            final List<org.ow2.proactive.catalog.dto.Metadata> metadataList, final byte[] rawObject,
            final CatalogObjectEntity catalogObjectEntity) {

        List<KeyValueLabelMetadataEntity> keyValueMetadataEntities = KeyValueLabelMetadataHelper.convertToEntity(metadataList);

        List<KeyValueLabelMetadataEntity> keyValues = CollectionUtils.isEmpty(metadataList) ? keyValueLabelMetadataHelper.extractKeyValuesFromRaw(catalogObjectEntity.getKind(),
                                                                                                                                                  rawObject)
                                                                                            : keyValueMetadataEntities;

        GenericInfoBucketData genericInfoBucketData = createGenericInfoBucketData(catalogObjectEntity.getBucket());
        List<KeyValueLabelMetadataEntity> genericInformationWithBucketDataList = keyValueLabelMetadataHelper.replaceMetadataRelatedGenericInfoAndKeepOthers(keyValues,
                                                                                                                                                            genericInfoBucketData);
        byte[] workflowWithReplacedGenericInfo = genericInformationAdder.addGenericInformationToRawObjectIfWorkflow(rawObject,
                                                                                                                    catalogObjectEntity.getKind(),
                                                                                                                    keyValueLabelMetadataHelper.toMap(keyValueLabelMetadataHelper.getOnlyGenericInformation(genericInformationWithBucketDataList)));

        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(commitMessage)
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
        bucketNames.forEach(bucketName -> findBucketByNameAndCheck(bucketName));
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsInBucket(bucketNames);

        return buildMetadataWithLink(result);
    }

    private List<CatalogObjectMetadata> buildMetadataWithLink(List<CatalogObjectRevisionEntity> result) {
        return result.stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    public List<CatalogObjectMetadata> listCatalogObjectsByKind(List<String> bucketNames, String kind) {
        bucketNames.forEach(bucketName -> findBucketByNameAndCheck(bucketName));
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindInBucket(bucketNames,
                                                                                                                           kind);

        return buildMetadataWithLink(result);
    }

    // find catalog objects by kind and content type
    public List<CatalogObjectMetadata> listCatalogObjectsByKindAndContentType(List<String> bucketNames, String kind,
            String contentType) {
        bucketNames.forEach(bucketName -> findBucketByNameAndCheck(bucketName));
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindAndContentTypeInBucket(bucketNames,
                                                                                                                                         kind,
                                                                                                                                         contentType);

        return buildMetadataWithLink(result);
    }

    // find catalog objects by content type
    public List<CatalogObjectMetadata> listCatalogObjectsByContentType(List<String> bucketNames, String contentType) {
        bucketNames.forEach(bucketName -> findBucketByNameAndCheck(bucketName));
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfContentTypeInBucket(bucketNames,
                                                                                                                                  contentType);

        return buildMetadataWithLink(result);
    }

    public ZipArchiveContent getCatalogObjectsAsZipArchive(String bucketName, List<String> catalogObjectsNames) {
        findBucketByNameAndCheck(bucketName);
        List<CatalogObjectRevisionEntity> revisions = catalogObjectsNames.stream()
                                                                         .map(name -> catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                                                             name))
                                                                         .collect(Collectors.toList());

        return archiveManager.compressZIP(revisions);
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

    /** ####################  Revision Operations ###################**/

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String commitMessage,
            byte[] rawObject) {
        return this.createCatalogObjectRevision(bucketName, name, commitMessage, Collections.emptyList(), rawObject);
    }

    public CatalogObjectMetadata createCatalogObjectRevision(String bucketName, String name, String commitMessage,
            List<Metadata> metadataListParsed, byte[] rawObject) {

        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketEntity.getId(),
                                                                                                                           name));

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException(bucketName, name);
        }

        CatalogObjectRevisionEntity revisionEntity = buildCatalogObjectRevisionEntity(commitMessage,
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
                                                                                        keyValueLabelMetadataHelper.convertFromEntity(catalogObjectRevision.getKeyValueMetadataList()),
                                                                                        catalogObjectRevision.getRawObject(),
                                                                                        catalogObjectRevision.getCatalogObject());

        return new CatalogObjectMetadata(restoredRevision);
    }

    /**
     * Method returns all ordered stored kinds for all objects in catalog
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
