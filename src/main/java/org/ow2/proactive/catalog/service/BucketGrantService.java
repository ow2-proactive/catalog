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
import org.ow2.proactive.catalog.repository.BucketGrantRepository;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectGrantRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.ow2.proactive.catalog.service.exception.BucketGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
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
    private CatalogObjectGrantRepository catalogObjectGrantRepository;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private GrantAccessTypeHelperService grantAccessTypeHelperService;

    /**
     *
     * Get the grants assigned to a user and his group
     *
     * @param username username
     * @param userGroup user group
     * @return all grants that are assigned to a specific username and his group
     */
    public List<BucketGrantMetadata> getAllAssignedGrantsForUserAndHisGroup(String username, String userGroup) {
        // Get all user assigned grants from the db
        List<BucketGrantEntity> dbUserBucketGrants = bucketGrantRepository.findAllGrantsAssignedToAUsername(username);
        // Add the grants that are assigned to his group
        List<BucketGrantEntity> dbGroupBucketGrants = bucketGrantRepository.findAllGrantsAssignedToAUserGroup(userGroup);

        // Result list
        List<BucketGrantEntity> result = new LinkedList<>();

        // Check for duplicates grants in the group grant
        // If the user group has a grant over a different bucket we add it to the result
        // If not, we check the access type and compare them to add only the higher access grant
        if (dbGroupBucketGrants != null && !dbGroupBucketGrants.isEmpty()) {
            for (BucketGrantEntity groupBucketGrant : dbGroupBucketGrants) {
                for (BucketGrantEntity userBucketGrant : dbUserBucketGrants) {
                    // Grant over different buckets
                    if (!groupBucketGrant.getBucketEntity().getId().equals(userBucketGrant.getBucketEntity().getId())) {
                        result.add(userBucketGrant);
                        result.add(groupBucketGrant);
                    } else if (!groupBucketGrant.getAccessType().equals(userBucketGrant.getAccessType())) {
                        // Grant over the same bucket but with different access type
                        String userGrantAccessType = userBucketGrant.getAccessType();
                        String userGroupGrantAccessType = groupBucketGrant.getAccessType();
                        // Compare the access type and add the higher one
                        if (grantAccessTypeHelperService.getPriorityLevel(userGrantAccessType, userGroupGrantAccessType) == 2) {
                            result.add(groupBucketGrant);
                        } else {
                            result.add(userBucketGrant);
                        }
                    }
                }
            }
        } else if(dbUserBucketGrants != null && !dbUserBucketGrants.isEmpty()) {
            result.addAll(dbUserBucketGrants);
        }
        if(!result.isEmpty()) {
            return result.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    /**
     *
     * Update the access type for an existing user grant
     *
     * @param bucketName bucket name
     * @param username username
     * @param accessType new access type
     * @return the updated grant assigned to the user and for the specific bucket
     */
    public BucketGrantMetadata updateBucketGrantForASpecificUser(String bucketName, String username,
            String accessType) {

        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);

        if(bucketEntity !=null) {
            bucketId = bucketEntity.getId();

            // Find the bucket grant assigned to the current user
            BucketGrantEntity bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsername(bucketId, username);

            if(bucketGrantEntity !=null) {
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
     * @param bucketName bucket name
     * @param userGroup user group
     * @param accessType new access type
     * @return the updated grant assigned to the user group and the specific bucket name
     */
    public BucketGrantMetadata updateBucketGrantForASpecificUserGroup(String bucketName, String userGroup,
            String accessType) {

        // Find the bucket and get its id
        long bucketId;
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if(bucketEntity !=null) {
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
        if(dbUserCreatedGrants != null) {
            return dbUserCreatedGrants.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
        } else {
            return new LinkedList<>();
        }
    }

    /**
     *
     *
     * @param bucketName bucket name
     * @param currentUser user with admin rights on the current bucket
     * @param accessType grant access type
     * @param username targeted username
     * @param groupName targeted group name
     * @return a newly created bucket grant assigned to a user or a user group
     * @throws DataIntegrityViolationException in case of duplicate grant or internal db errors
     */
    public BucketGrantMetadata createBucketGrant(String bucketName, String currentUser, String accessType,
            String username, String groupName) throws DataIntegrityViolationException {
        // Find the corresponding bucket from the DB
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        // Throw an error if the bucket was not found
        if (bucket == null) {
            throw new DataIntegrityViolationException("Bucket: " + bucketName + " does not exist in the catalog");
        }

        BucketGrantEntity bucketGrantEntity = null;
        if (!username.equals("") && groupName.equals("")) {
            // Case if the grant targets a specific user
            // Check if a similar grant is available in the DB
            BucketGrantEntity dbUsernameBucketGrant = bucketGrantRepository.findBucketGrantByUsername(bucket.getId(),
                                                                                                      username);
            // Throw an exception if similar grant exists
            if (dbUsernameBucketGrant != null && dbUsernameBucketGrant.getProfiteer().equals(username) &&
                dbUsernameBucketGrant.getBucketEntity().getId().equals(bucket.getId()) &&
                dbUsernameBucketGrant.getAccessType().equals(accessType)) {
                throw new BucketGrantAlreadyExistsException(bucketName, username);
            }
            // BucketGrant attributes: Type, Profiteer, Access Type and the Bucket
            bucketGrantEntity = new BucketGrantEntity("user", currentUser, username, accessType, bucket);
        } else if (username.equals("") && !groupName.equals("")) {
            // Case if the grant targets a specific group
            // Check if a similar grant is available in the DB
            BucketGrantEntity dbUserGroupBucketGrant = bucketGrantRepository.findBucketGrantByUserGroup(bucket.getId(),
                                                                                                        groupName);
            // Throw an exception if similar grant exists
            if (dbUserGroupBucketGrant != null && dbUserGroupBucketGrant.getProfiteer().equals(groupName) &&
                dbUserGroupBucketGrant.getBucketEntity().getId().equals(bucket.getId()) &&
                dbUserGroupBucketGrant.getAccessType().equals(accessType)) {
                throw new BucketGrantAlreadyExistsException(bucketName, groupName);
            }
            // BucketGrant attributes: Type, Profiteer, Access Type and the Bucket
            bucketGrantEntity = new BucketGrantEntity("group", currentUser, groupName, accessType, bucket);
        }

        // Save the grant in db
        bucketGrantEntity = bucketGrantRepository.save(bucketGrantEntity);

        return new BucketGrantMetadata(bucketGrantEntity);
    }

    /**
     *
     * @param bucketName bucket name
     * @param username username
     * @param userGroup user group
     * @return a response indicating the status of deletion of an existing grant
     */
    public BucketGrantMetadata deleteBucketGrant(String bucketName, String username, String userGroup) {
        // Find the bucket
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        BucketGrantEntity bucketGrantEntity = null;
        if (!username.equals("") && userGroup.equals("")) {
            // Case if the grant targets a specific user
            // Find the Grant for the current user
            bucketGrantEntity = bucketGrantRepository.findBucketGrantByUsername(bucketEntity.getId(), username);
        } else if (username.equals("") && !userGroup.equals("")) {
            // Case if the grant targets a specific group
            // Find the Grant fot the current userGroup
            bucketGrantEntity = bucketGrantRepository.findBucketGrantByUserGroup(bucketEntity.getId(), userGroup);
        }

        if (bucketGrantEntity != null) {// Delete the grant from DB
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
    public boolean isTheUserGrantSufficientForTheCurrentTask(AuthenticatedUser user,
                                                             String bucketName, String requiredAccessType) {
        // Find the bucket and get its id
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();

        List<BucketGrantEntity> grants = bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId);

        grants.removeIf(bucketGrantEntity -> ((!bucketGrantEntity.getProfiteer().equals(user.getName()) &&
                                               bucketGrantEntity.getGrantee().equals("user")) ||
                                              (!user.getGroups().contains(bucketGrantEntity.getProfiteer()) &&
                                               bucketGrantEntity.getGrantee().equals("group"))));

        BucketGrantEntity bucketGrantEntity;

        // We need to pick only the grant with the higher access type
        if (grants.size() > 0) {
            bucketGrantEntity = grants.get(0);
            if (!bucketGrantEntity.getAccessType().equals(admin.toString())) {
                for (int index = 1; index < grants.size(); index++) {
                    if (grantAccessTypeHelperService.getPriorityLevel(bucketGrantEntity.getAccessType(), grants.get(index).getAccessType()) == 2) {
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
        for (String group : user.getGroups()) {
            bucketGrants.addAll(this.getAllAssignedGrantsForUserAndHisGroup(user.getName(), group));
            bucketsIdsFromCatalogObjectGrants.addAll(catalogObjectGrantService.getAllBucketIdsFromGrantsAssignedToUserGroup(group));
        }

        // Get from bucket grants the targeted buckets
        for (BucketGrantMetadata bucketGrantMetadata : bucketGrants) {
            BucketEntity bucket = bucketRepository.findOne(bucketGrantMetadata.getBucketId());
            BucketMetadata bucketMetadata = new BucketMetadata(bucket, bucket.getCatalogObjects().size());
            bucketMetadataList.add(bucketMetadata);
        }

        // Get the buckets from the list of buckets' ids
        for (Long id : bucketsIdsFromCatalogObjectGrants) {
            BucketEntity bucket =  bucketRepository.findOne(id);
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
    public void deleteAllBucketGrants(long bucketId) {
        // Get existing grants
        List<BucketGrantEntity> existingBucketGrantsToDelete = bucketGrantRepository.findBucketGrantEntitiesByBucketEntityId(bucketId);

        if (existingBucketGrantsToDelete != null) {
            // Delete all bucket Grants
            bucketGrantRepository.delete(existingBucketGrantsToDelete);

            //Delete all catalog objects grants found in this bucket
            catalogObjectGrantService.deleteAllCatalogObjectsGrantsAssignedToABucket(bucketId);
        }
    }
}
