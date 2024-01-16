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
package org.ow2.proactive.catalog.rest.controller;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Value;

import com.jayway.restassured.RestAssured;


/**
 * @author ActiveEon Team
 */
public abstract class AbstractRestAssuredTest {
    protected static final String BUCKET_RESOURCE = "/buckets/{bucketName}";

    protected static final String BUCKETS_RESOURCE = "/buckets";

    protected static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketName}/resources/{name}";

    protected static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketName}/resources/";

    protected static final String CATALOG_OBJECTS_EXPORT_RESOURCE = "/buckets/{bucketName}/resources/export";

    protected static final String CATALOG_OBJECTS_IMPORT_RESOURCE = "/buckets/{bucketName}/resources/import";

    protected static final String CATALOG_OBJECT_REVISIONS_RESOURCE = "/buckets/{bucketName}/resources/{name}/revisions";

    protected static final String CATALOG_OBJECT_REVISION_RESOURCE_WITH_TIME = "/buckets/{bucketName}/resources/{name}/revisions/{commitTimeRaw}";

    protected static final String CATALOG_OBJECT_REPORT = "/buckets/report/";

    protected static final String CATALOG_OBJECT_CALL_GRAPH = "/buckets/call-graph/";

    protected static final String CATALOG_OBJECT_BUCKET_REPORT = "/buckets/report/selected/{bucketName}";

    protected static final String CATALOG_OBJECTS_REFERENCE = "/buckets/references";

    protected static final String ERROR_MESSAGE = "errorMessage";

    protected static final String BUCKET_GRANTS_RESOURCE_USER = "/buckets/{bucketName}/grant/user";

    protected static final String BUCKET_GRANTS_RESOURCE_GROUP = "/buckets/{bucketName}/grant/group";

    protected static final String CATALOG_OBJECT_GRANTS_RESOURCE_USER = "/buckets/{bucketName}/resources/{catalogObjectName}/grant/user";

    protected static final String CATALOG_OBJECTS_GRANTS_RESOURCE_GROUP = "/buckets/{bucketName}/resources/{catalogObjectName}/grant/group";

    @Value("${local.server.port}")
    private int serverPort;

    @Before
    public void configureRestAssured() {
        RestAssured.port = serverPort;
    }

}
