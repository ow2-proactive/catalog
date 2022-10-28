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
import java.util.Set;

import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.util.parser.WorkflowParser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


/**
 * @author ActiveEon Team
 */
public interface CatalogObjectRepository
        extends JpaRepository<CatalogObjectEntity, CatalogObjectEntity.CatalogObjectEntityKey>,
        JpaSpecificationExecutor<CatalogObjectEntity>, QueryDslPredicateExecutor<CatalogObjectEntity> {

    @EntityGraph("catalogObject.withRevisions")
    CatalogObjectEntity readCatalogObjectRevisionsById(CatalogObjectEntity.CatalogObjectEntityKey key);

    @Query(value = "SELECT DISTINCT cos.kind FROM CatalogObjectEntity cos")
    Set<String> findAllKinds();

    @Query(value = "SELECT DISTINCT cos.contentType FROM CatalogObjectEntity cos")
    Set<String> findAllContentTypes();

    @Query(value = "SELECT DISTINCT kvlem.value FROM KeyValueLabelMetadataEntity kvlem where kvlem.label=" +
                   WorkflowParser.JOB_WORKFLOW_TAG_LABEL)
    Set<String> findAllWorkflowTags();

    @Query("SELECT cos FROM CatalogObjectEntity cos WHERE cos.nameLower = null OR cos.kindLower = null OR cos.contentTypeLower = null")
    List<CatalogObjectEntity> findWithNullNameKindOrContentType();

}
