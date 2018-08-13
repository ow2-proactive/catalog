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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FilenameUtils;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ByteSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;


@Component
public class ArchiveManagerHelper {

    @Autowired
    private RawObjectResponseCreator rawObjectResponseCreator;

    public static class ZipArchiveContent {

        private byte[] content;

        private boolean partial;

        public ZipArchiveContent() {
            content = null;
            partial = false;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public boolean isPartial() {
            return partial;
        }

        public void setPartial(boolean partial) {
            this.partial = partial;
        }

    }

    public static class FileNameAndContent {

        private byte[] content;

        private String name;

        private String fileNameWithExtension;

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFileNameWithExtension() {
            return fileNameWithExtension;
        }

        public void setFileNameWithExtension(String fileNameWithExtension) {
            this.fileNameWithExtension = fileNameWithExtension;
        }
    }

    /**
     * Compress a list of CatalogObjectRevision files into a ZIP archive
     * @param catalogObjectList the list of catalogObjects to compress
     * @return a byte array corresponding to the archive containing the files
     */
    public ZipArchiveContent compressZIP(List<CatalogObjectRevisionEntity> catalogObjectList) {

        if (catalogObjectList == null) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            ZipArchiveContent zipContent = new ZipArchiveContent();

            Stream<ZipEntrySource> streamSources = catalogObjectList.stream().filter(catalogObjectRevision -> {
                if (catalogObjectRevision == null) {
                    zipContent.setPartial(true);
                }
                return catalogObjectRevision != null;
            }).map(catalogObjectRevision -> {
                CatalogObjectEntity catalogObjectEntity = catalogObjectRevision.getCatalogObject();
                String fileNameWithExtension = rawObjectResponseCreator.getNameWithFileExtension(catalogObjectEntity.getId()
                                                                                                                    .getName(),
                                                                                                 catalogObjectEntity.getExtension(),
                                                                                                 catalogObjectEntity.getKind());
                return new ByteSource(fileNameWithExtension, catalogObjectRevision.getRawObject());
            });
            ZipEntrySource[] sources = streamSources.toArray(size -> new ZipEntrySource[size]);
            ZipUtil.pack(sources, byteArrayOutputStream);

            zipContent.setContent(byteArrayOutputStream.toByteArray());
            return zipContent;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    /**
     * Extract files from an archive
     * @param byteArrayArchive the archive as byte array
     * @return the list of catalogObjects byte arrays
     */
    public List<FileNameAndContent> extractZIP(byte[] byteArrayArchive) {

        List<FileNameAndContent> filesList = new ArrayList<>();
        if (byteArrayArchive == null) {
            return filesList;
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayArchive)) {
            ZipUtil.iterate(byteArrayInputStream, (in, zipEntry) -> checkAndAddFileFromZip(filesList, in, zipEntry));
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

        return filesList;
    }

    /**
     * check the name of zip entry, exclude containing folder as extracting file
     * @param filesList
     * @param in
     * @param entry
     */
    private void checkAndAddFileFromZip(List<FileNameAndContent> filesList, InputStream in, ZipEntry entry) {
        String nameZipEntry = FilenameUtils.getName(entry.getName());
        if (!nameZipEntry.isEmpty()) {
            filesList.add(process(in, entry));
        }
    }

    /**
     * Extract ZIP entry into a byte array
     * @param in entry content
     * @param entry ZipEntry
     * @return FileNameAndContent
     */
    private FileNameAndContent process(InputStream in, ZipEntry entry) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            FileNameAndContent file = new FileNameAndContent();
            file.setName(FilenameUtils.getBaseName(entry.getName()));
            file.setFileNameWithExtension(FilenameUtils.getName(entry.getName()));

            int data = 0;
            while ((data = in.read()) != -1) {
                outputStream.write(data);
            }
            file.setContent(outputStream.toByteArray());
            return file;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
