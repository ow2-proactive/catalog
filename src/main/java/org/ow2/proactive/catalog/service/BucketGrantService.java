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

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.repository.BucketGrantRepository;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.ow2.proactive.catalog.service.exception.BucketGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeValidator;
import org.ow2.proactive.catalog.util.AllBucketGrants;
import org.ow2.proactive.catalog.util.PriorityLevelValidator;
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
public class BucketGrantService {

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private BucketGrantRepository bucketGrantRepository;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    public List<BucketGrantMetadata> getAllBucketGrantsAssignedForTheUserOnABucket(AuthenticatedUser user,
            String bucketName) {
        long bucketId = this.getBucketIdByName(bucketName);
        List<BucketGrantMetadata> result = bucketGrantRepository.findAllBucketGrantsAssignedToAUsernameInsideABucket(user.getName(),
                                                                                                                     bucketId)
                                                                .stream()
                                                                .map(BucketGrantMetadata::new)
                                                                .collect(Collectors.toList());
        result.addAll(bucketGrantRepository.findAllBucketGrantsAssignedToTheUserGroupsInsideABucket(user.getGroups(),
                                                                                                    bucketId)
                                           .stream()
                                           .filter(grant -> grant.getBucketEntity().getBucketName().equals(bucketName))
                                           .map(BucketGrantMetadata::new)
                                           .collect(Collectors.toList()));
        return result;
    }

    public List<BucketGrantMetadata> getAllBucketGrantsAssignedToAUser(AuthenticatedUser user) {
        List<BucketGrantEntity> userGrants = bucketGrantRepository.findAllBucketGrantsAssignedToAUsername(user.getName());
        userGrants.addAll(bucketGrantRepository.findAllBucketGrantsAssignedToTheUserGroups(user.getGroups()));
        return userGrants.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
    }

