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

import java.util.*;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.repository.BucketGrantRepository;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.ow2.proactive.catalog.service.exception.BucketGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.GrantNotFoundException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeValidator;
import org.ow2.proactive.catalog.util.AllBucketGrants;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.ow2.proactive.catalog.util.ModificationHistoryData;
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

    /**
     * Get the list of all the bucket grants assigned to the user and its groups for all the buckets.
     *
     * @param user authenticated user
     * @return the list of all the bucket grants assigned to the user and its groups
     */
    public List<BucketGrantMetadata> getUserAllBucketsGrants(AuthenticatedUser user) {
        List<BucketGrantEntity> grantEntities = bucketGrantRepository.findAllBucketsGrantsByUsername(user.getName());
        grantEntities.addAll(bucketGrantRepository.findAllBucketsGrantsByUserGroup(user.getGroups()));
        return GrantHelper.mapToGrants(grantEntities);
    }

    /**
     * Get the list of bucket grants (including all access types) assigned to a user and its group for the specific bucket.
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return return all bucket grants assigned to the given user
     */
    public List<BucketGrantMetadata> getUserBucketGrants(AuthenticatedUser user, String bucketName) {
        long bucketId = getBucketIdByName(bucketName);
        return GrantHelper.filterBucketsGrantsAssignedToUser(user,
                                                             bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId));

    }

    /**
     * Get the list of all positive (i.e. its access type is not "noAccess") bucket grants assigned to the user and its groups for all the buckets
     *
     * @param user authenticated user
     * @return the list of all positive bucket grants assigned to the user and its groups
     */
    public List<BucketGrantMetadata> getUserAccessibleBucketsGrants(AuthenticatedUser user) {
        List<BucketGrantEntity> userGrants = bucketGrantRepository.findAccessibleBucketsGrantsAssignedToAUsername(user.getName());
        userGrants.addAll(bucketGrantRepository.findAccessibleBucketsGrantsAssignedToUserGroups(user.getGroups()));
        return GrantHelper.mapToGrants(userGrants);
    }

    /**
     * Get the list of all no-access bucket grants assigned to the user and its groups for all the buckets
     * @return list of all no-access bucket grants assigned to the user and its groups
     */
    public List<BucketGrantMetadata> getNoAccessBucketsGrants(AuthenticatedUser user) {
        List<BucketGrantEntity> result = bucketGrantRepository.findBucketsGrantsAssignedToAUsernameWithNoAccessRight(user.getName());
        result.addAll(bucketGrantRepository.findBucketsGrantsAssignedToUserGroupsWithNoAccessRight(user.getGroups()));
        return GrantHelper.mapToGrants(result);
    }

    /**
     * Get the list of user-specific and positive (i.e. its access type is not "noAccess") bucket grants assigned to the user targeting the specific bucket
     *
     * @param user authenticated user
     * @param bucketName the bucket name
     * @return the list of user-specific positive bucket grants assigned to the user (not its groups) targeting the specific bucket
     */
    public Optional<BucketGrantMetadata> getUserSpecificPostiveGrants(AuthenticatedUser user, String bucketName) {
        long bucketId = this.getBucketIdByName(bucketName);
        List<BucketGrantEntity> result = bucketGrantRepository.findAccessibleBucketsGrantsAssignedToAUsername(user.getName(),
                                                                                                              bucketId);
        return result.stream().findFirst().map(BucketGrantMetadata::new);
    }

    /**
     *
     * This functions requires admin rights
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return all created bucket grants
     */
    public List<BucketGrantMetadata> getAllBucketGrantsForABucket(String bucketName) {
        long bucketId = this.getBucketIdByName(bucketName);
        return GrantHelper.mapToGrants(bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId));
    }

    /**
     * Get all grants created for a bucket and its object
     * @param bucketName name of the bucket where the catalog object is stored. of the bucket where the catalog object is stored. bucket name
     * @return all bucket grants
     */
    public AllBucketGrants getAllBucketAndObjectGrants(String bucketName) {
        AllBucketGrants allBucketGrants = new AllBucketGrants();
        List<BucketGrantMetadata> bucketGrants = this.getAllBucketGrantsForABucket(bucketName);
        List<CatalogObjectGrantMetadata> objectGrants = catalogObjectGrantService.getObjectsGrantsInABucket(bucketName);
        allBucketGrants.setBucketGrants(bucketGrants);
        allBucketGrants.setObjectGrants(objectGrants);
        return allBucketGrants;
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
    @Transactional
    public BucketGrantMetadata updateBucketGrantForASpecificUser(AuthenticatedUser currentUser, String bucketName,
            String username, String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity != null) {
            bucketId = bucketEntity.getId();
            // Find the bucket grant assigned to the current user
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsername(bucketId, username);
            if (bucketGrantEntity != null) {
                String oldValue = bucketGrantEntity.toString();
                // Update the access type
                bucketGrantEntity.setAccessType(accessType);
                // Add modification history
                ModificationHistoryData modificationHistoryData = new ModificationHistoryData(System.currentTimeMillis(),
                                                                                              currentUser.getName());
                // Set old values in history
                modificationHistoryData.setOldValues(oldValue);
                // Set new values in history
                modificationHistoryData.setNewValues(bucketGrantEntity.toString());
                // Compute changes
                modificationHistoryData.computeChanges("user", oldValue, bucketGrantEntity.toString());
                bucketGrantEntity.getModificationHistory().push(modificationHistoryData);
                // Save the grant
                bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
                return new BucketGrantMetadata(bucketGrantEntity);
            } else {
                throw new GrantNotFoundException(username, bucketName);
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
    public BucketGrantMetadata updateBucketGrantForASpecificUserGroup(AuthenticatedUser currentUser, String bucketName,
            String userGroup, String accessType, int priority) {
        PriorityLevelValidator.checkAndValidateTheGivenPriorityLevel(priority);
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity != null) {
            bucketId = bucketEntity.getId();
            // Find the bucket grant assigned to the current user group
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUserGroup(bucketId, userGroup);
            if (bucketGrantEntity != null) {
                String oldValue = bucketGrantEntity.toString();
                // Update the access type
                bucketGrantEntity.setAccessType(accessType);
                // Update the priority
                bucketGrantEntity.setPriority(priority);
                // Add modification history
                ModificationHistoryData modificationHistoryData = new ModificationHistoryData(System.currentTimeMillis(),
                                                                                              currentUser.getName());
                // Set old values in history
                modificationHistoryData.setOldValues(oldValue);
                // Set new values in history
                modificationHistoryData.setNewValues(bucketGrantEntity.toString());
                // Compute changes
                modificationHistoryData.computeChanges("group", oldValue, bucketGrantEntity.toString());
                bucketGrantEntity.getModificationHistory().push(modificationHistoryData);
            } else {
                throw new GrantNotFoundException(userGroup, bucketName);
            }
            // Save the modifications
            bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);
            return new BucketGrantMetadata(bucketGrantEntity);
        }
        return null;
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
        return GrantHelper.mapToGrants(bucketGrantRepository.deleteAllByBucketEntityId(bucketId));
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return the id of a bucket
     */
    private long getBucketIdByName(String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        if (bucket != null) {
            return bucket.getId();
        } else {
            throw new BucketNotFoundException(bucketName);
        }
    }
}
