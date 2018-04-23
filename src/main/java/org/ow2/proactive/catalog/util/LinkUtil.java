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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.ow2.proactive.catalog.rest.controller.CatalogObjectController;
import org.ow2.proactive.catalog.rest.controller.CatalogObjectRevisionController;
import org.ow2.proactive.catalog.service.exception.AccessDeniedException;
import org.ow2.proactive.catalog.service.exception.NotAuthenticatedException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.util.UriComponents;

import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 11/07/2017
 */
@Log4j2
public class LinkUtil {

    /**
     * This is used to generate the absolute URL of the given object revision based on the service domain.
     *
     * @param bucketName The name of the bucket holding this object
     * @param name The name of the object which is the identifier of the object
     * @param commitTime The commit time of the object which is also the identifier of this revision
     * @return a <code>Link</code> referencing the given object's revision raw content
     */
    public static Link createLink(String bucketName, String name, LocalDateTime commitTime)
            throws NotAuthenticatedException, AccessDeniedException {
        try {
            long epochMilli = commitTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            String absoluteLink = linkTo(methodOn(CatalogObjectRevisionController.class).getRaw(null,
                                                                                                bucketName,
                                                                                                URLEncoder.encode(name,
                                                                                                                  "UTF-8")
                                                                                                          .replace("+",
                                                                                                                   "%20"),
                                                                                                epochMilli));

            return new Link(absoluteLink).withRel("content");
        } catch (UnsupportedEncodingException e) {
            log.error("{} cannot be encoded", name, e);
        }
        return null;
    }

    /**
     * This is used to generate the absolute URL of the given object based on the service domain.
     *
     * @param bucketName The name of the bucket holding this object
     * @param name The name of the object which is the identifier of the object
     * @return a <code>Link</code> referencing the given object's raw content
     */
    public static Link createLink(String bucketName, String name)
            throws NotAuthenticatedException, AccessDeniedException {
        try {
            String absoluteLink = linkTo(methodOn(CatalogObjectController.class).getRaw(null,
                                                                                        bucketName,
                                                                                        encodeUrl(name)

            ));
            return new Link(absoluteLink).withRel("content");
        } catch (UnsupportedEncodingException e) {
            log.error("{} cannot be encoded", name, e);
        }
        return null;
    }

    /**
     * This is used to generate the relative URL of the given object revision.
     * The URL will only contain the path <code>buckets/../resources/../revisions/../raw</code>.
     *
     * @param bucketName The id of the bucket holding this object
     * @param objectName The name of the object which is the identifier of the object
     * @param commitTime The commit time of the object which is also the identifier of this revision
     * @return a <code>Link</code> referencing the given object's revision raw content
     */
    public static Link createRelativeLink(String bucketName, String objectName, LocalDateTime commitTime) {
        Link link = null;
        try {
            long epochMilli = commitTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            link = new Link("buckets/" + bucketName + "/resources/" + encodeUrl(objectName) + "/revisions/" +
                            epochMilli).withRel("relative");

        } catch (UnsupportedEncodingException e) {
            log.error("{} cannot be encoded", objectName, e);
        }
        return link;
    }

    /**
     * This is used to generate the relative URL of the given object.
     * The URL will only contain the path <code>buckets/../resources/../raw</code>.
     *
     * @param bucketName The id of the bucket holding this object
     * @param objectName The nokame of the object which is the identifier of the object
     * @return a <code>Link</code> referencing the given object's raw content
     */
    public static Link createRelativeLink(String bucketName, String objectName) {
        Link link = null;
        try {
            link = new Link("buckets/" + bucketName + "/resources/" + encodeUrl(objectName)).withRel("relative");

        } catch (UnsupportedEncodingException e) {
            log.error("{} cannot be encoded", objectName, e);
        }
        return link;
    }

    // ControllerLinkBuilder.linkTo has a problem: it double-URL-encodes characters!
    // e.g. ' ' (whitespace) is encoded to '%2525' instead of '%20'
    // TODO remove hack when https://github.com/spring-projects/spring-hateoas/issues/40 is resolved
    private static final Field uriComponentsField;
    static {
        try {
            uriComponentsField = LinkBuilderSupport.class.getDeclaredField("uriComponents");
            uriComponentsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method is craeted to avoid double encoding
     * @param invocationValue
     * @return encoded link
     */
    public static String linkTo(Object invocationValue) {
        try {
            final UriComponents uriComponents = (UriComponents) uriComponentsField.get(ControllerLinkBuilder.linkTo(invocationValue));
            return uriComponents.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The aim of this method to encode the specified value, so it can be decoded in the same way for Spring: @PathVariable and @RequestParam
     * According to specification:
     * '+' is accepted by @RequestParam as space. In case of @PathVariable, '+' is accepted as '+'
     * @param valueToEncode
     * @return the encoded string that will be compliant with REST API requests
     * @throws UnsupportedEncodingException
     */
    private static String encodeUrl(String valueToEncode) throws UnsupportedEncodingException {
        return URLEncoder.encode(valueToEncode, "UTF-8").replace("+", "%20");
    }

}
