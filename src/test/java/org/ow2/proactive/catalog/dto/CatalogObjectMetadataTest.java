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
package org.ow2.proactive.catalog.dto;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;


/**
 * @author ActiveEon Team
 */
public class CatalogObjectMetadataTest {

    @Test
    public void testEquality1() throws Exception {
        List<Metadata> metadataList = Lists.newArrayList(new Metadata("project_name", "project XYZ", "label"));
        CatalogObjectMetadata a = createObjectMetadata("bucketName", "name", metadataList);
        CatalogObjectMetadata b = createObjectMetadata("bucketName", "name", metadataList);

        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testGetProjectNameIfExists() throws Exception {
        List<Metadata> metadataList = Lists.newArrayList(new Metadata("project_name", "project XYZ", "label"));
        CatalogObjectMetadata a = createObjectMetadata("bucketName", "name", metadataList);

        assertThat(a.getProjectName()).isEqualTo("project XYZ");
    }

    @Test
    public void testGetProjectNameIfExistsNull() throws Exception {
        List<Metadata> metadataList = Lists.newArrayList(new Metadata("project_name", "project XYZ", "label"));
        CatalogObjectMetadata a = createObjectMetadata("bucketName", "name", null);

        assertThat(a.getProjectName()).isEqualTo("");
    }

    private CatalogObjectMetadata createObjectMetadata(String bucketName, String name, List<Metadata> metadataList) {

        CatalogObjectMetadata catalogObjectMetadata = new CatalogObjectMetadata(bucketName,
                                                                                name,
                                                                                "kind",
                                                                                "contentType",
                                                                                123546587L,
                                                                                "commitMessage",
                                                                                metadataList,
                                                                                "xml");
        return catalogObjectMetadata;
    }

}
