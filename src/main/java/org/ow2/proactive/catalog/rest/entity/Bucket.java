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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "BUCKET", uniqueConstraints = @UniqueConstraint(columnNames = { "NAME", "OWNER" }))
public class Bucket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "OWNER", nullable = false)
    protected String owner;

    @OneToMany(mappedBy = "bucket")
    private List<CatalogObject> catalogObjects;

    public Bucket() {
    }

    public Bucket(String name, String owner) {
        this(name, owner, new CatalogObject[0]);
    }

    public Bucket(String name, String owner, CatalogObject... catalogObjects) {
        this.name = name;
        this.owner = owner;
        this.catalogObjects = Lists.newArrayList(catalogObjects);
    }

    public void addWorkflow(CatalogObject catalogObject) {
        this.catalogObjects.add(catalogObject);
        catalogObject.setBucket(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CatalogObject> getCatalogObjects() {
        return catalogObjects;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCatalogObjects(List<CatalogObject> catalogObjects) {
        this.catalogObjects = catalogObjects;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
