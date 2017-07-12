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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.dto.Metadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.DefaultCatalogObjectsFolderNotFoundException;
import org.ow2.proactive.catalog.service.exception.DefaultRawCatalogObjectsFolderNotFoundException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.ow2.proactive.catalog.util.CatalogObjectJSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;

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

    @Value("${pa.catalog.default.buckets}")
    private String[] defaultBucketNames;

    @Autowired
    private Environment environment;

    private static final String DEFAULT_BUCKET_OWNER = "object-catalog";

    private static final String DEFAULT_OBJECTS_FOLDER = "/default-objects";

    private static final String RAW_OBJECTS_FOLDER = "/raw-objects";

    public CatalogObjectService() {
    }

    @PostConstruct
    public void init() throws Exception {
        boolean isTestProfileEnabled = Arrays.stream(environment.getActiveProfiles()).anyMatch("test"::equals);

        // We define the initial start by no existing buckets in the Catalog
        // On initial start, we load the Catalog with predefined objects
        if (!isTestProfileEnabled && bucketRepository.count() == 0) {
            populateCatalog(defaultBucketNames, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);
        }
    }

    /**
     * The Catalog can be populated with buckets and objects all at once.
     *
     * @param bucketNames The array of bucket names to create
     * @param objectsFolder The folder that contains sub-folders of all objects to inject
     * @throws SecurityException if the Catalog is not allowed to read or access the file
     * @throws IOException if the file or folder could not be found or read properly
     */
    public void populateCatalog(String[] bucketNames, String objectsFolder, String rawObjectsFolder)
            throws SecurityException, IOException {
        for (String bucketName : bucketNames) {
            final Long bucketId = bucketRepository.save(new BucketEntity(bucketName, DEFAULT_BUCKET_OWNER)).getId();
            final URL folderResource = getClass().getResource(objectsFolder);
            if (folderResource == null) {
                throw new DefaultCatalogObjectsFolderNotFoundException();
            }

            final URL rawFolderResource = getClass().getResource(rawObjectsFolder);
            if (rawFolderResource == null) {
                throw new DefaultRawCatalogObjectsFolderNotFoundException();
            }

            final File bucketFolder = new File(folderResource.getPath() + File.separator + bucketName);
            if (bucketFolder.isDirectory()) {
                String[] wfs = bucketFolder.list();
                Arrays.sort(wfs);
                for (String object : wfs) {
                    FileInputStream fisobject = null;
                    try {
                        File catalogObjectFile = new File(bucketFolder.getPath() + File.separator + object);
                        CatalogObjectJSONParser.CatalogObjectData objectData = CatalogObjectJSONParser.parseJSONFile(catalogObjectFile);

                        File fobject = new File(rawFolderResource.getPath() + File.separator +
                                                objectData.getObjectFileName());
                        fisobject = new FileInputStream(fobject);
                        byte[] bObject = ByteStreams.toByteArray(fisobject);
                        createCatalogObject(bucketId,
                                            objectData.getName(),
                                            objectData.getKind(),
                                            objectData.getCommitMessage(),
                                            objectData.getContentType(),
                                            Collections.emptyList(),
                                            bObject);
                    } finally {
                        if (fisobject != null) {
                            fisobject.close();
                        }
                    }
                }
            }
        }
    }

    public CatalogObjectMetadata createCatalogObject(Long bucketId, String name, String kind, String commitMessage,
            String contentType, byte[] rawObject) {
        return this.createCatalogObject(bucketId,
                                        name,
                                        kind,
                                        commitMessage,
                                        contentType,
                                        Collections.emptyList(),
                                        rawObject);
    }

    public CatalogObjectMetadata createCatalogObject(Long bucketId, String name, String kind, String commitMessage,
            String contentType, List<Metadata> metadataList, byte[] rawObject) {

        BucketEntity bucketEntity = bucketRepository.findOne(bucketId);
        if (bucketEntity == null) {
            throw new BucketNotFoundException("Cannot find bucket with id : " + bucketId);
        }

        CatalogObjectEntity catalogObjectEntity = CatalogObjectEntity.builder()
                                                                     .bucket(bucketEntity)
                                                                     .contentType(contentType)
                                                                     .kind(kind)
                                                                     .id(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                        name))
                                                                     .build();
        bucketEntity.addCatalogObject(catalogObjectEntity);

        CatalogObjectRevisionEntity result = buildCatalogObjectRevisionEntity(kind,
                                                                              commitMessage,
                                                                              metadataList,
                                                                              rawObject,
                                                                              catalogObjectEntity);

        return new CatalogObjectMetadata(result);
    }

    private CatalogObjectRevisionEntity buildCatalogObjectRevisionEntity(String kind, String commitMessage,
            List<Metadata> metadataList, byte[] rawObject, CatalogObjectEntity catalogObjectEntity) {

        List<KeyValueMetadataEntity> keyValueMetadataEntities = KeyValueMetadataHelper.convertToEntity(metadataList);

        List<KeyValueMetadataEntity> keyValues = CollectionUtils.isEmpty(metadataList) ? KeyValueMetadataHelper.extractKeyValuesFromRaw(kind,
                                                                                                                                        rawObject)
                                                                                       : keyValueMetadataEntities;

        CatalogObjectRevisionEntity catalogObjectRevisionEntity = CatalogObjectRevisionEntity.builder()
                                                                                             .commitMessage(commitMessage)
                                                                                             .commitTime(LocalDateTime.now()
                                                                                                                      .atZone(ZoneId.systemDefault())
                                                                                                                      .toInstant()
                                                                                                                      .toEpochMilli())
                                                                                             .keyValueMetadataList(keyValues)
                                                                                             .rawObject(rawObject)
                                                                                             .catalogObject(catalogObjectEntity)
                                                                                             .build();

        keyValues.stream().forEach(keyValue -> keyValue.setCatalogObjectRevision(catalogObjectRevisionEntity));

        catalogObjectEntity.addRevision(catalogObjectRevisionEntity);

        return catalogObjectRevisionRepository.save(catalogObjectRevisionEntity);
    }

    public List<CatalogObjectMetadata> listCatalogObjects(Long bucketId) {
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsInBucket(bucketId);

        return buildMetadataWithLink(bucketId, result);
    }

    private List<CatalogObjectMetadata> buildMetadataWithLink(Long bucketId, List<CatalogObjectRevisionEntity> result) {
        return result.stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    public List<CatalogObjectMetadata> listCatalogObjectsByKind(Long bucketId, String kind) {
        List<CatalogObjectRevisionEntity> result = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindInBucket(bucketId,
                                                                                                                           kind);

        return buildMetadataWithLink(bucketId, result);
    }

    public void delete(Long bucketId, String name) throws CatalogObjectNotFoundException {
        try {
            catalogObjectRepository.delete(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId, name));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            log.warn("CatalogObject {} does not exist in bucket {}", name, bucketId);
            throw new CatalogObjectNotFoundException("name:" + name + " bucket id : " + bucketId);
        }
    }

    public CatalogObjectMetadata getCatalogObjectMetadata(Long bucketId, String name) {
        CatalogObjectRevisionEntity catalogObject = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketId,
                                                                                                                           name);

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException("bucketId : " + bucketId + " name : " + name);
        }

        return new CatalogObjectMetadata(catalogObject);
    }

    public CatalogRawObject getCatalogRawObject(Long bucketId, String name) {
        CatalogObjectRevisionEntity catalogObject = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketId,
                                                                                                                           name);

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException("bucketId : " + bucketId + " name : " + name);
        }

        return new CatalogRawObject(catalogObject);
    }

    /** ####################  Revision Operations ###################**/

    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String name, String commitMessage,
            byte[] rawObject) {
        return this.createCatalogObjectRevision(bucketId, name, commitMessage, Collections.emptyList(), rawObject);
    }

    public CatalogObjectMetadata createCatalogObjectRevision(Long bucketId, String name, String commitMessage,
            List<Metadata> metadataListParsed, byte[] rawObject) {

        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                           name));

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException("bucketid : " + bucketId + " name : " + name);
        }

        CatalogObjectRevisionEntity revisionEntity = buildCatalogObjectRevisionEntity(catalogObject.getKind(),
                                                                                      commitMessage,
                                                                                      metadataListParsed,
                                                                                      rawObject,
                                                                                      catalogObject);

        return new CatalogObjectMetadata(revisionEntity);
    }

    public List<CatalogObjectMetadata> listCatalogObjectRevisions(Long bucketId, String name) {

        CatalogObjectEntity list = catalogObjectRepository.readCatalogObjectRevisionsById(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                                         name));

        return list.getRevisions().stream().map(CatalogObjectMetadata::new).collect(Collectors.toList());
    }

    public CatalogObjectMetadata getCatalogObjectRevision(Long bucketId, String name, long commitTime)
            throws UnsupportedEncodingException {
        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketId,
                                                                                                name,
                                                                                                commitTime);

        return new CatalogObjectMetadata(revisionEntity);
    }

    public CatalogRawObject getCatalogObjectRevisionRaw(Long bucketId, String name, long commitTime)
            throws UnsupportedEncodingException {

        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketId,
                                                                                                name,
                                                                                                commitTime);

        return new CatalogRawObject(revisionEntity);

    }

    @VisibleForTesting
    protected CatalogObjectRevisionEntity getCatalogObjectRevisionEntityByCommitTime(Long bucketId, String name,
            long commitTime) {
        CatalogObjectRevisionEntity revisionEntity = catalogObjectRevisionRepository.findCatalogObjectRevisionByCommitTime(bucketId,
                                                                                                                           name,
                                                                                                                           commitTime);
        if (revisionEntity == null) {
            throw new RevisionNotFoundException("name : " + name + " commitTime : " + commitTime);
        }
        return revisionEntity;
    }

}
