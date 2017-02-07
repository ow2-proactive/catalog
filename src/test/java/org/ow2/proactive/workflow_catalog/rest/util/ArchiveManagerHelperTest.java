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
package org.ow2.proactive.workflow_catalog.rest.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;


public class ArchiveManagerHelperTest {

    private static URI XML_FILE_0;

    private static URI XML_FILE_1;

    private static URI ZIP_FILE;

    @InjectMocks
    private ArchiveManagerHelper zipManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setFilesUrl() throws Exception {
        XML_FILE_0 = ArchiveManagerHelperTest.class.getResource("/archives/workflow_0.xml").toURI();
        XML_FILE_1 = ArchiveManagerHelperTest.class.getResource("/archives/workflow_1.xml").toURI();
        ZIP_FILE = ArchiveManagerHelperTest.class.getResource("/archives/archive.zip").toURI();
    }

    private byte[] convertFromURIToByteArray(URI uri) throws IOException {
        return Files.readAllBytes(Paths.get(uri));
    }

    @Test
    public void testCompressZip() throws IOException {

        List<WorkflowRevision> files = new ArrayList<>();
        files.add(new WorkflowRevision((long) 0,
                                       (long) 0,
                                       new File(XML_FILE_0).getName(),
                                       null,
                                       null,
                                       null,
                                       convertFromURIToByteArray(XML_FILE_0)));
        files.add(new WorkflowRevision((long) 0,
                                       (long) 0,
                                       new File(XML_FILE_1).getName(),
                                       null,
                                       null,
                                       null,
                                       convertFromURIToByteArray(XML_FILE_1)));

        byte[] archive = zipManager.compressZIP(files);

        FileOutputStream fos = new FileOutputStream("/tmp/archive.zip");
        fos.write(archive);
        fos.close();
    }

    @Test
    public void testExtractZip() throws IOException {

        List<byte[]> files = zipManager.extractZIP(convertFromURIToByteArray(ZIP_FILE));
        for (int i = 0; i < files.size(); i++) {
            byte[] file = files.get(i);
            FileOutputStream fos = new FileOutputStream(String.format("/tmp/workflow_%d.xml", i));
            fos.write(file);
            fos.close();
        }
    }

    @Test
    public void testGetName() throws IOException {

        Set<String> existingNames = new HashSet<>();
        existingNames.add("file1");
        existingNames.add("file2");
        existingNames.add("file2_1");
        existingNames.add("file2_2");
        assertEquals("file0", zipManager.getName(existingNames, "file0"));
        assertEquals("file1_1", zipManager.getName(existingNames, "file1"));
        assertEquals("file2_3", zipManager.getName(existingNames, "file2"));

    }
}
