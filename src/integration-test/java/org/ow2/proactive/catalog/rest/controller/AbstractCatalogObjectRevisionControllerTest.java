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

import org.ow2.proactive.catalog.repository.BucketRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRepository;
import org.ow2.proactive.catalog.repository.CatalogObjectRevisionRepository;
import org.ow2.proactive.catalog.service.BucketService;
import org.ow2.proactive.catalog.service.CatalogObjectService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


/**
 * @author ActiveEon Team
 */
public class AbstractCatalogObjectRevisionControllerTest extends AbstractRestAssuredTest {

    protected static final String CATALOG_OBJECT_RESOURCE = "/buckets/{bucketId}/resources/{name}";

    protected static final String CATALOG_OBJECTS_RESOURCE = "/buckets/{bucketId}/resources/";

    protected static final String CATALOG_OBJECT_REVISIONS_RESOURCE = "/buckets/{bucketId}/resources/{name}/revisions";

    protected static final String CATALOG_OBJECT_REVISION_RESOURCE = "/buckets/{bucketId}/resources/{name}/revisions/{commitTime}";

    @Autowired
    protected BucketRepository bucketRepository;

    @Autowired
    protected BucketService bucketService;

    @Autowired
    protected CatalogObjectService catalogObjectService;

    @Autowired
    protected CatalogObjectRepository catalogObjectRepository;

    @Autowired
    protected CatalogObjectRevisionRepository catalogObjectRevisionRepository;

    protected String prettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(json);

        return gson.toJson(jsonElement);
    }

}
