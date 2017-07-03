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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.CatalogRawObject;
import org.ow2.proactive.catalog.dto.KeyValueMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.entity.KeyValueMetadataEntity;
import org.ow2.proactive.catalog.rest.controller.CatalogObjectRevisionController;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.RevisionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
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

    public CatalogObjectService() {
    }

    @Autowired
    public CatalogObjectService(CatalogObjectRepository catalogObjectRepository,
            CatalogObjectRevisionRepository catalogObjectRevisionRepository, BucketRepository bucketRepository) {
        this.catalogObjectRepository = catalogObjectRepository;
        this.catalogObjectRevisionRepository = catalogObjectRevisionRepository;
        this.bucketRepository = bucketRepository;
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
            String contentType, List<KeyValueMetadata> keyValueMetadataList, byte[] rawObject) {

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
                                                                              keyValueMetadataList,
                                                                              rawObject,
                                                                              catalogObjectEntity);

        return new CatalogObjectMetadata(result);
    }

    private CatalogObjectRevisionEntity buildCatalogObjectRevisionEntity(String kind, String commitMessage,
            List<KeyValueMetadata> keyValueMetadataList, byte[] rawObject, CatalogObjectEntity catalogObjectEntity) {

        List<KeyValueMetadataEntity> keyValueMetadataEntities = KeyValueMetadataHelper.convertToEntity(keyValueMetadataList);

        List<KeyValueMetadataEntity> keyValues = CollectionUtils.isEmpty(keyValueMetadataList) ? KeyValueMetadataHelper.extractKeyValuesFromRaw(kind,
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
        return result.stream().map(entity -> {
            CatalogObjectMetadata catalogObjectMetadata = new CatalogObjectMetadata(entity);
            try {
                catalogObjectMetadata.add(createLink(bucketId,
                                                     catalogObjectMetadata.getName(),
                                                     entity.getCommitTime()));
            } catch (UnsupportedEncodingException e) {
                log.error("bucketId : {}, name : {}", bucketId, catalogObjectMetadata.getName(), e);
            }
            return catalogObjectMetadata;
        }).collect(Collectors.toList());
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
            List<KeyValueMetadata> keyValueMetadataListParsed, byte[] rawObject) {

        CatalogObjectEntity catalogObject = catalogObjectRepository.findOne(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                           name));

        if (catalogObject == null) {
            throw new CatalogObjectNotFoundException("bucketid : " + bucketId + " name : " + name);
        }

        CatalogObjectRevisionEntity revisionEntity = buildCatalogObjectRevisionEntity(catalogObject.getKind(),
                                                                                      commitMessage,
                                                                                      keyValueMetadataListParsed,
                                                                                      rawObject,
                                                                                      catalogObject);

        return new CatalogObjectMetadata(revisionEntity);
    }

    public List<CatalogObjectMetadata> listCatalogObjectRevisions(Long bucketId, String name) {

        CatalogObjectEntity list = catalogObjectRepository.readCatalogObjectRevisionsById(new CatalogObjectEntity.CatalogObjectEntityKey(bucketId,
                                                                                                                                         name));

        return list.getRevisions().stream().map(entity -> {
            CatalogObjectMetadata catalogObjectMetadata = new CatalogObjectMetadata(entity);
            try {
                catalogObjectMetadata.add(createLink(bucketId, name, entity.getCommitTime()));
            } catch (UnsupportedEncodingException e) {
                log.error("bucketId : {}, name : {}", bucketId, name, e);
            }
            return catalogObjectMetadata;
        }).collect(Collectors.toList());
    }

    public CatalogObjectMetadata getCatalogObjectRevision(Long bucketId, String name, long commitTime)
            throws UnsupportedEncodingException {
        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketId,
                                                                                                name,
                                                                                                commitTime);

        CatalogObjectMetadata objectMetadata = new CatalogObjectMetadata(revisionEntity);

        objectMetadata.add(createLink(bucketId, name, revisionEntity.getCommitTime()));

        return objectMetadata;
    }

    public CatalogRawObject getCatalogObjectRevisionRaw(Long bucketId, String name, long commitTime)
            throws UnsupportedEncodingException {

        CatalogObjectRevisionEntity revisionEntity = getCatalogObjectRevisionEntityByCommitTime(bucketId,
                                                                                                name,
                                                                                                commitTime);

        CatalogRawObject object = new CatalogRawObject(revisionEntity);

        object.add(createLink(bucketId, name, revisionEntity.getCommitTime()));

        return object;

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

    public Link createLink(Long bucketId, String name, long commitTime) throws UnsupportedEncodingException {
        ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(CatalogObjectRevisionController.class).getRaw(bucketId,
                                                                                                                    name,
                                                                                                                    commitTime));

        return new Link(controllerLinkBuilder.toString()).withRel("content");
    }

}
