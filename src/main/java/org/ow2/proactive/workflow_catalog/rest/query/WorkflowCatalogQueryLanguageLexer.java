// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class WorkflowCatalogQueryLanguageLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, ATTRIBUTE=3, CONJUNCTION=4, OPERATOR=5, VALUE=6, WHITESPACE=7;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "ATTRIBUTE", "CONJUNCTION", "OPERATOR", "VALUE", "WHITESPACE"
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


	public WorkflowCatalogQueryLanguageLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "WorkflowCatalogQueryLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\tF\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\3\2\3\2\3\3\3\3\3\4\6\4"+
		"\27\n\4\r\4\16\4\30\3\4\6\4\34\n\4\r\4\16\4\35\3\4\7\4!\n\4\f\4\16\4$"+
		"\13\4\7\4&\n\4\f\4\16\4)\13\4\3\5\3\5\3\5\3\5\3\5\5\5\60\n\5\3\6\3\6\3"+
		"\6\5\6\65\n\6\3\7\3\7\7\79\n\7\f\7\16\7<\13\7\3\7\3\7\3\b\6\bA\n\b\r\b"+
		"\16\bB\3\b\3\b\2\2\t\3\3\5\4\7\5\t\6\13\7\r\b\17\t\3\2\7\4\2C\\c|\4\2"+
		"\60\60aa\5\2\62;C\\c|\3\2\13\f\5\2\13\f\17\17\"\"M\2\3\3\2\2\2\2\5\3\2"+
		"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\3\21"+
		"\3\2\2\2\5\23\3\2\2\2\7\26\3\2\2\2\t/\3\2\2\2\13\64\3\2\2\2\r\66\3\2\2"+
		"\2\17@\3\2\2\2\21\22\7*\2\2\22\4\3\2\2\2\23\24\7+\2\2\24\6\3\2\2\2\25"+
		"\27\t\2\2\2\26\25\3\2\2\2\27\30\3\2\2\2\30\26\3\2\2\2\30\31\3\2\2\2\31"+
		"\'\3\2\2\2\32\34\t\3\2\2\33\32\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35"+
		"\36\3\2\2\2\36\"\3\2\2\2\37!\t\4\2\2 \37\3\2\2\2!$\3\2\2\2\" \3\2\2\2"+
		"\"#\3\2\2\2#&\3\2\2\2$\"\3\2\2\2%\33\3\2\2\2&)\3\2\2\2\'%\3\2\2\2\'(\3"+
		"\2\2\2(\b\3\2\2\2)\'\3\2\2\2*+\7C\2\2+,\7P\2\2,\60\7F\2\2-.\7Q\2\2.\60"+
		"\7T\2\2/*\3\2\2\2/-\3\2\2\2\60\n\3\2\2\2\61\62\7#\2\2\62\65\7?\2\2\63"+
		"\65\7?\2\2\64\61\3\2\2\2\64\63\3\2\2\2\65\f\3\2\2\2\66:\7$\2\2\679\n\5"+
		"\2\28\67\3\2\2\29<\3\2\2\2:8\3\2\2\2:;\3\2\2\2;=\3\2\2\2<:\3\2\2\2=>\7"+
		"$\2\2>\16\3\2\2\2?A\t\6\2\2@?\3\2\2\2AB\3\2\2\2B@\3\2\2\2BC\3\2\2\2CD"+
		"\3\2\2\2DE\b\b\2\2E\20\3\2\2\2\r\2\26\30\35 \"\'/\64:B\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}