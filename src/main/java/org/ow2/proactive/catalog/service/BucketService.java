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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DeleteNonEmptyBucketException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 */
@Log4j2
@Service
@Transactional
public class BucketService {

    public static final String DEFAULT_BUCKET_OWNER = OwnerGroupStringHelper.GROUP_PREFIX + "public-objects";

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CatalogObjectService catalogObjectService;

    public BucketMetadata createBucket(String name) {
        return createBucket(name, DEFAULT_BUCKET_OWNER);
    }

    public BucketMetadata createBucket(String name, String owner) throws DataIntegrityViolationException {
        BucketEntity bucket = new BucketEntity(name, owner);

        bucket = bucketRepository.save(bucket);
        return new BucketMetadata(bucket);
    }

    public BucketMetadata getBucketMetadata(long id) {
        BucketEntity bucket = bucketRepository.findOne(id);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return new BucketMetadata(bucket);
    }

    public List<BucketMetadata> listBuckets(List<String> owners, String kind) {
        if (owners == null) {
            return Collections.emptyList();
        }

        List<BucketEntity> entities;
        if (!StringUtils.isEmpty(kind)) {
            entities = bucketRepository.findByOwnerIsInContainingKind(owners, kind);
        } else {
            entities = bucketRepository.findByOwnerIn(owners);
        }

        log.info("Buckets size {}", entities.size());
        return entities.stream().map(BucketMetadata::new).collect(Collectors.toList());
    }

    public List<BucketMetadata> listBuckets(String ownerName, String kind) {
        List<BucketEntity> entities;
        List<String> owners = Collections.singletonList(ownerName);

        if (!StringUtils.isEmpty(ownerName) && !StringUtils.isEmpty(kind)) {
            entities = bucketRepository.findByOwnerIsInContainingKind(owners, kind);
        } else if (!StringUtils.isEmpty(ownerName)) {
            entities = bucketRepository.findByOwner(ownerName);
        } else if (!StringUtils.isEmpty(kind)) {
            entities = bucketRepository.findContainingKind(kind);
        } else {
            entities = bucketRepository.findAll();
        }

        log.info("Buckets size {}", entities.size());
        return entities.stream().map(BucketMetadata::new).collect(Collectors.toList());
    }

    public void cleanAllEmptyBuckets() {
        List<BucketEntity> emptyBucketsForUpdate = bucketRepository.findEmptyBucketsForUpdate();
        bucketRepository.deleteInBatch(emptyBucketsForUpdate);
    }

    public void cleanAll() {
        bucketRepository.deleteAll();
    }

    public BucketMetadata deleteEmptyBucket(long bucketId) {
        BucketEntity bucket = bucketRepository.findBucketForUpdate(bucketId);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        if (!bucket.getCatalogObjects().isEmpty()) {
            throw new DeleteNonEmptyBucketException();
        }
        bucketRepository.delete(bucketId);
        return new BucketMetadata(bucket);
    }

}
