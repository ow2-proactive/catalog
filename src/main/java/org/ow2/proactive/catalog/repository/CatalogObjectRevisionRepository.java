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
package org.ow2.proactive.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


/**
 * @author ActiveEon Team
 * @since 25/06/2017
 */
public interface CatalogObjectRevisionRepository extends JpaRepository<CatalogObjectRevisionEntity, UUID>,
        JpaSpecificationExecutor<CatalogObjectRevisionEntity> {

    @Query("SELECT cor FROM CatalogObjectRevisionEntity cor WHERE cor.catalogObject.bucket.bucketName in ?1 AND cor.catalogObject.lastCommitTime = cor.commitTime")
    List<CatalogObjectRevisionEntity> findDefaultCatalogObjectsInBucket(List<String> bucketNames);

    @Query("SELECT cor FROM CatalogObjectRevisionEntity cor WHERE cor.catalogObject.bucket.bucketName in ?1" +
           " AND lower(cor.catalogObject.kind) LIKE lower(concat(?2, '%')) AND lower(cor.catalogObject.contentType) LIKE lower(concat(?3, '%'))" +
           " AND lower(cor.catalogObject.id.name) LIKE lower(concat('%', ?4, '%')) AND cor.catalogObject.lastCommitTime = cor.commitTime")
    List<CatalogObjectRevisionEntity> findDefaultCatalogObjectsOfKindAndContentTypeInBucket(List<String> bucketNames,
            String kind, String contentType, String objectName);

    @Query("SELECT cor FROM CatalogObjectRevisionEntity cor WHERE cor.catalogObject.bucket.bucketName in ?1 AND cor.catalogObject.id.name = ?2 AND cor.catalogObject.lastCommitTime = cor.commitTime")
    CatalogObjectRevisionEntity findDefaultCatalogObjectByNameInBucket(List<String> bucketNames, String name);

    @Query("SELECT cor FROM CatalogObjectRevisionEntity cor WHERE cor.catalogObject.bucket.bucketName in ?1 AND cor.catalogObject.id.name = ?2 AND cor.commitTime = ?3")
    CatalogObjectRevisionEntity findCatalogObjectRevisionByCommitTime(List<String> bucketNames, String name,
            long commitTime);

    @Query("SELECT metadata.key FROM CatalogObjectRevisionEntity cor INNER JOIN cor.keyValueMetadataList metadata WHERE metadata.label = '" +
           WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL +
           "' AND cor.catalogObject.bucket.bucketName = :bucketName AND cor.catalogObject.id.name = :objectName " +
           " AND cor.commitTime = :revisionTime")
    List<String> findDependsOnCatalogObjectNamesFromKeyValueMetadata(@Param("bucketName") String bucketName,
            @Param("objectName") String objectName, @Param("revisionTime") long commitTime);

    @Query("SELECT metadata.value FROM CatalogObjectRevisionEntity cor INNER JOIN cor.keyValueMetadataList metadata WHERE metadata.label = '" +
           WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL +
           "' AND cor.catalogObject.bucket.bucketName = :bucketName AND cor.catalogObject.id.name = :objectName " +
           " AND cor.commitTime = :revisionTime AND metadata.key = :dependsOnObject")
    String findRevisionOfDependsOnCatalogObjectFromKeyLabelMetadata(@Param("bucketName") String bucketName,
            @Param("objectName") String objectName, @Param("revisionTime") long commitTime,
            @Param("dependsOnObject") String dependsOnObject);

    /**
     *
     * @param bucketObjectName
     * @return a list of objects (checking only the last revision) the depend on the passed bucketObjectName
     */
    @Query("SELECT cor FROM CatalogObjectRevisionEntity cor INNER JOIN cor.keyValueMetadataList metadata WHERE metadata.label = '" +
           WorkflowParser.ATTRIBUTE_DEPENDS_ON_LABEL + "' AND metadata.key = :bucketObjectName" +
           " AND cor.commitTime = cor.catalogObject.lastCommitTime")
    List<CatalogObjectRevisionEntity>
            findCalledByCatalogObjectsFromKeyValueMetadata(@Param("bucketObjectName") String bucketObjectName);

}
