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
package org.ow2.proactive.catalog.util;

import static org.ow2.proactive.catalog.util.AccessType.admin;
import static org.ow2.proactive.catalog.util.AccessType.noAccess;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.dto.GrantMetadata;
import org.ow2.proactive.catalog.repository.entity.BucketGrantEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectGrantEntity;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.OwnerGroupStringHelper;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;


public class GrantHelper {
    public static final String GROUP_GRANTEE_TYPE = "group";

    public static final String USER_GRANTEE_TYPE = "user";

    // bucket owner is in the format of "GROUP:XXXXX"
    private static final int BUCKET_OWNER_PREFIX_LENGTH = OwnerGroupStringHelper.GROUP_PREFIX.length();

    public static boolean isPublicBucket(String bucketOwner) {
        return BucketService.DEFAULT_BUCKET_OWNER.equals(bucketOwner);
    }

    /**
     * remove the prefix of the bucket owner to get the bucket belongs to which group
     *
     * @param bucketOwner bucket owner (complete format, "GROUP:XXXXX")
     * @return the group name that the bucket belongs to
     */
    public static String extractBucketOwnerGroup(String bucketOwner) {
        return bucketOwner.substring(BUCKET_OWNER_PREFIX_LENGTH);
    }

    public static BucketGrantMetadata ownerGroupGrant(String bucketOwner, String bucketName) {
        return new BucketGrantMetadata(GROUP_GRANTEE_TYPE,
                                       "",
                                       extractBucketOwnerGroup(bucketOwner),
                                       admin.name(),
                                       5,
                                       0,
                                       bucketName);
    }

    /**
     * Get the first user-specific (i.e., its grantee type is "user") bucket grant from a list of bucket grants.
     *
     * Note that it's supposed to be used for a list of grants targeting a specific bucket assigned to a specific user.
     * In this case, there is at most one grant with the grantee type "user".
     *
     * @param grants list of bucket grants assigned to a user
     * @return the user grant if it exists
     */
    public static <T extends GrantMetadata> Optional<T> filterFirstUserSpecificGrant(List<T> grants) {
        return grants.stream().filter(GrantHelper::isUserSpecificGrant).findFirst();
    }

    public static <T extends GrantMetadata> List<T> filterUserSpecificGrants(List<T> grants) {
        return grants.stream().filter(GrantHelper::isUserSpecificGrant).collect(Collectors.toList());
    }

    public static List<GrantMetadata> filterGroupGrants(List<? extends GrantMetadata> grants) {
        return grants.stream().filter(GrantHelper::isGroupGrant).collect(Collectors.toList());
    }

    public static <T extends GrantMetadata> List<T> filterPositiveGrants(List<T> grants) {
        return grants.stream().filter(g -> !g.getAccessType().equals(noAccess.name())).collect(Collectors.toList());
    }

    public static <T extends GrantMetadata> List<T> filterNoAccessGrants(List<T> grants) {
        return grants.stream().filter(g -> g.getAccessType().equals(noAccess.name())).collect(Collectors.toList());
    }

    public static List<CatalogObjectGrantMetadata> filterObjectGrants(List<CatalogObjectGrantMetadata> grants,
            String catalogObjectName) {
        return grants.stream()
                     .filter(g -> g.getCatalogObjectName().equals(catalogObjectName))
                     .collect(Collectors.toList());
    }

    public static <T extends GrantMetadata> List<T> filterBucketGrants(List<T> grants, String bucketName) {
        return grants.stream().filter(g -> g.getBucketName().equals(bucketName)).collect(Collectors.toList());
    }

    public static Set<String> collectBucketNames(List<? extends GrantMetadata> bucketGrants) {
        return bucketGrants.stream().map(GrantMetadata::getBucketName).collect(Collectors.toSet());
    }

    public static Set<String> collectObjectNames(List<CatalogObjectGrantMetadata> grants) {
        return grants.stream().map(CatalogObjectGrantMetadata::getCatalogObjectName).collect(Collectors.toSet());
    }

    public static String getAccessType(Optional<? extends GrantMetadata> grant) {
        return grant.map(GrantMetadata::getAccessType).orElse(noAccess.name());
    }

    public static boolean isGroupGrant(GrantMetadata grant) {
        return grant.getGranteeType().equals(GROUP_GRANTEE_TYPE);
    }

    public static boolean isUserSpecificGrant(GrantMetadata grant) {
        return grant.getGranteeType().equals(USER_GRANTEE_TYPE);
    }

    public static boolean isPositiveGrant(GrantMetadata grant) {
        return !grant.getAccessType().equals(noAccess.name());
    }

    public static boolean isNoAccessGrant(GrantMetadata grant) {
        return grant.getAccessType().equals(noAccess.name());
    }

    public static boolean hasSameBucketName(GrantMetadata grant, BucketGrantMetadata targetGrant) {
        return grant.getBucketName().equals(targetGrant.getBucketName());
    }

    public static List<BucketGrantMetadata> filterBucketsGrantsAssignedToUserOrItsGroups(AuthenticatedUser user,
            List<BucketGrantEntity> grants) {
        return grants.stream()
                     .filter(grant -> isGrantAssignedToUserOrItsGroups(user, grant))
                     .map(BucketGrantMetadata::new)
                     .collect(Collectors.toList());
    }

    public static List<CatalogObjectGrantMetadata> filterObjectsGrantsAssignedToUserOrItsGroups(AuthenticatedUser user,
            List<CatalogObjectGrantEntity> grants) {
        return grants.stream()
                     .filter(grant -> isGrantAssignedToUserOrItsGroups(user, grant))
                     .map(CatalogObjectGrantMetadata::new)
                     .collect(Collectors.toList());
    }

    public static <T extends GrantMetadata> List<T> filterGrantsAssignedToUserOrItsGroups(AuthenticatedUser user,
            List<T> grants) {
        return grants.stream()
                     .filter(grant -> isGrantAssignedToUserOrItsGroups(user, grant))
                     .collect(Collectors.toList());
    }

    public static boolean isGrantAssignedToUserOrItsGroups(AuthenticatedUser user, GrantMetadata grant) {
        return isGrantAssignedToUserOrItsGroups(user, grant.getGranteeType(), grant.getGrantee());
    }

    public static boolean isGrantAssignedToUserOrItsGroups(AuthenticatedUser user, BucketGrantEntity grant) {
        return isGrantAssignedToUserOrItsGroups(user, grant.getGranteeType(), grant.getGrantee());
    }

    public static boolean isGrantAssignedToUserOrItsGroups(AuthenticatedUser user, CatalogObjectGrantEntity grant) {
        return isGrantAssignedToUserOrItsGroups(user, grant.getGranteeType(), grant.getGrantee());
    }

    public static boolean isGrantAssignedToUserOrItsGroups(AuthenticatedUser user, String granteeType, String grantee) {
        return (grantee.equals(user.getName()) && granteeType.equals(USER_GRANTEE_TYPE)) ||
               (user.getGroups().contains(grantee) && granteeType.equals(GROUP_GRANTEE_TYPE));
    }

    public static List<BucketGrantMetadata> mapToGrants(List<BucketGrantEntity> grantEntities) {
        return grantEntities.stream().map(BucketGrantMetadata::new).collect(Collectors.toList());
    }

    public static List<CatalogObjectGrantMetadata> mapToObjectGrants(List<CatalogObjectGrantEntity> grantEntities) {
        return grantEntities.stream().map(CatalogObjectGrantMetadata::new).collect(Collectors.toList());
    }
}
