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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.Optional;

import org.ow2.proactive.catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.catalog.rest.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;


/**
 * @author ActiveEon Team
 */
@RestController
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @ApiOperation(value = "Creates a new bucket")
    @RequestMapping(value = "/buckets", method = POST)
    @ResponseStatus(HttpStatus.CREATED)
    public BucketMetadata create(@RequestParam(value = "name", required = true) String bucketName,
            @ApiParam(value = "The name of the user that will own the Bucket") @RequestParam(value = "owner", required = true) String ownerName) {
        return bucketService.createBucket(bucketName, ownerName);
    }

    @ApiOperation(value = "Gets a bucket's metadata by ID")
    @ApiResponses(value = @ApiResponse(code = 404, message = "Bucket not found"))
    @RequestMapping(value = "/buckets/{bucketId}", method = GET)
    public BucketMetadata getMetadata(@PathVariable long bucketId) {
        return bucketService.getBucketMetadata(bucketId);
    }

    @ApiOperation(value = "Lists the buckets")
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "Results page you want to retrieve (0..N)"),
                         @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "Number of records per page."),
                         @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). " +
                                                                                                                                  "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
    @RequestMapping(value = "/buckets", method = GET)
    public PagedResources list(
            @ApiParam(value = "The name of the user who owns the Bucket") @RequestParam(value = "owner", required = false) Optional<String> ownerName,
            Pageable pageable, PagedResourcesAssembler assembler) {
        return bucketService.listBuckets(ownerName, pageable, assembler);
    }

}
