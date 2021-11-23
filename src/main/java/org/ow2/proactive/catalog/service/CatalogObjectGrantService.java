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
     * @param bucketName bucket name
     * @param catalogObjectName WF name
     * @param currentUser current user with admin rights over the bucket
     * @param accessType access type
     * @param username username
     * @param groupName user group
     * @return a new grant access for the specific WF
     * @throws DataIntegrityViolationException in case of an internal db query error
     */
    public CatalogObjectGrantMetadata createCatalogObjectGrant(String bucketName, String catalogObjectName,
            String currentUser, String accessType, String username, String groupName)
            throws DataIntegrityViolationException {

        CatalogObjectGrantEntity catalogObjectGrantEntity = null;

        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);

        checkForAvailability(catalogObjectRevisionEntity, bucketName, catalogObjectName);

        if (!username.isEmpty() && groupName.isEmpty()) {
            // Case if the grant targets a specific user
            CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUser = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                                                       username,
                                                                                                                                       bucket.getId());
            if (dbCatalogObjectGrantEntityForUser != null &&
                (dbCatalogObjectGrantEntityForUser.getGrantee().equals(username) &&
                 dbCatalogObjectGrantEntityForUser.getCatalogObjectRevisionEntity()
                                                  .getId()
                                                  .equals(catalogObjectRevisionEntity.getId()) &&
                 dbCatalogObjectGrantEntityForUser.getAccessType().equals(accessType))) {
                throw new CatalogObjectGrantAlreadyExistsException(catalogObjectName, bucketName, username);
            }
            catalogObjectGrantEntity = new CatalogObjectGrantEntity("user",
                                                                    currentUser,
                                                                    username,
                                                                    accessType,
                                                                    catalogObjectRevisionEntity,
                                                                    bucket);

        } else if (username.isEmpty() && !groupName.isEmpty()) {
            // Case if the grant targets a specific user group
            CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUserGroup = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                                                             groupName,
                                                                                                                                             bucket.getId());
            if (dbCatalogObjectGrantEntityForUserGroup != null &&
                dbCatalogObjectGrantEntityForUserGroup.getGrantee().equals(username) &&
                dbCatalogObjectGrantEntityForUserGroup.getCatalogObjectRevisionEntity()
                                                      .getId()
                                                      .equals(catalogObjectRevisionEntity.getId()) &&
                dbCatalogObjectGrantEntityForUserGroup.getAccessType().equals(accessType)) {
                throw new CatalogObjectGrantAlreadyExistsException(catalogObjectName, bucketName, groupName);
            }
            // BucketGrant attributes: Type, Profiteer, Access Type and the Bucket
            catalogObjectGrantEntity = new CatalogObjectGrantEntity("group",
                                                                    currentUser,
                                                                    groupName,
                                                                    accessType,
                                                                    catalogObjectRevisionEntity,
                                                                    bucket);
        }
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);

        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     * Delete an existing user or user group grant
     * @param bucketName bucket name
     * @param catalogObjectName WF name
     * @param username username
     * @param groupName user group
     * @return an empty catalog grant metadata if the deletion process was successful
     */
    public CatalogObjectGrantMetadata deleteCatalogObjectGrant(String bucketName, String catalogObjectName,
            String username, String groupName) {

        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        checkForAvailability(catalogObjectRevisionEntity, bucketName, catalogObjectName);

        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);

        CatalogObjectGrantEntity catalogObjectGrantEntity = null;

        assert bucket != null;
        if (!username.isEmpty() && groupName.isEmpty()) {
            // Case if the grant targets a specific user
            catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                     username,
                                                                                                     bucket.getId());
        } else if (username.isEmpty() && !groupName.isEmpty()) {
            catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                      groupName,
                                                                                                      bucket.getId());
        }

        if (catalogObjectGrantEntity != null) {
            catalogObjectGrantRepository.delete(catalogObjectGrantEntity);
        } else {
            throw new DataIntegrityViolationException("Catalog object grant was not found in the DB.");
        }

        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);

    }

    /**
     *
     * Set a new access type for an available grant
     *
     * @param username username
     * @param groupName user group
     * @param catalogObjectName WF name
     * @param bucketName bucket name
     * @param accessType new grant access type
     * @return the updated grant
     */
    public CatalogObjectGrantMetadata updateCatalogObjectGrant(String username, String groupName,
            String catalogObjectName, String bucketName, String accessType) {

        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        checkForAvailability(catalogObjectRevisionEntity, bucketName, catalogObjectName);

        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);

        CatalogObjectGrantEntity catalogObjectGrantEntity = null;

        if (!username.isEmpty() && groupName.isEmpty()) {
            // Case if the grant targets a specific user
            catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsername(catalogObjectRevisionEntity.getId(),
                                                                                                     username,
                                                                                                     bucket.getId());
        } else if (username.isEmpty() && !groupName.isEmpty()) {
            // Case if the grant targets a specific user group
            catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroup(catalogObjectRevisionEntity.getId(),
                                                                                                      groupName,
                                                                                                      bucket.getId());
        }

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
     * @param username username
     * @param userGroup user group
     * @param catalogObjectName WF name
     * @param bucketName bucket name
     * @return the list of all catalog objects grants assigned to a user and his group
     */
    public List<CatalogObjectGrantMetadata> getAllAssignedCatalogObjectGrantsForTheCurrentUserAndHisGroup(
            String username, String userGroup, String catalogObjectName, String bucketName) {

        // Find the catalog object
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        checkForAvailability(catalogObjectRevisionEntity, bucketName, catalogObjectName);

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

    public List<CatalogObjectGrantMetadata> getAllCreatedCatalogObjectGrantsForThisBucket(String username,
            String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        List<CatalogObjectGrantEntity> dbUserCreatedCatalogObjectGrants = catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByCreatorAndBucketEntityId(username,
                                                                                                                                                                bucket.getId());
        return dbUserCreatedCatalogObjectGrants.stream()
                                               .map(CatalogObjectGrantMetadata::new)
                                               .collect(Collectors.toList());
    }

    /**
     * Check if a WF is available in a specific bucket
     * @param catalogObjectRevisionEntity catalog entity
     * @param bucketName bucket name
     * @param catalogObjectName WF name
     */
    private void checkForAvailability(CatalogObjectRevisionEntity catalogObjectRevisionEntity, String bucketName,
            String catalogObjectName) {
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
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        long catalogObjectId = catalogObjectRevisionEntity.getId();

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
                return false;
            }
        }

        return true;
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

        // Find the catalog object id
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        long catalogObjectId = catalogObjectRevisionEntity.getId();

        // Remove all grants that are not assigned to the current user and his group
        grantEntities.removeIf(catalogObjectGrantEntity -> (((!catalogObjectGrantEntity.getGrantee()
                                                                                       .equals(user.getName()) &&
                                                              catalogObjectGrantEntity.getGrantee().equals("user")) ||
                                                             (!user.getGroups()
                                                                   .contains(catalogObjectGrantEntity.getGrantee()) &&
                                                              catalogObjectGrantEntity.getGrantee().equals("group"))) &&
                                                            catalogObjectGrantEntity.getCatalogObjectRevisionEntity()
                                                                                    .getId() != catalogObjectId));

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
    public void deleteAllCatalogObjectGrants(long bucketId, long catalogObjectId) {
        // Get the catalog objects grants
        List<CatalogObjectGrantEntity> catalogObjectGrants = catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByCatalogObjectRevisionEntityIdAndBucketEntityId(catalogObjectId,
                                                                                                                                                                         bucketId);

        if (!catalogObjectGrants.isEmpty()) {
            catalogObjectGrantRepository.delete(catalogObjectGrants);
        }
    }

}
