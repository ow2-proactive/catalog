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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.ow2.proactive.catalog.rest.controller.CatalogObjectRevisionController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 11/07/2017
 */
@Log4j2
public class LinkUtil {

    public static Link createLink(Long bucketId, String name, LocalDateTime commitTime) {
        try {
            long epochMilli = commitTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            ControllerLinkBuilder controllerLinkBuilder = linkTo(methodOn(CatalogObjectRevisionController.class).getRaw(bucketId,
                                                                                                                        URLEncoder.encode(name,
                                                                                                                                          "UTF-8"),
                                                                                                                        epochMilli));

            return new Link(controllerLinkBuilder.toString()).withRel("content");
        } catch (UnsupportedEncodingException e) {
            log.error("{} cannot be encoded", name, e);
        }
        return null;
    }
}
