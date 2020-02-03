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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketNameIsNotValidException;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DeleteNonEmptyBucketException;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Service
@Transactional
public class BucketService {

    public static final String DEFAULT_BUCKET_OWNER = OwnerGroupStringHelper.GROUP_PREFIX + "public-objects";

    protected static final String COMMIT_MESSAGE_UPDATE_BUCKET = "Update the bucket owner";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private BucketNameValidator bucketNameValidator;

    @Autowired
    private OwnerGroupStringHelper ownerGroupStringHelper;

    @Autowired
    CatalogObjectService catalogObjectService;

    public BucketMetadata createBucket(String name) {
        return createBucket(name, DEFAULT_BUCKET_OWNER);
    }

    public BucketMetadata createBucket(String name, String owner) throws DataIntegrityViolationException {
        if (!bucketNameValidator.isValid(name)) {
            throw new BucketNameIsNotValidException(name);
        }

        BucketEntity bucketEntity = new BucketEntity(name, owner);

        bucketEntity = bucketRepository.save(bucketEntity);
        return new BucketMetadata(bucketEntity, 0);
    }

    public BucketMetadata updateOwnerByBucketName(String bucketName, String owner)
            throws DataIntegrityViolationException {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        bucketEntity.setOwner(owner);

        bucketEntity = bucketRepository.save(bucketEntity);

        createRevisionForObjects(bucketName, COMMIT_MESSAGE_UPDATE_BUCKET);

        return new BucketMetadata(bucketEntity, bucketEntity.getCatalogObjects().size());
    }

    //create a new revision for objects when the bucket owner is updated
    protected void createRevisionForObjects(String bucketName, String commitMessage) {
        List<CatalogObjectRevisionEntity> objectsList = catalogObjectService.listCatalogObjectsEntities(Arrays.asList(bucketName));

        objectsList.forEach(obj -> catalogObjectService.createCatalogObjectRevision(obj, commitMessage));
    }

    public BucketMetadata getBucketMetadata(String bucketName) {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        return new BucketMetadata(bucketEntity);
    }

    public List<BucketMetadata> listBuckets(List<String> owners, Optional<String> kind, Optional<String> contentType,
            Optional<String> objectName) {
        if (owners == null) {
            return Collections.emptyList();
        }

        List<BucketMetadata> entities = getBucketEntities(owners, kind, contentType, objectName);

        log.info("Buckets count {}", entities.size());
        return entities;
    }

    private List<BucketMetadata> getBucketEntities(List<String> owners, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName) {
        List<BucketMetadata> entities;
        if (kind.isPresent() || contentType.isPresent() || objectName.isPresent()) {
            entities = generateBucketMetadataListFromObject(bucketRepository.findByOwnerIsInContainingKindAndContentTypeAndObjectName(owners,
                                                                                                                                      kind.orElse(""),
                                                                                                                                      contentType.orElse(""),
                                                                                                                                      objectName.orElse("")));

        } else {
            entities = generateBucketMetadataList(bucketRepository.findByOwnerIn(owners));
        }
        return entities;
    }

    private List<BucketMetadata> generateBucketMetadataListFromObject(List<Object[]> bucketEntityWithContentCountList) {
        return bucketEntityWithContentCountList.stream()
                                               .map(bucketEntityWithContentCount -> new BucketMetadata((BucketEntity) bucketEntityWithContentCount[0],
                                                                                                       ((Long) bucketEntityWithContentCount[1]).intValue()))
                                               .collect(Collectors.toList());

    }

    private List<BucketMetadata> generateBucketMetadataList(List<BucketEntity> bucketEntityList) {
        return bucketEntityList.stream()
                               .map(bucketEntity -> new BucketMetadata(bucketEntity,
                                                                       bucketEntity.getCatalogObjects().size()))
                               .collect(Collectors.toList());

    }

    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType) {
        return listBuckets(ownerName, kind, contentType, Optional.empty());
    }

    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType,
            Optional<String> objectName) {
        List<BucketMetadata> entities;
        List<String> owners = Collections.singletonList(ownerName);

        if (!StringUtils.isEmpty(ownerName)) {
            entities = getBucketEntities(owners, kind, contentType, objectName);
        } else if (kind.isPresent() || contentType.isPresent() || objectName.isPresent()) {
            entities = generateBucketMetadataListFromObject(bucketRepository.findContainingKindAndContentTypeAndObjectName(kind.orElse(""),
                                                                                                                           contentType.orElse(""),
                                                                                                                           objectName.orElse("")));

        } else {
            entities = generateBucketMetadataList(bucketRepository.findAll());
        }

        log.info("Buckets count {}", entities.size());
        return entities;
    }

    public void cleanAllEmptyBuckets() {
        List<BucketEntity> emptyBucketsForUpdate = bucketRepository.findEmptyBucketsForUpdate();
        bucketRepository.deleteInBatch(emptyBucketsForUpdate);
    }

    public void cleanAll() {
        bucketRepository.deleteAll();
    }

    public BucketMetadata deleteEmptyBucket(String bucketName) {
        BucketEntity bucketEntity = bucketRepository.findBucketForUpdate(bucketName);

        if (bucketEntity == null) {
            throw new BucketNotFoundException(bucketName);
        }

        if (!bucketEntity.getCatalogObjects().isEmpty()) {
            throw new DeleteNonEmptyBucketException(bucketName);
        }
        bucketRepository.delete(bucketEntity.getId());
        return new BucketMetadata(bucketEntity);
    }

    private BucketEntity findBucketByNameAndCheck(String bucketName) {
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity == null) {
            throw new BucketNotFoundException(bucketName);
        }
        return bucketEntity;
    }

    public List<BucketMetadata> getBucketsByGroups(String ownerName, Optional<String> kind,
            Optional<String> contentType, Supplier<List<String>> authenticatedUserGroupsSupplier) {
        return getBucketsByGroups(ownerName, kind, contentType, Optional.empty(), authenticatedUserGroupsSupplier);
    }

    public List<BucketMetadata> getBucketsByGroups(String ownerName, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName,
            Supplier<List<String>> authenticatedUserGroupsSupplier)
            throws NotAuthenticatedException, AccessDeniedException {
        List<String> groups;

        if (ownerName == null) {
            groups = ownerGroupStringHelper.getGroupsWithPrefixFromGroupList(authenticatedUserGroupsSupplier.get());
            groups.add(BucketService.DEFAULT_BUCKET_OWNER);
        } else {
            groups = Collections.singletonList(ownerName);
        }

        return listBuckets(groups, kind, contentType, objectName);
    }
}
