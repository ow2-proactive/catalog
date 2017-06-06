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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * @author ActiveEon Team
 */
@Entity
@Table(name = "METADATA_KEY_VALUE", indexes = { @Index(columnList = "KEY"), @Index(columnList = "VALUE") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class KeyValueMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    protected Long id;

    @Column(name = "KEY", nullable = false)
    protected String key;

    @Column(name = "VALUE", nullable = false)
    protected String value;

    @Column(name = "TYPE", nullable = true)
    protected String type;

    @ManyToOne(cascade = CascadeType.ALL)
    protected CatalogObjectRevision catalogObjectRevision;

    public KeyValueMetadata() {
    }

    public KeyValueMetadata(String key, String value, String label) {
        this.key = key;
        this.value = value;
        this.type = label;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return type;
    }

    public CatalogObjectRevision getCatalogObjectRevision() {
        return catalogObjectRevision;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.type = label;
    }

    public void setCatalogObjectRevision(CatalogObjectRevision catalogObjectRevision) {
        this.catalogObjectRevision = catalogObjectRevision;
    }

    @Override
    public String toString() {
        return "KeyValueMetadata [id=" + id + ", key=" + key + ", value=" + value + ", type=" + type + "]";
    }

}
