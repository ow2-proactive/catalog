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

package org.ow2.proactive.workflow_catalog.rest.entity;

import com.google.common.collect.ImmutableList;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "BUCKET")
public class Bucket extends NamedEntity {

    @OneToMany(mappedBy = "bucket")
    private List<WorkflowRevision> workflowRevisions;

    public Bucket() {
    }

    public Bucket(String name, LocalDateTime createdAt, WorkflowRevision... workflowRevisions) {
        super(name, createdAt);
        this.workflowRevisions = ImmutableList.copyOf(workflowRevisions);
    }

    public Bucket(String name, LocalDateTime createdAt) {
        this(name, createdAt, new WorkflowRevision[0]);
    }

    public List<WorkflowRevision> getWorkflowRevisions() {
        return workflowRevisions;
    }

    public void setWorkflowRevisions(List<WorkflowRevision> workflowRevisions) {
        this.workflowRevisions = workflowRevisions;
    }

}