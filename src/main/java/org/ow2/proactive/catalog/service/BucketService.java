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

import static org.ow2.proactive.catalog.dto.AssociationStatus.ALL;
import static org.ow2.proactive.catalog.dto.AssociationStatus.UNPLANNED;

import java.util.*;
import java.util.function.Function;
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
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Service
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

    @Value("${pa.catalog.tenant.filtering}")
    private boolean isTenantFiltering;

    public BucketMetadata createBucket(String name) {
        return createBucket(name, DEFAULT_BUCKET_OWNER, null);
    }

    public BucketMetadata createBucket(String name, String owner, String tenant)
            throws DataIntegrityViolationException {
        if (!bucketNameValidator.isValid(name)) {
            throw new BucketNameIsNotValidException(name);
        }

        BucketEntity bucketEntity = new BucketEntity(name, owner, tenant);

        bucketEntity = bucketRepository.save(bucketEntity);
        return new BucketMetadata(bucketEntity, 0);
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public BucketMetadata getBucketMetadata(String bucketName) {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        return new BucketMetadata(bucketEntity);
    }

    @Transactional(readOnly = true)
    public List<BucketMetadata> listBuckets(List<String> owners, String tenant, AuthenticatedUser user,
            Optional<String> kind, Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, Optional<String> projectName, Optional<String> lastCommitBy,
            Optional<String> committedAtLeastOnceBy, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, String sessionId, boolean allBuckets) {
        if (owners == null) {
            return Collections.emptyList();
        }

        List<BucketMetadata> entities = getBucketEntities(owners,
                                                          kind,
                                                          contentType,
                                                          objectName,
                                                          lastCommitTimeGreater,
                                                          lastCommitTimeLessThan,
                                                          allBuckets,
                                                          tenant,
                                                          user);

        // Consider now objectTag, association status, project name, last committed by, commit time range filters
        if (tag.isPresent() || associationStatus.isPresent() || projectName.isPresent() || lastCommitBy.isPresent() ||
            committedAtLeastOnceBy.isPresent() || lastCommitTimeGreater.isPresent() ||
            lastCommitTimeLessThan.isPresent()) {
            filterByTagOrAssociationStatusOrProjectNameOrLastCommittedBy(entities,
                                                                         convertKindFilterToList(kind),
                                                                         contentType,
                                                                         objectName,
                                                                         tag,
                                                                         associationStatus,
                                                                         projectName,
                                                                         lastCommitBy,
                                                                         committedAtLeastOnceBy,
                                                                         Optional.of(0L),
                                                                         Optional.of(0L),
                                                                         sessionId,
                                                                         allBuckets);
        }

        log.info("Buckets count {}", entities.size());
        return entities;
    }

    private List<BucketMetadata> getBucketEntities(List<String> owners, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, boolean allBuckets, String tenant, AuthenticatedUser user) {
        List<String> kindList = convertKindFilterToList(kind);
        long startTime = System.currentTimeMillis();
        List<Object[]> filteredBucketsFromDB = bucketRepository.findBucketByOwnerContainingKindListAndContentTypeAndObjectNameAndLastCommittedTimeInterval(owners,
                                                                                                                                                           kindList,
                                                                                                                                                           contentType.orElse(""),
                                                                                                                                                           objectName.orElse(""),
                                                                                                                                                           lastCommitTimeGreater.orElse(0L),
                                                                                                                                                           lastCommitTimeLessThan.orElse(0L),
                                                                                                                                                           tenant,
                                                                                                                                                           user);
        List<BucketEntity> allBucketsFromDB = getAllFilteredBucketsFromDB(allBuckets, tenant, user);
        log.debug("bucket list timer : get buckets : DB request with filtering {} ms",
                  (System.currentTimeMillis() - startTime));
        List<BucketMetadata> filteredEntities = generateBucketMetadataListFromObject(filteredBucketsFromDB);
        List<BucketMetadata> allEntities = allBuckets ? generateBucketMetadataList(allBucketsFromDB) : null;

        List<BucketMetadata> answer = mergeEntities(filteredEntities, allEntities);
        log.debug("bucket list timer : get buckets : total method time {} ms",
                  (System.currentTimeMillis() - startTime));

        return answer;
    }

    private List<BucketEntity> getAllFilteredBucketsFromDB(boolean allBuckets, String tenant, AuthenticatedUser user) {
        List<BucketEntity> allBucketsFromDB;
        if (isTenantFiltering && user != null && !user.isAllTenantAccess()) {
            allBucketsFromDB = getBucketsFilteredByTenant(tenant, user);
        } else {
            allBucketsFromDB = allBuckets ? bucketRepository.findAll() : null;
        }
        return allBucketsFromDB;
    }

    private List<BucketEntity> getBucketsFilteredByTenant(String tenant, AuthenticatedUser user) {
        List<BucketEntity> allBucketsFromDB;
        String userTenant = user.getTenant();
        boolean hasUserTenant = !Strings.isNullOrEmpty(userTenant);
        boolean hasTenant = !Strings.isNullOrEmpty(tenant);
        if (hasUserTenant && hasTenant) {
            allBucketsFromDB = bucketRepository.findByTenantIn(Arrays.asList(userTenant, tenant, null));
        } else if (hasUserTenant) {
            allBucketsFromDB = bucketRepository.findByTenantIn(Arrays.asList(userTenant, null));
        } else if (hasTenant) {
            allBucketsFromDB = bucketRepository.findByTenantIn(Arrays.asList(tenant, null));
        } else {
            allBucketsFromDB = bucketRepository.findByTenantIsNull();
        }
        return allBucketsFromDB;
    }

    private List<BucketMetadata> mergeEntities(List<BucketMetadata> filteredEntities,
            List<BucketMetadata> allEntities) {
        if (allEntities == null) {
            return filteredEntities;
        }
        allEntities.stream().forEach(bucketMetadata -> bucketMetadata.setObjectCount(0));
        return allEntities.stream()
                          .map(bucketMetadata -> filteredEntities.stream()
                                                                 .filter(filteredBucketMetadata -> filteredBucketMetadata.getName()
                                                                                                                         .equals(bucketMetadata.getName()))
                                                                 .findFirst()
                                                                 .orElse(bucketMetadata))
                          .collect(Collectors.toList());
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
                                                                                                       ((Long) bucketEntityWithContentCount[2]).intValue(),
                                                                                                       (String) bucketEntityWithContentCount[3]))
                                               .collect(Collectors.toList());

    }

    private List<BucketMetadata> generateBucketMetadataList(List<BucketEntity> bucketEntityList) {
        return bucketEntityList.stream()
                               .map(bucketEntity -> new BucketMetadata(bucketEntity,
                                                                       bucketEntity.getCatalogObjects().size()))
                               .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType) {
        return listBuckets(ownerName,
                           null,
                           null,
                           kind,
                           contentType,
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           Optional.empty(),
                           null);
    }

    @Transactional(readOnly = true)
    public List<BucketMetadata> listBuckets(String ownerName, String tenant, AuthenticatedUser user,
            Optional<String> kind, Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, Optional<String> projectName, Optional<String> lastCommitBy,
            Optional<String> committedAtLeastOnceBy, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, String sessionId) {

        return listBuckets(ownerName,
                           tenant,
                           user,
                           kind,
                           contentType,
                           objectName,
                           tag,
                           associationStatus,
                           projectName,
                           lastCommitBy,
                           committedAtLeastOnceBy,
                           lastCommitTimeGreater,
                           lastCommitTimeLessThan,
                           sessionId,
                           false);
    }

    @Transactional(readOnly = true)
    public List<BucketMetadata> listBuckets(String ownerName, String tenant, AuthenticatedUser user,
            Optional<String> kind, Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, Optional<String> projectName, Optional<String> lastCommitBy,
            Optional<String> committedAtLeastOnceBy, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, String sessionId, boolean allBuckets) {
        List<String> owners = StringUtils.isEmpty(ownerName) ? Collections.emptyList()
                                                             : Collections.singletonList(ownerName);

        return listBuckets(owners,
                           tenant,
                           user,
                           kind,
                           contentType,
                           objectName,
                           tag,
                           associationStatus,
                           projectName,
                           lastCommitBy,
                           committedAtLeastOnceBy,
                           lastCommitTimeGreater,
                           lastCommitTimeLessThan,
                           sessionId,
                           allBuckets);
    }

    private void filterByTagOrAssociationStatusOrProjectNameOrLastCommittedBy(List<BucketMetadata> entities,
            List<String> kindList, Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, Optional<String> projectName, Optional<String> lastCommitBy,
            Optional<String> committedAtLeastOnceBy, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, String sessionId, boolean allBuckets) {
        long startTime = System.currentTimeMillis();
        String tagFilter = tag.orElse(null);
        String associationStatusFilter = associationStatus.orElse(null);
        String projectNameFilter = projectName.orElse(null);
        String lastCommitByFilter = lastCommitBy.orElse(null);
        String committedAtLeastOnceByFilter = committedAtLeastOnceBy.orElse(null);
        Long lastCommitTimeGreaterFilter = lastCommitTimeGreater.orElse(0L);
        Long lastCommitTimeLessThanFilter = lastCommitTimeLessThan.orElse(0L);
        if (Strings.isNullOrEmpty(tagFilter) && Strings.isNullOrEmpty(associationStatusFilter) &&
            Strings.isNullOrEmpty(projectNameFilter) && Strings.isNullOrEmpty(lastCommitByFilter) &&
            Strings.isNullOrEmpty(committedAtLeastOnceByFilter)) {
            return;
        }
        List<String> bucketNames = entities.stream().map(BucketMetadata::getName).collect(Collectors.toList());
        List<String> objectNames = null;
        // search in the DB the latest revision of catalog objects which matching the other filters including the tag
        List<CatalogObjectRevisionEntity> objectList;
        List<AssociatedObjectsByBucket> associatedObjectsByBucketList = null;

        if (sessionId != null && associationStatus.isPresent()) {
            associatedObjectsByBucketList = jobPlannerService.getAssociatedObjects(sessionId);
            if (isFilteringEnabledAssociations(associationStatusFilter)) {
                bucketNames = new ArrayList<>(Sets.intersection(findAllBucketNamesContainingAssociations(associatedObjectsByBucketList),
                                                                new HashSet<>(bucketNames)));
                objectNames = new ArrayList<>(findAllObjectNamesWithAssociations(associatedObjectsByBucketList));
            }
        }

        objectList = catalogObjectRevisionRepository.findDefaultCatalogObjectsOfKindListAndContentTypeAndObjectNameAndTagInBucket(bucketNames,
                                                                                                                                  objectNames,
                                                                                                                                  kindList,
                                                                                                                                  contentType.orElse(null),
                                                                                                                                  objectName.orElse(null),
                                                                                                                                  projectNameFilter,
                                                                                                                                  lastCommitByFilter,
                                                                                                                                  committedAtLeastOnceByFilter,
                                                                                                                                  tagFilter,
                                                                                                                                  lastCommitTimeGreaterFilter,
                                                                                                                                  lastCommitTimeLessThanFilter,
                                                                                                                                  0,
                                                                                                                                  Integer.MAX_VALUE);
        // filter by association status if requested

        if (sessionId != null && associationStatus.isPresent()) {
            Map<String, AssociatedObjectsByBucket> associatedObjectsByBucketMap = associatedObjectsByBucketList.stream()
                                                                                                               .collect(Collectors.toMap(AssociatedObjectsByBucket::getBucketName,
                                                                                                                                         Function.identity()));
            objectList = objectList.stream()
                                   .filter(entity -> isObjectMatchingJobPlannerAssociationStatus(entity,
                                                                                                 associatedObjectsByBucketMap.get(entity.getCatalogObject()
                                                                                                                                        .getBucket()
                                                                                                                                        .getBucketName()),
                                                                                                 associationStatusFilter))
                                   .collect(Collectors.toList());
        }

        // group entities per bucket
        Map<String, List<CatalogObjectRevisionEntity>> objectsPerBucket = objectList.stream()
                                                                                    .collect(Collectors.groupingBy(obj -> obj.getCatalogObject()
                                                                                                                             .getBucket()
                                                                                                                             .getBucketName()));

        // updating the object counts based on the objects which satisfying also the tag filter
        for (Iterator<BucketMetadata> it = entities.iterator(); it.hasNext();) {
            BucketMetadata bucket = it.next();
            int objectsCount = 0;
            if (objectsPerBucket.containsKey(bucket.getName())) {
                objectsCount = objectsPerBucket.get(bucket.getName()).size();
            }
            if (objectsCount == 0 && !allBuckets) {
                it.remove();
            } else {
                bucket.setObjectCount(objectsCount);
            }
        }
        log.debug("bucket list timer : filter tags time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private boolean isFilteringEnabledAssociations(String associationFilter) {
        return !UNPLANNED.equalsIgnoreCase(associationFilter);
    }

    private Set<String>
            findAllBucketNamesContainingAssociations(List<AssociatedObjectsByBucket> associatedObjectsByBucketList) {
        return associatedObjectsByBucketList.stream().map(object -> object.getBucketName()).collect(Collectors.toSet());
    }

    private Set<String>
            findAllObjectNamesWithAssociations(List<AssociatedObjectsByBucket> associatedObjectsByBucketList) {
        return associatedObjectsByBucketList.stream()
                                            .map(objectsbyBucket -> objectsbyBucket.getObjects())
                                            .flatMap(List::stream)
                                            .map(object -> object.getObjectName().toLowerCase())
                                            .collect(Collectors.toSet());
    }

    private boolean isObjectMatchingJobPlannerAssociationStatus(CatalogObjectRevisionEntity entity,
            AssociatedObjectsByBucket associatedObjectsByBucket, String expectedStatus) {
        if (associatedObjectsByBucket == null) {
            return UNPLANNED.equalsIgnoreCase(expectedStatus);
        }
        switch (expectedStatus) {
            case ALL:
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .anyMatch(associatedObject -> associatedObject.getObjectName()
                                                                                              .equalsIgnoreCase(entity.getCatalogObject()
                                                                                                                      .getNameLower()) &&
                                                                              !associatedObject.getStatuses()
                                                                                               .isEmpty());
            case UNPLANNED:
                // object must not have a job-planner association
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .noneMatch(associatedObject -> associatedObject.getObjectName()
                                                                                               .equalsIgnoreCase(entity.getCatalogObject()
                                                                                                                       .getNameLower()));
            default:
                AssociationStatus associationStatus = AssociationStatus.convert(expectedStatus);
                return associatedObjectsByBucket.getObjects()
                                                .stream()
                                                .anyMatch(associatedObject -> associatedObject.getObjectName()
                                                                                              .equalsIgnoreCase(entity.getCatalogObject()
                                                                                                                      .getNameLower()) &&
                                                                              associatedObject.getStatuses()
                                                                                              .contains(associationStatus));
        }
    }

    @Transactional
    public List<String> getAllEmptyBuckets() {
        return bucketRepository.findEmptyBucketsForUpdate()
                               .stream()
                               .map(entity -> entity.getBucketName())
                               .collect(Collectors.toList());
    }

    @Transactional
    public void cleanAllEmptyBuckets() {
        List<BucketEntity> emptyBucketsForUpdate = bucketRepository.findEmptyBucketsForUpdate();
        bucketRepository.deleteInBatch(emptyBucketsForUpdate);
    }

    @Transactional
    public void cleanAll() {
        bucketRepository.deleteAll();
    }

    @Transactional
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

    @Transactional(readOnly = true)
    public List<BucketMetadata> getBucketsByGroups(String ownerName, Optional<String> kind,
            Optional<String> contentType, AuthenticatedUser user) {
        return getBucketsByGroups(ownerName,
                                  null,
                                  kind,
                                  contentType,
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  Optional.empty(),
                                  null,
                                  false,
                                  user);
    }

    @Transactional(readOnly = true)
    public List<BucketMetadata> getBucketsByGroups(String ownerName, String tenant, Optional<String> kind,
            Optional<String> contentType, Optional<String> objectName, Optional<String> tag,
            Optional<String> associationStatus, Optional<String> projectName, Optional<String> lastCommitBy,
            Optional<String> committedAtLeastOnceBy, Optional<Long> lastCommitTimeGreater,
            Optional<Long> lastCommitTimeLessThan, String sessionId, boolean allBuckets, AuthenticatedUser user)
            throws NotAuthenticatedException, AccessDeniedException {
        List<String> groups;
        long startTime = System.currentTimeMillis();

        if (ownerName == null) {
            if (user.isCatalogAdmin()) {
                groups = Collections.emptyList();
            } else {
                groups = ownerGroupStringHelper.getGroupsWithPrefixFromGroupList(user.getGroups());
                groups.add(BucketService.DEFAULT_BUCKET_OWNER);
            }
        } else {
            groups = Collections.singletonList(ownerName);
        }

        List<BucketMetadata> bucketsByGroups = listBuckets(groups,
                                                           tenant,
                                                           user,
                                                           kind,
                                                           contentType,
                                                           objectName,
                                                           tag,
                                                           associationStatus,
                                                           projectName,
                                                           lastCommitBy,
                                                           committedAtLeastOnceBy,
                                                           lastCommitTimeGreater,
                                                           lastCommitTimeLessThan,
                                                           sessionId,
                                                           allBuckets);
        log.debug("bucket list timer : get buckets by groups : " + (System.currentTimeMillis() - startTime) + " ms");
        return bucketsByGroups;
    }
}
