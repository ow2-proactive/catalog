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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author ActiveEon Team
 */
@AllArgsConstructor
@Builder
@Data
@BatchSize(size = 25)
@Entity
@NamedEntityGraph(name = "catalogObject.withRevisions", attributeNodes = { @NamedAttributeNode("revisions") })
@Table(name = "CATALOG_OBJECT", indexes = { @Index(columnList = "LAST_COMMIT_TIME,NAME_LOWER,KIND_LOWER,CONTENT_TYPE_LOWER") })
public class CatalogObjectEntity implements Serializable {

    @AllArgsConstructor
    @Data
    @Embeddable
    @NoArgsConstructor
    public static class CatalogObjectEntityKey implements Serializable {

        @Column(name = "BUCKET_ID")
        private Long bucketId;

        @Column(name = "NAME", nullable = false)
        private String name;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;

            CatalogObjectEntityKey that = (CatalogObjectEntityKey) o;

            if (bucketId != null ? !bucketId.equals(that.bucketId) : that.bucketId != null)
                return false;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (bucketId != null ? bucketId.hashCode() : 0);
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    @EmbeddedId
    private CatalogObjectEntityKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bucketId")
    @JoinColumn(name = "BUCKET_ID", nullable = false)
    private BucketEntity bucket;

    @Column(name = "NAME_LOWER")
    @ColumnTransformer(write = "LOWER(?)")
    private String nameLower;

    @Column(name = "CONTENT_TYPE")
    private String contentType;

    @Column(name = "CONTENT_TYPE_LOWER")
    @ColumnTransformer(write = "LOWER(?)")
    private String contentTypeLower;

    @Column(name = "KIND", nullable = false)
    private String kind;

    @Column(name = "KIND_LOWER")
    @ColumnTransformer(write = "LOWER(?)")
    private String kindLower;

    @Column(name = "EXTENSION")
    private String extension;

    @OneToMany(mappedBy = "catalogObject", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
                                                                               CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("commitTime DESC")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @Builder.Default
    private SortedSet<CatalogObjectRevisionEntity> revisions = new TreeSet<>();

    @Column(name = "LAST_COMMIT_TIME")
    private long lastCommitTime;

    public CatalogObjectEntity() {
        revisions = new TreeSet<>();
    }

    public void addRevision(CatalogObjectRevisionEntity catalogObjectRevision) {
        this.revisions.add(catalogObjectRevision);
        this.lastCommitTime = catalogObjectRevision.getCommitTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        CatalogObjectEntity that = (CatalogObjectEntity) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CatalogObjectEntity{" + "id=" + id + ", bucket=" + bucket + ", contentType='" + contentType + '\'' +
               ", kind='" + kind + '\'' + ", lastCommitTime=" + lastCommitTime + '}';
    }
}
