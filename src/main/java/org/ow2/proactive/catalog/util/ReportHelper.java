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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.ow2.proactive.catalog.dto.BucketGrantMetadata;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.dto.CatalogObjectGrantMetadata;
import org.ow2.proactive.catalog.service.*;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.model.AuthenticatedUser;
import org.ow2.proactive.microservices.common.exception.NotAuthenticatedException;
import org.springframework.http.MediaType;


public class ReportHelper {

    public static void flushResponse(HttpServletResponse response, byte[] content, String fileName) throws IOException {
        response.addHeader("Content-size", Integer.toString(content.length));
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName); // call-graph.pdf

        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    public static List<String> getListOfAuthorizedBuckets(BucketGrantService bucketGrantService,
            BucketService bucketService, CatalogObjectGrantService catalogObjectGrantService,
            GrantRightsService grantRightsService, RestApiAccessService restApiAccessService, boolean sessionIdRequired,
            String sessionId, String ownerName, Optional<String> kind, Optional<String> contentType)
            throws NotAuthenticatedException, AccessDeniedException {
        List<BucketMetadata> authorisedBuckets;
        AuthenticatedUser user;
        if (sessionIdRequired) {
            // Check session validation
            if (!restApiAccessService.isSessionActive(sessionId)) {
                throw new AccessDeniedException("Session id is not active. Please login.");
            }

            // Check Grants
            user = restApiAccessService.getUserFromSessionId(sessionId);

            authorisedBuckets = bucketService.getBucketsByGroups(ownerName, kind, contentType, user);
            authorisedBuckets.addAll(grantRightsService.getBucketsByPrioritizedGrants(user));

            List<BucketGrantMetadata> allBucketsGrants = bucketGrantService.getUserAllBucketsGrants(user);
            List<CatalogObjectGrantMetadata> allCatalogObjectsGrants = catalogObjectGrantService.getObjectsGrants(user);

            List<BucketMetadata> res = new LinkedList<>();
            for (BucketMetadata data : authorisedBuckets) {
                List<BucketGrantMetadata> bucketGrants = GrantHelper.filterBucketGrants(allBucketsGrants,
                                                                                        data.getName());
                grantRightsService.addGrantsForBucketOwner(user, data.getName(), data.getOwner(), bucketGrants);
                List<CatalogObjectGrantMetadata> objectsInBucketGrants = GrantHelper.filterBucketGrants(allCatalogObjectsGrants,
                                                                                                        data.getName());

                String bucketGrantAccessType = grantRightsService.getBucketRights(bucketGrants);
                int objectCount = grantRightsService.getNumberOfAccessibleObjectsInBucket(data,
                                                                                          bucketGrants,
                                                                                          objectsInBucketGrants);
                BucketMetadata metadata = new BucketMetadata(data.getName(),
                                                             data.getOwner(),
                                                             objectCount,
                                                             data.getTenant());
                metadata.setRights(bucketGrantAccessType);
                if (!res.contains(metadata)) {
                    res.add(metadata);
                }
            }
            authorisedBuckets.clear();
            authorisedBuckets.addAll(res);

        } else {
            authorisedBuckets = bucketService.listBuckets(ownerName, kind, contentType);
        }

        return authorisedBuckets.stream().map(BucketMetadata::getName).collect(Collectors.toList());
    }
}
