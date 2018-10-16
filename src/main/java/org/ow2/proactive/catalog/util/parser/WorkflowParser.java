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
package org.ow2.proactive.catalog.util.parser;

import java.io.InputStream;
import java.util.List;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * ProActiveWorkflowParser aims to parse a ProActive XML workflow (whatever the schema version is)
 * in order to extract some specific values (job name, project name, generic
 * information and variables).
 * <p>
 * No validation is applied for now. Besides parsing stop once required information have
 * been extracted, mainly for performance reasons.
 *
 * @author ActiveEon Team
 */
@Log4j2
@NoArgsConstructor
@Component
public final class WorkflowParser extends AbstractCatalogObjectParser {

    private static final String JOB_NAME_KEY = "name";

    private static final String PROJECT_NAME_KEY = "project_name";

    private static final String JOB_AND_PROJECT_LABEL = "job_information";

    public static final String ATTRIBUTE_GENERIC_INFORMATION_LABEL = "generic_information";

    private static final String ATTRIBUTE_VARIABLE_LABEL = "variable";

    private static final String ATTRIBUTE_VARIABLE_MODEL_LABEL = "variable_model";

    private static final String JOB_DESCRIPTION_KEY = "description";

    @Override
    List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) {

        /*
         * Variables indicating which parts of the document have been parsed. Thanks to these
         * information, parsing can be stopped once required information have been extracted.
         */
        Job job;
        try {
            job = JobFactory.getFactory().createJob(inputStream);
        } catch (JobCreationException e) {
            throw new ParsingObjectException(e.getMessage());
        }

        ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder = ImmutableList.builder();

        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(PROJECT_NAME_KEY,
                job.getProjectName(),
                JOB_AND_PROJECT_LABEL));

        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_NAME_KEY, job.getName(), JOB_AND_PROJECT_LABEL));

        job.getGenericInformation().forEach((name, value) ->
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, value, ATTRIBUTE_GENERIC_INFORMATION_LABEL)));

        job.getVariables().forEach((jobVariableName, jobVariable) -> {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(jobVariable.getName(), jobVariable.getValue(), ATTRIBUTE_VARIABLE_LABEL));
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(jobVariable.getName(), jobVariable.getModel(), ATTRIBUTE_VARIABLE_MODEL_LABEL));
        });

        keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_DESCRIPTION_KEY, job.getDescription(), GENERAL_LABEL));

        return keyValueMapBuilder.build();
    }

    @Override
    public boolean isMyKind(String kind) {
        return kind.toLowerCase().startsWith(SupportedParserKinds.WORKFLOW.toString().toLowerCase());
    }

    @Override
    public String getIconPath(List<KeyValueLabelMetadataEntity> keyValueMetadataEntities) {
        return keyValueMetadataEntities.stream()
                                       .filter(keyValue -> keyValue.getLabel()
                                                                   .equals(ATTRIBUTE_GENERIC_INFORMATION_LABEL) &&
                                                           keyValue.getKey().equals("workflow.icon"))
                                       .map(keyValue -> keyValue.getValue())
                                       .findAny()
                                       .orElse(SupportedParserKinds.WORKFLOW.getDefaultIcon());

    }

}
