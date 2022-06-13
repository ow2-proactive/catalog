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
import org.ow2.proactive.catalog.util.AccessTypeAndPriority;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
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
    public String getBucketRights(AuthenticatedUser user, String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }

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
                return getAccessTypeFromHighestPriorityUserGroupGrant(getBucketAccessTypePerUserGroup(userBucketGrants));
            }
        }
        return noAccess.toString();
    }

    /**
     * Calculate the user's rights (i.e. access type) of the bucket based on the user's grants for this bucket.
     *
     * @param userBucketGrants the list of all grants assigned to the user targeting the bucket
     * @return bucket rights as string
     */
    public String getBucketRights(List<BucketGrantMetadata> userBucketGrants) {
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
    public String getCatalogObjectRights(AuthenticatedUser user, String bucketName, String catalogObjectName) {
        CatalogObjectRevisionEntity catalogObjectEntity = catalogObjectGrantService.getCatalogObject(bucketName,
                                                                                                     catalogObjectName);
        if (catalogObjectEntity == null) {
            throw new CatalogObjectNotFoundException(bucketName, catalogObjectName);
        }

        List<CatalogObjectGrantMetadata> userCatalogObjectGrants = catalogObjectGrantService.getAllObjectGrantsAssignedToAnObjectInsideABucketForAUser(user,
                                                                                                                                                       bucketName,
                                                                                                                                                       catalogObjectName);
        if (!userCatalogObjectGrants.isEmpty()) {
            Optional<String> userBucketRights = bucketGrantService.getUserAccessibleBucketGrants(user, bucketName)
                                                                  .stream()
                                                                  .filter(grant -> grant.getGranteeType()
                                                                                        .equals("user"))
                                                                  .findFirst()
                                                                  .map(BucketGrantMetadata::getAccessType);
            return getHighestPriorityRightForCatalogObject(userBucketRights, userCatalogObjectGrants);
        }
        // In case when the user and user group object grants are unavailable, we check in the bucket grants for the accessType
        return this.getBucketRights(user, bucketName);
    }

    /**
     * Calculate the rights (i.e., access type) of the catalog object based on 1) whether its bucket is public 2) its bucket rights
     * 3) its bucket rights which is defined by a user-specific grant (Optional) 4) the grants specified for this catalog object
     *
     * @param isPublicBucket whether its bucket is public
     * @param bucketRights the rights of its bucket
     * @param userSpecificBucketRights its bucket rights which is defined by a user-specific grant (Optional)
     * @param userObjectGrants the grants specified for this catalog object
     * @return catalog object rights as string
     */
    public String getCatalogObjectRights(boolean isPublicBucket, String bucketRights,
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

    public int getTheNumberOfAccessibleObjectsInTheBucket(BucketMetadata bucket, List<BucketGrantMetadata> bucketGrants,
            List<CatalogObjectGrantMetadata> objectsGrants) {
        List<CatalogObjectGrantMetadata> objectsPositiveGrants = objectsGrants.stream()
                                                                              .filter(g -> !g.getAccessType()
                                                                                             .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        List<CatalogObjectGrantMetadata> objectsNoAccessGrants = objectsGrants.stream()
                                                                              .filter(g -> g.getAccessType()
                                                                                            .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        String bucketRights = getBucketRights(bucketGrants);
        Optional<BucketGrantMetadata> bucketUserSpecificGrant = bucketGrants.stream()
                                                                            .filter(g -> g.getGranteeType()
                                                                                          .equals("user"))
                                                                            .findAny();

        if (bucketRights.equals(noAccess.name())) {
            // When the user has no access on the bucket, we count only objects that the user have a positive grant over them

            // The catalog objects have a user-specific positive grant (the user-specific objects' grants always has the highest priority)
            // Remainder: for each catalog object, the user has only one user-specific grant.
            Set<String> accessibleObjects = objectsPositiveGrants.stream()
                                                                 .filter(g -> g.getGranteeType().equals("user"))
                                                                 .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                                 .collect(Collectors.toSet());

            if (!bucketUserSpecificGrant.isPresent()) {
                // When the bucket has the no-access right not through user-specific grant (i.e., through its groups grants), we need to
                // calculate grant priority for getting the accessible catalog objects.
                Set<String> objectsWithPositiveGrants = new HashSet<>();
                for (CatalogObjectGrantMetadata grant : objectsPositiveGrants) {
                    objectsWithPositiveGrants.add(grant.getCatalogObjectName());
                }
                for (String catalogObjectName : objectsWithPositiveGrants) {
                    // Get the list of grants assigned to the user groups for this catalog object
                    List<CatalogObjectGrantMetadata> objectGrants = objectsGrants.stream()
                                                                                 .filter(g -> g.getCatalogObjectName()
                                                                                               .equals(catalogObjectName))
                                                                                 .collect(Collectors.toList());
                    // Check and get the group grant that has the highest priority to decide whether the object accessible
                    Optional<CatalogObjectGrantMetadata> highestPriorityPositiveGroupsObjectGrant = this.getHighestPriorityObjectGroupGrants(objectGrants);
                    if (highestPriorityPositiveGroupsObjectGrant.isPresent() &&
                        !noAccess.name().equals(highestPriorityPositiveGroupsObjectGrant.get().getAccessType())) {
                        accessibleObjects.add(catalogObjectName);
                    }
                }
            }
            // When the bucket has a user-specific no-access grant, group grants are ignored, only the user-specific catalog object grants are taken into account.
            return accessibleObjects.size();
        } else {
            // When the user has access on the bucket, we count the objects that the user has a negative grant over it, then reduce it from the bucket objects count

            // user specific catalog object grants have the highest priority. The objects having a user-speicific no-access grant are always not accessible.
            Set<String> inaccessibleObjects = objectsNoAccessGrants.stream()
                                                                   .filter(g -> g.getGranteeType().equals("user"))
                                                                   .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                                   .collect(Collectors.toSet());

            if (!bucketUserSpecificGrant.isPresent()) {
                // When the bucket has the positive right not through user-specific grant (i.e., through its groups grants), we need to
                // calculate grant priority for getting the inaccessible catalog objects.
                // Therefore, for each object which has no-access grants, we calculate the highest priority grant for this object. If the highest priority grant is the type of no-access, it's added to the inaccessibleObjects.
                Set<String> objectsWithNoAccessGrants = new HashSet<>();
                for (CatalogObjectGrantMetadata grant : objectsNoAccessGrants) {
                    objectsWithNoAccessGrants.add(grant.getCatalogObjectName());
                }
                for (String catalogObjectName : objectsWithNoAccessGrants) {
                    // Get the list of grants assigned to this catalog object
                    List<CatalogObjectGrantMetadata> objectGrants = objectsGrants.stream()
                                                                                 .filter(g -> g.getCatalogObjectName()
                                                                                               .equals(catalogObjectName))
                                                                                 .collect(Collectors.toList());
                    // Check and get the group grant that has the highest priority to decide whether the object accessible
                    Optional<CatalogObjectGrantMetadata> highestPriorityPositiveGroupsObjectGrant = this.getHighestPriorityObjectGroupGrants(objectGrants);
                    if (highestPriorityPositiveGroupsObjectGrant.isPresent() &&
                        noAccess.name().equals(highestPriorityPositiveGroupsObjectGrant.get().getAccessType())) {
                        inaccessibleObjects.add(catalogObjectName);
                    }
                }
            }
            // // When the bucket has a user-specific positive grant, group grants are ignored, only the user-specific no-access catalog object grants are taken into account.
            return bucket.getObjectCount() - inaccessibleObjects.size();
        }
    }

    /**
     * calculate the user's highest priority right defined for the catalog object
     * @param userSpecificBucketRights the user's right on the bucket which is defined through a user-specific bucket grant which is optional
     * @param userCatalogObjectGrants all the grants specified for this catalog object for this user or its groups.
     * @return the user's right for the catalog object calculated through its highest priority grant
     */
    private String getHighestPriorityRightForCatalogObject(Optional<String> userSpecificBucketRights,
            List<CatalogObjectGrantMetadata> userCatalogObjectGrants) {
        // Check for a user grant
        Optional<String> userSpecificObjectRight = userCatalogObjectGrants.stream()
                                                                          .filter(grant -> grant.getGranteeType()
                                                                                                .equals("user"))
                                                                          .map(CatalogObjectGrantMetadata::getAccessType)
                                                                          .findFirst();
        if (userSpecificObjectRight.isPresent()) {
            // Return the user grant accessType (A user has only one user grant)
            return userSpecificObjectRight.get();
        } else if (userSpecificBucketRights.isPresent()) {
            return userSpecificBucketRights.get();
        } else {
            // Check the user group grants
            return this.getAccessTypeFromHighestPriorityUserGroupGrant(getObjectAccessTypePerUserGroup(userCatalogObjectGrants));
        }
    }

    /**
     * Get the user group bucket grants and return the access right and priority per group
     *
     * @param userBucketGrants bucket grants assigned to a user
     * @return a hashmap that map each group to its access right and its priority
     */
    private Map<String, AccessTypeAndPriority>
            getBucketAccessTypePerUserGroup(List<BucketGrantMetadata> userBucketGrants) {
        Map<String, AccessTypeAndPriority> accessTypePerUserGroup = new HashMap<>();
        List<BucketGrantMetadata> userGroupBucketGrants = userBucketGrants.stream()
                                                                          .filter(grant -> grant.getGranteeType()
                                                                                                .equals("group"))
                                                                          .sorted(Comparator.comparingInt(BucketGrantMetadata::getPriority))
                                                                          .collect(Collectors.toList());
        for (BucketGrantMetadata grant : userGroupBucketGrants) {
            accessTypePerUserGroup.put(grant.getGrantee(),
                                       new AccessTypeAndPriority(grant.getAccessType(), grant.getPriority()));
        }
        return accessTypePerUserGroup;
    }

    /**
     * Check the user group catalog object grants and return the access right and priority level per group
     *
     * @param allUserObjectGrants catalog object grants assigned to a user
     * @return a hashmap that map each group to its access right and its priority
     */
    private Map<String, AccessTypeAndPriority>
            getObjectAccessTypePerUserGroup(List<CatalogObjectGrantMetadata> allUserObjectGrants) {
        Map<String, AccessTypeAndPriority> accessTypesPerUserGroup = new HashMap<>();
        List<CatalogObjectGrantMetadata> userGroupCatalogObjectGrants = allUserObjectGrants.stream()
                                                                                           .filter(grant -> grant.getGranteeType()
                                                                                                                 .equals("group"))
                                                                                           .collect(Collectors.toList());
        if (!userGroupCatalogObjectGrants.isEmpty()) {
            for (CatalogObjectGrantMetadata grant : userGroupCatalogObjectGrants) {
                AccessTypeAndPriority accessTypeAndPriorityForGroupGrant = new AccessTypeAndPriority(grant.getAccessType(),
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
    private String
            getAccessTypeFromHighestPriorityUserGroupGrant(Map<String, AccessTypeAndPriority> accessTypesPerUserGroup) {
        int highestPriority = 0;
        String highestAccessType = "";
        String highestGrantByPriorityKey = "";
        if (!accessTypesPerUserGroup.isEmpty()) {
            for (String groupKey : accessTypesPerUserGroup.keySet()) {
                AccessTypeAndPriority groupAccessTypeAndPriority = accessTypesPerUserGroup.get(groupKey);
                if (groupAccessTypeAndPriority.getPriority() > highestPriority) {
                    highestPriority = groupAccessTypeAndPriority.getPriority();
                    highestAccessType = groupAccessTypeAndPriority.getAccessType();
                    highestGrantByPriorityKey = groupKey;
                } else if (groupAccessTypeAndPriority.getPriority() == highestPriority) {
                    if (AccessTypeHelper.compare(groupAccessTypeAndPriority.getAccessType(), highestAccessType) > 0) {
                        highestPriority = groupAccessTypeAndPriority.getPriority();
                        highestAccessType = groupAccessTypeAndPriority.getAccessType();
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
    public List<BucketMetadata> getBucketsByPrioritiedGrants(AuthenticatedUser user) {
        List<BucketGrantMetadata> bucketGrants = bucketGrantService.getUserAccessibleBucketsGrants(user);
        List<CatalogObjectGrantMetadata> catalogObjectGrants = catalogObjectGrantService.getAccessibleObjectsGrantsAssignedToAUser(user);

        List<BucketGrantMetadata> noAccessGrantBuckets = bucketGrantService.getAllNoAccessGrants();
        for (BucketGrantMetadata nonAccessibleBucketGrant : noAccessGrantBuckets) {
            if (nonAccessibleBucketGrant.getGranteeType().equals("user") &&
                nonAccessibleBucketGrant.getGrantee().equals(user.getName())) {
                bucketGrants.removeIf(grant -> grant.getBucketName().equals(nonAccessibleBucketGrant.getBucketName()));
                // userBucket grant > groupObject grant
                catalogObjectGrants.removeIf(grant -> grant.getGranteeType().equals("group") &&
                                                      grant.getBucketName()
                                                           .equals(nonAccessibleBucketGrant.getBucketName()));
            } else if (nonAccessibleBucketGrant.getGranteeType().equals("group") &&
                       user.getGroups().contains(nonAccessibleBucketGrant.getGrantee())) {
                bucketGrants.removeIf(grant -> grant.getGranteeType().equals("group") &&
                                               grant.getPriority() < nonAccessibleBucketGrant.getPriority() &&
                                               grant.getBucketName().equals(nonAccessibleBucketGrant.getBucketName()));
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
        String highestAccess = bucketGrants.get(0).getAccessType();
        for (int index = 1; index < bucketGrants.size(); index++) {
            // when find a grant with higher access type
            if (AccessTypeHelper.compare(bucketGrants.get(index).getAccessType(), highestAccess) > 0) {
                highestAccess = bucketGrants.get(index).getAccessType();
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
     * @param metadataList list of catalog objects in a bucket
     * @param bucketGrants list of user's bucket grants
     * @param objectsGrants list of user's catalog object grants for catalog objects in the bucket
     */
    public void removeAllObjectsWithoutAccessRights(List<CatalogObjectMetadata> metadataList,
            List<BucketGrantMetadata> bucketGrants, List<CatalogObjectGrantMetadata> objectsGrants) {
        List<CatalogObjectGrantMetadata> objectsPositiveGrants = objectsGrants.stream()
                                                                              .filter(g -> !g.getAccessType()
                                                                                             .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        List<CatalogObjectGrantMetadata> objectsNoAccessGrants = objectsGrants.stream()
                                                                              .filter(g -> g.getAccessType()
                                                                                            .equals(noAccess.name()))
                                                                              .collect(Collectors.toList());
        String bucketRights = getBucketRights(bucketGrants);
        Optional<BucketGrantMetadata> bucketUserSpecificGrant = bucketGrants.stream()
                                                                            .filter(g -> g.getGranteeType()
                                                                                          .equals("user"))
                                                                            .findAny();

        if (bucketRights.equals(noAccess.name())) {
            // When the user has no access on the bucket, we count only objects that the user have a positive grant over them

            // The catalog objects have a user-specific positive grant (the user-specific objects' grants always has the highest priority)
            // Remainder: for each catalog object, the user has only one user-specific grant.
            Set<String> accessibleObjects = objectsPositiveGrants.stream()
                                                                 .filter(g -> g.getGranteeType().equals("user"))
                                                                 .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                                 .collect(Collectors.toSet());

            if (!bucketUserSpecificGrant.isPresent()) {
                // When the bucket has the no-access right not through user-specific grant (i.e., through its groups grants), we need to
                // calculate grant priority for getting the accessible catalog objects.
                Set<String> objectsWithPositiveGrants = new HashSet<>();
                for (CatalogObjectGrantMetadata grant : objectsPositiveGrants) {
                    objectsWithPositiveGrants.add(grant.getCatalogObjectName());
                }
                for (String catalogObjectName : objectsWithPositiveGrants) {
                    // Get the list of grants assigned to the user groups for this catalog object
                    List<CatalogObjectGrantMetadata> objectGrants = objectsGrants.stream()
                                                                                 .filter(g -> g.getCatalogObjectName()
                                                                                               .equals(catalogObjectName))
                                                                                 .collect(Collectors.toList());
                    // Check and get the group grant that has the highest priority to decide whether the object accessible
                    Optional<CatalogObjectGrantMetadata> highestPriorityPositiveGroupsObjectGrant = this.getHighestPriorityObjectGroupGrants(objectGrants);
                    if (highestPriorityPositiveGroupsObjectGrant.isPresent() &&
                        !noAccess.name().equals(highestPriorityPositiveGroupsObjectGrant.get().getAccessType())) {
                        accessibleObjects.add(catalogObjectName);
                    }
                }
            }
            // When the bucket has a user-specific no-access grant, group grants are ignored, only the user-specific catalog object grants are taken into account.
            metadataList.removeIf(object -> !accessibleObjects.contains(object.getName()));
        } else {
            // When the user has access on the bucket, we count the objects that the user has a negative grant over it, then reduce it from the bucket objects count

            // user specific catalog object grants have the highest priority. The objects having a user-speicific no-access grant are always not accessible.
            Set<String> inaccessibleObjects = objectsNoAccessGrants.stream()
                                                                   .filter(g -> g.getGranteeType().equals("user"))
                                                                   .map(CatalogObjectGrantMetadata::getCatalogObjectName)
                                                                   .collect(Collectors.toSet());

            if (!bucketUserSpecificGrant.isPresent()) {
                // When the bucket has the positive right not through user-specific grant (i.e., through its groups grants), we need to
                // calculate grant priority for getting the inaccessible catalog objects.
                // Therefore, for each object which has no-access grants, we calculate the highest priority grant for this object. If the highest priority grant is the type of no-access, it's added to the inaccessibleObjects.
                Set<String> objectsWithNoAccessGrants = new HashSet<>();
                for (CatalogObjectGrantMetadata grant : objectsNoAccessGrants) {
                    objectsWithNoAccessGrants.add(grant.getCatalogObjectName());
                }
                for (String catalogObjectName : objectsWithNoAccessGrants) {
                    // Get the list of grants assigned to this catalog object
                    List<CatalogObjectGrantMetadata> objectGrants = objectsGrants.stream()
                                                                                 .filter(g -> g.getCatalogObjectName()
                                                                                               .equals(catalogObjectName))
                                                                                 .collect(Collectors.toList());
                    // Check and get the group grant that has the highest priority to decide whether the object accessible
                    Optional<CatalogObjectGrantMetadata> highestPriorityPositiveGroupsObjectGrant = this.getHighestPriorityObjectGroupGrants(objectGrants);
                    if (highestPriorityPositiveGroupsObjectGrant.isPresent() &&
                        noAccess.name().equals(highestPriorityPositiveGroupsObjectGrant.get().getAccessType())) {
                        inaccessibleObjects.add(catalogObjectName);
                    }
                }
            }
            // // When the bucket has a user-specific positive grant, group grants are ignored, only the user-specific no-access catalog object grants are taken into account.
            metadataList.removeIf(object -> inaccessibleObjects.contains(object.getName()));
        }
    }

    /**
     *
     * @param objectGrants list of object grants
     * @return the group grant with the highest priority
     */
    private Optional<CatalogObjectGrantMetadata>
            getHighestPriorityObjectGroupGrants(List<CatalogObjectGrantMetadata> objectGrants) {
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
                result = Optional.of(this.getHighestAccesTypeObjectGrant(objectGrants));
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
    private CatalogObjectGrantMetadata getHighestAccesTypeObjectGrant(List<CatalogObjectGrantMetadata> objectGrants) {
        int highestAccessTypeIndex = 0;
        for (int index = 1; index < objectGrants.size(); index++) {
            if (AccessTypeHelper.compare(objectGrants.get(highestAccessTypeIndex).getAccessType(),
                                         objectGrants.get(index).getAccessType()) < 0) {
                highestAccessTypeIndex = index;
            }
        }
        return objectGrants.get(highestAccessTypeIndex);
    }
}
