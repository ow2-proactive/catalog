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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


/**
 * Parse a ProActive XML workflow (whatever the schema version is)
 * in order to extract some specific values (job name, project name, generic
 * information and variables).
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

    private static final String JOB_VISUALIZATION_KEY = "visualization";

    @Override
    List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) {
        Job job;
        try {
            job = JobFactory.getFactory().createJob(inputStream);
        } catch (JobCreationException e) {
            throw new ParsingObjectException(e.getMessage(), e);
        }

        ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder = ImmutableList.builder();

        addProjectNameIfNotNullAndNotEmpty(keyValueMapBuilder, job);
        addJobNameIfNotNull(keyValueMapBuilder, job);
        job.getUnresolvedGenericInformation()
           .forEach((name, value) -> addGenericInformationIfNotNull(keyValueMapBuilder, name, value));
        job.getUnresolvedVariables().forEach((jobVariableName,
                jobVariable) -> addVariableIfNotNullAndModelIfNotEmpty(keyValueMapBuilder, jobVariable));
        addJobDescriptionIfNotNullAndNotEmpty(keyValueMapBuilder, job);
        addJobVizualisationIfNotNullAndNotEmpty(keyValueMapBuilder, job);

        return keyValueMapBuilder.build();
    }

    private void addProjectNameIfNotNullAndNotEmpty(
            ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String projectName = job.getProjectName();
        if (checkIfNotNull(projectName) && checkIfNotEmpty(projectName)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(PROJECT_NAME_KEY,
                                                                   projectName,
                                                                   JOB_AND_PROJECT_LABEL));
        }
    }

    private void addJobNameIfNotNull(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String name = job.getName();
        if (checkIfNotNull(name)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_NAME_KEY, name, JOB_AND_PROJECT_LABEL));
        }
    }

    private void addGenericInformationIfNotNull(ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            String name, String value) {
        if (checkIfNotNull(name, value)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, value, ATTRIBUTE_GENERIC_INFORMATION_LABEL));
        }
    }

    private void addVariableIfNotNullAndModelIfNotEmpty(
            ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, JobVariable jobVariable) {
        String name = jobVariable.getName();
        String value = jobVariable.getValue();
        String model = jobVariable.getModel();
        if (checkIfNotNull(name, value)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, value, ATTRIBUTE_VARIABLE_LABEL));
        }
        if (checkIfNotNull(name, model) && checkIfNotEmpty(name, model)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, model, ATTRIBUTE_VARIABLE_MODEL_LABEL));
        }
    }

    private void addJobDescriptionIfNotNullAndNotEmpty(
            ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String description = job.getDescription();
        if (checkIfNotNull(description) && checkIfNotEmpty(description)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_DESCRIPTION_KEY, description, GENERAL_LABEL));
        }
    }

    private void addJobVizualisationIfNotNullAndNotEmpty(
            ImmutableList.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String vizualisation = job.getVisualization();
        if (checkIfNotNull(vizualisation) && checkIfNotEmpty(vizualisation)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_VISUALIZATION_KEY,
                                                                   vizualisation,
                                                                   JOB_AND_PROJECT_LABEL));
        }
    }

    private boolean checkIfNotNull(String... values) {
        return Arrays.stream(values).noneMatch(Objects::isNull);

    }

    private boolean checkIfNotEmpty(String... values) {
        return Arrays.stream(values).noneMatch(String::isEmpty);

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
                                       .map(KeyValueLabelMetadataEntity::getValue)
                                       .findAny()
                                       .orElse(SupportedParserKinds.WORKFLOW.getDefaultIcon());

    }

}
