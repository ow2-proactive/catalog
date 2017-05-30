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
package org.ow2.proactive.catalog.rest.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.ow2.proactive.catalog.rest.assembler.BucketResourceAssembler;
import org.ow2.proactive.catalog.rest.dto.BucketMetadata;
import org.ow2.proactive.catalog.rest.entity.Bucket;
import org.ow2.proactive.catalog.rest.service.exception.BucketAlreadyExistingException;
import org.ow2.proactive.catalog.rest.service.exception.BucketNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.DefaultCatalogObjectsFolderNotFoundException;
import org.ow2.proactive.catalog.rest.service.exception.DefaultRawCatalogObjectsFolderNotFoundException;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.util.CatalogObjectJSONParser;
import org.ow2.proactive.catalog.rest.util.CatalogObjectJSONParser.CatalogObjectData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;


/**
 * @author ActiveEon Team
 */
@Service
public class BucketService {

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private BucketResourceAssembler bucketAssembler;

    @Autowired
    private CatalogObjectService catalogObjectService;

    @Value("${pa.catalog.default.buckets}")
    private String[] defaultBucketNames;

    @Autowired
    private Environment environment;

    private static final String DEFAULT_BUCKET_OWNER = "object-catalog";

    private static final String DEFAULT_OBJECTS_FOLDER = "/default-objects";

    private static final String RAW_OBJECTS_FOLDER = "/raw-objects";

    @PostConstruct
    public void init() throws Exception {
        boolean isTestProfileEnabled = Arrays.stream(environment.getActiveProfiles()).anyMatch("test"::equals);

        // We define the initial start by no existing buckets in the Catalog
        // On initial start, we load the Catalog with predefined objects
        if (!isTestProfileEnabled && bucketRepository.count() == 0) {
            populateCatalog(defaultBucketNames, DEFAULT_OBJECTS_FOLDER, RAW_OBJECTS_FOLDER);
        }
    }

    /**
     * The Catalog can be populated with buckets and objects all at once.
     *
     * @param bucketNames The array of bucket names to create
     * @param objectsFolder The folder that contains sub-folders of all objects to inject
     * @throws SecurityException if the Catalog is not allowed to read or access the file
     * @throws IOException if the file or folder could not be found or read properly
     */
    protected void populateCatalog(String[] bucketNames, String objectsFolder, String rawObjectsFolder)
            throws SecurityException, IOException {
        for (String bucketName : bucketNames) {
            final Long bucketId = bucketRepository.save(new Bucket(bucketName, DEFAULT_BUCKET_OWNER)).getId();
            final URL folderResource = getClass().getResource(objectsFolder);
            if (folderResource == null) {
                throw new DefaultCatalogObjectsFolderNotFoundException();
            }

            final URL rawFolderResource = getClass().getResource(rawObjectsFolder);
            if (rawFolderResource == null) {
                throw new DefaultRawCatalogObjectsFolderNotFoundException();
            }

            final File bucketFolder = new File(folderResource.getPath() + File.separator + bucketName);
            if (bucketFolder.isDirectory()) {
                String[] wfs = bucketFolder.list();
                Arrays.sort(wfs);
                for (String object : wfs) {
                    File catalogObjectFile = new File(bucketFolder.getPath() + File.separator + object);
                    CatalogObjectData objectData = CatalogObjectJSONParser.parseJSONFile(catalogObjectFile);

                    File fobject = new File(rawFolderResource.getPath() + File.separator +
                                            objectData.getObjectFileName());
                    FileInputStream fisobject = new FileInputStream(fobject);
                    byte[] bObject = ByteStreams.toByteArray(fisobject);
                    catalogObjectService.createCatalogObject(bucketId,
                                                             objectData.getKind(),
                                                             objectData.getName(),
                                                             objectData.getCommitMessage(),
                                                             Optional.empty(),
                                                             bObject);
                }
            }
        }
    }

    public BucketMetadata createBucket(String name) {
        return createBucket(name, DEFAULT_BUCKET_OWNER);
    }

    public BucketMetadata createBucket(String name, String owner) {
        Bucket bucket = new Bucket(name, owner);
        try {
            bucket = bucketRepository.save(bucket);
        } catch (DataIntegrityViolationException exception) {
            throw new BucketAlreadyExistingException("The bucket named " + name + " owned by " + owner +
                                                     " already exist");
        }
        return new BucketMetadata(bucket);
    }

    public BucketMetadata getBucketMetadata(long id) {
        Bucket bucket = bucketRepository.findOne(id);

        if (bucket == null) {
            throw new BucketNotFoundException();
        }

        return new BucketMetadata(bucket);
    }

    public PagedResources listBuckets(Optional<String> ownerName, Pageable pageable,
            PagedResourcesAssembler assembler) {
        Page<Bucket> page;
        if (ownerName.isPresent()) {
            page = bucketRepository.findByOwner(ownerName.get(), pageable);
        } else {
            page = bucketRepository.findAll(pageable);
        }

        return assembler.toResource(page, bucketAssembler);
    }

}
