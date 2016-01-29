// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link WorkflowCatalogQueryLanguageParser}.
 */
public interface WorkflowCatalogQueryLanguageListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(WorkflowCatalogQueryLanguageParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(WorkflowCatalogQueryLanguageParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#and_expression}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expression(WorkflowCatalogQueryLanguageParser.And_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#and_expression}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expression(WorkflowCatalogQueryLanguageParser.And_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#or_expression}.
	 * @param ctx the parse tree
	 */
	void enterOr_expression(WorkflowCatalogQueryLanguageParser.Or_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#or_expression}.
	 * @param ctx the parse tree
	 */
	void exitOr_expression(WorkflowCatalogQueryLanguageParser.Or_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clause}.
	 * @param ctx the parse tree
	 */
	void enterClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clause}.
	 * @param ctx the parse tree
	 */
	void exitClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx);
}