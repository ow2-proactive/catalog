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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectTest {

    private Bucket bucket;

    private CatalogObject catalogObject;

    private CatalogObjectRevision catalogObjectRevision;

    @Before
    public void setUp() {
        bucket = new Bucket("test", "WorkflowTestUser");
        catalogObjectRevision = newCatalogObjectRevision(1L, LocalDateTime.now());
        catalogObject = new CatalogObject(bucket);
    }

    @Test
    public void testAddRevision() throws Exception {

        assertThat(catalogObject.getLastCommitId()).isEqualTo(0L);
        assertThat(catalogObject.getRevisions()).hasSize(0);

        catalogObject.addRevision(catalogObjectRevision);

        assertThat(catalogObject.getLastCommitId()).isEqualTo(1L);
        assertThat(catalogObject.getRevisions()).hasSize(1);
    }

    @Test
    public void testSetRevisions() throws Exception {
        SortedSet<CatalogObjectRevision> revisions = ImmutableSortedSet.of(catalogObjectRevision);
        catalogObject.setRevisions(revisions);
        assertEquals(revisions, catalogObject.getRevisions());
    }

    @Test
    public void testGetRevisions() throws Exception {
        SortedSet<CatalogObjectRevision> revisions = new TreeSet<>();
        revisions.add(catalogObjectRevision);
        revisions.add(newCatalogObjectRevision(10L, LocalDateTime.now().plusHours(1)));
        revisions.add(newCatalogObjectRevision(2L, LocalDateTime.now().plusHours(2)));
        catalogObject.setRevisions(revisions);
        assertEquals(revisions, catalogObject.getRevisions());
        Iterator iterator = catalogObject.getRevisions().iterator();
        assertEquals(2L, ((CatalogObjectRevision) iterator.next()).getCommitId().longValue());
        assertEquals(10L, ((CatalogObjectRevision) iterator.next()).getCommitId().longValue());
        assertEquals(1L, ((CatalogObjectRevision) iterator.next()).getCommitId().longValue());
    }

    private CatalogObjectRevision newCatalogObjectRevision(Long revisionId, LocalDateTime date) {
        return new CatalogObjectRevision(revisionId,
                                         "workflow",
                                         date,
                                         "name",
                                         "commit message",
                                         1L,
                                         "application/xml",
                                         new byte[0]);
    }
}
