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

package org.ow2.proactive.workflow_catalog.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ActiveEon Team
 */
public final class WorkflowMetadata extends NamedMetadata {

    @JsonProperty("original_id")
    public final Long originalId;

    public final Long revision;

    @JsonProperty("bucket_id")
    public final Long bucketId;

    @JsonProperty("project_name")
    public final String projectName;

    @JsonProperty("generic_information")
    public final List<GenericInformation> genericInformation;

    public final List<Variable> variables;

    public WorkflowMetadata(WorkflowRevision workflowRevision) {
        this(workflowRevision.getBucket().getId(), workflowRevision.getId(), workflowRevision.getOriginalId(),
                workflowRevision.getCreatedAt(), workflowRevision.getName(), workflowRevision.getProjectName(),
                workflowRevision.getRevision(), GenericInformation.to(workflowRevision.getGenericInformation()),
                Variable.to(workflowRevision.getVariables()));
    }

    public WorkflowMetadata(Long bucketId, Long id, Long originalId, LocalDateTime createdAt,
                            String name, String projectName, Long revision, List<GenericInformation> genericInformation,
                            List<Variable> variables) {

        super(id, name, createdAt);

        this.originalId = originalId;
        this.revision = revision;
        this.bucketId = bucketId;
        this.projectName = projectName;
        this.genericInformation = genericInformation;
        this.variables = variables;
    }

}
