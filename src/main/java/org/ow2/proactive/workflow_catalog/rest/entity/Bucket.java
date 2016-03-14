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

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.ow2.proactive.workflow_catalog.rest.util.LocalDateTimeAttributeConverter;
import com.google.common.collect.Lists;
import org.springframework.data.annotation.CreatedDate;

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

    @Column(name = "OWNER", nullable = false)
    protected String owner;

    @OneToMany(mappedBy = "bucket")
    private List<Workflow> workflows;

    public Bucket() {
    }

    public Bucket(String name, String owner) {
        this(name, LocalDateTime.now(), owner);
    }

    public Bucket(String name, LocalDateTime createdAt, String owner) {
        this(name, createdAt, owner, new Workflow[0]);
    }

    public Bucket(String name, String owner, Workflow... workflows) {
        this(name, LocalDateTime.now(), owner, workflows);
    }

    public Bucket(String name, LocalDateTime createdAt, String owner, Workflow... workflows) {
        this.name = name;
        this.createdAt = createdAt;
        this.owner = owner;
        this.workflows = Lists.newArrayList(workflows);
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}