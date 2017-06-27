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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Data
@Entity
@NoArgsConstructor
@Table(name = "METADATA_KEY_VALUE", uniqueConstraints = @UniqueConstraint(columnNames = { "CATALOGOBJECTREVISION",
                                                                                          "KEY" }), indexes = { @Index(columnList = "KEY"),
                                                                                                                @Index(columnList = "VALUE") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class KeyValueMetadataEntity implements Serializable {

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID", columnDefinition = "BINARY(16)")
    @Id
    private UUID id;

    @Column(name = "KEY", nullable = false)
    protected String key;

    @Column(name = "VALUE", nullable = false)
    protected String value;

    @Column(name = "TYPE", nullable = true)
    protected String type;

    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "CATALOGOBJECTREVISION", referencedColumnName = "ID") })
    protected CatalogObjectRevisionEntity catalogObjectRevision;

    public KeyValueMetadataEntity(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        KeyValueMetadataEntity that = (KeyValueMetadataEntity) o;

        if (!key.equals(that.key))
            return false;
        return catalogObjectRevision.equals(that.catalogObjectRevision);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + catalogObjectRevision.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "KeyValueMetadataEntity{" + "key='" + key + '\'' + ", value='" + value + '\'' + ", type='" + type +
               '\'' + '}';
    }
}
