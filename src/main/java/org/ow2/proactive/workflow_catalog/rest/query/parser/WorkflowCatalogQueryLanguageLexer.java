// Generated from WorkflowCatalogQueryLanguage.g4 by ANTLR 4.5.1

   package org.ow2.proactive.workflow_catalog.rest.query.parser;

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
		AND=1, OR=2, COMPARE_OPERATOR=3, LPAREN=4, RPAREN=5, StringLiteral=6, 
		AttributeLiteral=7, WS=8;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"AND", "OR", "COMPARE_OPERATOR", "LPAREN", "RPAREN", "StringLiteral", 
		"AttributeLiteral", "WS", "DIGIT", "LETTER", "LOWERCASE", "UPPERCASE"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, "'('", "')'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "AND", "OR", "COMPARE_OPERATOR", "LPAREN", "RPAREN", "StringLiteral", 
		"AttributeLiteral", "WS"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\nY\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\3\2\3\2\3\2\3\2\3\2\5\2!\n\2\3\3\3\3\3\3\3\3\5\3\'"+
		"\n\3\3\4\3\4\3\4\5\4,\n\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\5\7\67\n"+
		"\7\7\79\n\7\f\7\16\7<\13\7\3\7\3\7\3\b\3\b\3\b\3\b\7\bD\n\b\f\b\16\bG"+
		"\13\b\3\t\6\tJ\n\t\r\t\16\tK\3\t\3\t\3\n\3\n\3\13\3\13\5\13T\n\13\3\f"+
		"\3\f\3\r\3\r\2\2\16\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2\27\2"+
		"\31\2\3\2\b\6\2\f\f\17\17$$^^\4\2\60\60aa\5\2\13\f\17\17\"\"\3\2\62;\3"+
		"\2c|\3\2C\\_\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\3 \3\2\2\2\5&\3\2\2\2\7+\3"+
		"\2\2\2\t-\3\2\2\2\13/\3\2\2\2\r\61\3\2\2\2\17?\3\2\2\2\21I\3\2\2\2\23"+
		"O\3\2\2\2\25S\3\2\2\2\27U\3\2\2\2\31W\3\2\2\2\33\34\7C\2\2\34\35\7P\2"+
		"\2\35!\7F\2\2\36\37\7(\2\2\37!\7(\2\2 \33\3\2\2\2 \36\3\2\2\2!\4\3\2\2"+
		"\2\"#\7Q\2\2#\'\7T\2\2$%\7~\2\2%\'\7~\2\2&\"\3\2\2\2&$\3\2\2\2\'\6\3\2"+
		"\2\2()\7#\2\2),\7?\2\2*,\7?\2\2+(\3\2\2\2+*\3\2\2\2,\b\3\2\2\2-.\7*\2"+
		"\2.\n\3\2\2\2/\60\7+\2\2\60\f\3\2\2\2\61:\7$\2\2\629\n\2\2\2\63\66\7^"+
		"\2\2\64\67\13\2\2\2\65\67\7\2\2\3\66\64\3\2\2\2\66\65\3\2\2\2\679\3\2"+
		"\2\28\62\3\2\2\28\63\3\2\2\29<\3\2\2\2:8\3\2\2\2:;\3\2\2\2;=\3\2\2\2<"+
		":\3\2\2\2=>\7$\2\2>\16\3\2\2\2?E\5\25\13\2@D\5\25\13\2AD\5\23\n\2BD\t"+
		"\3\2\2C@\3\2\2\2CA\3\2\2\2CB\3\2\2\2DG\3\2\2\2EC\3\2\2\2EF\3\2\2\2F\20"+
		"\3\2\2\2GE\3\2\2\2HJ\t\4\2\2IH\3\2\2\2JK\3\2\2\2KI\3\2\2\2KL\3\2\2\2L"+
		"M\3\2\2\2MN\b\t\2\2N\22\3\2\2\2OP\t\5\2\2P\24\3\2\2\2QT\5\27\f\2RT\5\31"+
		"\r\2SQ\3\2\2\2SR\3\2\2\2T\26\3\2\2\2UV\t\6\2\2V\30\3\2\2\2WX\t\7\2\2X"+
		"\32\3\2\2\2\r\2 &+\668:CEKS\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}