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

import static org.ow2.proactive.catalog.dto.AssociationStatus.UNPLANNED;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.catalog.dto.AssociatedObjectsByBucket;
import org.ow2.proactive.catalog.dto.AssociationStatus;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;

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

    protected final Sort sortById = new Sort(Sort.Direction.ASC, "id");

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private BucketNameValidator bucketNameValidator;

    @Autowired
    private OwnerGroupStringHelper ownerGroupStringHelper;

    @Autowired
    private BucketGrantService bucketGrantService;

    @Autowired
    CatalogObjectService catalogObjectService;

    @Autowired
    JobPlannerService jobPlannerService;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

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
        List<CatalogObjectRevisionEntity> objectsList = catalogObjectService.listCatalogObjectsEntities(Arrays.asList(bucketName),
                                                                                                        0,
                                                                                                        Integer.MAX_VALUE);

        objectsList.forEach(obj -> catalogObjectService.createCatalogObjectRevision(obj, commitMessage));
    }

    public BucketMetadata getBucketMetadata(String bucketName) {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        return new BucketMetadata(bucketEntity);
    }

    public List<BucketMetadata> listBuckets(List<String> owners, Optional<String> kind, Optional<String> contentType,
            Optional<String> objectName, Optional<String> tag, Optional<String> associationStatus, String sessionId) {
        if (owners == null) {
            return Collections.emptyList();
        }

        List<BucketMetadata> entities = getBucketEntities(owners, kind, contentType, objectName);

        // Consider now objectTag filter
        if (tag.isPresent() || associationStatus.isPresent()) {
            filterByTagOrAssociationStatus(entities,
                                           convertKindFilterToList(kind),
                                           contentType,
                                           objectName,
                                           tag,
                                           associationStatus,
                                           sessionId);
        }

        log.info("Buckets count {}", entities.size());
        return entities;
    }

    private List<BucketMetadata> getBucketEntities(List<String> owners, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName) {
        List<BucketMetadata> entities;
        List<String> kindList = convertKindFilterToList(kind);
        long startTime = System.currentTimeMillis();
        if (kind.isPresent() || contentType.isPresent() || objectName.isPresent()) {
            List<Object[]> bucketsFromDB;
            if (contentType.isPresent() || objectName.isPresent()) {
                bucketsFromDB = bucketRepository.findBucketByOwnerContainingKindListAndContentTypeAndObjectName(owners,
                                                                                                                kindList,
                                                                                                                contentType.orElse(""),
                                                                                                                objectName.orElse(""));
                log.debug("bucket list timer : get buckets : DB request with filtering all parameters" +
                          (System.currentTimeMillis() - startTime) + " ms");
            } else { // only kind.isPresent()
                bucketsFromDB = bucketRepository.findBucketByOwnerContainingKindList(owners, kindList);
                log.debug("bucket list timer : get buckets : DB request with filtering only KIND parameter" +
                          (System.currentTimeMillis() - startTime) + " ms");
            }
            entities = generateBucketMetadataListFromObject(bucketsFromDB);

        } else {
            List<BucketEntity> bucketsFromDB = bucketRepository.findByOwnerIn(owners, sortById);
            log.debug("bucket list timer : get buckets : DB request without filtering " +
                      (System.currentTimeMillis() - startTime) + " ms");
            entities = generateBucketMetadataList(bucketsFromDB);
        }
        log.debug("bucket list timer : get buckets : total method time " + (System.currentTimeMillis() - startTime) +
                  " ms");
        return entities;
    }

    private List<String> convertKindFilterToList(Optional<String> kindFilter) {
        List<String> kindList = new ArrayList<>();
        if (kindFilter.isPresent()) {
            kindList = Arrays.asList(kindFilter.get().toLowerCase().split(","));
        }
        return kindList;
    }

    private List<BucketMetadata> generateBucketMetadataListFromObject(List<Object[]> bucketEntityWithContentCountList) {
        return bucketEntityWithContentCountList.stream()
                                               .map(bucketEntityWithContentCount -> new BucketMetadata((String) bucketEntityWithContentCount[0],
                                                                                                       (String) bucketEntityWithContentCount[1],
                                                                                                       ((Long) bucketEntityWithContentCount[2]).intValue()))
                                               .collect(Collectors.toList());

    }

    private List<BucketMetadata> generateBucketMetadataList(List<BucketEntity> bucketEntityList) {
        return bucketEntityList.stream()
                               .map(bucketEntity -> new BucketMetadata(bucketEntity,
                                                                       bucketEntity.getCatalogObjects().size()))
                               .collect(Collectors.toList());

    }

    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType) {
        return listBuckets(ownerName, kind, contentType, Optional.empty(), Optional.empty(), Optional.empty(), null);
    }

    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType,
            Optional<String> objectName, Optional<String> tag, Optional<String> associationStatus, String sessionId) {
        List<BucketMetadata> entities;
        List<String> kindList = convertKindFilterToList(kind);
        List<String> owners = Collections.singletonList(ownerName);

        if (!StringUtils.isEmpty(ownerName)) {
            entities = getBucketEntities(owners, kind, contentType, objectName);
        } else if (kind.isPresent() || contentType.isPresent() || objectName.isPresent()) {
            List<Object[]> bucketsFromDB;
            if (contentType.isPresent() || objectName.isPresent()) {
                bucketsFromDB = bucketRepository.findBucketContainingKindListAndContentTypeAndObjectName(kindList,
                                                                                                         contentType.orElse(null),
                                                                                                         objectName.orElse(null));
            } else { // only kind.isPresent()
                bucketsFromDB = bucketRepository.findBucketContainingKindList(kindList);
            }
            entities = generateBucketMetadataListFromObject(bucketsFromDB);

        } else {
            entities = generateBucketMetadataList(bucketRepository.findAll(sortById));
        }

        // Consider now objectTag filter
        if (tag.isPresent() || associationStatus.isPresent()) {
            filterByTagOrAssociationStatus(entities,
                                           kindList,
                                           contentType,
                                           objectName,
                                           tag,
                                           associationStatus,
                                           sessionId);
        }

        return entities;
    }

    private void filterByTagOrAssociationStatus(List<BucketMetadata> entities, List<String> kindList,
            Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, String sessionId) {
        long startTime = System.currentTimeMillis();
        String tagFilter = tag.orElse(null);
        String associationStatusFilter = associationStatus.orElse(null);
        if (Strings.isNullOrEmpty(tagFilter) && Strings.isNullOrEmpty(associationStatusFilter)) {
            return;
        }
        List<String> bucketNames = entities.stream().map(BucketMetadata::getName).collect(Collectors.toList());
        // search in the DB the latest revision of catalog objects which matching the other filters including the tag
        List<CatalogObjectRevisionEntity> objectList;
        objectList = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameAndTagInBucket(bucketNames,
                                                                                                                                  kindList,
                                                                                                                                  contentType.orElse(null),
                                                                                                                                  objectName.orElse(null),
                                                                                                                                  tagFilter,
                                                                                                                                  0,
                                                                                                                                  Integer.MAX_VALUE);
        // filter by association status if requested

        if (sessionId != null && associationStatus.isPresent()) {
            List<AssociatedObjectsByBucket> associatedObjectsByBucketList = jobPlannerService.getAssociatedObjects(sessionId);
            Map<String, AssociatedObjectsByBucket> associatedObjectsByBucketMap = associatedObjectsByBucketList.stream()
                                                                                                               .collect(Collectors.toMap(AssociatedObjectsByBucket::getBucketName,
                                                                                                                                         Function.identity()));
            AssociationStatus associationStatusObject = UNPLANNED.equalsIgnoreCase(associationStatusFilter) ? null
                                                                                                            : AssociationStatus.convert(associationStatusFilter);
            objectList = objectList.stream()
                                   .filter(entity -> isObjectMatchingJobPlannerAssociationStatus(entity,
                                                                                                 associatedObjectsByBucketMap.get(entity.getCatalogObject()
                                                                                                                                        .getBucket()
                                                                                                                                        .getBucketName()),
                                                                                                 associationStatusObject))
                                   .collect(Collectors.toList());
        }

        // group entities per bucket
        Map<String, List<CatalogObjectRevisionEntity>> objectsPerBucket = objectList.stream()
                                                                                    .collect(Collectors.groupingBy(obj -> obj.getCatalogObject()
                                                                                                                             .getBucket()
                                                                                                                             .getBucketName()));

        // updating the object counts based on the objects which satisfying also the tag filter
        for (BucketMetadata bucket : entities) {
            int objectsCount = 0;
            if (objectsPerBucket.containsKey(bucket.getName())) {
                objectsCount = objectsPerBucket.get(bucket.getName()).size();
            }
            bucket.setObjectCount(objectsCount);
        }
        log.debug("bucket list timer : filter tags time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private boolean isObjectMatchingJobPlannerAssociationStatus(CatalogObjectRevisionEntity entity,
            AssociatedObjectsByBucket associatedObjectsByBucket, AssociationStatus associationStatus) {
        if (associatedObjectsByBucket == null) {
            // if no objects are associated in the bucket, then we return true only when searching for UNPLANNED jobs
            return associationStatus == null;
        }
        if (associationStatus == null) {
            // object must not have a job-planner association
            return associatedObjectsByBucket.getObjects()
                                            .stream()
                                            .noneMatch(associatedObject -> associatedObject.getObjectName()
                                                                                           .equalsIgnoreCase(entity.getCatalogObject()
                                                                                                                   .getNameLower()));
        }
        return associatedObjectsByBucket.getObjects()
                                        .stream()
                                        .anyMatch(associatedObject -> associatedObject.getObjectName()
                                                                                      .equalsIgnoreCase(entity.getCatalogObject()
                                                                                                              .getNameLower()) &&
                                                                      associatedObject.getStatuses()
                                                                                      .contains(associationStatus));
    }

    public List<String> getAllEmptyBuckets() {
        return bucketRepository.findEmptyBucketsForUpdate()
                               .stream()
                               .map(entity -> entity.getBucketName())
                               .collect(Collectors.toList());
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
        // Get the bucketId
        long bucketId = bucketEntity.getId();

        // Delete all bucket grants
        bucketGrantService.deleteAllGrantsAssignedToABucketAndItsObjects(bucketId);

        // Delete the bucket
        bucketRepository.delete(bucketId);

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
        return getBucketsByGroups(ownerName,
                                  kind,
                                  contentType,
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  null,
                                  authenticatedUserGroupsSupplier);
    }

    public List<BucketMetadata> getBucketsByGroups(String ownerName, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, String sessionId,
            Supplier<List<String>> authenticatedUserGroupsSupplier)
            throws NotAuthenticatedException, AccessDeniedException {
        List<String> groups;
        long startTime = System.currentTimeMillis();

        if (ownerName == null) {
            groups = ownerGroupStringHelper.getGroupsWithPrefixFromGroupList(authenticatedUserGroupsSupplier.get());
            groups.add(BucketService.DEFAULT_BUCKET_OWNER);
        } else {
            groups = Collections.singletonList(ownerName);
        }

        List<BucketMetadata> bucketsByGroups = listBuckets(groups,
                                                           kind,
                                                           contentType,
                                                           objectName,
                                                           tag,
                                                           associationStatus,
                                                           sessionId);
        log.debug("bucket list timer : get buckets by groups : " + (System.currentTimeMillis() - startTime) + " ms");
        return bucketsByGroups;
    }
}
