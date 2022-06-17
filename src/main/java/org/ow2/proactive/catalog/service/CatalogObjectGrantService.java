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

import static org.ow2.proactive.catalog.util.GrantHelper.GROUP_GRANTEE_TYPE;
import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

import java.util.*;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectGrantRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.repository.entity.*;
import org.ow2.proactive.catalog.service.exception.CatalogObjectGrantAlreadyExistsException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.exception.GrantNotFoundException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeValidator;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.ow2.proactive.catalog.util.ModificationHistoryData;
import org.ow2.proactive.catalog.util.PriorityLevelValidator;
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

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName object name
     * @return the id of an object
     */
    public CatalogObjectRevisionEntity getCatalogObject(String bucketName, String catalogObjectName) {
        // Find the catalog object id
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(Collections.singletonList(bucketName),
                                                                                                                                         catalogObjectName);
        if (catalogObjectRevisionEntity != null) {
            return catalogObjectRevisionEntity;
        } else {
            throw new CatalogObjectNotFoundException(bucketName, catalogObjectName);
        }
    }

    /**
     * Get the list of all the catalog object grants assigned to the user and its groups for all the catalog objects.
     *
     * @param user authenticated user
     * @return the list of all the catalog object grants assigned to the user and its groups
     */
    public List<CatalogObjectGrantMetadata> getObjectsGrants(AuthenticatedUser user) {
        List<CatalogObjectGrantEntity> userGrants = catalogObjectGrantRepository.findAllObjectGrantsAssignedToAUser(user.getName());
        userGrants.addAll(catalogObjectGrantRepository.findAllObjectGrantsAssignedToUserGroups(user.getGroups()));
        return GrantHelper.mapToObjectGrants(userGrants);
    }

    /**
     * Get the list of grants assigned to a specific catalog object
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName object name
     * @return all grants created on a specific object in a bucket
     */
    public List<CatalogObjectGrantMetadata> getObjectGrants(String bucketName, String catalogObjectName) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity object = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                    catalogObjectName);
        if (object == null) {
            throw new CatalogObjectNotFoundException(bucketName, catalogObjectName);
        }
        return GrantHelper.mapToObjectGrants(catalogObjectGrantRepository.findCatalogObjectGrantsByBucketNameAndCatalogObjectName(bucketName,
                                                                                                                                  catalogObjectName));
    }

    /**
     * Get list of grants assigned to the user for a specific catalog object
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName object name
     * @return a list of object grants without the no access grant type
     */
    public List<CatalogObjectGrantMetadata> getObjectGrants(AuthenticatedUser user, String bucketName,
            String catalogObjectName) {
        List<CatalogObjectGrantEntity> result = catalogObjectGrantRepository.findGrantsAssignedToAnObject(bucketName,
                                                                                                          catalogObjectName);
        return GrantHelper.filterObjectsGrantsAssignedToUserOrItsGroups(user, result);
    }

    /**
     *
     * Get all grants metadata assigned to a specific bucket
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return the list of all catalog object grant metadata assigned to a bucket
     */
    public List<CatalogObjectGrantMetadata> getObjectsGrantsInABucket(String bucketName) {
        long bucketId = bucketRepository.findOneByBucketName(bucketName).getId();
        return GrantHelper.mapToObjectGrants(catalogObjectGrantRepository.findCatalogObjectGrantEntitiesByBucketEntityId(bucketId));
    }

    /**
     * Get the list of grants (including all access types) for the catalog objects in the specific bucket assigned to the specific user and its groups.
     *
     * @param user authenticated user
     * @param bucketName name of the specific bucket
     * @return a list of objects grants in the bucket assigned to the user and its groups.
     */
    public List<CatalogObjectGrantMetadata> getObjectsGrantsInABucket(AuthenticatedUser user, String bucketName) {
        return GrantHelper.filterGrantsAssignedToUserOrItsGroups(user, getObjectsGrantsInABucket(bucketName));
    }

    /**
     *
     * @param user authenticated user
     * @return the list of all grants with a noAccess rights assigned to a user
     */
    public List<CatalogObjectGrantMetadata> getNoAccessGrant(AuthenticatedUser user) {
        List<CatalogObjectGrantEntity> userGrants = catalogObjectGrantRepository.findAllObjectGrantsWithNoAccessRightsAndAssignedToAUsername(user.getName());
        List<CatalogObjectGrantEntity> userGroupGrants = catalogObjectGrantRepository.findAllObjectGrantsWithNoAccessRightsAndAssignedToAUserGroup(user.getGroups());
        if (userGroupGrants != null && !userGroupGrants.isEmpty()) {
            userGrants.addAll(userGroupGrants);
        }
        return userGrants.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
    }

    /**
     * Get the list of positive catalog object grants assigned to the user and its groups for all the catalog objects.
     *
     * @param user authenticated user
     * @return the list of all the catalog object grants assigned to the user and its groups
     */
    public List<CatalogObjectGrantMetadata> getAccessibleObjectsGrants(AuthenticatedUser user) {
        List<CatalogObjectGrantEntity> userGrants = catalogObjectGrantRepository.findAllAccessibleObjectGrantsAssignedToAUser(user.getName());
        userGrants.addAll(catalogObjectGrantRepository.findAllAccessibleObjectGrantsAssignedToUserGroups(user.getGroups()));
        return GrantHelper.mapToObjectGrants(userGrants);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param currentUser username of the admin creating this grant.
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @param username The name of the user that will benefit of the access grant.
     * @return a catalog object grant for a user
     * @throws DataIntegrityViolationException if a similar grant exists
     */
    @Transactional
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
        CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUser = catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(catalogObjectName,
                                                                                                                                            username,
                                                                                                                                            bucket.getId());
        if (dbCatalogObjectGrantEntityForUser != null &&
            (dbCatalogObjectGrantEntityForUser.getGrantee().equals(username) &&
             dbCatalogObjectGrantEntityForUser.getCatalogObject().getId().getName().equals(catalogObjectName))) {
            throw new CatalogObjectGrantAlreadyExistsException("Grant exists for object: " + catalogObjectName +
                                                               " in bucket: " + bucketName +
                                                               " and already assigned to user: " + username);
        }
        // Create new grant
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(USER_GRANTEE_TYPE,
                                                                                         currentUser,
                                                                                         username,
                                                                                         accessType,
                                                                                         catalogObjectRevisionEntity.getCatalogObject());
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
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @param userGroup The name of the group of users that will benefit of the access grant.
     * @return a catalog object grant for a group of users
     * @throws DataIntegrityViolationException if a similar grant exists
     */
    @Transactional
    public CatalogObjectGrantMetadata createCatalogObjectGrantForAGroup(String bucketName, String catalogObjectName,
            String currentUser, String accessType, int priority, String userGroup)
            throws DataIntegrityViolationException {
        PriorityLevelValidator.checkAndValidateTheGivenPriorityLevel(priority);
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        // Find the bucket and the catalog object
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        // Check if a similar grant exists
        CatalogObjectGrantEntity dbCatalogObjectGrantEntityForUserGroup = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroupForUpdate(catalogObjectName,
                                                                                                                                                  userGroup,
                                                                                                                                                  bucket.getId());
        if (dbCatalogObjectGrantEntityForUserGroup != null &&
            dbCatalogObjectGrantEntityForUserGroup.getGrantee().equals(userGroup) &&
            dbCatalogObjectGrantEntityForUserGroup.getCatalogObject().getId().getName().equals(catalogObjectName)) {
            throw new CatalogObjectGrantAlreadyExistsException("Grant exists for object: " + catalogObjectName +
                                                               " in bucket: " + bucketName +
                                                               " and already assigned to user group: " + userGroup);
        }
        // Create new grant
        CatalogObjectGrantEntity catalogObjectGrantEntity = new CatalogObjectGrantEntity(GROUP_GRANTEE_TYPE,
                                                                                         currentUser,
                                                                                         userGroup,
                                                                                         accessType,
                                                                                         priority,
                                                                                         catalogObjectRevisionEntity.getCatalogObject());
        // Save the grant
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        // Return the result
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param username name of the user that is benefiting from the access grant.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @return the updated user object grant
     */
    @Transactional
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAUser(AuthenticatedUser currentUser, String username,
            String catalogObjectName, String bucketName, String accessType) {
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(catalogObjectName,
                                                                                                                                   username,
                                                                                                                                   bucket.getId());
        if (catalogObjectGrantEntity != null) {
            String oldValue = catalogObjectGrantEntity.toString();
            // Update the access type
            catalogObjectGrantEntity.setAccessType(accessType);
            // Add modification history
            ModificationHistoryData modificationHistoryData = new ModificationHistoryData(System.currentTimeMillis(),
                                                                                          currentUser.getName());
            // Set old values in history
            modificationHistoryData.setOldValues(oldValue);
            // Set new values in history
            modificationHistoryData.setNewValues(catalogObjectGrantEntity.toString());
            // Compute changes
            modificationHistoryData.computeChanges(USER_GRANTEE_TYPE, oldValue, catalogObjectGrantEntity.toString());
            catalogObjectGrantEntity.getModificationHistory().push(modificationHistoryData);
        } else {
            throw new GrantNotFoundException(username, bucketName, catalogObjectName);
        }
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param userGroup name of the group of users that are benefiting of the access grant.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param accessType type of the access grant. It can be either noAccess, read, write or admin.
     * @return the updated group object grant
     */
    @Transactional
    public CatalogObjectGrantMetadata updateCatalogObjectGrantForAGroup(AuthenticatedUser currentUser, String userGroup,
            String catalogObjectName, String bucketName, String accessType, int priority) {
        PriorityLevelValidator.checkAndValidateTheGivenPriorityLevel(priority);
        accessType = AccessTypeValidator.checkAndValidateTheGivenAccessType(accessType);
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroupForUpdate(catalogObjectName,
                                                                                                                                    userGroup,
                                                                                                                                    bucket.getId());
        if (catalogObjectGrantEntity != null) {
            String oldValue = catalogObjectGrantEntity.toString();
            // Update th access type
            catalogObjectGrantEntity.setAccessType(accessType);
            // Update the priority
            catalogObjectGrantEntity.setPriority(priority);
            // Add modification history
            ModificationHistoryData modificationHistoryData = new ModificationHistoryData(System.currentTimeMillis(),
                                                                                          currentUser.getName());
            // Set old values in history
            modificationHistoryData.setOldValues(oldValue);
            // Set new values in history
            modificationHistoryData.setNewValues(catalogObjectGrantEntity.toString());
            // Compute changes
            modificationHistoryData.computeChanges(GROUP_GRANTEE_TYPE, oldValue, catalogObjectGrantEntity.toString());
            catalogObjectGrantEntity.getModificationHistory().push(modificationHistoryData);
        } else {
            throw new GrantNotFoundException(userGroup, bucketName, catalogObjectName);
        }
        catalogObjectGrantEntity = catalogObjectGrantRepository.save(catalogObjectGrantEntity);
        return new CatalogObjectGrantMetadata(catalogObjectGrantEntity);
    }

    /**
     *
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @param username name of the user that is benefiting from the access grant.
     * @return the deleted object grant
     */
    @Transactional
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAUser(String bucketName, String catalogObjectName,
            String username) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);

        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        assert bucket != null;
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUsernameForUpdate(catalogObjectName,
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
    @Transactional
    public CatalogObjectGrantMetadata deleteCatalogObjectGrantForAGroup(String bucketName, String catalogObjectName,
            String userGroup) {
        List<String> bucketsName = new LinkedList<>();
        bucketsName.add(bucketName);
        CatalogObjectRevisionEntity catalogObjectRevisionEntity = catalogObjectRevisionRepository.findDefaultCatalogObjectByNameInBucket(bucketsName,
                                                                                                                                         catalogObjectName);
        throwExceptionIfCatalogObjectIsNull(catalogObjectRevisionEntity, bucketName, catalogObjectName);
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        assert bucket != null;
        CatalogObjectGrantEntity catalogObjectGrantEntity = catalogObjectGrantRepository.findCatalogObjectGrantByUserGroupForUpdate(catalogObjectName,
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
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName object name
     * @return the list of deleted grants created for an object inside a bucket
     */
    @Transactional
    public List<CatalogObjectGrantMetadata> deleteAllCatalogObjectGrantsAssignedToAnObjectInABucket(String bucketName,
            String catalogObjectName) {
        List<CatalogObjectGrantEntity> result = catalogObjectGrantRepository.findGrantsAssignedToAnObject(bucketName,
                                                                                                          catalogObjectName);

        catalogObjectGrantRepository.delete(result);
        return result.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
    }

    /**
     *
     * Delete all catalog object grant assigned to a specific object
     *
     * @param bucketName bucket name
     * @param catalogObjectName catalog object name
     */
    public void deleteAllCatalogObjectGrantsByBucketNameAndObjectName(String bucketName, String catalogObjectName) {
        // Get the catalog objects grants
        List<CatalogObjectGrantEntity> catalogObjectGrants = catalogObjectGrantRepository.findCatalogObjectGrantsByBucketNameAndCatalogObjectName(bucketName,
                                                                                                                                                  catalogObjectName);
        catalogObjectGrantRepository.delete(catalogObjectGrants);
    }

    /**
     * Check if a WF is available in a specific bucket
     * @param catalogObjectRevisionEntity catalog entity
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName WF name
     */
    private void throwExceptionIfCatalogObjectIsNull(CatalogObjectRevisionEntity catalogObjectRevisionEntity,
            String bucketName, String catalogObjectName) {
        if (catalogObjectRevisionEntity == null) {
            throw new DataIntegrityViolationException("Catalog object:" + catalogObjectName +
                                                      " does not exist in bucket: " + bucketName);
        }
    }
}
