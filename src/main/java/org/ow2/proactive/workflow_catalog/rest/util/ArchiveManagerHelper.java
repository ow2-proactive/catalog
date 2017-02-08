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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.springframework.stereotype.Component;


@Component
public class ArchiveManagerHelper {

    /**
     * Get the name of the Workflow used into the archive (in order not to have twice the same
     * name). If the name of the Workflow already exists, an index is append to the name.
     * @param existingNames the set of names already used
     * @param currentName the Workflow current name
     * @return the name of the Workflow different from all existing names
     */
    public String getName(Set<String> existingNames, String currentName) {

        if (!existingNames.contains(currentName))
            return currentName;

        for (int i = 1; i <= existingNames.size(); i++) {
            String newName = currentName + "_" + i;
            if (!existingNames.contains(newName)) {
                return newName;
            }
        }

        return currentName + "_0";

    }

    /**
     * Compress a list of WorkflowRevision into a ZIP archive
     * @param workflowsList the list of workflows to compress
     * @return a byte array corresponding to the archive containing the workflows
     */
    public byte[] compressZIP(List<WorkflowRevision> workflowsList) {

        if (workflowsList == null || workflowsList.size() == 0) {
            return null;
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            Set<String> existingNames = new HashSet<>();
            for (WorkflowRevision file : workflowsList) {
                String fileName = getName(existingNames, file.getName());
                existingNames.add(fileName);

                byte[] xmlByteArray = file.getXmlPayload();

                ZipEntry entry = new ZipEntry(fileName);
                entry.setSize(xmlByteArray.length);

                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(xmlByteArray);
                zipOutputStream.closeEntry();
            }

            zipOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    /**
     * Extract workflows from an archive
     * @param byteArrayArchive the archive as byte array
     * @return the list of Workflows byte arrays
     */
    public List<byte[]> extractZIP(byte[] byteArrayArchive) {

        List<byte[]> filesList = new ArrayList<>();
        if (byteArrayArchive == null) {
            return filesList;
        }

        try {
            ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(byteArrayArchive));

            while (zipInputStream.getNextEntry() != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int data = 0;
                while ((data = zipInputStream.read()) != -1) {
                    outputStream.write(data);
                }
                outputStream.close();
                filesList.add(outputStream.toByteArray());
            }
            zipInputStream.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return filesList;
    }
}
