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

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "WORKFLOW", indexes = {
        @Index(columnList = "ORIGINAL_ID"),
        @Index(columnList = "REVISION")
})
public class WorkflowRevision extends NamedEntity {

    @Column(name = "ORIGINAL_ID")
    private Long originalId;

    @Column(name = "REVISION", nullable = false)
    private Long revision;

    @ManyToOne
    @JoinColumn(name = "BUCKET_ID", nullable = false)
    private Bucket bucket;

    @OneToMany
    private List<GenericInformation> genericInformation;

    @OneToMany
    private List<Variable> variables;

    @Lob
    @Column(name = "XML_PAYLOAD")
    private byte[] xmlPayload;

    public WorkflowRevision() {
        super();
    }

    public WorkflowRevision(Bucket bucket, String name, LocalDateTime createdAt, byte[] xmlPayload) {
        super(name, createdAt);

        this.originalId = -1L;
        this.revision = 0L;
        this.bucket = bucket;
        this.xmlPayload = xmlPayload;
    }

    public WorkflowRevision(Bucket bucket, Long originalId, Long revision, String name, LocalDateTime createdAt, byte[] xmlPayload) {
        super(name, createdAt);

        this.originalId = originalId;
        this.revision = revision;
        this.bucket = bucket;
        this.xmlPayload = xmlPayload;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    public Long getRevision() {
        return revision;
    }

    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public byte[] getXmlPayload() {
        return xmlPayload;
    }

    public void setXmlPayload(byte[] xmlPayload) {
        this.xmlPayload = xmlPayload;
    }

    public List<GenericInformation> getGenericInformation() {
        return genericInformation;
    }

    public void setGenericInformation(List<GenericInformation> genericInformation) {
        this.genericInformation = genericInformation;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }
}