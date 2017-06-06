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
package org.ow2.proactive.catalog.rest.entity;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "CATALOG_OBJECT", indexes = { @Index(columnList = "LAST_COMMIT_ID") })
public class CatalogObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @ManyToOne
    @JoinColumn(name = "BUCKET_ID", nullable = false)
    private Bucket bucket;

    @OneToMany(mappedBy = "catalogObject", orphanRemoval = true)
    @OrderBy("commitDate DESC")
    private SortedSet<CatalogObjectRevision> revisions;

    @Column(name = "LAST_COMMIT_ID")
    private Long lastCommitId = 0L;

    public CatalogObject() {
        this.revisions = new TreeSet<CatalogObjectRevision>();
    }

    public CatalogObject(Bucket bucket) {
        this(bucket, Lists.newArrayList());
    }

    public CatalogObject(Bucket bucket, CatalogObjectRevision... revisions) {
        this(bucket, Lists.newArrayList(revisions));
    }

    public CatalogObject(Bucket bucket, List<CatalogObjectRevision> revisions) {
        this.bucket = bucket;
        this.revisions = new TreeSet<CatalogObjectRevision>();
        revisions.forEach(this::addRevision);
    }

    public void addRevision(CatalogObjectRevision catalogObjectRevision) {
        this.revisions.add(catalogObjectRevision);
        this.lastCommitId = catalogObjectRevision.getCommitId();
        catalogObjectRevision.setCatalogObject(this);
    }

    public Long getId() {
        return id;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public Long getLastCommitId() {
        return lastCommitId;
    }

    public SortedSet<CatalogObjectRevision> getRevisions() {
        return revisions;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public void setLastCommitId(Long lastCommitId) {
        this.lastCommitId = lastCommitId;
    }

    public void setRevisions(SortedSet<CatalogObjectRevision> revisions) {
        this.revisions = revisions;
    }

    @Override
    public String toString() {
        return "CatalogObject{" + "id=" + id + ", bucket=" + bucket + ", lastCommitId=" + lastCommitId + '}';
    }

}
