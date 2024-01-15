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
package org.ow2.proactive.catalog.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity.CatalogObjectEntityKey;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.FileNameAndContent;
import org.ow2.proactive.catalog.util.ArchiveManagerHelper.ZipArchiveContent;


public class ArchiveManagerHelperTest {

    private static URI XML_FILE_0;

    private static URI XML_FILE_1;

    private static URI JSON_FILE_1;

    private static URI ZIP_FILE;

    private static URI ZIP_FILE_DIFF_TYPES;

    @Mock
    private RawObjectResponseCreator rawObjectResponseCreator;

    @InjectMocks
    private ArchiveManagerHelper archiveManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void setFilesUrl() throws Exception {
        XML_FILE_0 = ArchiveManagerHelperTest.class.getResource("/archives/workflow_0.xml").toURI();
        XML_FILE_1 = ArchiveManagerHelperTest.class.getResource("/archives/workflow_1.xml").toURI();
        JSON_FILE_1 = ArchiveManagerHelperTest.class.getResource("/archives/array.json").toURI();
        ZIP_FILE = ArchiveManagerHelperTest.class.getResource("/archives/archive.zip").toURI();
        ZIP_FILE_DIFF_TYPES = ArchiveManagerHelperTest.class.getResource("/archives/archiveDiffTypes.zip").toURI();
    }

    private CatalogObjectRevisionEntity getCatalogObjectRevisionEntity(String name, byte[] fileContent,
            String extension) throws IOException {
        CatalogObjectEntity object = new CatalogObjectEntity();
        object.setId(new CatalogObjectEntityKey(1L, name));
        object.setExtension(extension);

        CatalogObjectRevisionEntity revision = new CatalogObjectRevisionEntity();
        revision.setCatalogObject(object);
        object.addRevision(revision);
        revision.setRawObject(fileContent);

        return revision;
    }

    @Test
    public void testCompressZipWithDifferentFileTypes() throws IOException {

        assertNull(archiveManager.compressZIP(false, null));

        byte[] workflowByteArray0 = convertFromURIToByteArray(XML_FILE_0);
        byte[] jsonByteArray1 = convertFromURIToByteArray(XML_FILE_1);
        when(rawObjectResponseCreator.getNameWithFileExtension("workflow_0", "xml", null)).thenReturn("workflow_0.xml");
        when(rawObjectResponseCreator.getNameWithFileExtension("array", "json", null)).thenReturn("array.json");
        List<CatalogObjectRevisionEntity> expectedFiles = new ArrayList<>();
        expectedFiles.add(getCatalogObjectRevisionEntity("workflow_0", workflowByteArray0, "xml"));
        expectedFiles.add(getCatalogObjectRevisionEntity("array", jsonByteArray1, "json"));
        //Compress
        ZipArchiveContent archive = archiveManager.compressZIP(false, expectedFiles);
        //Then extract
        List<FileNameAndContent> actualFiles = archiveManager.extractZIP(archive.getContent());
        assertEquals(2, actualFiles.size());

        compare(workflowByteArray0, actualFiles.get(0).getContent());
        compare(jsonByteArray1, actualFiles.get(1).getContent());
        assertEquals("workflow_0.xml", actualFiles.get(0).getFileNameWithExtension());
        assertEquals("array.json", actualFiles.get(1).getFileNameWithExtension());
    }

    @Test
    public void testCompressZip() throws IOException {

        assertNull(archiveManager.compressZIP(false, null));

        byte[] workflowByteArray0 = convertFromURIToByteArray(XML_FILE_0);
        byte[] workflowByteArray1 = convertFromURIToByteArray(XML_FILE_1);
        List<CatalogObjectRevisionEntity> expectedFiles = new ArrayList<>();
        expectedFiles.add(getCatalogObjectRevisionEntity("workflow_0", workflowByteArray0, "xml"));
        expectedFiles.add(getCatalogObjectRevisionEntity("workflow_1", workflowByteArray1, "xml"));
        when(rawObjectResponseCreator.getNameWithFileExtension("workflow_0", "xml", null)).thenReturn("workflow_0.xml");
        when(rawObjectResponseCreator.getNameWithFileExtension("workflow_1", "xml", null)).thenReturn("workflow_1.xml");
        //Compress
        ZipArchiveContent archive = archiveManager.compressZIP(false, expectedFiles);
        //Then extract
        List<FileNameAndContent> actualFiles = archiveManager.extractZIP(archive.getContent());
        assertEquals(2, actualFiles.size());

        compare(workflowByteArray0, actualFiles.get(0).getContent());
        compare(workflowByteArray1, actualFiles.get(1).getContent());
        assertEquals("workflow_0.xml", actualFiles.get(0).getFileNameWithExtension());
        assertEquals("workflow_1.xml", actualFiles.get(1).getFileNameWithExtension());
    }

    @Test
    public void testExtractZip() throws IOException {
        assertTrue(archiveManager.extractZIP(null).isEmpty());
        List<FileNameAndContent> files = archiveManager.extractZIP(convertFromURIToByteArray(ZIP_FILE));
        assertEquals(2, files.size());

        compare(convertFromURIToByteArray(XML_FILE_0), files.get(0).getContent());
        compare(convertFromURIToByteArray(XML_FILE_1), files.get(1).getContent());
    }

    @Test
    public void testExtractZipWithDiffTypes() throws IOException {
        assertTrue(archiveManager.extractZIP(null).isEmpty());
        List<FileNameAndContent> files = archiveManager.extractZIP(convertFromURIToByteArray(ZIP_FILE_DIFF_TYPES));
        assertEquals(2, files.size());

        compare(convertFromURIToByteArray(JSON_FILE_1), files.get(0).getContent());
        compare(convertFromURIToByteArray(XML_FILE_0), files.get(1).getContent());
    }

    /**
     * Compares 2 files as byte arrays
     * @param expectedFile first file to compare
     * @param actualFile second file to compare
     * @throws IOException
     */
    private void compare(byte[] expectedFile, byte[] actualFile) throws IOException {
        BufferedReader actualReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(actualFile)));
        BufferedReader expectedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(expectedFile)));

        String expectedLine = expectedReader.readLine();
        String actualLine = actualReader.readLine();

        while (expectedLine != null) {
            if (actualLine == null) {
                fail();
            }

            assertEquals(expectedLine, actualLine);

            expectedLine = expectedReader.readLine();
            actualLine = actualReader.readLine();
        }

        if (expectedLine == null && actualLine != null) {
            fail();
        }
    }

    /**
     * Get a byte array of a given file using te file's URI
     * @param uri the URI of the file
     * @return the file as byte array
     * @throws IOException when IO error occurs
     */
    private byte[] convertFromURIToByteArray(URI uri) throws IOException {
        return Files.readAllBytes(Paths.get(uri));
    }
}
