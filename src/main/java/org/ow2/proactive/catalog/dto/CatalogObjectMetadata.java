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
package org.ow2.proactive.catalog.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectEntity;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.util.KeyValueEntityToDtoTransformer;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author ActiveEon Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CatalogObjectMetadata extends ResourceSupport implements Comparable<CatalogObjectMetadata> {

    @JsonProperty("commit_time_raw")
    protected final String commitTimeRaw;

    @JsonProperty("kind")
    protected final String kind;

    @JsonProperty("bucket_name")
    protected final String bucketName;

    @JsonProperty
    protected final String name;

    @JsonProperty("project_name")
    protected final String projectName;

    @JsonProperty("tags")
    protected final String tags;

    @JsonProperty
    protected final String extension;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("commit_time")
    protected final LocalDateTime commitDateTime;

    @JsonProperty("content_type")
    protected final String contentType;

    @JsonProperty("commit_message")
    protected final String commitMessage;

    @JsonProperty("username")
    protected final String username;

    @JsonProperty("object_key_values")
    protected final List<Metadata> metadataList;

    @JsonProperty("variables_order")
    protected final LinkedHashMap<String, LinkedHashMap<String, JobVariable>> variablesOrder;

    @JsonProperty("rights")
    protected String rights;

    public CatalogObjectMetadata(CatalogObjectEntity catalogObject) {
        this(catalogObject.getBucket().getBucketName(),
             catalogObject.getId().getName(),
             catalogObject.getRevisions().first().getProjectName(),
             catalogObject.getRevisions().first().getTags(),
             catalogObject.getKind(),
             catalogObject.getContentType(),
             catalogObject.getRevisions().first().getCommitTime(),
             catalogObject.getRevisions().first().getCommitMessage(),
             catalogObject.getRevisions().first().getUsername(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getRevisions().first().getKeyValueMetadataList()),
             catalogObject.getExtension());
    }

    public CatalogObjectMetadata(CatalogObjectRevisionEntity catalogObject) {
        this(catalogObject.getCatalogObject().getBucket().getBucketName(),
             catalogObject.getCatalogObject().getId().getName(),
             catalogObject.getProjectName(),
             catalogObject.getTags(),
             catalogObject.getCatalogObject().getKind(),
             catalogObject.getCatalogObject().getContentType(),
             catalogObject.getCommitTime(),
             catalogObject.getCommitMessage(),
             catalogObject.getUsername(),
             KeyValueEntityToDtoTransformer.to(catalogObject.getKeyValueMetadataList()),
             catalogObject.getCatalogObject().getExtension());
    }

    public CatalogObjectMetadata(String bucketName, String name, String projectName, String tags, String kind,
            String contentType, long commitTime, String commitMessage, String username, List<Metadata> metadataList,
            String extension) {
        this.bucketName = bucketName;
        this.name = name;
        this.kind = kind;
        this.contentType = contentType;
        this.commitTimeRaw = String.valueOf(commitTime);
        this.commitDateTime = Instant.ofEpochMilli(commitTime).atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.commitMessage = commitMessage;
        this.username = username;
        if (metadataList == null) {
            this.metadataList = new ArrayList<>();
            this.variablesOrder = new LinkedHashMap<>();
        } else {
            this.metadataList = metadataList;
            this.variablesOrder = setVariablesOrder(metadataList);
        }
        if (!StringUtils.isBlank(projectName)) {
            this.projectName = projectName;
        } else {
            this.projectName = getMetadataValueIfExistsOrEmptyString("project_name");
        }
        if (!StringUtils.isBlank(tags)) {
            this.tags = tags;
        } else {
            this.tags = getMetadataValueIfExistsOrEmptyString("tags");
        }
        this.extension = extension;
    }

    private String getMetadataValueIfExistsOrEmptyString(String metadataKey) {
        return metadataList.stream()
                           .filter(property -> property.getKey().equals(metadataKey) &&
                                               !StringUtils.isBlank(property.getValue()))
                           .findFirst()
                           .map(Metadata::getValue)
                           .orElse("");
    }

    private LinkedHashMap<String, LinkedHashMap<String, JobVariable>> setVariablesOrder(List<Metadata> metadataList) {
        LinkedHashMap<String, LinkedHashMap<String, JobVariable>> variablesOrder = new LinkedHashMap<>();

        LinkedHashSet<String> groups = new LinkedHashSet<>();

        // This item corresponds to the variables that have no groups - They must be added first
        groups.add("");

        // Get all variables with groups
        groups.addAll(metadataList.stream()
                                  .filter(metadata -> metadata.getLabel().equals("variable_group") &&
                                                      !metadata.getValue().equals("") && metadata.getValue() != null)
                                  .map(Metadata::getValue)
                                  .collect(Collectors.toCollection(LinkedHashSet::new)));

        // Map that associates each variable to its group
        Map<String, String> variablesToGroups = metadataList.stream()
                                                            .filter(metadata -> metadata.getLabel()
                                                                                        .equals("variable_group") &&
                                                                                !metadata.getValue().equals("") &&
                                                                                metadata.getValue() != null)
                                                            .collect(Collectors.toMap(Metadata::getKey,
                                                                                      Metadata::getValue,
                                                                                      (e1, e2) -> e1,
                                                                                      LinkedHashMap::new));

        for (String groupName : groups) {

            LinkedHashMap<String, JobVariable> data = new LinkedHashMap<>();

            // Case: No groups assigned for the variable(s)
            if (groupName.equals("")) {
                for (Metadata metadata : metadataList) {
                    if (metadata.getLabel().equals("variable")) {
                        String variableName = metadata.getKey();
                        if (!variablesToGroups.containsKey(variableName)) {
                            JobVariable variable = createJobVariableFromMetadataAndAssignItToTheCorrespondingGroup(metadataList,
                                                                                                                   groupName,
                                                                                                                   variableName);
                            data.put(variableName, variable);
                        }
                    }
                }
            } else {
                // For each group -> get all variables that belong to the current group
                LinkedHashSet<String> variablesNames = variablesToGroups.entrySet()
                                                                        .stream()
                                                                        .filter(entry -> entry.getValue()
                                                                                              .equals(groupName))
                                                                        .map(Map.Entry::getKey)
                                                                        .collect(Collectors.toCollection(LinkedHashSet::new));

                for (String varName : variablesNames) {
                    // For each variable -> get the data that is used to create the Job Variable object
                    JobVariable variable = createJobVariableFromMetadataAndAssignItToTheCorrespondingGroup(metadataList,
                                                                                                           groupName,
                                                                                                           varName);

                    // Add the Variable name and object to the List
                    data.put(varName, variable);
                }
            }

            // Add the previous list to the corresponding group
            if (!data.isEmpty()) {
                variablesOrder.put(groupName, data);
            }
        }
        return variablesOrder;
    }

    private JobVariable createJobVariableFromMetadataAndAssignItToTheCorrespondingGroup(List<Metadata> metadataList,
            String groupName, String varName) {

        List<Metadata> variableMetadata = metadataList.stream()
                                                      .filter(metadata -> metadata.getKey().equals(varName))
                                                      .collect(Collectors.toList());

        Optional<Metadata> value = variableMetadata.stream()
                                                   .filter(metadata -> metadata.getLabel().equals("variable"))
                                                   .findFirst();
        Optional<Metadata> model = variableMetadata.stream()
                                                   .filter(metadata -> metadata.getLabel().equals("variable_model"))
                                                   .findFirst();
        Optional<Metadata> description = variableMetadata.stream()
                                                         .filter(metadata -> metadata.getLabel()
                                                                                     .equals("variable_description"))
                                                         .findFirst();
        Optional<Metadata> advanced = variableMetadata.stream()
                                                      .filter(metadata -> metadata.getLabel()
                                                                                  .equals("variable_advanced"))
                                                      .findFirst();
        Optional<Metadata> hidden = variableMetadata.stream()
                                                    .filter(metadata -> metadata.getLabel().equals("variable_hidden"))
                                                    .findFirst();

        // Create a Job Variable object with the previous attributes
        return new JobVariable(varName,
                               value.map(Metadata::getValue).orElse(null),
                               model.map(Metadata::getValue).orElse(null),
                               description.map(Metadata::getValue).orElse(null),
                               groupName,
                               advanced.map(metadata -> Boolean.parseBoolean(advanced.get().getValue())).orElse(false),
                               hidden.map(metadata -> Boolean.parseBoolean(hidden.get().getValue())).orElse(false));
    }

    @Override
    public int compareTo(CatalogObjectMetadata catalogObjectMetadata) {
        if (getBucketName().equals(catalogObjectMetadata.getBucketName())) {
            if (getProjectName().equals(catalogObjectMetadata.getProjectName())) {
                return getName().compareTo(catalogObjectMetadata.getName());
            } else {
                return getProjectName().compareTo(catalogObjectMetadata.getProjectName());
            }
        }
        return getBucketName().compareTo(catalogObjectMetadata.getBucketName());
    }
}
