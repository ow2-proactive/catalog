// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link WorkflowCatalogQueryLanguageParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface WorkflowCatalogQueryLanguageVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#clauses}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx);
	/**
	 * Visit a parse tree produced by {@link WorkflowCatalogQueryLanguageParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx);
}