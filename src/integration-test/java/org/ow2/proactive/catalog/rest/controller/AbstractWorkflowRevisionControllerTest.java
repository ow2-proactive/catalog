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

import org.ow2.proactive.catalog.rest.service.BucketService;
import org.ow2.proactive.catalog.rest.service.CatalogObjectRevisionService;
import org.ow2.proactive.catalog.rest.service.CatalogObjectService;
import org.ow2.proactive.catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.catalog.rest.service.repository.CatalogObjectRevisionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


/**
 * @author ActiveEon Team
 */
public class AbstractWorkflowRevisionControllerTest extends AbstractRestAssuredTest {

    protected static final String WORKFLOW_RESOURCE = "/buckets/{bucketId}/workflows/{workflowId}";

    protected static final String WORKFLOWS_RESOURCE = "/buckets/{bucketId}/workflows/";

    protected static final String WORKFLOW_REVISIONS_RESOURCE = "/buckets/{bucketId}/workflows/{workflowId}/revisions";

    protected static final String WORKFLOW_REVISION_RESOURCE = "/buckets/{bucketId}/workflows/{workflowId}/revisions/{revisionId}";

    @Autowired
    protected BucketRepository bucketRepository;

    @Autowired
    protected CatalogObjectRevisionRepository workflowRevisionRepository;

    @Autowired
    protected BucketService bucketService;

    @Autowired
    protected CatalogObjectService workflowService;

    @Autowired
    protected CatalogObjectRevisionService catalogObjectRevisionService;

    protected String prettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(json);

        return gson.toJson(jsonElement);
    }

}
