// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link WorkflowCatalogQueryLanguageParser}.
 */
public interface WorkflowCatalogQueryLanguageListener extends ParseTreeListener {
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
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clauses}.
	 * @param ctx the parse tree
	 */
	void enterClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clauses}.
	 * @param ctx the parse tree
	 */
	void exitClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx);
	/**
	 * Enter a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx);
}