    /**
     *
     * Update the access type for an existing user grant
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param username name of the user that is benefiting from the access grants.
     * @param accessType new type of the access grant. It can be either noAccess, read, write or admin.
     * @return the updated grant assigned to the user for a specific bucket
     */
    public BucketGrantMetadata updateBucketGrantForASpecificUser(String bucketName, String username,
            String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity != null) {
            bucketId = bucketEntity.getId();
            // Find the bucket grant assigned to the current user
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsernameForUpdate(bucketId,
                                                                                                           username);
            if (bucketGrantEntity != null) {
                // Update the access type
                bucketGrantEntity.setAccessType(accessType);
                // Save the grant
                bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
                return new BucketGrantMetadata(bucketGrantEntity);
            }
        }
        return null;
    }

    /**
     *
     * Update the access type for an existing user group grant
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param userGroup name of the group of users that are benefiting from the access grant.
     * @param accessType new type of the access grant. It can be either noAccess, read, write or admin.
     * @return the updated grant assigned to the group of users for a specific bucket
     */
    @Transactional
    public BucketGrantMetadata updateBucketGrantForASpecificUserGroup(String bucketName, String userGroup,
            String accessType, int priority) {
        PriorityLevelValidator.checkAndValidateTheGivenPriorityLevel(priority);
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity != null) {
            bucketId = bucketEntity.getId();
            // Find the bucket grant assigned to the current user group
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUserGroupForUpdate(bucketId,
                                                                                                            userGroup);
            if (bucketGrantEntity != null) {
                // Update the access type
                bucketGrantEntity.setAccessType(accessType);
                bucketGrantEntity.setPriority(priority);
            } else {
                throw new DataIntegrityViolationException("Bucket grant was not found in the DB");
            }
            // Save the modifications
            bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
            return new BucketGrantMetadata(bucketGrantEntity);
        }
        return null;
    }

    /**
     *
     * @param username username
     * @return all grants created by the current user
     */
    public List<BucketGrantMetadata> getAllGrantsCreatedByUsername(String username) {
        // Get from the db all grants that are created by the current user
        List<BucketGrantEntity> dbUserCreatedGrants = bucketGrantRepository.findBucketGrantEntitiesByCreator(username);
        // Transform the result to BucketGrantMetadata and return it
        if (dbUserCreatedGrants != null) {
            return dbUserCreatedGrants.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    /**
     *
     * Create a bucket grant for a user
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param currentUser name of the user creating the grant.
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @param username name of the user that will benefit of the access grant.
     * @return a created bucket grant
     * @throws DataIntegrityViolationException in case of a bad bucket name and in case of an existing similar grant
     */
    @Transactional
    public BucketGrantMetadata createBucketGrantForAUser(String bucketName, String currentUser, String accessType,
            String username) throws DataIntegrityViolationException {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the corresponding bucket from the DB
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        // Throw an error if the bucket was not found
        if (bucket == null) {
            throw new DataIntegrityViolationException("Bucket: " + bucketName + " does not exist in the catalog");
        }
        // Check if a similar grant is available in the DB
        BucketGrantEntity dbUsernameBucketGrant = bucketGrantRepository.findBucketGrantByUsername(bucket.getId(),
                                                                                                  username);
        // Throw an exception if similar grant exists
        if (dbUsernameBucketGrant != null && dbUsernameBucketGrant.getGrantee().equals(username) &&
            dbUsernameBucketGrant.getBucketEntity().getId().equals(bucket.getId())) {
            throw new BucketGrantAlreadyExistsException("Grant exists for bucket: " + bucketName +
                                                        " and already assigned to user: " + username);
        }
        // BucketGrant attributes: Type, Profiteer, Access Type and the Bucket
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("user", currentUser, username, accessType, bucket);
        // Save the grant in db
        bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
        return new BucketGrantMetadata(bucketGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param currentUser name of the user creating the grant.
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @param userGroup name of the group of users that will benefit of the access grant.
     * @return a created bucket grant
     * @throws DataIntegrityViolationException in case of a bad bucket name and in case of an existing similar grant
     */
    @Transactional
    public BucketGrantMetadata createBucketGrantForAGroup(String bucketName, String currentUser, String accessType,
            int priority, String userGroup) throws DataIntegrityViolationException {
        PriorityLevelValidator.checkAndValidateTheGivenPriorityLevel(priority);
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the corresponding bucket from the DB
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        // Throw an error if the bucket was not found
        if (bucket == null) {
            throw new DataIntegrityViolationException("Bucket: " + bucketName + " does not exist in the catalog");
        }
        // Check if a similar grant is available in the DB
        BucketGrantEntity dbUserGroupBucketGrant = bucketGrantRepository.findBucketGrantByUserGroup(bucket.getId(),
                                                                                                    userGroup);
        // Throw an exception if similar grant exists
        if (dbUserGroupBucketGrant != null && dbUserGroupBucketGrant.getGrantee().equals(userGroup) &&
            dbUserGroupBucketGrant.getBucketEntity().getId().equals(bucket.getId())) {
            throw new BucketGrantAlreadyExistsException("Grant exists for bucket: " + bucketName +
                                                        " and already assigned to user group: " + userGroup);
        }
        // BucketGrant attributes: Type, Profiteer, Access Type and the Bucket
        BucketGrantEntity bucketGrantEntity = new BucketGrantEntity("group",
                                                                    currentUser,
                                                                    userGroup,
                                                                    accessType,
                                                                    priority,
                                                                    bucket);
        // Save the grant in db
        bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
        return new BucketGrantMetadata(bucketGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param username name of the user that is benefiting from the access grant.
     * @return the deleted bucket grant
     */
    @Transactional
    public BucketGrantMetadata deleteBucketGrantForAUser(String bucketName, String username) {
        // Find the bucket
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        // Find the user grant
        BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsername(bucketEntity.getId(),
                                                                                              username);
        if (bucketGrantEntity != null) {
            // Delete the grant from DB
            bucketGrantRepository.delete(bucketGrantEntity);
        } else {
            throw new DataIntegrityViolationException("Bucket grant was not found in the DB.");
        }
        return new BucketGrantMetadata(bucketGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param userGroup name of the group of users that are benefiting from the access grant.
     * @return the deleted bucket grant
     */
    @Transactional
    public BucketGrantMetadata deleteBucketGrantForAGroup(String bucketName, String userGroup) {
        // Find the bucket
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        // Find the group grant
        BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUserGroup(bucketEntity.getId(),
                                                                                               userGroup);
        if (bucketGrantEntity != null) {
            // Delete the grant from DB
            bucketGrantRepository.delete(bucketGrantEntity);
        } else {
            throw new DataIntegrityViolationException("Bucket grant was not found in the DB.");
        }
        return new BucketGrantMetadata(bucketGrantEntity);
    }

    /**
     *
     * This function delete all bucket grants when the bucket is deleted. It will delete also all bucket
     * objects grants if they were found.
     *
     * @param bucketId the deleted bucket name
     *
     */
    public void deleteAllGrantsAssignedToABucketAndItsObjects(long bucketId) {
        // Get existing grants
        List<BucketGrantEntity> existingBucketGrantsToDelete = bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId);
        if (existingBucketGrantsToDelete != null) {
            // Delete all bucket Grants
            bucketGrantRepository.delete(existingBucketGrantsToDelete);
        }
        //Delete all catalog objects grants found in this bucket
        catalogObjectGrantService.deleteAllCatalogObjectsGrantsAssignedToABucket(bucketId);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return the id of a bucket
     */
    public long getBucketIdByName(String bucketName) {
        return bucketRepository.findOneByBucketName(bucketName).getId();
    }

    /**
     *
     * Count the objects that are accessible for the user via the bucket or object grants
     *
     * @param user authenticated user
     * @param bucket bucket metadata
     * @return the number of user's accessible objects inside the bucket
     */
    public int getTheNumberOfAccessibleObjectsInTheBucket(AuthenticatedUser user, BucketMetadata bucket) {
        String bucketName = bucket.getName();
        List<BucketGrantMetadata> allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket = getAllBucketGrantsAssignedToTheUserForTheCurrentBucket(user,
                                                                                                                                                    bucketName);
        allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket.removeIf(grant -> grant.getAccessType()
                                                                                        .equals(noAccess.toString()));
        if (allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket.size() > 0) {
            Set<String> directAccessibleBucketsForTheUser = allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket.stream()
                                                                                                                    .map(BucketGrantMetadata::getBucketName)
                                                                                                                    .collect(Collectors.toSet());
            List<CatalogObjectGrantMetadata> userInaccessibleObjects = catalogObjectGrantService.getUserNoAccessGrant(user);
            Set<String> catalogObjectsToRemove = userInaccessibleObjects.stream()
                                                                        .filter(grant -> directAccessibleBucketsForTheUser.contains(grant.getBucketName()))
                                                                        .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                                        .collect(Collectors.toSet());

            return bucket.getObjectCount() - catalogObjectsToRemove.size();
        } else {
            List<CatalogObjectGrantMetadata> catalogObjectGrants = getAllObjectGrantsAssignedToTheCurrentUserForTheCurrentBucket(user,
                                                                                                                                 bucketName);
            catalogObjectGrants.removeIf(grant -> grant.getAccessType().equals(noAccess.toString()));
            if (catalogObjectGrants.size() > 0) {
                List<String> distinctObjectNamesByGrants = new LinkedList<>();
                for (CatalogObjectGrantMetadata catalogObjectGrantMetadata : catalogObjectGrants) {
                    String catalogObjectName = catalogObjectGrantMetadata.getCatalogObjectName();
                    // In some case a user can have multiple grants over the same object: multiple group grants
                    if (!distinctObjectNamesByGrants.contains(catalogObjectName)) {
                        distinctObjectNamesByGrants.add(catalogObjectName);
                    }
                }
                return distinctObjectNamesByGrants.size();
            } else {
                // The bucket is public since it has no bucket or object grants
                return bucket.getObjectCount();
            }
        }
    }

    /**
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return all object grants assigned to the given user inside the given bucket
     */
    private List<CatalogObjectGrantMetadata>
            getAllObjectGrantsAssignedToTheCurrentUserForTheCurrentBucket(AuthenticatedUser user, String bucketName) {
        return catalogObjectGrantService.findAllCatalogObjectGrantsAssignedToABucket(bucketName)
                                        .stream()
                                        .filter(catalogObjectGrantMetadata -> (catalogObjectGrantMetadata.getGrantee()
                                                                                                         .equals(user.getName()) &&
                                                                               catalogObjectGrantMetadata.getGranteeType()
                                                                                                         .equals("user")) ||
                                                                              (user.getGroups()
                                                                                   .contains(catalogObjectGrantMetadata.getGrantee()) &&
                                                                               catalogObjectGrantMetadata.getGranteeType()
                                                                                                         .equals("group")))
                                        .collect(Collectors.toList());
    }

    /**
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return return all bucket grants assigned to the given user
     */
    private List<BucketGrantMetadata> getAllBucketGrantsAssignedToTheUserForTheCurrentBucket(AuthenticatedUser user,
            String bucketName) {
        return bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(getBucketIdByName(bucketName))
                                    .stream()
                                    .filter(bucketGrantEntity -> (bucketGrantEntity.getGrantee()
                                                                                   .equals(user.getName()) &&
                                                                  bucketGrantEntity.getGranteeType().equals("user")) ||
                                                                 (user.getGroups()
                                                                      .contains(bucketGrantEntity.getGrantee()) &&
                                                                  bucketGrantEntity.getGranteeType().equals("group")))
                                    .map(BucketGrantMetadata::new)
                                    .collect(Collectors.toList());
    }

    /**
     *
     * This functions requires admin rights
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return all created bucket grants
     */
    public List<BucketGrantMetadata> getAllCreatedBucketGrantsForABucket(String bucketName) {
        long bucketId = this.getBucketIdByName(bucketName);
        return bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId)
                                    .stream()
                                    .map(BucketGrantMetadata::new)
                                    .collect(Collectors.toList());
    }

    /**
     * Get all grants created for a bucket and its object
     * @param bucketName name of the bucket where the catalog object is stored. of the bucket where the catalog object is stored. bucket name
     * @return all bucket grants
     */
    public AllBucketGrants getAllBucketAndObjectGrants(String bucketName) {
        AllBucketGrants allBucketGrants = new AllBucketGrants();
        List<BucketGrantMetadata> bucketGrants = this.getAllCreatedBucketGrantsForABucket(bucketName);
        List<CatalogObjectGrantMetadata> objectGrants = catalogObjectGrantService.findAllCatalogObjectGrantsAssignedToABucket(bucketName);
        allBucketGrants.setBucketGrants(bucketGrants);
        allBucketGrants.setObjectGrants(objectGrants);
        return allBucketGrants;
    }

    public List<BucketGrantMetadata> getAllNoAccessGrants() {
        return bucketGrantRepository.findAllBucketGrantsWithNoAccessRight()
                                    .stream()
                                    .map(BucketGrantMetadata::new)
                                    .collect(Collectors.toList());
    }

    @Transactional
    public AllBucketGrants deleteAllBucketAndItsObjectsGrants(String bucketName) {
        AllBucketGrants allBucketGrants = this.getAllBucketAndObjectGrants(bucketName);
        this.deleteAllGrantsAssignedToABucketAndItsObjects(this.getBucketIdByName(bucketName));
        return allBucketGrants;
    }

    /**
     *
     * Delete all bucket grants for a specific bucket
     *
     * @param bucketName of the bucket where the catalog object is stored.
     * @return the list of all deleted bucket grants
     */
    public List<BucketGrantMetadata> deleteAllGrantsAssignedToABucket(String bucketName) {
        long bucketId = this.getBucketIdByName(bucketName);
        return bucketGrantRepository.deleteAllByBucketEntityId(bucketId)
                                    .stream()
                                    .map(BucketGrantMetadata::new)
                                    .collect(Collectors.toList());
    }

    /**
     *
     * Remove from the list of catalog objects all the objects that are inaccessible for the user
     *
     * @param user authenticated user
     * @param metadataList list of catalog object entities
     * @param grants list of catalog object grants
     */
    public void removeAllUserInaccessibleObjectsFromTheBucket(AuthenticatedUser user,
            List<CatalogObjectMetadata> metadataList, List<CatalogObjectGrantMetadata> grants) {
        List<CatalogObjectMetadata> objectsToRemove = new LinkedList<>();
        List<CatalogObjectMetadata> objectsNotToRemove = new LinkedList<>();
        for (CatalogObjectGrantMetadata grant : grants) {
            String objectNameFromGrant = grant.getCatalogObjectName();
            if ((grant.getGrantee().equals(user.getName()) && grant.getGranteeType().equals("user")) ||
                (user.getGroups().contains(grant.getGrantee()) && grant.getGranteeType().equals("group"))) {
                for (CatalogObjectMetadata catalogObject : metadataList) {
                    if (!catalogObject.getName().equals(objectNameFromGrant) &&
                        !objectsToRemove.contains(catalogObject)) {
                        objectsToRemove.add(catalogObject);
                    } else if (catalogObject.getName().equals(objectNameFromGrant) &&
                               !objectsNotToRemove.contains(catalogObject)) {
                        objectsNotToRemove.add(catalogObject);
                    }
                }
            }
        }
        objectsToRemove.removeAll(objectsNotToRemove);
        metadataList.removeAll(objectsToRemove);
    }
}
