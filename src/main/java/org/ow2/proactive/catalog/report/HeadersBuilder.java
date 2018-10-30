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
package org.ow2.proactive.catalog.report;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDPage;
import org.ow2.proactive.catalog.dto.CatalogObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.image.Image;
import lombok.extern.log4j.Log4j2;


@Component
@Log4j2
public class HeadersBuilder {

    private static final String SPACE_BETWEEN_INFO = "          ";

    @Value("${pa.scheduler.url}")
    private String schedulerUrl;

    @Autowired
    private CellFactory cellFactory;

    private static final String ACTIVEEON_LOGO = "/automation-dashboard/styles/patterns/AE-Logo.png";

    private static final String MAIN_TITLE = "ProActive Catalog Report";

    public void createMainHeader(BaseTable table) throws IOException {
        Row<PDPage> headerRow = table.createRow(15f);

        try {
            URL url = new URL(schedulerUrl + ACTIVEEON_LOGO);
            BufferedImage imageFile = ImageIO.read(url);
            headerRow.createImageCell((100 / 12f) * 3, new Image(imageFile));
        } catch (Exception e) {
            headerRow.createCell((100 / 12f) * 3, "Activeeon", HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
            log.error("Impossible to create the image from the url: " + schedulerUrl + ACTIVEEON_LOGO,e);
        }
        cellFactory.addMainTitleCell(headerRow, MAIN_TITLE);

        table.addHeaderRow(headerRow);
    }

    public void createInfoHeader(BaseTable table, Set<CatalogObjectMetadata> orderedObjectsPerBucket,
            Optional<String> kind, Optional<String> contentType) {
        Row<PDPage> infoHeaderRow = table.createRow(15f);

        String infoHeaderMessage = "Objects Kind: " + kind.orElse("All") + " -  Content Type: " +
                                   contentType.orElse("All") + SPACE_BETWEEN_INFO + " Bucket Number: " +
                                   calculateTotalBuckets(orderedObjectsPerBucket) + SPACE_BETWEEN_INFO +
                                   " Object Number: " + orderedObjectsPerBucket.size() + SPACE_BETWEEN_INFO +
                                   " Generated: " + getNowDate();

        cellFactory.addSecondaryHeaderCell(infoHeaderRow, infoHeaderMessage);

    }

    private int calculateTotalBuckets(Set<CatalogObjectMetadata> orderedObjectsPerBucket) {
        String currentBucketName = "";
        int totalNumberOfBuckets = 0;

        for (CatalogObjectMetadata catalogObject : orderedObjectsPerBucket) {

            if (!currentBucketName.equals(catalogObject.getBucketName())) {
                currentBucketName = catalogObject.getBucketName();
                totalNumberOfBuckets++;
            }

        }

        return totalNumberOfBuckets;
    }

    private String getNowDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return new StringBuilder().append(localDateTime.getYear())
                                  .append("-")
                                  .append(localDateTime.getMonthValue())
                                  .append("-")
                                  .append(localDateTime.getDayOfMonth())
                                  .append(" ")
                                  .append(localDateTime.getHour())
                                  .append(":")
                                  .append(localDateTime.getMinute())
                                  .append(":")
                                  .append(localDateTime.getSecond())
                                  .toString();

    }

}
