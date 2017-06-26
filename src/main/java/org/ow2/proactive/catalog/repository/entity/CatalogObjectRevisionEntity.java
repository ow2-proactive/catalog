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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "CATALOG_OBJECT_REVISION", uniqueConstraints = @UniqueConstraint(columnNames = { "BUCKET", "NAME",
                                                                                               "COMMIT_TIME" }), indexes = { @Index(name = "REVISION_INDEX", columnList = "BUCKET,NAME,COMMIT_TIME") })
public class CatalogObjectRevisionEntity implements Comparable, Serializable {

    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID", columnDefinition = "BINARY(16)")
    @Id
    private UUID id;

    @Column(name = "COMMIT_MESSAGE")
    private String commitMessage;

    @Column(name = "COMMIT_TIME", nullable = false)
    private long commitTime;

    @ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
    @JoinColumns({ @JoinColumn(name = "BUCKET", referencedColumnName = "BUCKET_ID"),
                   @JoinColumn(name = "NAME", referencedColumnName = "NAME") })
    private CatalogObjectEntity catalogObject;

    @OneToMany(mappedBy = "catalogObjectRevision", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @Builder.Default
    private List<KeyValueMetadataEntity> keyValueMetadataList = new ArrayList<>();

    @Lob
    @Column(name = "RAW_OBJECT", length = Integer.MAX_VALUE)
    private byte[] rawObject;

    @Override
    public int compareTo(Object o) {
        return Long.valueOf(((CatalogObjectRevisionEntity) o).commitTime).compareTo(Long.valueOf(commitTime));
    }

    public CatalogObjectRevisionEntity() {
        keyValueMetadataList = new ArrayList<>();
    }

    public void addKeyValue(KeyValueMetadataEntity keyValueMetadata) {
        this.keyValueMetadataList.add(keyValueMetadata);
        keyValueMetadata.setCatalogObjectRevision(this);
    }

    public void addKeyValueList(Collection<KeyValueMetadataEntity> keyValueMetadataList) {
        keyValueMetadataList.forEach(kv -> addKeyValue(kv));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        CatalogObjectRevisionEntity that = (CatalogObjectRevisionEntity) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CatalogObjectRevisionRepository{" + "commitMessage='" + commitMessage + '\'' + ", commitTime=" +
               commitTime + ", keyValueMetadataList=" + keyValueMetadataList + '}';
    }
}
