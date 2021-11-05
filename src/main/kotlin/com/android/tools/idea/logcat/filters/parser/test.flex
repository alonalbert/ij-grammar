package com.android.tools.idea.logcat.filters.parser;

%%
%class TestLexer
%type String

ID =      [a-zA-Z_][a-zA-Z0-9_]*
STRING =  \"(\\.|[^\\\"])*\"

%%

"If"                      { System.out.println("IF: " + yytext());             return "IF"; }
"Then"                    { System.out.println("THEN:  " + yytext());          return "THEN"; }
"Endif"                   { System.out.println("ENDIF:  " + yytext());         return "ENDIF"; }
"While"                   { System.out.println("WHILE:  " + yytext());         return "WHILE"; }
"Do"                      { System.out.println("DO:   " + yytext());           return "DO"; }
"EndWhile"                { System.out.println("ENDWHILE:  " + yytext());      return "ENDWHILE"; }
{STRING}                  { System.out.println("STRING:  " + yytext());        return "STRING"; }
{ID}                      { System.out.println("IDENTIFIER:  " + yytext());    return "IDENTIFIER"; }
.                         {                                                    return "Ignore token"; }



