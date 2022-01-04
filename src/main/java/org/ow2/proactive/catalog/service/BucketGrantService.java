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
import org.ow2.proactive.catalog.service.exception.BucketGrantAccessException;
import org.ow2.proactive.catalog.service.exception.BucketGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectGrantAccessException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeValidator;
import org.ow2.proactive.catalog.util.AllBucketGrants;
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

    @Autowired
    private GrantAccessTypeHelperService grantAccessTypeHelperService;

    /**
     *
     * Get the grants assigned to a user and his group
     *
     * @param username name of the user that is benefiting from the access grants.
     * @param userGroups list of the groups of the user that is benefiting of the access grant.
     * @return all grants that are assigned to a specific user and his groups
     */
    public List<BucketGrantMetadata> getAllAssignedGrantsForUserAndHisGroups(String username, List<String> userGroups) {
        // Get all user assigned grants from the db
        List<BucketGrantEntity> dbUserBucketGrants = bucketGrantRepository.findAllGrantsAssignedToAUsername(username);
        // Add the grants that are assigned to his group
        List<BucketGrantEntity> dbGroupBucketGrants = bucketGrantRepository.findAllGrantsAssignedToTheUserGroups(userGroups);
        // Result list
        List<BucketGrantEntity> result = new LinkedList<>();
        // Check for duplicates grants in the group grant
        // If the user group has a grant over a different bucket we add it to the result
        // If not, we check the access type and compare them to add only the higher access grant
        if (dbGroupBucketGrants != null && !dbGroupBucketGrants.isEmpty()) {
            for (BucketGrantEntity groupBucketGrant : dbGroupBucketGrants) {
                if (dbUserBucketGrants != null && !dbUserBucketGrants.isEmpty()) {
                    for (BucketGrantEntity userBucketGrant : dbUserBucketGrants) {
                        // Grant over different buckets
                        if (!groupBucketGrant.getBucketEntity()
                                             .getId()
                                             .equals(userBucketGrant.getBucketEntity().getId())) {
                            result.add(userBucketGrant);
                            result.add(groupBucketGrant);
                        } else if (!groupBucketGrant.getAccessType().equals(userBucketGrant.getAccessType())) {
                            // Grant over the same bucket but with different access type
                            String userGrantAccessType = userBucketGrant.getAccessType();
                            String userGroupGrantAccessType = groupBucketGrant.getAccessType();
                            // Compare the access type and add the higher one
                            if (grantAccessTypeHelperService.getPriorityLevel(userGrantAccessType,
                                                                              userGroupGrantAccessType) == 2) {
                                result.add(groupBucketGrant);
                            } else {
                                result.add(userBucketGrant);
                            }
                        }
                    }
                } else {
                    result.addAll(dbGroupBucketGrants);
                    break;
                }
            }
        } else if (dbUserBucketGrants != null && !dbUserBucketGrants.isEmpty()) {
            result.addAll(dbUserBucketGrants);
        }
        if (!result.isEmpty()) {
            return result.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    /**
     *
     * Update the access type for an existing user grant
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param username name of the user that is benefiting from the access grants.
     * @param accessType new type of the access grant. It can be either read, write or admin.
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
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsername(bucketId, username);
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
     * @param accessType new type of the access grant. It can be either read, write or admin.
     * @return the updated grant assigned to the group of users for a specific bucket
     */
    public BucketGrantMetadata updateBucketGrantForASpecificUserGroup(String bucketName, String userGroup,
            String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity != null) {
            bucketId = bucketEntity.getId();
            // Find the bucket grant assigned to the current user group
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUserGroup(bucketId, userGroup);
            if (bucketGrantEntity != null) {
                // Update the access type
                bucketGrantEntity.setAccessType(accessType);
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
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @param username name of the user that will benefit of the access grant.
     * @return a created bucket grant
     * @throws DataIntegrityViolationException in case of a bad bucket name and in case of an existing similar grant
     */
    public BucketGrantMetadata createBucketGrantForAUSer(String bucketName, String currentUser, String accessType,
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
     * @param accessType type of the access grant. It can be either read, write or admin.
     * @param userGroup name of the group of users that will benefit of the access grant.
     * @return a created bucket grant
     * @throws DataIntegrityViolationException in case of a bad bucket name and in case of an existing similar grant
     */
    public BucketGrantMetadata createBucketGrantForAGroup(String bucketName, String currentUser, String accessType,
            String userGroup) throws DataIntegrityViolationException {
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
     * This function checks for grant rights over a bucket for the current user
     *
     * @param user username
     * @param bucketName bucket name
     * @return true if the user has a sufficient grant right over the bucket and false otherwise
     */
    public boolean isTheUserGrantSufficientForTheCurrentTask(AuthenticatedUser user, String bucketName,
            String requiredAccessType) {
        // Find the bucket and get its id
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        List<BucketGrantEntity> grants = bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId);
        List<BucketGrantEntity> grantsToRemove = new LinkedList<>();
        for (BucketGrantEntity entity : grants) {
            if (entity.getGranteeType().equals("user") && !entity.getGrantee().equals(user.getName())) {
                grantsToRemove.add(entity);
            } else if (entity.getGranteeType().equals("group") && !user.getGroups().contains(entity.getGrantee())) {
                grantsToRemove.add(entity);
            }
        }
        grants.removeAll(grantsToRemove);
        BucketGrantEntity bucketGrantEntity;
        // We need to pick only the grant with the higher access type
        if (grants.size() > 0) {
            bucketGrantEntity = grants.get(0);
            if (!bucketGrantEntity.getAccessType().equals(admin.toString())) {
                for (int index = 1; index < grants.size(); index++) {
                    if (grantAccessTypeHelperService.getPriorityLevel(bucketGrantEntity.getAccessType(),
                                                                      grants.get(index).getAccessType()) == 2) {
                        bucketGrantEntity = grants.get(index);
                    }
                }
            } else {
                // The bucketGrantEntity object has an admin grant type - no need to de complementary checks
                return true;
            }
        } else {
            return false;
        }
        return grantAccessTypeHelperService.compareGrantAccessType(bucketGrantEntity.getAccessType(),
                                                                   requiredAccessType);
    }

    /**
     *
     * @param user authenticated used
     * @return the list of buckets that the user has grant access over them
     */
    public List<BucketMetadata> getBucketsForUserByGrants(AuthenticatedUser user) {
        // Buckets list to return
        List<BucketMetadata> bucketMetadataList = new LinkedList<>();
        // Bucket grants metadata list
        List<BucketGrantMetadata> bucketGrants = new LinkedList<>();
        // Get the list of bucket ids from all object grants assigned to a user
        Set<Long> bucketsIdsFromCatalogObjectGrants = new HashSet<>(catalogObjectGrantService.getAllBucketIdsFromGrantsAssignedToUsername(user.getName()));
        // Get bucket grants from DB and add them to the bucket grant list
        // Get distinct buckets' ids from catalog object grants and add them to the corresponding list
        bucketGrants.addAll(this.getAllAssignedGrantsForUserAndHisGroups(user.getName(), user.getGroups()));
        bucketsIdsFromCatalogObjectGrants.addAll(catalogObjectGrantService.getAllBucketIdsFromGrantsAssignedToUserGroup(user.getGroups()));
        // Get from bucket grants the targeted buckets
        for (BucketGrantMetadata bucketGrantMetadata : bucketGrants) {
            BucketEntity bucket = bucketRepository.findOne(bucketGrantMetadata.getBucketId());
            BucketMetadata bucketMetadata = new BucketMetadata(bucket, bucket.getCatalogObjects().size());
            bucketMetadataList.add(bucketMetadata);
        }
        // Get the buckets from the list of buckets' ids
        for (Long id : bucketsIdsFromCatalogObjectGrants) {
            BucketEntity bucket = bucketRepository.findOne(id);
            BucketMetadata bucketMetadata = new BucketMetadata(bucket, bucket.getCatalogObjects().size());
            bucketMetadataList.add(bucketMetadata);
        }
        return bucketMetadataList;
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
     * @param bucketName bucket name
     * @return the id of a bucket
     */
    public long getBucketIdByName(String bucketName) {
        return bucketRepository.findOneByBucketName(bucketName).getId();
    }

    /**
     *
     * @param user authenticated user
     * @param bucketName bucket name
     * @param bucketOwner bucket owner
     * @return the highest grant rights that the user has over a bucket
     */
    public String getHighestGrantAccessTypeFromBucketGrants(AuthenticatedUser user, String bucketName,
            String bucketOwner) {
        String accessType = "";
        if (user.getName().equals(bucketOwner) || user.getGroups().contains(bucketOwner.substring(6)) ||
            bucketOwner.equals("GROUP:public-objects")) {
            accessType = admin.toString();
        } else {
            long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
            List<BucketGrantMetadata> grants = this.getAllAssignedGrantsForUserAndHisGroups(user.getName(),
                                                                                            user.getGroups());
            grants = grants.stream().filter(grant -> grant.getBucketId() == bucketId).collect(Collectors.toList());
            if (grants.size() > 0) {
                BucketGrantMetadata bucketGrantMetadata = grants.get(0);
                for (int index = 1; index < grants.size(); index++) {
                    if (grantAccessTypeHelperService.getPriorityLevel(bucketGrantMetadata.getAccessType(),
                                                                      grants.get(index).getAccessType()) == 2) {
                        bucketGrantMetadata = grants.get(index);
                    }
                }
                accessType = bucketGrantMetadata.getAccessType();
            } else {
                accessType = "";
            }
        }
        return accessType;
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
        // TODO Take into consideration the objects that have an accessType: noAccess, once the negative grants are implemented
        String bucketName = bucket.getName();
        List<BucketGrantMetadata> allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket = getAllBucketGrantsAssignedToTheUserForTheCurrentBucket(user,
                                                                                                                                                    bucketName);
        if (allGrantsAssignedToTheUserAndHisGroupForTheCurrentBucket.size() > 0) {
            return bucket.getObjectCount();
        } else {
            List<CatalogObjectGrantMetadata> catalogObjectGrants = getAllObjectGrantsAssignedToTheCurrentUserForTheCurrentBucket(user,
                                                                                                                                 bucketName);
            if (catalogObjectGrants.size() > 0) {
                List<String> distinctObjectNamesByGrants = new LinkedList<>();
                for (CatalogObjectGrantMetadata catalogObjectGrantMetadata : catalogObjectGrants) {
                    String catalogObjectName = catalogObjectGrantService.getCatalogObjectNameFromGrant(catalogObjectGrantMetadata);
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
     * @param bucketName name of the bucket
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
     * @param bucketName name of the bucket
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
     * @param bucketName bucket name
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
     * @param bucketName bucket name
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

    public AllBucketGrants deleteAllBucketAndItsObjectsGrants(String bucketName) {
        AllBucketGrants allBucketGrants = this.getAllBucketAndObjectGrants(bucketName);
        this.deleteAllGrantsAssignedToABucketAndItsObjects(this.getBucketIdByName(bucketName));
        return allBucketGrants;
    }

    /**
     *
     * Delete all bucket grants for a specific bucket
     *
     * @param bucketName bucket name
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
     * Check if the user has an admin grant over the object.
     * If the object grants are absent, the method checks if the user has admin rights over the bucket
     *
     * @param user authenticated user
     * @param bucketName bucket name
     * @param catalogObjectName catalog object name
     */
    public void checkIfTheUserHasAdminRightsOverTheObjectOrOverTheBucket(AuthenticatedUser user, String bucketName,
            String catalogObjectName) {
        List<CatalogObjectGrantMetadata> catalogObjectGrantsAssignedToTheCurrentUserForTheSpecificObject = getCatalogObjectGrantsAssignedToTheCurrentUserForTheSpecificObject(user,
                                                                                                                                                                              bucketName,
                                                                                                                                                                              catalogObjectName);
        // If the list is not empty --> the user has a grant access for the object
        if (!catalogObjectGrantsAssignedToTheCurrentUserForTheSpecificObject.isEmpty()) {
            // Check if the grant has admin as access type and throw an exception if not
            if (!catalogObjectGrantService.isTheUserGrantSufficientForTheCurrentTask(user,
                                                                                     bucketName,
                                                                                     catalogObjectName,
                                                                                     admin.toString())) {
                throw new CatalogObjectGrantAccessException(bucketName, catalogObjectName);
            }
        }
        // If the user has not an object grant, we need to check if he has a grant for the current bucket with admin access type
        else if (!this.isTheUserGrantSufficientForTheCurrentTask(user, bucketName, admin.toString())) {
            throw new BucketGrantAccessException(bucketName);
        }
    }

    /**
     *
     * @param user authenticated user
     * @param bucketName name of the bucket
     * @param catalogObjectName name of the object
     * @return all grants for the specific object that are assigned to the given user inside the given bucket
     */
    private LinkedList<CatalogObjectGrantMetadata> getCatalogObjectGrantsAssignedToTheCurrentUserForTheSpecificObject(
            AuthenticatedUser user, String bucketName, String catalogObjectName) {
        return catalogObjectGrantService.getAllAssignedCatalogObjectGrantsForUser(user)
                                        .stream()
                                        .filter(grant -> grant.getCatalogObjectName().equals(catalogObjectName) &&
                                                         grant.getBucketName().equals(bucketName))
                                        .collect(Collectors.toCollection(LinkedList::new));
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
            String objectNameFromGrant = catalogObjectGrantService.getCatalogObjectNameFromGrant(grant);
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

    /**
     *
     * @param grants list of object grants
     * @return a map containing the highest user rights for each accessible object from the grants
     */
    public Map<String, String> getRightsPerObjectFromGrants(List<CatalogObjectGrantMetadata> grants) {
        Map<String, String> accessGrantPerObject = new HashMap<>();
        for (CatalogObjectGrantMetadata grant : grants) {
            String objectNameFromGrant = catalogObjectGrantService.getCatalogObjectNameFromGrant(grant);
            if (!accessGrantPerObject.containsKey(objectNameFromGrant)) {
                accessGrantPerObject.put(objectNameFromGrant, grant.getAccessType());
            } else {
                if (grantAccessTypeHelperService.getPriorityLevel(accessGrantPerObject.get(objectNameFromGrant),
                                                                  grant.getAccessType()) == 2) {
                    accessGrantPerObject.put(objectNameFromGrant, grant.getAccessType());
                }
            }
        }
        return accessGrantPerObject;
    }
}
