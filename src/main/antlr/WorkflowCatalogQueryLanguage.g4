grammar WorkflowCatalogQueryLanguage;

@header {
   package org.ow2.proactive.workflow_catalog.rest.query.parser;
}

// PARSER

start // start rule, begin parsing here
    : expression
    ;

expression // ANTLR resolves ambiguities in favor of the alternative given first
    : expression AND expression  #andExpression // match subexpressions joined with AND
    | expression OR expression #orExpression // match subexpressions joined with OR
    | clause #clauseExpression
    // | LPAREN expression RPAREN #parenthesedExpression
    ;

clause
    : AttributeLiteral COMPARE_OPERATOR StringLiteral #atomicClause
    | AttributeLiteral LPAREN StringLiteral PAIR_SEPARATOR StringLiteral RPAREN #keyValueClause
    ;

// LEXER

AND                 : 'AND' ;
OR                  : 'OR' ;
COMPARE_OPERATOR    : '!=' | '=' ;
LPAREN              : '(' ;
RPAREN              : ')' ;
PAIR_SEPARATOR      : ',' ;

AttributeLiteral
    : ID_LETTER (ID_LETTER | DIGIT)*
    ;

StringLiteral
    : '"' ( ESC | . )*? '"'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

fragment ID_LETTER  : [a-z]|[A-Z]|'_' ;
fragment DIGIT      : [0-9];
fragment ESC        : '\\"' | '\\\\' | '\\%'; // 2-char sequences \" and \\
fragment LETTER     : LOWERCASE | UPPERCASE;
fragment LOWERCASE  : [a-z];
fragment UPPERCASE  : [A-Z];
