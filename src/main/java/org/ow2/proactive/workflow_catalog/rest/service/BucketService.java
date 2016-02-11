/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.service;

import org.ow2.proactive.workflow_catalog.rest.assembler.BucketResourceAssembler;
import org.ow2.proactive.workflow_catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.workflow_catalog.rest.entity.Bucket;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author ActiveEon Team
 */
@Service
public class BucketService {

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private BucketResourceAssembler bucketAssembler;

    @Value("${pa.workflow_catalog.default.buckets}")
    private String[] defaultBucketNames;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() throws Exception {
        boolean isTestProfileEnabled =
                Arrays.stream(environment.getActiveProfiles())
                        .anyMatch("test"::equals);

        if (!isTestProfileEnabled && bucketRepository.count() == 0) {
            Stream.of(defaultBucketNames).forEachOrdered(
                    name -> bucketRepository.save(new Bucket(name))
            );
        }
    }

    public BucketMetadata createBucket(String name) {
        Bucket bucket = new Bucket(name, LocalDateTime.now());
        bucket = bucketRepository.save(bucket);

        return new BucketMetadata(bucket);
    }

    public BucketMetadata getBucketMetadata(long id) {
        Bucket bucket = bucketRepository.findOne(id);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return new BucketMetadata(bucket);
    }

    public PagedResources listBuckets(Pageable pageable, PagedResourcesAssembler assembler) {
        Page<Bucket> page = bucketRepository.findAll(pageable);
        return assembler.toResource(page, bucketAssembler);
    }

}
