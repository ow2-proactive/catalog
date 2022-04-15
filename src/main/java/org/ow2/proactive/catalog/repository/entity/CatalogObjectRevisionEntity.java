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
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Builder
@BatchSize(size = 10)
@Data
@Entity
@Table(name = "CATALOG_OBJECT_REVISION", uniqueConstraints = @UniqueConstraint(columnNames = { "BUCKET", "NAME",
                                                                                               "COMMIT_TIME" }), indexes = { @Index(name = "REVISION_INDEX", columnList = "BUCKET,NAME,COMMIT_TIME") })
public class CatalogObjectRevisionEntity implements Comparable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CATALOG_OBJECT_REVISION_SEQ")
    @GenericGenerator(name = "CATALOG_OBJECT_REVISION_SEQ", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "CATALOG_OBJECT_REVISION_SEQ"),
                                                                                                                                          @Parameter(name = "initial_value", value = "1"),
                                                                                                                                          @Parameter(name = "increment_size", value = "1") })
    @Column(name = "ID")
    protected Long id;

    @Column(name = "PROJECT_NAME")
    private String projectName;

    @Column(name = "COMMIT_MESSAGE")
    private String commitMessage;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "COMMIT_TIME", nullable = false)
    private long commitTime;

    @ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
    @JoinColumns({ @JoinColumn(name = "BUCKET", referencedColumnName = "BUCKET_ID"),
                   @JoinColumn(name = "NAME", referencedColumnName = "NAME") })
    private CatalogObjectEntity catalogObject;

    @OneToMany(mappedBy = "catalogObjectRevision", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @OrderBy("id ASC")
    @BatchSize(size = 10)
    @Builder.Default
    private List<KeyValueLabelMetadataEntity> keyValueMetadataList = new ArrayList<>();

    @Lob
    @Column(name = "RAW_OBJECT", length = Integer.MAX_VALUE)
    private byte[] rawObject;

    @OneToMany(mappedBy = "catalogObjectRevisionEntity", cascade = { CascadeType.REMOVE }, orphanRemoval = true)
    private Set<CatalogObjectGrantEntity> objectGrants = new LinkedHashSet<>();

    @Override
    public int compareTo(Object o) {
        return Long.valueOf(((CatalogObjectRevisionEntity) o).commitTime).compareTo(Long.valueOf(commitTime));
    }

    public CatalogObjectEntity getCatalogObjectEntity() {
        return this.catalogObject;
    }

    public CatalogObjectRevisionEntity() {
        keyValueMetadataList = new ArrayList<>();
    }

    public void addKeyValue(KeyValueLabelMetadataEntity keyValueMetadata) {
        this.keyValueMetadataList.add(keyValueMetadata);
        keyValueMetadata.setCatalogObjectRevision(this);
    }

    public void addKeyValueList(Collection<KeyValueLabelMetadataEntity> keyValueMetadataList) {
        keyValueMetadataList.forEach(kv -> addKeyValue(kv));
    }

    public String getProjectName() {
        return this.projectName == null ? "" : this.projectName;
    }

    public String getCommitMessage() {
        return this.commitMessage == null ? "" : this.commitMessage;
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
        return "CatalogObjectRevisionRepository{" + "commitMessage='" + commitMessage + '\'' + ", username='" +
               username + '\'' + ", commitTime='" + commitTime + '\'' + ", projectName='" + projectName + '\'' +
               ", metadataList=" + keyValueMetadataList + '}';
    }
}
