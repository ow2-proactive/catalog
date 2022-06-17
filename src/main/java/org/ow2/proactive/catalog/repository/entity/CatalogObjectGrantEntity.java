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

import static org.ow2.proactive.catalog.util.GrantHelper.USER_GRANTEE_TYPE;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.type.SerializableToBlobType;
import org.ow2.proactive.catalog.util.ModificationHistoryData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Data
@Entity
@Table(name = "CATALOG_OBJECT_GRANT", uniqueConstraints = @UniqueConstraint(columnNames = { "ID" }), indexes = { @Index(name = "OBJECT_GRANT_IBDEX", columnList = "ID, GRANTEE, CREATOR, GRANTEE_TYPE") })
@ToString()
public class CatalogObjectGrantEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CATALOG_OBJECT_GRANT_SEQUENCE")
    @GenericGenerator(name = "CATALOG_OBJECT_GRANT_SEQUENCE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "CATALOG_OBJECT_GRANT_SEQUENCE"),
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

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = CatalogObjectEntity.class)
    @JoinColumns({ @JoinColumn(name = "BUCKET", referencedColumnName = "BUCKET_ID"),
                   @JoinColumn(name = "NAME", referencedColumnName = "NAME") })
    private CatalogObjectEntity catalogObject;

    // Created date
    @Column(name = "CREATION_DATE")
    protected long creationDate;

    @Column(name = "MODIFICATION_HISTORY", length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.SerializableToBlobType", parameters = @Parameter(name = SerializableToBlobType.CLASS_NAME, value = "java.lang.Object"))
    protected Deque<ModificationHistoryData> modificationHistory;

    public CatalogObjectGrantEntity() {
    }

    // Constructor used to create user grant
    public CatalogObjectGrantEntity(String granteeType, String creator, String grantee, String accessType,
            CatalogObjectEntity catalogObjectEntity) {
        this.granteeType = granteeType;
        this.creator = creator;
        this.grantee = grantee;
        this.accessType = accessType;
        this.catalogObject = catalogObjectEntity;
        this.creationDate = System.currentTimeMillis();
        this.modificationHistory = new ArrayDeque<>();
    }

    // Constructor used to create group grant
    public CatalogObjectGrantEntity(String granteeType, String creator, String grantee, String accessType, int priority,
            CatalogObjectEntity catalogObjectEntity) {
        this.granteeType = granteeType;
        this.creator = creator;
        this.grantee = grantee;
        this.accessType = accessType;
        this.priority = priority;
        this.catalogObject = catalogObjectEntity;
        this.creationDate = System.currentTimeMillis();
        this.modificationHistory = new ArrayDeque<>();
    }

    @Override
    public String toString() {
        if (this.granteeType.equals(USER_GRANTEE_TYPE)) {
            return accessType;
        } else {
            return accessType + "/" + priority;
        }
    }
}
