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
package org.ow2.proactive.catalog.repository.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Data
@Entity
@Table(name = "BUCKET_GRANT", uniqueConstraints = @UniqueConstraint(columnNames = { "ID" }), indexes = { @Index(name = "BUCKET_GRANT_INDEX", columnList = "ID, GRANTEE_TYPE, CREATOR, GRANTEE") })
@ToString()
public class BucketGrantEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GRANT_SEQUENCE")
    @GenericGenerator(name = "GRANT_SEQUENCE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "GRANT_SEQUENCE"),
                                                                                                                             @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                                                                                                                             @org.hibernate.annotations.Parameter(name = "increment_size", value = "1") })
    @Column(name = "ID")
    protected Long id;

    // Type of this grant: user or group
    @Column(name = "GRANTEE_TYPE", nullable = false)
    protected String granteeType;

    // The User who created this grant
    @Column(name = "CREATOR", nullable = false)
    protected String creator;

    // Username or group Name
    @Column(name = "GRANTEE", nullable = false)
    protected String grantee;

    // Access type: admin, write or read
    @Column(name = "ACCESS_TYPE", nullable = false)
    protected String accessType;

    // Grant priority level used for group grants only
    @Column(name = "PRIORITY")
    protected int priority;

    // Bucket association
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = BucketEntity.class)
    @JoinColumn(name = "BUCKET", nullable = false)
    protected BucketEntity bucketEntity;

    // Constructor used to create user grants
    public BucketGrantEntity(String granteeType, String creator, String grantee, String accessType,
            BucketEntity bucketEntity) {
        this.granteeType = granteeType;
        this.creator = creator;
        this.grantee = grantee;
        this.accessType = accessType;
        this.bucketEntity = bucketEntity;
    }

    // Constructor used to create group grants
    public BucketGrantEntity(String granteeType, String creator, String grantee, String accessType, int priority,
            BucketEntity bucketEntity) {
        this.granteeType = granteeType;
        this.creator = creator;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.bucketEntity = bucketEntity;
    }

    public BucketGrantEntity() {
    }
}