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

import static org.ow2.proactive.catalog.util.AccessType.admin;
import static org.ow2.proactive.catalog.util.AccessType.noAccess;

import java.util.*;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeAndPriorityForGroupGrant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Service
@Transactional
public class GrantRightsService {

    @Autowired
    private BucketGrantService bucketGrantService;

    @Autowired
    private CatalogObjectGrantService catalogObjectGrantService;

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    GrantAccessTypeHelperService grantAccessTypeHelperService;

    /**
     *
     * This method calculates the resulting grant for a user for an operation regarding a bucket, taking into consideration the priorities of the grants assigned
     * to his group.
     * The grant assigned to the user himself/herself is prioritized over the ones assigned to his/her group(s).
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @return the resulting access right for a bucket related operation
     */
    public String getResultingAccessTypeFromUserGrantsForBucketOperations(AuthenticatedUser user, String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }
        //        String assignedRights = getResultingAccessTypeFromUserGrantsForBucketOperations(user, bucketName);

        List<BucketGrantMetadata> userBucketGrants = bucketGrantService.getUserAccessibleBucketGrants(user, bucketName);
        if (user.getGroups().contains(bucket.getOwner().substring(6)) ||
            bucket.getOwner().substring(6).equals("public-objects")) {
            BucketGrantMetadata defaultBucketGrantMetadata = new BucketGrantMetadata("group",
                                                                                     user.getName(),
                                                                                     bucket.getOwner().substring(6),
                                                                                     "admin",
                                                                                     5,
                                                                                     bucket.getId(),
                                                                                     bucketName);
            userBucketGrants.add(defaultBucketGrantMetadata);
        }
        if (!userBucketGrants.isEmpty()) {
            // Check for a user grant
            Optional<BucketGrantMetadata> optUserSpecificGrant = filterFirstUserSpecificBucketGrant(userBucketGrants);
            if (optUserSpecificGrant.isPresent()) {
                // Return the user grant accessType (A user has only one user grant)
                return optUserSpecificGrant.get().getAccessType();
            } else {
                // Check the user group grants
                Map<String, AccessTypeAndPriorityForGroupGrant> accessTypePerUserGroup = this.checkIfUserGroupBucketGrantsExistsForTheCurrentBucketGrantListAndReturnTheMapOfAccessTypePerUserGroup(userBucketGrants);
                return this.getAccessTypeFromHighestPriorityUserGroupGrant(accessTypePerUserGroup);
            }
        }
        return noAccess.toString();
    }

    /**
     * Calculate the access type of the bucket based on the user's grants for this bucket.
     *
     * @param userBucketGrants the list of all grants assigned to the user targeting the bucket
     * @return bucket access type as string
     */
    public String getBucketAccessType(List<BucketGrantMetadata> userBucketGrants) {
        // In case when the user and user group object grants are unavailable, we check in the bucket grants for the accessType
        if (userBucketGrants.isEmpty()) {
            return noAccess.toString();
        }
        Optional<BucketGrantMetadata> highestPriorityGrants = getHighestPriorityGrantForABucket(userBucketGrants);
        if (highestPriorityGrants.isPresent()) {
            return highestPriorityGrants.get().getAccessType();
        } else {
            return noAccess.toString();
        }
    }

    /**
     * This method calculates the resulting grant for a user for an operation regarding an object, taking into consideration the priorities of the grants assigned
     * to his group.
     * The grant assigned to the user himself/herself over the object is prioritized first, then the ones over the bucket, then the ones assigned to his/her group(s) over the object
     * and finally the ones assigned to his/her group(s) over the bucket.
     *
     * @param user authenticated user
     * @param bucketName name of the bucket where the catalog object is stored.
     * @param catalogObjectName name of the catalog object subject of the access grant.
     * @return the resulting access rights for a catalog object related operation
     */
    public String getResultingAccessTypeFromUserGrantsForCatalogObjectOperations(AuthenticatedUser user,
            String bucketName, String catalogObjectName) {
        CatalogObjectRevisionEntity catalogObjectEntity = catalogObjectGrantService.getCatalogObject(bucketName,
                                                                                                     catalogObjectName);
        if (catalogObjectEntity == null) {
            throw new CatalogObjectNotFoundException(bucketName, catalogObjectName);
        }

        List<CatalogObjectGrantMetadata> allUserCatalogObjectGrants = catalogObjectGrantService.getAllObjectGrantsAssignedToAnObjectInsideABucketForAUser(user,
                                                                                                                                                          bucketName,
                                                                                                                                                          catalogObjectName);
        if (!allUserCatalogObjectGrants.isEmpty()) {
            Optional<String> userBucketRights = bucketGrantService.getUserAccessibleBucketGrants(user, bucketName)
                                                                  .stream()
                                                                  .filter(grant -> grant.getGranteeType()
                                                                                        .equals("user"))
                                                                  .findFirst()
                                                                  .map(BucketGrantMetadata::getAccessType);
            return getHighestPriorityRightForCatalogObject(userBucketRights, allUserCatalogObjectGrants);
        }
        // In case when the user and user group object grants are unavailable, we check in the bucket grants for the accessType
        return this.getResultingAccessTypeFromUserGrantsForBucketOperations(user, bucketName);
    }

    //TODO
    public String getCatalogObjectAccessType(boolean isPublicBucket, String bucketRights,
            Optional<String> userSpecificBucketRights, List<CatalogObjectGrantMetadata> userObjectGrants) {
        if (isPublicBucket) {
            return admin.name();
        } else {
            String objectRights = bucketRights;
            if (!userObjectGrants.isEmpty()) {
                objectRights = getHighestPriorityRightForCatalogObject(userSpecificBucketRights, userObjectGrants);
            }
            return objectRights;
        }
    }

    //TODO
    private String getHighestPriorityRightForCatalogObject(Optional<String> userSpecificBucketRights,
            List<CatalogObjectGrantMetadata> allUserCatalogObjectGrants) {
        // Check for a user grant
        Optional<String> userSpecificObjectRight = this.getUserSpecificObjectGrantAccessType(allUserCatalogObjectGrants);
        if (userSpecificObjectRight.isPresent()) {
            // Return the user grant accessType (A user has only one user grant)
            return userSpecificObjectRight.get();
        } else if (userSpecificBucketRights.isPresent()) {
            return userSpecificBucketRights.get();
        } else {
            // Check the user group grants
            return this.getAccessTypeFromHighestPriorityUserGroupGrant(getObjectGrantsAccessTypePerUserGroup(allUserCatalogObjectGrants));
        }
    }

    /**
     * Check if the user has an object grant and return its access right value
     *
     * @param allUserCatalogObjectGrants catalog objects grants assigned to the user
     * @return the catalog object grant access right from the user grant
     */
    private Optional<String>
            getUserSpecificObjectGrantAccessType(List<CatalogObjectGrantMetadata> allUserCatalogObjectGrants) {
        return allUserCatalogObjectGrants.stream()
                                         .filter(grant -> grant.getGranteeType().equals("user"))
                                         .map(CatalogObjectGrantMetadata::getAccessType)
                                         .findFirst();
    }

    /**
     * Check the user group bucket grants and return the access right and priority level per group
     *
     * @param allUserBucketGrants bucket grants assigned to a user
     * @return a hashmap that map each group to its access right and its priority
     */
    private Map<String, AccessTypeAndPriorityForGroupGrant>
            checkIfUserGroupBucketGrantsExistsForTheCurrentBucketGrantListAndReturnTheMapOfAccessTypePerUserGroup(
                    List<BucketGrantMetadata> allUserBucketGrants) {
        Map<String, AccessTypeAndPriorityForGroupGrant> accessTypesPerUserGroup = new HashMap<>();
        List<BucketGrantMetadata> userGroupBucketGrants = allUserBucketGrants.stream()
                                                                             .filter(grant -> grant.getGranteeType()
                                                                                                   .equals("group"))
                                                                             .sorted(Comparator.comparingInt(BucketGrantMetadata::getPriority))
                                                                             .collect(Collectors.toList());
        for (BucketGrantMetadata grant : userGroupBucketGrants) {
            AccessTypeAndPriorityForGroupGrant accessTypeAndPriorityForGroupGrant = new AccessTypeAndPriorityForGroupGrant(grant.getAccessType(),
                                                                                                                           grant.getPriority());
            accessTypesPerUserGroup.put(grant.getGrantee(), accessTypeAndPriorityForGroupGrant);
        }
        return accessTypesPerUserGroup;
    }

    /**
     * Check the user group catalog object grants and return the access right and priority level per group
     *
     * @param allUserObjectGrants catalog object grants assigned to a user
     * @return a hashmap that map each group to its access right and its priority
     */
    private Map<String, AccessTypeAndPriorityForGroupGrant>
            getObjectGrantsAccessTypePerUserGroup(List<CatalogObjectGrantMetadata> allUserObjectGrants) {
        Map<String, AccessTypeAndPriorityForGroupGrant> accessTypesPerUserGroup = new HashMap<>();
        List<CatalogObjectGrantMetadata> userGroupCatalogObjectGrants = allUserObjectGrants.stream()
                                                                                           .filter(grant -> grant.getGranteeType()
                                                                                                                 .equals("group"))
                                                                                           .collect(Collectors.toList());
        if (!userGroupCatalogObjectGrants.isEmpty()) {
            for (CatalogObjectGrantMetadata grant : userGroupCatalogObjectGrants) {
                AccessTypeAndPriorityForGroupGrant accessTypeAndPriorityForGroupGrant = new AccessTypeAndPriorityForGroupGrant(grant.getAccessType(),
                                                                                                                               grant.getPriority());
                accessTypesPerUserGroup.put(grant.getGrantee(), accessTypeAndPriorityForGroupGrant);
            }
        }
        return accessTypesPerUserGroup;
    }

    /**
     *
     * @param accessTypesPerUserGroup a map of access rights and their priority
     * @return the access right of the grant with the highest priority
     */
    private String getAccessTypeFromHighestPriorityUserGroupGrant(
            Map<String, AccessTypeAndPriorityForGroupGrant> accessTypesPerUserGroup) {
        int highestPriority = 0;
        String highestGrantByPriorityKey = "";
        if (!accessTypesPerUserGroup.isEmpty()) {
            for (String groupKey : accessTypesPerUserGroup.keySet()) {
                AccessTypeAndPriorityForGroupGrant currentGroupAccessAccessTypeAndPriority = accessTypesPerUserGroup.get(groupKey);
                if (currentGroupAccessAccessTypeAndPriority.getPriorityLevel() > highestPriority) {
                    highestPriority = currentGroupAccessAccessTypeAndPriority.getPriorityLevel();
                    highestGrantByPriorityKey = groupKey;
                } else if (currentGroupAccessAccessTypeAndPriority.getPriorityLevel() == highestPriority) {
                    if (grantAccessTypeHelperService.getPriorityLevel(accessTypesPerUserGroup.get(highestGrantByPriorityKey)
                                                                                             .getAccessType(),
                                                                      currentGroupAccessAccessTypeAndPriority.getAccessType()) == 2) {
                        highestPriority = currentGroupAccessAccessTypeAndPriority.getPriorityLevel();
                        highestGrantByPriorityKey = groupKey;
                    }
                }
            }
            return accessTypesPerUserGroup.get(highestGrantByPriorityKey).getAccessType();
        }
        return noAccess.toString();
    }

    /**
     *
     * @param user authenticated user
     * @return the list of buckets that are accessible for the user via his grants
     */
    public List<BucketMetadata> getBucketsForUserByGrantsAndPriority(AuthenticatedUser user) {
        List<BucketGrantMetadata> bucketGrants = bucketGrantService.getUserAccessibleBucketsGrants(user);
        List<CatalogObjectGrantMetadata> catalogObjectGrants = catalogObjectGrantService.getAccessibleObjectsGrantsAssignedToAUser(user);
        Optional<BucketGrantMetadata> optUserBucketGrant = bucketGrants.stream()
                                                                       .filter(grant -> grant.getGranteeType()
                                                                                             .equals("user"))
                                                                       .findFirst();
        List<BucketGrantMetadata> noAccessGrantBuckets = bucketGrantService.getAllNoAccessGrants();
        for (BucketGrantMetadata nonAccessibleBucketGrants : noAccessGrantBuckets) {
            if (nonAccessibleBucketGrants.getGranteeType().equals("user") &&
                nonAccessibleBucketGrants.getGrantee().equals(user.getName())) {
                bucketGrants.removeIf(grant -> grant.getBucketName().equals(nonAccessibleBucketGrants.getBucketName()));
            } else if (nonAccessibleBucketGrants.getGranteeType().equals("group") &&
                       user.getGroups().contains(nonAccessibleBucketGrants.getGrantee())) {
                bucketGrants.removeIf(grant -> grant.getPriority() < nonAccessibleBucketGrants.getPriority() &&
                                               grant.getBucketName()
                                                    .equals(nonAccessibleBucketGrants.getBucketName()) &&
                                               !optUserBucketGrant.isPresent());
            }
        }

        Set<String> bucketNames = bucketGrants.stream()
                                              .map(BucketGrantMetadata::getBucketName)
                                              .collect(Collectors.toSet());

        bucketNames.addAll(catalogObjectGrants.stream()
                                              .map(CatalogObjectGrantMetadata::getBucketName)
                                              .collect(Collectors.toSet()));
        List<BucketMetadata> bucketMetadataList = new LinkedList<>();
        for (String bucketName : bucketNames) {
            long bucketId = bucketGrantService.getBucketIdByName(bucketName);
            BucketEntity bucket = bucketRepository.findOne(bucketId);
            BucketMetadata bucketMetadata = new BucketMetadata(bucket, bucket.getCatalogObjects().size());
            bucketMetadataList.add(bucketMetadata);
        }
        return bucketMetadataList;
    }

    /**
     * TODO
     * @param user authenticated user
     * @return the list of bucket grants that have the highest priorities per group
     */
    public List<BucketGrantMetadata> findAllUserBucketGrants(AuthenticatedUser user, String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }
        List<BucketGrantMetadata> userBucketGrants = bucketGrantService.getUserAccessibleBucketGrants(user, bucketName);
        addGrantsForBucketOwner(user, bucketName, bucket.getOwner(), userBucketGrants);
        return userBucketGrants;
    }

    public boolean isPublicBucket(String bucketOwner) {
        return bucketOwner.substring(6).equals("public-objects");
    }

    public void addGrantsForBucketOwner(AuthenticatedUser user, String bucketName, String bucketOwner,
            List<BucketGrantMetadata> userBucketGrants) {
        if (user.getGroups().contains(bucketOwner.substring(6))) {

            BucketGrantMetadata defaultBucketGrantMetadata = new BucketGrantMetadata("group",
                                                                                     user.getName(),
                                                                                     bucketOwner.substring(6),
                                                                                     "admin",
                                                                                     5,
                                                                                     0,
                                                                                     bucketName);
            userBucketGrants.add(defaultBucketGrantMetadata);
        }
    }

    /**
     * TODO remove
     * @param userBucketGrants list of user bucket grants
     * @return list of the highest group grants assigned to a user
     */
    private List<BucketGrantMetadata>
            getListOfBucketGrantWithHighestPriority(List<BucketGrantMetadata> userBucketGrants) {
        Set<String> bucketNames = userBucketGrants.stream()
                                                  .map(BucketGrantMetadata::getBucketName)
                                                  .collect(Collectors.toSet());
        List<BucketGrantMetadata> result = new LinkedList<>();
        for (String bucketName : bucketNames) {
            List<BucketGrantMetadata> bucketGrants = userBucketGrants.stream()
                                                                     .filter(grant -> grant.getBucketName()
                                                                                           .equals(bucketName))
                                                                     .collect(Collectors.toList());
            getHighestPriorityGrantForABucket(bucketGrants).ifPresent(result::add);
        }
        return result;
    }

    //TODO
    private Optional<BucketGrantMetadata> getHighestPriorityGrantForABucket(List<BucketGrantMetadata> bucketGrants) {
        Optional<BucketGrantMetadata> optUserBucketGrant = this.filterFirstUserSpecificBucketGrant(bucketGrants);
        if (optUserBucketGrant.isPresent()) {
            return optUserBucketGrant;
        } else {
            // Add Highest Ranked buckets Grants
            return this.filterGroupBucketGrantWithHighestPriority(bucketGrants);
        }
    }

    /**
     * Get the first user-specific (i.e., its grantee type is "user") bucket grant from a list of bucket grants.
     *
     * Note that it's supposed to be used for a list of grants targeting a specific bucket assigned to a specific user.
     * In this case, there is at most one grant with the grantee type "user".
     *
     * @param bucketGrants list of bucket grants assigned to a user
     * @return the user grant if it exists
     */
    private Optional<BucketGrantMetadata> filterFirstUserSpecificBucketGrant(List<BucketGrantMetadata> bucketGrants) {
        return bucketGrants.stream().filter(grant -> grant.getGranteeType().equals("user")).findFirst();
    }

    /**
     *
     * @param bucketGrants list of bucket grants assigned to a user
     * @return the user group grant with the highest priority if it exists
     */
    private Optional<BucketGrantMetadata>
            filterGroupBucketGrantWithHighestPriority(List<BucketGrantMetadata> bucketGrants) {
        OptionalInt highestRank = this.getHighestRankFromBucketGrants(bucketGrants);
        Optional<BucketGrantMetadata> result = Optional.empty();
        if (highestRank.isPresent()) {
            List<BucketGrantMetadata> highestGrants = bucketGrants.stream()
                                                                  .filter(grant -> grant.getGranteeType()
                                                                                        .equals("group") &&
                                                                                   grant.getPriority() == highestRank.getAsInt())
                                                                  .collect(Collectors.toList());
            if (highestGrants.size() == 1) {
                result = Optional.of(highestGrants.get(0));
            } else if (highestGrants.size() > 1) {
                result = Optional.of(this.getHighestGrantFromSamePriorityLevelGrants(bucketGrants));
            }
        }
        return result;
    }

    /**
     *
     * @param bucketGrants list of bucket grants assigned to a user
     * @return the priority value of the highest grant assigned to the user groups
     */
    private OptionalInt getHighestRankFromBucketGrants(List<BucketGrantMetadata> bucketGrants) {
        return bucketGrants.stream()
                           .filter(grant -> grant.getGranteeType().equals("group"))
                           .map(BucketGrantMetadata::getPriority)
                           .mapToInt(v -> v)
                           .max();
    }

    /**
     *
     * @param bucketGrants list of bucket grants assigned to the user groups that have the same priority level
     * @return the grant that has the highest access right value
     */
    private BucketGrantMetadata getHighestGrantFromSamePriorityLevelGrants(List<BucketGrantMetadata> bucketGrants) {
        int highestAccessTypeIndex = 0;
        for (int index = 1; index < bucketGrants.size(); index++) {
            if (grantAccessTypeHelperService.getPriorityLevel(bucketGrants.get(highestAccessTypeIndex).getAccessType(),
                                                              bucketGrants.get(index).getAccessType()) == 2) {
                highestAccessTypeIndex = index;
            }
        }
        return bucketGrants.get(highestAccessTypeIndex);
    }

    /**
     * Remove all inaccessible objects in a bucket for the user
     *
     * Note that the calculation of the accessibility is following the rule: userObject grant > userBucket grant > groupObject grant > groupBucket grant
     *
     * TODO update params
     * @param metadataList list of catalog objects in a bucket
     */
    public void removeAllObjectsWithNoAccessGrant(List<CatalogObjectMetadata> metadataList,
            List<BucketGrantMetadata> bucketGrants, List<CatalogObjectGrantMetadata> objectsGrants) {
        List<CatalogObjectGrantMetadata> objectsPositiveGrants = objectsGrants.stream()
                                                                              .filter(g -> !g.getAccessType()
                                                                                             .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        List<CatalogObjectGrantMetadata> objectsNoAccessGrants = objectsGrants.stream()
                                                                              .filter(g -> g.getAccessType()
                                                                                            .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        List<BucketGrantMetadata> bucketPositiveGrants = bucketGrants.stream()
                                                                     .filter(g -> !g.getAccessType()
                                                                                    .equals(noAccess.name()))
                                                                     .collect(Collectors.toList());
        List<BucketGrantMetadata> bucketNoAccessGrants = bucketGrants.stream()
                                                                     .filter(g -> g.getAccessType()
                                                                                   .equals(noAccess.name()))
                                                                     .collect(Collectors.toList());

        // For the catalog objects have a user-specific positive grant, it will be kept (i.e., it's accessible regardless the access type of his groups).
        // Because the user-specific objects' grants has the highest priority, and for each catalog object, the user has only one user-specific grant.
        Set<String> objectsToKeep = objectsPositiveGrants.stream()
                                                         .filter(g -> g.getGranteeType().equals("user"))
                                                         .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                         .collect(Collectors.toSet());

        if (objectsNoAccessGrants.isEmpty()) {
            if (!bucketNoAccessGrants.isEmpty()) {
                // Case triggered when there is a noAccess user grant on the bucket
                // In the following we are checking if the user has a positive grant for one of its objects
                metadataList.removeIf(object -> !objectsToKeep.contains(object.getName()));
            }
            return;
        }

        // For the catalog objects with a user-specific no-access grant, it will be removed.
        Set<String> objectsToRemove = objectsNoAccessGrants.stream()
                                                           .filter(g -> g.getGranteeType().equals("user"))
                                                           .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                           .collect(Collectors.toSet());

        // Check and get the user grant on the bucket (higher priority than groupNoAccessGrants)
        boolean isBucketAccessibleByUserSpecificGrant = filterFirstUserSpecificBucketGrant(bucketPositiveGrants).isPresent();

        // group no access grants compare priority
        if (isBucketAccessibleByUserSpecificGrant) {
            metadataList.removeIf(object -> objectsToRemove.contains(object.getName()) &&
                                            !objectsToKeep.contains(object.getName()));
            return;
        }

        // Handle group no-access grant for catalog objects
        for (CatalogObjectGrantMetadata userGrantWithNoAccessRight : objectsNoAccessGrants) {
            if (userGrantWithNoAccessRight.getGranteeType().equals("user")) {
                // user-specific grants already handled
                continue;
            }

            String catalogObjectName = userGrantWithNoAccessRight.getCatalogObjectName();

            // Get the list of positive catalog objects grants assigned to the user
            List<CatalogObjectGrantMetadata> userGroupsCatalogObjectPositiveGrants = objectsPositiveGrants.stream()
                                                                                                          .filter(grant -> grant.getCatalogObjectName()
                                                                                                                                .equals(catalogObjectName))
                                                                                                          .collect(Collectors.toList());

            // Check and get the group grant that has the highest priority
            Optional<CatalogObjectGrantMetadata> highestPriorityPositiveGroupsObjectGrant = this.checkAndReturnObjectGrantWithHighestPriorityInGroupGrants(userGroupsCatalogObjectPositiveGrants);

            if (highestPriorityPositiveGroupsObjectGrant.isPresent()) {
                // This case indicates that there is not a direct access grant on the object itself for the user, but he got a positive
                // grant assigned to his group
                // Get the highest priority value from his group grant
                int highestPossitivePriority = highestPriorityPositiveGroupsObjectGrant.get().getPriority();
                // Check if the priority value is lower than the priority value of the no access grant assigned to the same object
                if (userGrantWithNoAccessRight.getPriority() < highestPossitivePriority) {
                    // Add the object to the list of objects to be removed if the priority of the positive grant > negative grant
                    objectsToRemove.add(catalogObjectName);
                }
            } else {
                // The user has no positive group grant with a higher priority than the negative grant
                // Add the object to the list of objects to be removed
                objectsToRemove.add(catalogObjectName);
            }
        }

        metadataList.removeIf(object -> objectsToRemove.contains(object.getName()) &&
                                        !objectsToKeep.contains(object.getName()));
    }

    /**
     *
     * @param user authenticated user
     * @param catalogObject catalog object
     * @return return all grants assigned to a user on the bucket that contains the object
     */
    private Optional<BucketGrantMetadata> getUserCatalogObjectGrant(AuthenticatedUser user,
            CatalogObjectMetadata catalogObject) {
        return bucketGrantService.getUserAccessibleBucketGrants(user, catalogObject.getBucketName())
                                 .stream()
                                 .filter(bucketGrant -> bucketGrant.getGranteeType().equals("user"))
                                 .findFirst();
    }

    /**
     *
     * @param objectGrants list of object grants
     * @return the group grant with the highest priority
     */
    private Optional<CatalogObjectGrantMetadata>
            checkAndReturnObjectGrantWithHighestPriorityInGroupGrants(List<CatalogObjectGrantMetadata> objectGrants) {
        OptionalInt highestRank = this.getHighestRankFromObjectGrants(objectGrants);
        Optional<CatalogObjectGrantMetadata> result = Optional.empty();
        if (highestRank.isPresent()) {
            List<CatalogObjectGrantMetadata> highestGrants = objectGrants.stream()
                                                                         .filter(grant -> grant.getGranteeType()
                                                                                               .equals("group") &&
                                                                                          grant.getPriority() == highestRank.getAsInt())
                                                                         .collect(Collectors.toList());
            if (highestGrants.size() == 1) {
                result = Optional.of(highestGrants.get(0));
            } else if (highestGrants.size() > 1) {
                result = Optional.of(this.getHighestPriorityObjectGrant(objectGrants));
            }
        }
        return result;
    }

    /**
     *
     * @param objectGrants list of object grants
     * @return the highest priority value of the group grants
     */
    private OptionalInt getHighestRankFromObjectGrants(List<CatalogObjectGrantMetadata> objectGrants) {
        return objectGrants.stream()
                           .filter(grant -> grant.getGranteeType().equals("group"))
                           .map(CatalogObjectGrantMetadata::getPriority)
                           .mapToInt(v -> v)
                           .max();
    }

    /**
     *
     * @param objectGrants list of object grants assigen to the user groups with the same priority level
     * @return the object grant with the highest access rights
     */
    private CatalogObjectGrantMetadata getHighestPriorityObjectGrant(List<CatalogObjectGrantMetadata> objectGrants) {
        int highestAccessTypeIndex = 0;
        for (int index = 1; index < objectGrants.size(); index++) {
            if (grantAccessTypeHelperService.getPriorityLevel(objectGrants.get(highestAccessTypeIndex).getAccessType(),
                                                              objectGrants.get(index).getAccessType()) == 2) {
                highestAccessTypeIndex = index;
            }
        }
        return objectGrants.get(highestAccessTypeIndex);
    }
}
