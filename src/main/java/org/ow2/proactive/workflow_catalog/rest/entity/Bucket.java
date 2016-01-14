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

import com.google.common.collect.Lists;
import org.ow2.proactive.workflow_catalog.rest.util.LocalDateTimeAttributeConverter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "BUCKET")
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @CreatedDate
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    @Column(name = "CREATED_AT", nullable = false)
    protected LocalDateTime createdAt;

    @Column(name = "NAME", nullable = false)
    protected String name;

    @OneToMany(mappedBy = "bucket")
    private List<Workflow> workflows;

    public Bucket() {
    }

    public Bucket(String name, Workflow... workflows) {
        this(name, LocalDateTime.now(), workflows);
    }

    public Bucket(String name, LocalDateTime createdAt, Workflow... workflows) {
        this.name = name;
        this.createdAt = createdAt;
        this.workflows = Lists.newArrayList(workflows);
    }

    public Bucket(String name, LocalDateTime createdAt) {
        this(name, createdAt, new Workflow[0]);
    }

    public void addWorkflow(Workflow workflow) {
        this.workflows.add(workflow);
        workflow.setBucket(this);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public List<Workflow> getWorkflows() {
        return workflows;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
    }

}