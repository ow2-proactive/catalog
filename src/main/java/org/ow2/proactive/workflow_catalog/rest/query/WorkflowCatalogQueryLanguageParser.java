// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WorkflowCatalogQueryLanguageParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, ATTRIBUTE=3, CONJUNCTION=4, OPERATOR=5, VALUE=6, WHITESPACE=7;
	public static final int
		RULE_clause = 0, RULE_clauses = 1, RULE_statement = 2;
	public static final String[] ruleNames = {
		"clause", "clauses", "statement"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, "ATTRIBUTE", "CONJUNCTION", "OPERATOR", "VALUE", "WHITESPACE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "WorkflowCatalogQueryLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public WorkflowCatalogQueryLanguageParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ClauseContext extends ParserRuleContext {
		public TerminalNode ATTRIBUTE() { return getToken(WorkflowCatalogQueryLanguageParser.ATTRIBUTE, 0); }
		public TerminalNode OPERATOR() { return getToken(WorkflowCatalogQueryLanguageParser.OPERATOR, 0); }
		public TerminalNode VALUE() { return getToken(WorkflowCatalogQueryLanguageParser.VALUE, 0); }
		public ClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).enterClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).exitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof WorkflowCatalogQueryLanguageVisitor ) return ((WorkflowCatalogQueryLanguageVisitor<? extends T>)visitor).visitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClauseContext clause() throws RecognitionException {
		ClauseContext _localctx = new ClauseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_clause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(6);
			match(ATTRIBUTE);
			setState(7);
			match(OPERATOR);
			setState(8);
			match(VALUE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClausesContext extends ParserRuleContext {
		public List<ClauseContext> clause() {
			return getRuleContexts(ClauseContext.class);
		}
		public ClauseContext clause(int i) {
			return getRuleContext(ClauseContext.class,i);
		}
		public List<TerminalNode> CONJUNCTION() { return getTokens(WorkflowCatalogQueryLanguageParser.CONJUNCTION); }
		public TerminalNode CONJUNCTION(int i) {
			return getToken(WorkflowCatalogQueryLanguageParser.CONJUNCTION, i);
		}
		public ClausesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clauses; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).enterClauses(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).exitClauses(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof WorkflowCatalogQueryLanguageVisitor ) return ((WorkflowCatalogQueryLanguageVisitor<? extends T>)visitor).visitClauses(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClausesContext clauses() throws RecognitionException {
		ClausesContext _localctx = new ClausesContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_clauses);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			clause();
			setState(15);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==CONJUNCTION) {
				{
				{
				setState(11);
				match(CONJUNCTION);
				setState(12);
				clause();
				}
				}
				setState(17);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public ClausesContext clauses() {
			return getRuleContext(ClausesContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof WorkflowCatalogQueryLanguageListener ) ((WorkflowCatalogQueryLanguageListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof WorkflowCatalogQueryLanguageVisitor ) return ((WorkflowCatalogQueryLanguageVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		try {
			setState(23);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(18);
				match(T__0);
				setState(19);
				clauses();
				setState(20);
				match(T__1);
				}
				break;
			case ATTRIBUTE:
				enterOuterAlt(_localctx, 2);
				{
				setState(22);
				clauses();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\t\34\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\3\2\3\2\3\2\3\2\3\3\3\3\3\3\7\3\20\n\3\f\3\16\3\23\13\3\3"+
		"\4\3\4\3\4\3\4\3\4\5\4\32\n\4\3\4\2\2\5\2\4\6\2\2\32\2\b\3\2\2\2\4\f\3"+
		"\2\2\2\6\31\3\2\2\2\b\t\7\5\2\2\t\n\7\7\2\2\n\13\7\b\2\2\13\3\3\2\2\2"+
		"\f\21\5\2\2\2\r\16\7\6\2\2\16\20\5\2\2\2\17\r\3\2\2\2\20\23\3\2\2\2\21"+
		"\17\3\2\2\2\21\22\3\2\2\2\22\5\3\2\2\2\23\21\3\2\2\2\24\25\7\3\2\2\25"+
		"\26\5\4\3\2\26\27\7\4\2\2\27\32\3\2\2\2\30\32\5\4\3\2\31\24\3\2\2\2\31"+
		"\30\3\2\2\2\32\7\3\2\2\2\4\21\31";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}