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

import static org.ow2.proactive.catalog.util.AccessType.*;

import java.util.*;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectGrantRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.*;
import org.ow2.proactive.catalog.service.exception.CatalogObjectGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Component
@Transactional
public class CatalogObjectGrantService {

    @Autowired
    private CatalogObjectGrantRepository catalogObjectGrantRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    @Autowired
    private GrantAccessTypeHelperService grantAccessTypeHelperService;

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param currentUser username of the admin creating this grant.
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @param username The name of the user that will benefit of the access grant.
     * @return a catalog object grant for a user
     * @throws DataIntegrityViolationException if a similar grant exists
     */
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAUser(String bucketName, String catalogObjectName,
            String currentUser, String accessType, String username) throws DataIntegrityViolationException {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and the catalog object
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        // Check if a similar grant exists
        CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUser = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                                                   username,
                                                                                                                                   bucket.getId());
        if (dbCatalogObjectGrantEntityForUser != null &&
            (dbCatalogObjectGrantEntityForUser.getGrantee().equals(username) &&
             dbCatalogObjectGrantEntityForUser.getCatalogObjectRevisionEntity()
                                              .getId()
                                              .equals(catalogObjectRevisionEntity.getId()))) {
            throw new CatalogObjectGrantAlreadyExistsException("Grant exists for object: " + catalogObjectName +
                                                               " in bucket: " + bucketName +
                                                               " and already assigned to user: " + username);
        }
        // Create new grant
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("user",
                                                                                         currentUser,
                                                                                         username,
                                                                                         accessType,
                                                                                         catalogObjectRevisionEntity,
                                                                                         bucket);
        // Save the grant
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        // Return the result
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param currentUser username of the admin creating this grant.
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @param userGroup The name of the group of users that will benefit of the access grant.
     * @return a catalog object grant for a group of users
     * @throws DataIntegrityViolationException if a similar grant exists
     */
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAGroup(String bucketName, String catalogObjectName,
            String currentUser, String accessType, String userGroup) throws DataIntegrityViolationException {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and the catalog object
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        // Check if a similar grant exists
        CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUserGroup = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                                                         userGroup,
                                                                                                                                         bucket.getId());
        if (dbCatalogObjectGrantEntityForUserGroup != null &&
            dbCatalogObjectGrantEntityForUserGroup.getGrantee().equals(userGroup) &&
            dbCatalogObjectGrantEntityForUserGroup.getCatalogObjectRevisionEntity()
                                                  .getId()
                                                  .equals(catalogObjectRevisionEntity.getId())) {
            throw new CatalogObjectGrantAlreadyExistsException("Grant exists for object: " + catalogObjectName +
                                                               " in bucket: " + bucketName +
                                                               " and already assigned to user group: " + userGroup);
        }
        // Create new grant
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity("group",
                                                                                         currentUser,
                                                                                         userGroup,
                                                                                         accessType,
                                                                                         catalogObjectRevisionEntity,
                                                                                         bucket);
        // Save the grant
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        // Return the result
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param username name of the user that is benefiting from the access grant.
     * @return the deleted object grant
     */
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAUser(String bucketName, String catalogObjectName,
            String username) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        assert bucket != null;
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                                          username,
                                                                                                                          bucket.getId());
        if (catalogObjectGrantEntity != null) {
            catalogObjectGrantRepository.delete(catalogObjectGrantEntity);
        } else {
            throw new DataIntegrityViolationException("Catalog object grant was not found in the DB.");
        }
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param userGroup name of the group of users that are benefiting of the access grant
     * @return the deleted object grants for a group
     */
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAGroup(String bucketName, String catalogObjectName,
            String userGroup) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        assert bucket != null;
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                                           userGroup,
                                                                                                                           bucket.getId());
        if (catalogObjectGrantEntity != null) {
            catalogObjectGrantRepository.delete(catalogObjectGrantEntity);
        } else {
            throw new DataIntegrityViolationException("Catalog object grant was not found in the DB.");
        }
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param username name of the user that is benefiting from the access grant.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @return the updated user object grant
     */
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAUser(String username, String catalogObjectName,
            String bucketName, String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                                          username,
                                                                                                                          bucket.getId());
        if (catalogObjectGrantEntity != null) {
            catalogObjectGrantEntity.setAccessType(accessType);
        } else {
            throw new DataIntegrityViolationException("Catalog object grant was not found in the DB");
        }
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param userGroup name of the group of users that are benefiting of the access grant.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @return the updated group object grant
     */
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAGroup(String userGroup, String catalogObjectName,
            String bucketName, String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                                           userGroup,
                                                                                                                           bucket.getId());
        if (catalogObjectGrantEntity != null) {
            catalogObjectGrantEntity.setAccessType(accessType);
        } else {
            throw new DataIntegrityViolationException("Catalog object grant was not found in the DB");
        }
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    // This function is kept for feature development - It returns all object grants for a user
    public List<CatalogObjectGrantMetadata> getAllAssignedCatalogObjectGrantsForUser(AuthenticatedUser user) {
        List<CatalogObjectGrantEntity> userGrants = catalogObjectGrantRepository.findAllGrantsAssignedToAUsername(user.getName());
        userGrants.addAll(catalogObjectGrantRepository.findAllGrantsAssignedToAUserGroup(user.getGroups()));
        return userGrants.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
    }

    /**
     *
     * @param username name of the user that is benefiting from the access grants.
     * @param userGroup name of the group of the user that is benefiting of the access grant.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return the list of all catalog objects grants assigned to a user and his group
     */
    public List<CatalogObjectGrantMetadata> getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(
            String username, String userGroup, String catalogObjectName, String bucketName) {
        // Find the catalog object
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        // Find the bucket
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        // Get all user catalog object grants from DB
        List<CatalogObjectGrantEntity> dbUserCatalogObjectGrants = catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUsername(username,
                                                                                                                                              catalogObjectRevisionEntity.getId(),
                                                                                                                                              bucket.getId());
        // Get all user group catalog objects grants from DB
        List<CatalogObjectGrantEntity> dbGroupCatalogObjectGrants = catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUserGroup(userGroup,
                                                                                                                                                catalogObjectRevisionEntity.getId(),
                                                                                                                                                bucket.getId());
        // Result list to return
        List<CatalogObjectGrantEntity> result = new LinkedList<>();
        // Check for duplicates grants in the group grant
        // We check the access type and compare them to add only the higher access grant
        if (dbGroupCatalogObjectGrants != null && !dbGroupCatalogObjectGrants.isEmpty()) {
            for (CatalogObjectGrantEntity groupObjectGrant : dbGroupCatalogObjectGrants) {
                for (CatalogObjectGrantEntity userObjectGrant : dbUserCatalogObjectGrants) {
                    if (!groupObjectGrant.getAccessType().equals(userObjectGrant.getAccessType())) {
                        // Get the access types of the user and the group grants
                        String userGrantAccessType = userObjectGrant.getAccessType();
                        String userGroupGrantAccessType = groupObjectGrant.getAccessType();
                        // Compare the access type and add the higher one
                        // If priority level equals 2 ==> Group grant is higher than the user
                        if (grantAccessTypeHelperService.getPriorityLevel(userGrantAccessType,
                                                                          userGroupGrantAccessType) == 2) {
                            // Add the group grant
                            result.add(groupObjectGrant);
                        } else {
                            // Add the user catalog object grant
                            result.add(userObjectGrant);
                        }
                    } else {
                        result.add(userObjectGrant);
                    }
                }
            }
        } else if (dbUserCatalogObjectGrants != null && !dbUserCatalogObjectGrants.isEmpty()) {
            result.addAll(dbUserCatalogObjectGrants);
        }
        if (!result.isEmpty()) {
            return result.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    public List<CatalogObjectGrantMetadata> getAllCreatedCatalogObjectGrantsForThisBucket(String bucketName,
            String catalogObjectName) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        CatalogObjectRevisionEntity object = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                    catalogObjectName);
        if (object != null) {
            long catalogObjectId = object.getId();
            return catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityIdAndCatalogObjectRevisionEntityId(bucketId,
                                                                                                                               catalogObjectId)
                                               .stream()
                                               .map(CatalogObjectGrantMetadata::new)
                                               .collect(Collectors.toList());
        } else {
            throw new DataIntegrityViolationException("Object: " + catalogObjectName + " was not found in bucket: " +
                                                      bucketName);
        }
    }

    /**
     * Check if a WF is available in a specific bucket
     * @param catalogObjectRevisionEntity catalog entity
     * @param bucketName bucket name
     * @param catalogObjectName WF name
     */
    private void throwExceptionIfCatalogObjectIsNull(CatalogObjectRevisionEntity catalogObjectRevisionEntity,
            String bucketName, String catalogObjectName) {
        if (catalogObjectRevisionEntity == null) {
            throw new DataIntegrityViolationException("Catalog object:" + catalogObjectName +
                                                      " does not exist in bucket: " + bucketName);
        }
    }

    /**
     *
     * Check if the user has admin grant rights over a catalog object
     *
     * @param user authenticated user
     * @param bucketName name of the bucket
     * @param catalogObjectName name of the catalog object
     * @return true if the user has admin grant rights over the catalog object
     */
    public boolean checkInCatalogObjectGrantsIfTheUserOrUserGroupHasAdminRightsOverTheCatalogObject(
            AuthenticatedUser user, String bucketName, String catalogObjectName) {
        // Find the bucket and get its id
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        // Find the catalog object id
        long catalogObjectId = getCatalogObjectId(bucketName, catalogObjectName);
        // Find the user assigned grant for this bucket
        List<CatalogObjectGrantEntity> dbUserCatalogObjectGrants = catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUsername(user.getName(),
                                                                                                                                              catalogObjectId,
                                                                                                                                              bucketId);
        // Find the user group grants assigned to this catalogObject
        List<CatalogObjectGrantEntity> grants = new LinkedList<>();
        for (String group : user.getGroups()) {
            List<CatalogObjectGrantEntity> groupGrants = catalogObjectGrantRepository.findAllCatalogObjectGrantsAssignedToAUserGroup(group,
                                                                                                                                     catalogObjectId,
                                                                                                                                     bucketId);
            if (!groupGrants.isEmpty()) {
                grants.addAll(groupGrants);
            }
        }
        grants.addAll(dbUserCatalogObjectGrants);
        for (CatalogObjectGrantEntity grant : grants) {
            if (grant.getAccessType().equals(admin.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * Get buckets' id from user assigned grants
     *
     * @param username username
     * @return the list of bucketIds found in all grants assigned to a user
     */
    public List<Long> getAllBucketIdsFromGrantsAssignedToUsername(String username) {
        return catalogObjectGrantRepository.findAllBucketsIdFromCatalogObjectGrantsAssignedToAUsername(username);
    }

    /**
     *
     * Get buckets' id from grants assigned to user group
     *
     * @param userGroups list of user groups
     * @return the list of bucketIds found in all grants assigned to a user group
     */
    public List<Long> getAllBucketIdsFromGrantsAssignedToUserGroup(List<String> userGroups) {
        return catalogObjectGrantRepository.findAllBucketsIdFromCatalogObjectGrantsAssignedToAUserGroup(userGroups);
    }

    /**
     *
     * Check if the user has grants over a bucket
     *
     * @param user authenticated user
     * @param bucketName name of the bucket
     * @return return true if the user has grants rights over a bucket
     */
    public boolean checkInCatalogGrantsIfUserOrUserGroupHasGrantsOverABucket(AuthenticatedUser user,
            String bucketName) {
        // Find the bucket and get its id
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        Set<Long> buckets = new HashSet<>(catalogObjectGrantRepository.findAllBucketsIdFromCatalogObjectGrantsAssignedToAUsername(user.getName()));
        buckets.addAll(catalogObjectGrantRepository.findAllBucketsIdFromCatalogObjectGrantsAssignedToAUserGroup(user.getGroups()));
        return buckets.contains(bucketId);
    }

    /**
     *
     * Get all grants metadata assigned to a specific bucket
     *
     * @param bucketName name of the bucket
     * @return the list of all catalog object grant metadata assigned to a bucket
     */
    public List<CatalogObjectGrantMetadata> findAllCatalogObjectGrantsAssignedToABucket(String bucketName) {
        return catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(bucketRepository.findOneByBucketName(bucketName)
                                                                                                           .getId())
                                           .stream()
                                           .map(CatalogObjectGrantMetadata::new)
                                           .collect(Collectors.toList());
    }

    /**
     *
     * Get the catalog object name from a catalog object grant
     *
     * @param grant catalog object grant
     * @return the name of the catalog object
     */
    public String getCatalogObjectNameFromGrant(CatalogObjectGrantMetadata grant) {
        long catalogObjectId = grant.getCatalogObjectId();
        long bucketId = grant.getCatalogObjectBucketId();
        List<CatalogObjectRevisionEntity> listOfObjects = catalogObjectRevisionRepository.findCatalogObject(catalogObjectId);
        Optional<CatalogObjectRevisionEntity> optCatalogObject = listOfObjects.stream()
                                                                              .filter(obj -> obj.getCatalogObject()
                                                                                                .getBucket()
                                                                                                .getId() == bucketId)
                                                                              .findFirst();
        if (optCatalogObject.isPresent()) {
            return optCatalogObject.get().getCatalogObject().getId().getName();
        } else {
            throw new DataIntegrityViolationException("Catalog Object not found");
        }
    }

    /**
     *
     * Check in grants if the user has the minimum required access type for the current task over the specific
     * catalog object
     *
     * @param user authenticated user
     * @param bucketName name of the bucket
     * @param catalogObjectName name of the catalog object
     * @param requiredAccessType minimum required access type
     * @return true if the user has the minimum grant access type over the current object
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isTheUserGrantSufficientForTheCurrentTask(AuthenticatedUser user, String bucketName,
            String catalogObjectName, String requiredAccessType) {
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        List<CatalogObjectGrantEntity> grantEntities = catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(bucketId);
        // Remove all grants that are not assigned to the current user and his group
        this.filterGrantsThatDoNotNotApplyToUser(user, catalogObjectName, grantEntities);
        CatalogObjectGrantEntity userCatalogObjectGrant;
        if (grantEntities.size() > 0) {
            userCatalogObjectGrant = grantEntities.get(0);
            if (!userCatalogObjectGrant.getAccessType().equals(admin.toString())) {
                for (int index = 1; index < grantEntities.size(); index++) {
                    if (grantAccessTypeHelperService.getPriorityLevel(userCatalogObjectGrant.getAccessType(),
                                                                      grantEntities.get(index).getAccessType()) == 2) {
                        userCatalogObjectGrant = grantEntities.get(index);
                    }
                }
            } else {
                // The userCatalogObjectGrant object has an admin grant type - no need to de complementary checks
                return true;
            }
        } else {
            return false;
        }
        return grantAccessTypeHelperService.compareGrantAccessType(userCatalogObjectGrant.getAccessType(),
                                                                   requiredAccessType);

    }

    /**
     *
     * Remove all grants that do not apply for the user
     *
     * @param user authenticated user
     * @param catalogObjectName name of the object
     * @param grantEntities list of object grant entities
     */
    private void filterGrantsThatDoNotNotApplyToUser(AuthenticatedUser user, String catalogObjectName,
            List<CatalogObjectGrantEntity> grantEntities) {
        List<CatalogObjectGrantEntity> grantEntitiesToRemove = new LinkedList<>();
        for (CatalogObjectGrantEntity entity : grantEntities) {
            if (!entity.getCatalogObjectRevisionEntity()
                       .getCatalogObject()
                       .getId()
                       .getName()
                       .equals(catalogObjectName)) {
                grantEntitiesToRemove.add(entity);
            } else {
                if (entity.getGranteeType().equals("user") && !entity.getGrantee().equals(user.getName())) {
                    grantEntitiesToRemove.add(entity);
                } else if (entity.getGranteeType().equals("group") && !user.getGroups().contains(entity.getGrantee())) {
                    grantEntitiesToRemove.add(entity);
                }
            }
        }
        grantEntities.removeAll(grantEntitiesToRemove);
    }

    /**
     *
     * Delete all catalog object grants assigned to a specific bucket
     *
     * @param bucketId bucket id
     */
    public void deleteAllCatalogObjectsGrantsAssignedToABucket(long bucketId) {
        // Get existing grants
        List<CatalogObjectGrantEntity> existingCatalogObjectsGrantsToDelete = catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(bucketId);
        // Delete grants
        if (existingCatalogObjectsGrantsToDelete != null) {
            catalogObjectGrantRepository.delete(existingCatalogObjectsGrantsToDelete);
        }
    }

    /**
     *
     * Delete all catalog object grant assigned to a specific object
     *
     * @param bucketId bucket id
     * @param catalogObjectId catalog object id
     */
    public void deleteAllCatalogObjectGrantsByBucketIdAndObjectId(long bucketId, long catalogObjectId) {
        // Get the catalog objects grants
        List<CatalogObjectGrantEntity> catalogObjectGrants = catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByCatalogObjectRevisionEntityIdAndBucketEntityId(catalogObjectId,
                                                                                                                                                                         bucketId);
        if (!catalogObjectGrants.isEmpty()) {
            catalogObjectGrantRepository.delete(catalogObjectGrants);
        }
    }

    public List<CatalogObjectGrantMetadata> deleteAllCatalogObjectGrantsAssignedToABucket(String bucketName,
            String catalogObjectName) {
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        long catalogObjectId = getCatalogObjectId(bucketName, catalogObjectName);
        List<CatalogObjectGrantEntity> result = catalogObjectGrantRepository.deleteAllByBucketEntityIdAndCatalogObjectRevisionEntityId(bucketId,
                                                                                                                                       catalogObjectId);
        return result.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
    }

    private long getCatalogObjectId(String bucketName, String catalogObjectName) {
        // Find the catalog object id
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        return catalogObjectRevisionEntity.getId();
    }
}
