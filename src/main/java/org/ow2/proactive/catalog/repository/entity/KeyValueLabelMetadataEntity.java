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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.ow2.proactive.catalog.dto.Metadata;

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
                                                                                          "PA_KEY",
                                                                                          "LABEL" }), indexes = { @Index(columnList = "PA_KEY") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class KeyValueLabelMetadataEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "METADATA_KEY_VALUE_SEQUENCE")
    @GenericGenerator(name = "METADATA_KEY_VALUE_SEQUENCE", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "METADATA_KEY_VALUE_SEQUENCE"),
                                                                                                                                          @Parameter(name = "initial_value", value = "1"),
                                                                                                                                          @Parameter(name = "increment_size", value = "1") })
    @Column(name = "ID")
    protected Long id;

    @Column(name = "PA_KEY", nullable = false)
    protected String key;

    @Lob
    @Column(name = "PA_VALUE", nullable = false, length = Integer.MAX_VALUE)
    @Type(type = "org.hibernate.type.TextType")
    protected String value;

    @SuppressWarnings("DefaultAnnotationParam")
    @Column(name = "LABEL", nullable = true)
    protected String label;

    @ManyToOne
    @JoinColumns({ @JoinColumn(name = "CATALOGOBJECTREVISION", referencedColumnName = "ID") })
    protected CatalogObjectRevisionEntity catalogObjectRevision;

    public KeyValueLabelMetadataEntity(String key, String value, String label) {
        this.key = key;
        this.value = value;
        this.label = label;
    }

    public KeyValueLabelMetadataEntity(Metadata metadata) {
        this.key = metadata.getKey();
        this.value = metadata.getValue();
        this.label = metadata.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        KeyValueLabelMetadataEntity that = (KeyValueLabelMetadataEntity) o;

        if (!key.equals(that.key))
            return false;
        return catalogObjectRevision.equals(that.catalogObjectRevision);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + ((catalogObjectRevision == null) ? 0 : catalogObjectRevision.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "KeyValueLabelMetadataEntity{" + "key='" + key + '\'' + ", value='" + value + '\'' + ", type='" + label +
               '\'' + '}';
    }
}
