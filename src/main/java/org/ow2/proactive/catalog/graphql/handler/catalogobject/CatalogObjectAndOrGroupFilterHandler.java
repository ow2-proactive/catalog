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
package org.ow2.proactive.catalog.graphql.handler.catalogobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.ow2.proactive.catalog.graphql.bean.argument.CatalogObjectWhereArgs;
import org.ow2.proactive.catalog.graphql.bean.common.Operations;
import org.ow2.proactive.catalog.graphql.handler.FilterHandler;
import org.ow2.proactive.catalog.repository.entity.CatalogObjectRevisionEntity;
import org.ow2.proactive.catalog.repository.specification.catalogobject.AndSpecification;
import org.ow2.proactive.catalog.repository.specification.catalogobject.OrSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;


/**
 * @author ActiveEon Team
 * @since 06/07/2017
 */
@Component
@Log4j2
public class CatalogObjectAndOrGroupFilterHandler
        implements FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> {

    @Autowired
    @Qualifier("catalogObjectBucketIdFilterHandler")
    private FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> bucketIdHandler;

    @Autowired
    @Qualifier("catalogObjectKindFilterHandler")
    private FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> kindHandler;

    @Autowired
    @Qualifier("catalogObjectNameFilterHandler")
    private FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> nameHandler;

    @Autowired
    @Qualifier("catalogObjectMetadataFilterHandler")
    private FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> metadataHandler;

    private List<FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity>> fieldFilterHandlers = new ArrayList<>();

    @PostConstruct
    public void init() {
        fieldFilterHandlers.add(bucketIdHandler);
        fieldFilterHandlers.add(kindHandler);
        fieldFilterHandlers.add(nameHandler);
        fieldFilterHandlers.add(metadataHandler);
    }

    @Override
    public Optional<Specification<CatalogObjectRevisionEntity>> handle(CatalogObjectWhereArgs catalogObjectWhereArgs) {
        List<CatalogObjectWhereArgs> andOrArgs;
        Operations operations;

        log.debug(catalogObjectWhereArgs);

        if (catalogObjectWhereArgs.getAndArg() != null) {
            andOrArgs = catalogObjectWhereArgs.getAndArg();
            operations = Operations.AND;
        } else {
            andOrArgs = catalogObjectWhereArgs.getOrArg();
            operations = Operations.OR;
        }

        // binary tree post-order traversal, iterative algorithm
        if (andOrArgs != null) {
            List<CatalogObjectWhereArgsTreeNode> ret = postOrderTraverseWhereArgsToHaveTreeNodes(andOrArgs, operations);

            log.debug(ret);

            // remove unnecessary items
            List<CatalogObjectWhereArgsTreeNode> collect = ret.stream()
                                                              .filter(treeNode -> treeNode.getWhereArgs().size() > 1)
                                                              .collect(Collectors.toList());

            log.debug(collect);

            Specification<CatalogObjectRevisionEntity> finalSpecification = buildFinalSpecification(collect);
            return Optional.of(finalSpecification);
        }
        return Optional.empty();
    }

    /**
     * build up the final specification from the post-order traverse node result
     * 
     * @param collect
     * @return
     */
    private Specification<CatalogObjectRevisionEntity>
            buildFinalSpecification(List<CatalogObjectWhereArgsTreeNode> collect) {
        Deque<Specification<CatalogObjectRevisionEntity>> stack = new LinkedList<>();

        // node
        for (CatalogObjectWhereArgsTreeNode argsTreeNode : collect) {

            Operations nodeOperations = argsTreeNode.getOperations();

            List<Specification<CatalogObjectRevisionEntity>> nodeSpecList = new ArrayList<>();

            for (CatalogObjectWhereArgs whereArg : argsTreeNode.getWhereArgs()) {
                if (whereArg.getOrArg() == null && whereArg.getAndArg() == null) {
                    for (FilterHandler<CatalogObjectWhereArgs, CatalogObjectRevisionEntity> fieldFilterHandler : fieldFilterHandlers) {
                        Optional<Specification<CatalogObjectRevisionEntity>> sp = fieldFilterHandler.handle(whereArg);
                        if (sp.isPresent()) {
                            nodeSpecList.add(sp.get());
                            break;
                        }
                    }
                } else {
                    nodeSpecList.add(stack.pop());
                }
            }

            if (nodeSpecList.isEmpty()) {
                throw new IllegalArgumentException("At least one argument is needed");
            }

            Specification<CatalogObjectRevisionEntity> temp;
            if (nodeOperations == Operations.AND) {
                temp = AndSpecification.builder().fieldSpecifications(nodeSpecList).build();
            } else {
                temp = OrSpecification.builder().fieldSpecifications(nodeSpecList).build();
            }

            stack.push(temp);

        }
        return stack.pop();
    }

    /**
     * /!\ NOTE : do not change this method without good reason and good tests
     *
     * https://discuss.leetcode.com/topic/44777/java-recursive-and-iterative-solutions
     *
     * @param andOrArgs
     * @param operations
     * @return
     */
    private List<CatalogObjectWhereArgsTreeNode>
            postOrderTraverseWhereArgsToHaveTreeNodes(List<CatalogObjectWhereArgs> andOrArgs, Operations operations) {

        // post-order traverse tree algorithm
        Deque<List<CatalogObjectWhereArgs>> stack = new LinkedList<>();
        stack.push(andOrArgs);

        List<CatalogObjectWhereArgsTreeNode> ret = new ArrayList<>();

        while (!stack.isEmpty()) {
            List<CatalogObjectWhereArgs> top = stack.pop();
            if (top != null) {
                ret.add(new CatalogObjectWhereArgsTreeNode(operations, top));

                ArrayList<CatalogObjectWhereArgs> argsCopy = new ArrayList<>(top);

                Iterator<CatalogObjectWhereArgs> iterator = argsCopy.iterator();
                List<CatalogObjectWhereArgs> left = null;
                List<CatalogObjectWhereArgs> right = null;
                while (iterator.hasNext()) {
                    CatalogObjectWhereArgs next = iterator.next();
                    if (next.getAndArg() != null) {
                        operations = Operations.AND;
                        left = next.getAndArg();
                        iterator.remove();
                        if (!argsCopy.isEmpty()) {
                            right = argsCopy;
                        }
                        break;
                    } else if (next.getOrArg() != null) {
                        operations = Operations.OR;
                        left = next.getOrArg();
                        iterator.remove();
                        if (!argsCopy.isEmpty()) {
                            right = argsCopy;
                        }
                        break;
                    }
                }
                stack.push(right);
                stack.push(left);
            }
        }

        Collections.reverse(ret);
        return ret;
    }

    @AllArgsConstructor
    @Data
    private static class CatalogObjectWhereArgsTreeNode {

        private Operations operations;

        private List<CatalogObjectWhereArgs> whereArgs;

    }

}
