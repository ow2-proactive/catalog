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
@Table(name = "BUCKET_GRANT", uniqueConstraints = @UniqueConstraint(columnNames = { "ID" }), indexes = { @Index(name = "ID", columnList = "ID") })
@ToString()
public class BucketGrantEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GRANT_SEQUENCE")
    @GenericGenerator(name = "GRANT_SEQUENCE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "GRANT_SEQUENCE"),
                                                                                                                             @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                                                                                                                             @org.hibernate.annotations.Parameter(name = "increment_size", value = "1") })
    @Column(name = "ID")
    protected Long id;

    // Type of this grant: User or Group
    @Column(name = "GRANTEE", nullable = false)
    protected String grantee;

    // Type of this grant: User or Group
    @Column(name = "CREATOR", nullable = false)
    protected String creator;

    // Username or Group Name
    @Column(name = "PROFITEER", nullable = false)
    protected String profiteer;

    // Access type: Admin, write or read
    @Column(name = "ACCESS_TYPE", nullable = false)
    protected String accessType;

    // Bucket association
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = BucketEntity.class)
    @JoinColumn(name = "BUCKET", nullable = false)
    protected BucketEntity bucketEntity;

    public BucketGrantEntity(String grantee, String creator, String profiteer, String accessType,
            BucketEntity bucketEntity) {
        this.grantee = grantee;
        this.creator = creator;
        this.profiteer = profiteer;
        this.accessType = accessType;
        this.bucketEntity = bucketEntity;
    }

    public BucketGrantEntity() {
    }
}
