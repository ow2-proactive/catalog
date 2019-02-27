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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ow2.proactive.catalog.repository.entity.KeyValueLabelMetadataEntity;
import org.ow2.proactive.catalog.service.exception.ParsingObjectException;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.JobFactory;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

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

    public static final String ATTRIBUTE_DEPENDS_ON_LABEL = "depends_on";

    private static final String CATALOG_OBJECT_MODEL = "PA:CATALOG_OBJECT";

    public static final String LATEST_VERSION = "latest";

    public static final String CATALOG_OBJECT_MODEL_REGEXP = "^([^/]+/[^/]+)(/[^/][0-9]{12})?";

    private static final String JOB_DESCRIPTION_KEY = "description";

    private static final String JOB_VISUALIZATION_KEY = "visualization";

    @Override
    List<KeyValueLabelMetadataEntity> getMetadataKeyValues(InputStream inputStream) {
        TaskFlowJob job;
        try {
            job = (TaskFlowJob) JobFactory.getFactory().createJob(inputStream);
        } catch (JobCreationException e) {
            throw new ParsingObjectException(e.getMessage(), e);
        }
        ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder = ImmutableSet.builder();

        addProjectNameIfNotNullAndNotEmpty(keyValueMapBuilder, job);
        addJobNameIfNotNull(keyValueMapBuilder, job);
        job.getUnresolvedGenericInformation()
           .forEach((name, value) -> addGenericInformationIfNotNull(keyValueMapBuilder, name, value));
        job.getUnresolvedVariables()
           .values()
           .forEach(jobVariable -> addVariableIfNotNullAndModelIfNotEmpty(keyValueMapBuilder, jobVariable));
        job.getTasks()
           .forEach(task -> task.getVariables()
                                .values()
                                .forEach(taskVariable -> addDependsOnIfCatalogObjectModelExistOnTaskVariable(keyValueMapBuilder,
                                                                                                             taskVariable)));
        addJobDescriptionIfNotNullAndNotEmpty(keyValueMapBuilder, job);
        addJobVizualisationIfNotNullAndNotEmpty(keyValueMapBuilder, job);

        return new ArrayList<>(keyValueMapBuilder.build());
    }

    private void addProjectNameIfNotNullAndNotEmpty(
            ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String projectName = job.getProjectName();
        if (checkIfNotNull(projectName) && checkIfNotEmpty(projectName)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(PROJECT_NAME_KEY,
                                                                   projectName,
                                                                   JOB_AND_PROJECT_LABEL));
        }
    }

    private void addJobNameIfNotNull(ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String name = job.getName();
        if (checkIfNotNull(name)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_NAME_KEY, name, JOB_AND_PROJECT_LABEL));
        }
    }

    private void addGenericInformationIfNotNull(ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder,
            String name, String value) {
        if (checkIfNotNull(name, value)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, value, ATTRIBUTE_GENERIC_INFORMATION_LABEL));
        }
    }

    private void addVariableIfNotNullAndModelIfNotEmpty(
            ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, JobVariable jobVariable) {
        String name = jobVariable.getName();
        String value = jobVariable.getValue();
        String model = jobVariable.getModel();
        if (checkIfNotNull(name, value)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, value, ATTRIBUTE_VARIABLE_LABEL));
        }
        if (checkIfNotNull(name, model) && checkIfNotEmpty(name, model)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(name, model, ATTRIBUTE_VARIABLE_MODEL_LABEL));
            addDependsOn(keyValueMapBuilder, value, model);
        }
    }

    private void addDependsOnIfCatalogObjectModelExistOnTaskVariable(
            ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, TaskVariable taskVariable) {
        String name = taskVariable.getName();
        String value = taskVariable.getValue();
        String model = taskVariable.getModel();
        if (checkIfNotNull(name, model) && checkIfNotEmpty(name, model)) {
            addDependsOn(keyValueMapBuilder, value, model);
        }

    }

    private void addDependsOn(ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, String value,
            String model) {
        if (model.equalsIgnoreCase(CATALOG_OBJECT_MODEL)) {
                keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(getNameAndBucketFromDependsOn(value),
                        getRevisionFromDependsOn(value).orElse(LATEST_VERSION),
                        ATTRIBUTE_DEPENDS_ON_LABEL));
        }
    }

    private String getNameAndBucketFromDependsOn(String calledWorkflow) {
        Pattern pattern = Pattern.compile(CATALOG_OBJECT_MODEL_REGEXP);
        Matcher matcher = pattern.matcher(calledWorkflow);
        if (!(calledWorkflow.matches(CATALOG_OBJECT_MODEL_REGEXP) && matcher.find())) {
            throw new RuntimeException(String.format("Impossible to parse the PA:CATALOG_OBJECT: %s, parsing error when getting the bucket and the workflow name",
                    calledWorkflow));
        } else {
            return (matcher.group(1));

        }
    }


    private Optional<String> getRevisionFromDependsOn(String calledWorkflow) {
        try {
            return Optional.of(calledWorkflow.split("/")[2]);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void addJobDescriptionIfNotNullAndNotEmpty(
            ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
        String description = job.getDescription();
        if (checkIfNotNull(description) && checkIfNotEmpty(description)) {
            keyValueMapBuilder.add(new KeyValueLabelMetadataEntity(JOB_DESCRIPTION_KEY, description, GENERAL_LABEL));
        }
    }

    private void addJobVizualisationIfNotNullAndNotEmpty(
            ImmutableSet.Builder<KeyValueLabelMetadataEntity> keyValueMapBuilder, Job job) {
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
