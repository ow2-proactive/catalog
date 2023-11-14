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
import static org.ow2.proactive.catalog.util.AccessType.read;

import java.util.*;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.ow2.proactive.catalog.dto.GrantMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.CatalogObjectNotFoundException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.catalog.util.AccessTypeHelper;
import org.ow2.proactive.catalog.util.GrantHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Service
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
    @Transactional(readOnly = true)
    public String getBucketRights(AuthenticatedUser user, String bucketName) {
        BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }
        if (GrantHelper.isPublicBucket(bucket.getOwner())) {
            return admin.name();
        }

        List<BucketGrantMetadata> userBucketGrants = bucketGrantService.getUserBucketGrants(user, bucketName);
        addGrantsForBucketOwner(user, bucketName, bucket.getOwner(), userBucketGrants);

        return getBucketRights(userBucketGrants);
    }

    /**
     * Calculate the user's rights (i.e. access type) of the bucket based on the user's grants for this bucket.
     *
     * @param userBucketGrants the list of all grants assigned to the user targeting the bucket
     * @return bucket rights as string
     */
    public static String getBucketRights(List<BucketGrantMetadata> userBucketGrants) {
        // In case when the user and user group object grants are unavailable, we check in the bucket grants for the accessType
        if (userBucketGrants.isEmpty()) {
            return noAccess.toString();
        }
        Optional<BucketGrantMetadata> highestPriorityGrants = getHighestPriorityBucketGrant(userBucketGrants);
        return GrantHelper.getAccessType(highestPriorityGrants);
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
    @Transactional(readOnly = true)
    public String getCatalogObjectRights(AuthenticatedUser user, String bucketName, String catalogObjectName) {
        CatalogObjectRevisionEntity object = catalogObjectGrantService.getCatalogObject(bucketName, catalogObjectName);
        if (object == null) {
            throw new CatalogObjectNotFoundException(bucketName, catalogObjectName);
        }

        List<CatalogObjectGrantMetadata> objGrants = catalogObjectGrantService.getObjectGrants(user,
                                                                                               bucketName,
                                                                                               catalogObjectName);
        if (!objGrants.isEmpty()) {
            Optional<String> userSpecBucketRights = bucketGrantService.getUserSpecificPostiveGrants(user, bucketName)
                                                                      .map(BucketGrantMetadata::getAccessType);
            return getCatalogObjectRightsFromHighestPriorityGrant(userSpecBucketRights, objGrants);
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
    public static String getCatalogObjectRights(boolean isPublicBucket, String bucketRights,
            Optional<String> userSpecificBucketRights, List<CatalogObjectGrantMetadata> userObjectGrants) {
        if (isPublicBucket) {
            return admin.name();
        }
        if (!userObjectGrants.isEmpty()) {
            return getCatalogObjectRightsFromHighestPriorityGrant(userSpecificBucketRights, userObjectGrants);
        } else {
            return bucketRights;
        }
    }

    /**
     * Get list of buckets which is accessible for the user via the grants
     *
     * @param user authenticated user
     * @return the list of buckets that are accessible for the user via his grants
     */
    @Transactional(readOnly = true)
    public List<BucketMetadata> getBucketsByPrioritiedGrants(AuthenticatedUser user) {
        List<BucketGrantMetadata> bucketsGrants = bucketGrantService.getUserAccessibleBucketsGrants(user);
        List<CatalogObjectGrantMetadata> catalogObjectGrants = catalogObjectGrantService.getAccessibleObjectsGrants(user);

        List<BucketGrantMetadata> noAccessGrantBuckets = bucketGrantService.getNoAccessBucketsGrants(user);
        for (BucketGrantMetadata nonAccessibleBucketGrant : noAccessGrantBuckets) {
            if (GrantHelper.isUserSpecificGrant(nonAccessibleBucketGrant) &&
                nonAccessibleBucketGrant.getGrantee().equals(user.getName())) {
                bucketsGrants.removeIf(grant -> grant.getBucketName().equals(nonAccessibleBucketGrant.getBucketName()));
                // priority rule: userBucket grant > groupObject grant
                catalogObjectGrants.removeIf(grant -> GrantHelper.isGroupGrant(grant) &&
                                                      GrantHelper.hasSameBucketName(grant, nonAccessibleBucketGrant));
            } else if (GrantHelper.isGroupGrant(nonAccessibleBucketGrant) &&
                       user.getGroups().contains(nonAccessibleBucketGrant.getGrantee())) {
                bucketsGrants.removeIf(grant -> GrantHelper.isGroupGrant(grant) &&
                                                grant.getPriority() < nonAccessibleBucketGrant.getPriority() &&
                                                GrantHelper.hasSameBucketName(grant, nonAccessibleBucketGrant));
            }
        }

        Set<String> bucketNames = GrantHelper.collectBucketNames(bucketsGrants);
        bucketNames.addAll(GrantHelper.collectBucketNames(catalogObjectGrants));

        List<BucketMetadata> bucketMetadataList = new LinkedList<>();
        for (String bucketName : bucketNames) {
            BucketEntity bucket = bucketRepository.findOneByBucketName(bucketName);
            if (bucket == null) {
                throw new BucketNotFoundException(bucketName);
            }
            BucketMetadata bucketMetadata = new BucketMetadata(bucket, bucket.getCatalogObjects().size());
            bucketMetadataList.add(bucketMetadata);
        }
        return bucketMetadataList;
    }

    /**
     * Get the number of accessible catalog objects in the bucket.
     *
     * @param bucket bucket metadata
     * @param bucketGrants list of all the user's grants on the bucket
     * @param objectsGrants list of all the user's grants on the catalog objects in the bucket
     * @return the number of accessible catalog objects in the bucket
     */
    public static int getNumberOfAccessibleObjectsInBucket(BucketMetadata bucket,
            List<BucketGrantMetadata> bucketGrants, List<CatalogObjectGrantMetadata> objectsGrants) {
        String bucketRights = getBucketRights(bucketGrants);
        Optional<BucketGrantMetadata> bucketUserSpecificGrant = GrantHelper.filterFirstUserSpecificGrant(bucketGrants);
        if (bucketRights.equals(noAccess.name())) {
            // When the user has no access on the bucket, we count only objects that the user have a positive grant over them
            return getAccessibleObjects(bucketUserSpecificGrant, objectsGrants).size();
        } else {
            // When the user has access on the bucket, we count the objects that the user has a negative grant over it, then reduce it from the bucket objects count
            return bucket.getObjectCount() - getInaccessibleObjects(bucketUserSpecificGrant, objectsGrants).size();
        }
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
    public static void removeInaccessibleObjectsInBucket(List<CatalogObjectMetadata> metadataList,
            List<BucketGrantMetadata> bucketGrants, List<CatalogObjectGrantMetadata> objectsGrants) {
        String bucketRights = getBucketRights(bucketGrants);
        Optional<BucketGrantMetadata> bucketUserSpecificGrant = GrantHelper.filterFirstUserSpecificGrant(bucketGrants);

        if (bucketRights.equals(noAccess.name())) {
            // When the user has no access on the bucket, we count only objects that the user have a positive grant over them
            Set<String> accessibleObjects = getAccessibleObjects(bucketUserSpecificGrant, objectsGrants);
            metadataList.removeIf(object -> !accessibleObjects.contains(object.getName()));
        } else {
            // When the user has access on the bucket, we count the objects that the user has a negative grant over it, then reduce it from the bucket objects count
            Set<String> inaccessibleObjects = getInaccessibleObjects(bucketUserSpecificGrant, objectsGrants);
            metadataList.removeIf(object -> inaccessibleObjects.contains(object.getName()));
        }
    }

    /**
     * When the user belongs to the bucket owner group, add the default admin grant.
     *
     * @param user authenticate user
     * @param bucketName name of bucket
     * @param bucketOwner owner of bucket
     * @param userBucketGrants user's grants for this bucket
     */
    public static void addGrantsForBucketOwner(AuthenticatedUser user, String bucketName, String bucketOwner,
            List<BucketGrantMetadata> userBucketGrants) {
        if (user.getGroups().contains(GrantHelper.extractBucketOwnerGroup(bucketOwner))) {
            userBucketGrants.add(GrantHelper.ownerGroupGrant(bucketOwner, bucketName));
        }
    }

    /**
     * Check whether a bucket is accessible, either through the bucket rights, or through accessible catalog objects in the bucket.
     *
     * @param user authenticated user
     * @param bucket bucket metadata
     * @return true if the bucket is accessible
     */
    @Transactional(readOnly = true)
    public boolean isBucketAccessible(AuthenticatedUser user, BucketMetadata bucket) {
        boolean isPublicBucket = GrantHelper.isPublicBucket(bucket.getOwner());

        if (isPublicBucket) {
            return true;
        } else {
            List<BucketGrantMetadata> bucketGrants = bucketGrantService.getUserBucketGrants(user, bucket.getName());
            addGrantsForBucketOwner(user, bucket.getName(), bucket.getOwner(), bucketGrants);
            List<CatalogObjectGrantMetadata> catalogObjectsGrants = catalogObjectGrantService.getObjectsGrantsInABucket(user,
                                                                                                                        bucket.getName());

            String bucketRights = getBucketRights(bucketGrants);
            return isBucketAccessible(bucketRights, bucketGrants, catalogObjectsGrants);
        }
    }

    /**
     * Check whether a bucket is accessible, either through the bucket rights, or through accessible catalog objects in the bucket.
     *
     * @param bucketRights the user's rights on the bucket
     * @param bucketGrants list of bucket grants
     * @param catalogObjectsGrants list of catalog objects grants in the bucket
     * @return true if the bucket is accessible
     */
    public static boolean isBucketAccessible(String bucketRights, List<BucketGrantMetadata> bucketGrants,
            List<CatalogObjectGrantMetadata> catalogObjectsGrants) {
        List<CatalogObjectGrantMetadata> positiveObjectsGrants = GrantHelper.filterPositiveGrants(catalogObjectsGrants);
        if (AccessTypeHelper.satisfy(bucketRights, read)) {
            return true;
        }
        // Check whether the user has the access to any object in the bucket (based on the rule: userObject grant > userBucket grant > groupObject grant > groupBucket grant).
        if (bucketGrants.stream().anyMatch(GrantHelper::isUserSpecificGrant)) {
            // When the bucket has a user-specific grant, only user-specific catalog object grants are taken into consideration.
            return positiveObjectsGrants.stream().anyMatch(GrantHelper::isUserSpecificGrant);
        } else {
            // Otherwise, all the catalog objects grants are taken into consideration.
            return !positiveObjectsGrants.isEmpty();
        }
    }

    private static Set<String> getAccessibleObjects(Optional<BucketGrantMetadata> bucketUserSpecificGrant,
            List<CatalogObjectGrantMetadata> objectsGrants) {
        List<CatalogObjectGrantMetadata> objectsPositiveGrants = GrantHelper.filterPositiveGrants(objectsGrants);
        // The catalog objects have a user-specific positive grant (the user-specific objects' grants always has the highest priority)
        // Remainder: for each catalog object, the user has only one user-specific grant.
        Set<String> accessibleObjects = GrantHelper.collectObjectNames(GrantHelper.filterUserSpecificGrants(objectsPositiveGrants));
        // When the bucket has a user-specific no-access grant, group grants are ignored, only the user-specific catalog object grants are taken into account.
        if (bucketUserSpecificGrant.isPresent()) {
            return accessibleObjects;
        }

        // When the bucket has the no-access right not through user-specific grant (i.e., through its groups grants), we need to
        // calculate grant priority for getting the accessible catalog objects.
        Set<String> objectsWithPositiveGrants = GrantHelper.collectObjectNames(objectsPositiveGrants);
        for (String objName : objectsWithPositiveGrants) {
            // Get the list of grants assigned to the user groups for this catalog object
            List<CatalogObjectGrantMetadata> objectGrants = GrantHelper.filterObjectGrants(objectsGrants, objName);
            // Check and get the group grant that has the highest priority to decide whether the object accessible
            Optional<CatalogObjectGrantMetadata> highestPriorityGroupGrant = getHighestPriorityGroupGrants(objectGrants);
            if (highestPriorityGroupGrant.isPresent() && GrantHelper.isPositiveGrant(highestPriorityGroupGrant.get())) {
                accessibleObjects.add(objName);
            }
        }
        return accessibleObjects;
    }

    private static Set<String> getInaccessibleObjects(Optional<BucketGrantMetadata> bucketUserSpecificGrant,
            List<CatalogObjectGrantMetadata> objectsGrants) {
        List<CatalogObjectGrantMetadata> objectsNoAccessGrants = GrantHelper.filterNoAccessGrants(objectsGrants);
        // user specific catalog object grants have the highest priority. The objects having a user-speicific no-access grant are always not accessible.
        Set<String> inaccessibleObjects = GrantHelper.collectObjectNames(GrantHelper.filterUserSpecificGrants(objectsNoAccessGrants));
        // When the bucket has a user-specific positive grant, group grants are ignored, only the user-specific no-access catalog object grants are taken into account.
        if (bucketUserSpecificGrant.isPresent()) {
            return inaccessibleObjects;
        }

        // When the bucket has the positive right not through user-specific grant (i.e., through its groups grants), we need to
        // calculate grant priority for getting the inaccessible catalog objects.
        // Therefore, for each object which has no-access grants, we calculate the highest priority grant for this object. If the highest priority grant is the type of no-access, it's added to the inaccessibleObjects.
        Set<String> objectsWithNoAccessGrants = GrantHelper.collectObjectNames(objectsNoAccessGrants);
        for (String objName : objectsWithNoAccessGrants) {
            // Get the list of grants assigned to this catalog object
            List<CatalogObjectGrantMetadata> objectGrants = GrantHelper.filterObjectGrants(objectsGrants, objName);
            // Check and get the group grant that has the highest priority to decide whether the object accessible
            Optional<CatalogObjectGrantMetadata> highestPriorityGroupGrant = getHighestPriorityGroupGrants(objectGrants);
            if (highestPriorityGroupGrant.isPresent() && GrantHelper.isNoAccessGrant(highestPriorityGroupGrant.get())) {
                inaccessibleObjects.add(objName);
            }
        }
        return inaccessibleObjects;
    }

    /**
     * calculate the user's highest priority right defined for the catalog object
     *
     * @param userSpecificBucketRights the user's right on the bucket which is defined through a user-specific bucket grant which is optional
     * @param catalogObjectGrants all the grants specified for this catalog object for this user or its groups.
     * @return the user's right for the catalog object calculated through its highest priority grant
     */
    private static String getCatalogObjectRightsFromHighestPriorityGrant(Optional<String> userSpecificBucketRights,
            List<CatalogObjectGrantMetadata> catalogObjectGrants) {
        // Check for a user grant
        Optional<String> userSpecificObjectRight = GrantHelper.filterFirstUserSpecificGrant(catalogObjectGrants)
                                                              .map(CatalogObjectGrantMetadata::getAccessType);
        if (userSpecificObjectRight.isPresent()) {
            // Return the user grant accessType (A user has only one user grant)
            return userSpecificObjectRight.get();
        } else if (userSpecificBucketRights.isPresent()) {
            return userSpecificBucketRights.get();
        } else {
            // Calculate the highest priority group grant and return its access type
            return GrantHelper.getAccessType(getHighestPriorityGroupGrants(catalogObjectGrants));
        }
    }

    private static Optional<BucketGrantMetadata> getHighestPriorityBucketGrant(List<BucketGrantMetadata> bucketGrants) {
        Optional<BucketGrantMetadata> optUserBucketGrant = GrantHelper.filterFirstUserSpecificGrant(bucketGrants);
        if (optUserBucketGrant.isPresent()) {
            return optUserBucketGrant;
        } else {
            return getHighestPriorityGroupGrants(bucketGrants);
        }
    }

    /**
     * Filter the group grants from the grants list, and get the highest priority grant from group grants.
     *
     * @param grants list of grants
     * @return the group grant with the highest priority
     */
    private static <T extends GrantMetadata> Optional<T> getHighestPriorityGroupGrants(List<T> grants) {
        return grants.stream().filter(GrantHelper::isGroupGrant).max(groupGrantsComparator());
    }

    /**
     * Comparator to get the highest priority grant from the group grants.
     * The grants are first compare by their priority value (int), the grants with the biggest priority value are prioritized.
     * For the grants with same priority value, they are compared by the access type (admin > write > read > noAccess).
     *
     * @return the grant that has the highest access type value
     */
    private static Comparator<GrantMetadata> groupGrantsComparator() {
        return Comparator.comparing(GrantMetadata::getPriority)
                         .thenComparing((g1, g2) -> AccessTypeHelper.compare(g1.getAccessType(), g2.getAccessType()));
    }
}
