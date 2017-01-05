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
package org.ow2.proactive.workflow_catalog.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author ActiveEon Team
 */
public final class WorkflowMetadata extends NamedMetadata {

    @JsonProperty("revision_id")
    public final Long revisionId;

    @JsonProperty("bucket_id")
    public final Long bucketId;

    @JsonProperty("project_name")
    public final String projectName;

    @JsonProperty("layout")
    public final String layout;

    @JsonProperty("generic_information")
    public final List<GenericInformation> genericInformation;

    public final List<Variable> variables;

    public WorkflowMetadata(WorkflowRevision workflowRevision) {
        this(workflowRevision.getBucketId(),
             workflowRevision.getWorkflow().getId(),
             workflowRevision.getCreatedAt(),
             workflowRevision.getName(),
             workflowRevision.getProjectName(),
             workflowRevision.getLayout(),
             workflowRevision.getRevisionId(),
             GenericInformation.to(workflowRevision.getGenericInformation()),
             Variable.to(workflowRevision.getVariables()));
    }

    public WorkflowMetadata(Long bucketId, Long id, LocalDateTime createdAt, String name, String projectName,
            String layout, Long revisionId, List<GenericInformation> genericInformation, List<Variable> variables) {

        super(id, name, createdAt);
        this.layout = layout;
        this.revisionId = revisionId;
        this.bucketId = bucketId;
        this.projectName = projectName;
        this.genericInformation = genericInformation;
        this.variables = variables;
    }

}
