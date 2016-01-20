grammar WorkflowCatalogQueryLanguage;

@header {
   package org.ow2.proactive.workflow_catalog.rest.query;
}

clause: ATTRIBUTE OPERATOR VALUE ;
clauses: clause ( CONJUNCTION clause )* ;
statement: '(' clauses ')' | clauses ;

ATTRIBUTE: ([a-z] | [A-Z])+ ([_.]+ ([a-z] | [A-Z] | [0-9])*)* ;
CONJUNCTION: 'AND' | 'OR' ;
OPERATOR: '!=' | '=' ;
VALUE: '"' (~[\t\n])* '"' ;

WHITESPACE: [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
