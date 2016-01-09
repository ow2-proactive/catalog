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

import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "WORKFLOW", indexes = {
        @Index(columnList = "ORIGINAL_ID"),
        @Index(columnList = "REVISION")
})
public class WorkflowRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    protected LocalDateTime createdAt;

    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "ORIGINAL_ID", nullable = false)
    private Long originalId;

    @Column(name = "REVISION", nullable = false)
    private Long revision;

    @ManyToOne
    @JoinColumn(name = "BUCKET_ID", nullable = false)
    private Bucket bucket;

    @Column(name = "PROJECT_NAME", nullable = false)
    private String projectName;

    @OneToMany
    private List<GenericInformation> genericInformation;

    @OneToMany
    private List<Variable> variables;

    @Lob
    @Column(name = "XML_PAYLOAD", columnDefinition = "blob")
    private byte[] xmlPayload;

    public WorkflowRevision() {
        super();
    }

    public WorkflowRevision(Bucket bucket, String name, String projectName, LocalDateTime createdAt,
                            List<GenericInformation> genericInformation, List<Variable> variables, byte[] xmlPayload) {
        this(bucket, -1L, 0L, name, projectName, createdAt, genericInformation, variables, xmlPayload);
    }

    public WorkflowRevision(Bucket bucket, Long originalId, Long revision, String name, String projectName,
                            LocalDateTime createdAt, List<GenericInformation> genericInformation,
                            List<Variable> variables, byte[] xmlPayload) {
        this.name = name;
        this.createdAt = createdAt;
        this.originalId = originalId;
        this.revision = revision;
        this.bucket = bucket;
        this.projectName = projectName;
        this.genericInformation = genericInformation;
        this.variables = variables;
        this.xmlPayload = xmlPayload;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkflowRevision other = (WorkflowRevision) o;

        if (!Objects.equals(this.name, other.name)) {
            return false;
        }

        return Objects.equals(this.createdAt, other.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, createdAt);
    }

    public Bucket getBucket() {
        return bucket;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<GenericInformation> getGenericInformation() {
        return genericInformation;
    }

    public String getName() {
        return name;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getRevision() {
        return revision;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public byte[] getXmlPayload() {
        return xmlPayload;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public void setGenericInformation(List<GenericInformation> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void setXmlPayload(byte[] xmlPayload) {
        this.xmlPayload = xmlPayload;
    }
}