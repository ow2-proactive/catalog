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
package org.ow2.proactive.workflow_catalog.rest.query;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ow2.proactive.workflow_catalog.rest.entity.QGenericInformation;
import org.ow2.proactive.workflow_catalog.rest.entity.QVariable;
import org.ow2.proactive.workflow_catalog.rest.entity.QWorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.entity.WorkflowRevision;
import org.ow2.proactive.workflow_catalog.rest.query.AtomicLexicalClause.FieldType;
import org.ow2.proactive.workflow_catalog.rest.query.AtomicLexicalClause.Operator;
import org.ow2.proactive.workflow_catalog.rest.query.KeyValueLexicalClause.PairType;
import org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageParser;
import com.google.common.collect.ImmutableMap;
import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow Catalog parse-tree listener.
 * <p>
 * The listener operates on events triggered by the built-in ANTLR tree walker.
 * The events are defined based on the Workflow Catalog grammar.
 * <p>
 * The purpose of the listener is to create a boolean expression that will be
 * used to query and filter results from the SQL database that indexes
 * workflows' data.
 *
 * @author ActiveEon Team
 */
public class WorkflowCatalogQueryLanguageListener implements org.ow2.proactive.workflow_catalog.rest.query.parser.WorkflowCatalogQueryLanguageListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowCatalogQueryLanguageListener.class);

    private static final Pattern WILDCARD_PATTERN = Pattern.compile(".*%.*");

    // Token names which are allowed

    private final static String EQ_TOKEN = "=";

    private final static String NEQ_TOKEN = "!=";

    private final static String NAME_TOKEN = "name";

    private static final String OR_TOKEN = "OR";

    private final static String PROJECT_NAME_TOKEN = "project_name";

    private final static String GI_TOKEN = "generic_information";

    private final static String VAR_TOKEN = "variable";

    // Maps that contain allowed clauses
    // They prevent to write several conditional statements

    private final Map<AtomicLexicalClause, Function<String, ListSubQuery<Long>>> atomicClausesToFuncMap;

    private final Map<KeyValueLexicalClause, BiFunction<String, String, ListSubQuery<Long>>> keyValueClausesToFuncMap;

    // Below are defined intermediate stacks which are used to create
    // the final result.

    // Stacks are populated in the order in which fields are defined.
    // Firstly, some clauses are added to stackOfClauses, then clauses
    // are moved to stackOfContexts. Secondly, contexts are transferred
    // to stackOfSubQueries. Finally, the boolean expression is created
    // from stackOfSubqueries

    private Stack<Clause> stackOfClauses;

    private Stack<Context> stackOfContexts;

    private Stack<ListSubQuery<WorkflowRevision>> stackOfSubQueries;

    // Result object

    private BooleanExpression booleanExpression;


    public WorkflowCatalogQueryLanguageListener() {
        atomicClausesToFuncMap = initAtomicClausesToFuncMap();
        keyValueClausesToFuncMap = initKeyValueClausesToFuncMap();

        stackOfClauses = new Stack<>();
        stackOfContexts = new Stack<>();
        stackOfSubQueries = new Stack<>();
    }

    @Override
    public void enterStart(WorkflowCatalogQueryLanguageParser.StartContext ctx) {
        stackOfContexts.push(new Context());
        log("ENTER START");
    }

    @Override
    public void exitStart(WorkflowCatalogQueryLanguageParser.StartContext ctx) {
        QWorkflowRevision workflowRevision = QWorkflowRevision.workflowRevision;

        if (!stackOfClauses.empty()) {
            moveClausesToContext();
            transformContextToSubQuery();
        }

        while (!stackOfContexts.empty()) {
            transformContextToSubQuery();
        }

        log("EXIT START INTERMEDIATE");

        booleanExpression = null;

        while (!stackOfSubQueries.empty()) {
            ListSubQuery<WorkflowRevision> subQuery = stackOfSubQueries.pop();
            if (booleanExpression == null) {
                booleanExpression = workflowRevision.in(subQuery);
            } else {
                booleanExpression = booleanExpression.or(workflowRevision.in(subQuery));
            }
        }

        log("EXIT START");
    }

    @Override
    public void enterAndExpression(WorkflowCatalogQueryLanguageParser.AndExpressionContext ctx) {
        log("ENTER AND EXPRESSION");
    }

    @Override
    public void exitAndExpression(WorkflowCatalogQueryLanguageParser.AndExpressionContext ctx) {
        assert stackOfClauses.size() == 1 || stackOfClauses.size() == 2;

        moveClausesToContext();

        log("EXIT AND EXPRESSION");
    }

    private void moveClausesToContext() {
        Context context = stackOfContexts.peek();

        while (!stackOfClauses.empty()) {
            context.addClause(stackOfClauses.pop());
        }
    }

    @Override
    public void enterOrExpression(WorkflowCatalogQueryLanguageParser.OrExpressionContext ctx) {
        log("ENTER OR EXPRESSION");
    }

    @Override
    public void exitOrExpression(WorkflowCatalogQueryLanguageParser.OrExpressionContext ctx) {
        log("EXIT OR EXPRESSION");
        moveClauses(stackOfContexts.peek());
    }

    private void transformContextToSubQuery() {
        JPASubQuery jpaSubQuery = new JPASubQuery();

        BooleanExpression booleanExpression = null;

        Context context = stackOfContexts.pop();

        if (context.clauses.size() > 0) {
            for (Clause clause : context.clauses) {
                if (booleanExpression == null) {
                    booleanExpression = QWorkflowRevision.workflowRevision.id.in(clause.listSubQuery);
                } else {
                    booleanExpression = booleanExpression.and(
                            QWorkflowRevision.workflowRevision.id.in(clause.listSubQuery));
                }
            }

            stackOfSubQueries.push(jpaSubQuery.from(
                    QWorkflowRevision.workflowRevision).where(booleanExpression).list(
                    QWorkflowRevision.workflowRevision));
        }
    }

    @Override
    public void enterClauseExpression(WorkflowCatalogQueryLanguageParser.ClauseExpressionContext ctx) {
        // ignore
    }

    @Override
    public void exitClauseExpression(WorkflowCatalogQueryLanguageParser.ClauseExpressionContext ctx) {
        // ignore
    }

    @Override
    public void enterAtomicClause(WorkflowCatalogQueryLanguageParser.AtomicClauseContext ctx) {
        log("ENTER ATOMIC CLAUSE");
    }

    @Override
    public void exitAtomicClause(WorkflowCatalogQueryLanguageParser.AtomicClauseContext ctx) {
        ListSubQuery<Long> listSubQuery = createAtomicLexicalClause(ctx);
        stackOfClauses.push(new Clause(listSubQuery));
        log("EXIT ATOMIC CLAUSE");
    }

    @Override
    public void enterKeyValueClause(WorkflowCatalogQueryLanguageParser.KeyValueClauseContext ctx) {
        log("ENTER KEYVALUE CLAUSE");
    }

    @Override
    public void exitKeyValueClause(WorkflowCatalogQueryLanguageParser.KeyValueClauseContext ctx) {
        ListSubQuery<Long> listSubQuery = createKeyValueLexicalClause(ctx);
        stackOfClauses.push(new Clause(listSubQuery));
        log("EXIT KEYVALUE CLAUSE");
    }

    private void log(String expression) {
        log.debug("{}:\n\t   stackOfClauses{}\n\t  stackOfContexts{}\n\tstackOfSubQueries{}", expression,
                stackOfClauses, stackOfContexts, stackOfSubQueries);
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    private ListSubQuery<Long> createAtomicLexicalClause(
            WorkflowCatalogQueryLanguageParser.AtomicClauseContext ctx) {
        String attributeLiteral = ctx.AttributeLiteral().getText();
        String stringLiteral = ctx.StringLiteral().getText();

        // a String literal always starts and ends by a " character
        // remove leading and trailing " character
        stringLiteral = removeQuotes(stringLiteral);

        AtomicLexicalClause.FieldType fieldType = getFieldType(attributeLiteral);
        Operator operator = getOperator(ctx.COMPARE_OPERATOR().getText());

        Matcher wildcardMatcher = WILDCARD_PATTERN.matcher(stringLiteral);
        boolean stringLiteralHasWildcard = wildcardMatcher.matches();

        AtomicLexicalClause atomicLexicalClause = new AtomicLexicalClause(fieldType, operator,
                stringLiteralHasWildcard);

        Function<String, ListSubQuery<Long>> stringListSubQueryFunction = atomicClausesToFuncMap.get(
                atomicLexicalClause);

        if (stringListSubQueryFunction == null) {
            throw new InvalidClauseRuntimeException("Invalid clause: " + atomicLexicalClause);
        }

        return stringListSubQueryFunction.apply(stringLiteral);

    }

    private ListSubQuery<Long> createKeyValueLexicalClause(
            WorkflowCatalogQueryLanguageParser.KeyValueClauseContext ctx) {

        String attributeLiteral = ctx.AttributeLiteral().getText();

        List<TerminalNode> terminalNodes = ctx.StringLiteral();
        String pairKey = removeQuotes(terminalNodes.get(0).getText());
        String pairValue = removeQuotes(terminalNodes.get(1).getText());

        KeyValueLexicalClause.PairType pairType = getPairType(attributeLiteral);

        boolean stringLiteralNameHasWildcard = WILDCARD_PATTERN.matcher(pairKey).matches();
        boolean stringLiteralValueHasWirldcard = WILDCARD_PATTERN.matcher(pairValue).matches();

        KeyValueLexicalClause keyValueLexicalClause = new KeyValueLexicalClause(pairType,
                stringLiteralNameHasWildcard, stringLiteralValueHasWirldcard);

        BiFunction<String, String, ListSubQuery<Long>> stringListSubQueryFunction =
                keyValueClausesToFuncMap.get(keyValueLexicalClause);

        if (stringListSubQueryFunction == null) {
            throw new InvalidClauseRuntimeException("Invalid clause: " + keyValueLexicalClause);
        }

        return stringListSubQueryFunction.apply(pairKey, pairValue);
    }

    private String removeQuotes(String value) {
        return value.substring(1, value.length() - 1);
    }

    private KeyValueLexicalClause.PairType getPairType(String literal) {
        if (literal.equalsIgnoreCase(VAR_TOKEN)) {
            return PairType.VARIABLE;
        } else if (literal.equalsIgnoreCase(GI_TOKEN)) {
            return PairType.GENERIC_INFORMATION;
        } else {
            throw new InvalidClauseRuntimeException("Invalid PairType '" + literal + "'");
        }
    }

    protected AtomicLexicalClause.FieldType getFieldType(String attributeName) {
        if (attributeName.equalsIgnoreCase(NAME_TOKEN)) {
            return FieldType.NAME;
        } else if (attributeName.equalsIgnoreCase(PROJECT_NAME_TOKEN)) {
            return FieldType.PROJECT_NAME;
        } else {
            throw new InvalidClauseRuntimeException("Invalid FieldType '" + attributeName + "'");
        }
    }

    protected Operator getOperator(String operation) {
        if (operation.contentEquals(EQ_TOKEN)) {
            return Operator.EQUAL;
        } else if (operation.contentEquals(NEQ_TOKEN)) {
            return Operator.NOT_EQUAL;
        } else {
            throw new InvalidClauseRuntimeException("Operator '" + operation + "' is invalid");
        }
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
        if (terminalNode.getSymbol().getText().equals(OR_TOKEN)) {
            createContextAndMoveClauses();
            createContext();
        }

        log("VISIT TERMINAL NODE " + terminalNode);
    }

    private void createContextAndMoveClauses() {
        Context context = createContext();
        moveClauses(context);
    }

    private Context createContext() {
        Context context = new Context();
        stackOfContexts.push(context);
        return context;
    }

    private void moveClauses(
            Context context) {
        while (!stackOfClauses.empty()) {
            Clause clause = stackOfClauses.pop();
            context.addClause(clause);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {
        // ignore
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
        // ignore
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
        // ignore
    }

    private static final class Context {

        private final Stack<Clause> clauses;

        public Context() {
            clauses = new Stack<>();
        }

        public void addClause(Clause clause) {
            clauses.push(clause);
        }

        @Override
        public String toString() {
            return "Context{stackOfClauses=" + clauses + '}';
        }

    }

    private static final class Clause {

        public final ListSubQuery<Long> listSubQuery;

        public Clause(ListSubQuery<Long> listSubQuery) {
            this.listSubQuery = listSubQuery;
        }

        @Override
        public String toString() {
            return "Clause{listSubQuery=" + listSubQuery + '}';
        }

    }

    private Map<AtomicLexicalClause, Function<String, ListSubQuery<Long>>> initAtomicClausesToFuncMap() {
        ImmutableMap.Builder<AtomicLexicalClause,
                Function<String, ListSubQuery<Long>>> builder = ImmutableMap.builder();

        builder.put(
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, false),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.name.eq(value))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.NAME, Operator.NOT_EQUAL, false),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.name.ne(value))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.PROJECT_NAME, Operator.EQUAL, false),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.projectName.eq(value))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.PROJECT_NAME, Operator.NOT_EQUAL, false),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.projectName.ne(value))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.NAME, Operator.EQUAL, true),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.name.like(value, '\\'))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.NAME, Operator.NOT_EQUAL, true),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.name.notLike(value, '\\'))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.PROJECT_NAME, Operator.EQUAL, true),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.projectName.like(value, '\\'))
        );

        builder.put(
                new AtomicLexicalClause(FieldType.PROJECT_NAME, Operator.NOT_EQUAL, true),
                value -> createSubQueryForAtomicClause(
                        QWorkflowRevision.workflowRevision.projectName.notLike(value, '\\'))
        );

        return builder.build();
    }

    private ListSubQuery<Long> createSubQueryForAtomicClause(BooleanExpression booleanExpression) {
        return new JPASubQuery().from(QWorkflowRevision.workflowRevision).where(booleanExpression)
                .list(QWorkflowRevision.workflowRevision.id);
    }


    private Map<KeyValueLexicalClause, BiFunction<String, String, ListSubQuery<Long>>> initKeyValueClausesToFuncMap() {
        ImmutableMap.Builder<KeyValueLexicalClause, BiFunction<String, String, ListSubQuery<Long>>> builder = ImmutableMap.builder();

        // GENERIC INFORMATION

        builder.put(
                new KeyValueLexicalClause(PairType.GENERIC_INFORMATION, false, false),
                (key, value) -> createSubQueryForGenericInformationKeyValueClause(
                        QGenericInformation.genericInformation.key.eq(key).and(
                                QGenericInformation.genericInformation.value.eq(value)))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.GENERIC_INFORMATION, true, false),
                (key, value) -> createSubQueryForGenericInformationKeyValueClause(
                        QGenericInformation.genericInformation.key.like(key, '\\').and(
                                QGenericInformation.genericInformation.value.eq(value)))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.GENERIC_INFORMATION, false, true),
                (key, value) -> createSubQueryForGenericInformationKeyValueClause(
                        QGenericInformation.genericInformation.key.eq(key).and(
                                QGenericInformation.genericInformation.value.like(value, '\\')))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.GENERIC_INFORMATION, true, true),
                (key, value) -> createSubQueryForGenericInformationKeyValueClause(
                        QGenericInformation.genericInformation.key.like(key, '\\').and(
                                QGenericInformation.genericInformation.value.like(value, '\\')))
        );

        // VARIABLES

        builder.put(
                new KeyValueLexicalClause(PairType.VARIABLE, false, false),
                (key, value) -> createSubQueryForVarKeyValueClause(
                        QVariable.variable.key.eq(key).and(QVariable.variable.value.eq(value)))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.VARIABLE, true, false),
                (key, value) -> createSubQueryForVarKeyValueClause(
                        QVariable.variable.key.like(key, '\\').and(QVariable.variable.value.eq(value)))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.VARIABLE, false, true),
                (key, value) -> createSubQueryForVarKeyValueClause(
                        QVariable.variable.key.eq(key).and(QVariable.variable.value.like(value, '\\')))
        );

        builder.put(
                new KeyValueLexicalClause(PairType.VARIABLE, true, true),
                (key, value) -> createSubQueryForVarKeyValueClause(
                        QVariable.variable.key.like(key, '\\').and(
                                QVariable.variable.value.like(value, '\\')))
        );

        return builder.build();
    }

    private ListSubQuery<Long> createSubQueryForGenericInformationKeyValueClause(
            BooleanExpression booleanExpression) {
        return new JPASubQuery().from(QGenericInformation.genericInformation).where(booleanExpression)
                .list(QGenericInformation.genericInformation.workflowRevision.id);
    }

    private ListSubQuery<Long> createSubQueryForVarKeyValueClause(BooleanExpression booleanExpression) {
        return new JPASubQuery().from(QVariable.variable).where(booleanExpression)
                .list(QVariable.variable.workflowRevision.id);
    }

}
