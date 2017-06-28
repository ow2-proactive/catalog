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
package org.ow2.proactive.catalog.rest.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Optional;

import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * @author ActiveEon Team
 */
@RestController
@RequestMapping(value = "/buckets")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @ApiOperation(value = "Creates a new bucket")
    @RequestMapping(method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketMetadata create(@RequestParam(value = "name", required = true) String bucketName,
            @ApiParam(value = "The name of the user that will own the Bucket") @RequestParam(value = "owner", required = true) String ownerName) {
        return bucketService.createBucket(bucketName, ownerName);
    }

    @ApiOperation(value = "Gets a bucket's metadata by ID")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket not found"))
    @RequestMapping(value = "/{bucketId}", method = GET)
    public BucketMetadata getMetadata(@PathVariable long bucketId) {
        return bucketService.getBucketMetadata(bucketId);
    }

    @ApiOperation(value = "Lists the buckets")
    @RequestMapping(method = GET)
    public List<BucketMetadata> list(
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) Optional<String> ownerName) {
        return bucketService.listBuckets(ownerName);
    }

    @ApiOperation(value = "Delete the empty buckets")
    @RequestMapping(method = DELETE)
    public void cleanEmpty() {
        bucketService.cleanAllEmptyBuckets();
    }
}
