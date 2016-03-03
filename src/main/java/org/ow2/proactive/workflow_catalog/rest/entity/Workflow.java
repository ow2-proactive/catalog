/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.workflow_catalog.rest.entity;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Lists;

/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "WORKFLOW", indexes = {
        @Index(columnList = "LAST_REVISION_ID"),
})
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "BUCKET_ID", nullable = false)
    private Bucket bucket;

    @OneToMany(mappedBy = "workflow")
    private List<WorkflowRevision> revisions;

    @Column(name = "LAST_REVISION_ID")
    private Long lastRevisionId = 0L;

    public Workflow() {
        this.revisions = new ArrayList<>(0);
    }

    public Workflow(Bucket bucket, WorkflowRevision... revisions) {
        this(bucket, Lists.newArrayList(revisions));
    }

    public Workflow(Bucket bucket, List<WorkflowRevision> revisions) {
        this.bucket = bucket;
        this.revisions = new ArrayList<>(revisions.size());

        revisions.forEach(this::addRevision);
    }

    public void addRevision(WorkflowRevision workflowRevision) {
        this.revisions.add(workflowRevision);
        this.lastRevisionId++;
        workflowRevision.setWorkflow(this);
    }

    public Long getId() {
        return id;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public Long getLastRevisionId() {
        return lastRevisionId;
    }

    public List<WorkflowRevision> getRevisions() {
        return revisions;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public void setRevisions(List<WorkflowRevision> revisions) {
        this.revisions = revisions;
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "id=" + id +
                ", bucket=" + bucket +
                ", lastRevisionId=" + lastRevisionId +
                '}';
    }

}
