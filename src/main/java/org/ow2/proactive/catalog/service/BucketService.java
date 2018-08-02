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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.catalog.dto.BucketMetadata;
import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.entity.BucketEntity;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.BucketNameIsNotValidException;
import org.ow2.proactive.catalog.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.service.exception.DeleteNonEmptyBucketException;
import org.ow2.proactive.catalog.util.name.validator.BucketNameValidator;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
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
    private BucketNameValidator bucketNameValidator;

    @Autowired
    private OwnerGroupStringHelper ownerGroupStringHelper;

    public BucketMetadata createBucket(String name) {
        return createBucket(name, DEFAULT_BUCKET_OWNER);
    }

    public BucketMetadata createBucket(String name, String owner) throws DataIntegrityViolationException {
        if (!bucketNameValidator.checkName(name)) {
            throw new BucketNameIsNotValidException(name);
        }

        BucketEntity bucket = new BucketEntity(name, owner);

        bucket = bucketRepository.save(bucket);
        return new BucketMetadata(bucket);
    }

    public BucketMetadata getBucketMetadata(String bucketName) {
        BucketEntity bucketEntity = findBucketByNameAndCheck(bucketName);
        return new BucketMetadata(bucketEntity);
    }

    public List<BucketMetadata> listBuckets(List<String> owners, Optional<String> kind, Optional<String> contentType) {
        if (owners == null) {
            return Collections.emptyList();
        }

        List<BucketEntity> entities = getBucketEntities(owners, kind, contentType);

        log.info("Buckets size {}", entities.size());
        return entities.stream().map(BucketMetadata::new).collect(Collectors.toList());
    }

    private List<BucketEntity> getBucketEntities(List<String> owners, Optional<String> kind,
            Optional<String> contentType) {
        List<BucketEntity> entities;
        if (kind.isPresent() && contentType.isPresent()) {
            entities = bucketRepository.findByOwnerIsInContainingKindAndContentType(owners,
                                                                                    kind.get(),
                                                                                    contentType.get());
        } else if (kind.isPresent()) {
            entities = bucketRepository.findByOwnerIsInContainingKind(owners, kind.get());
        } else if (contentType.isPresent()) {
            entities = bucketRepository.findByOwnerIsInContainingContentType(owners, contentType.get());
        } else {
            entities = bucketRepository.findByOwnerIn(owners);
        }
        return entities;
    }

    public List<BucketMetadata> listBuckets(String ownerName, Optional<String> kind, Optional<String> contentType) {
        List<BucketEntity> entities;
        List<String> owners = Collections.singletonList(ownerName);

        if (!StringUtils.isEmpty(ownerName)) {
            entities = getBucketEntities(owners, kind, contentType);
        } else if (StringUtils.isEmpty(ownerName)) {
            if (kind.isPresent() && contentType.isPresent()) {
                entities = bucketRepository.findContainingKindAndContentType(kind.get(), contentType.get());
            } else if (kind.isPresent()) {
                entities = bucketRepository.findContainingKind(kind.get());
            } else if (contentType.isPresent()) {
                entities = bucketRepository.findContainingContentType(contentType.get());
            } else {
                entities = bucketRepository.findAll();
            }
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

    public BucketMetadata deleteEmptyBucket(String bucketName) {
        BucketEntity bucket = bucketRepository.findBucketForUpdate(bucketName);

        if (bucket == null) {
            throw new BucketNotFoundException(bucketName);
        }

        if (!bucket.getCatalogObjects().isEmpty()) {
            throw new DeleteNonEmptyBucketException(bucketName);
        }
        bucketRepository.delete(bucket.getId());
        return new BucketMetadata(bucket);
    }

    private BucketEntity findBucketByNameAndCheck(String bucketName) {
        BucketEntity bucketEntity = bucketRepository.findOneByBucketName(bucketName);
        if (bucketEntity == null) {
            throw new BucketNotFoundException(bucketName);
        }
        return bucketEntity;
    }

    public List<BucketMetadata> getBucketsByGroups(String ownerName, Optional<String> kind,
            Optional<String> contentType, Supplier<List<String>> authenticatedUserGroupsSupplier)
            throws NotAuthenticatedException, AccessDeniedException {
        List<String> groups;

        if (ownerName == null) {
            groups = ownerGroupStringHelper.getGroupsWithPrefixFromGroupList(authenticatedUserGroupsSupplier.get());
            groups.add(BucketService.DEFAULT_BUCKET_OWNER);
        } else {
            groups = Collections.singletonList(ownerName);
        }

        return listBuckets(groups, kind, contentType);
    }
}
