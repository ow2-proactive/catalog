/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 * Initial developer(s):               The ProActive Team
 *                         http://proactive.inria.fr/team_members.htm
 */
package org.ow2.proactive.workflow_catalog.rest.controller;

import org.ow2.proactive.workflow_catalog.rest.service.BucketService;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowRevisionService;
import org.ow2.proactive.workflow_catalog.rest.service.WorkflowService;
import org.ow2.proactive.workflow_catalog.rest.service.repository.BucketRepository;
import org.ow2.proactive.workflow_catalog.rest.service.repository.WorkflowRevisionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;

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
    protected WorkflowRevisionRepository workflowRevisionRepository;

    @Autowired
    protected BucketService bucketService;

    @Autowired
    protected WorkflowService workflowService;

    @Autowired
    protected WorkflowRevisionService workflowRevisionService;

    protected String prettify(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(json);

        return gson.toJson(jsonElement);
    }

}
