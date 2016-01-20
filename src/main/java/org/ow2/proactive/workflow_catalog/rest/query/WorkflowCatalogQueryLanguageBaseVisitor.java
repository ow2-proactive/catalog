// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * This class provides an empty implementation of {@link WorkflowCatalogQueryLanguageVisitor},
 * which can be extended to create a visitor which only needs to handle a subset
 * of the available methods.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public class WorkflowCatalogQueryLanguageBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements WorkflowCatalogQueryLanguageVisitor<T> {
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitClause(WorkflowCatalogQueryLanguageParser.ClauseContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitClauses(WorkflowCatalogQueryLanguageParser.ClausesContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public T visitStatement(WorkflowCatalogQueryLanguageParser.StatementContext ctx) { return visitChildren(ctx); }
}