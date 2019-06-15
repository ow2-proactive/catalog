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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectEntityTest {

    private BucketEntity bucket;

    private CatalogObjectEntity catalogObject;

    private CatalogObjectRevisionEntity catalogObjectRevision;

    private LocalDateTime now;

    @Before
    public void setUp() {
        now = LocalDateTime.now();
        bucket = new BucketEntity("test", "WorkflowTestUser");
        catalogObjectRevision = newCatalogObjectRevision(now);
        catalogObject = new CatalogObjectEntity();
        catalogObject.setBucket(bucket);
    }

    @Test
    public void testAddRevision() throws Exception {

        assertThat(catalogObject.getLastCommitTime() == 0);
        assertThat(catalogObject.getRevisions()).hasSize(0);

        catalogObject.addRevision(catalogObjectRevision);

        assertThat(catalogObject.getLastCommitTime()).isEqualTo(now.atZone(ZoneId.systemDefault())
                                                                   .toInstant()
                                                                   .toEpochMilli());
        assertThat(catalogObject.getRevisions()).hasSize(1);
    }

    @Test
    public void testSetRevisions() throws Exception {
        catalogObject.addRevision(catalogObjectRevision);
        assertEquals(catalogObjectRevision, catalogObject.getRevisions().first());
    }

    @Test
    public void testGetRevisions() throws Exception {
        SortedSet<CatalogObjectRevisionEntity> revisions = new TreeSet<>();
        revisions.add(catalogObjectRevision);
        LocalDateTime rev1Time = LocalDateTime.now().plusHours(1);
        revisions.add(newCatalogObjectRevision(rev1Time));
        LocalDateTime rev2Time = LocalDateTime.now().plusHours(2);
        revisions.add(newCatalogObjectRevision(rev2Time));
        catalogObject.setRevisions(revisions);
        assertEquals(revisions, catalogObject.getRevisions());
        Iterator iterator = catalogObject.getRevisions().iterator();
        assertEquals(rev2Time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                     ((CatalogObjectRevisionEntity) iterator.next()).getCommitTime());
        assertEquals(rev1Time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                     ((CatalogObjectRevisionEntity) iterator.next()).getCommitTime());
        assertEquals(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                     ((CatalogObjectRevisionEntity) iterator.next()).getCommitTime());
    }

    private CatalogObjectRevisionEntity newCatalogObjectRevision(LocalDateTime time) {
        return new CatalogObjectRevisionEntity(null,
                                               "commit message",
                                               "username",
                                               time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                               catalogObject,
                                               Collections.emptyList(),
                                               new byte[0]);
    }
}
