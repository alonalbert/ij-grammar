package com.github.alonalbert.ijgrammar.logcatfilter;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.github.alonalbert.ijgrammar.logcatfilter.LogcatFilterTypes.*;

%%

%{
  public _LogcatFilterLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _LogcatFilterLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

EOL=\R
WHITE_SPACE=\s+

SPACE=[ \t\n\x0B\f\r]+
LITERALTEXT=[^ |&():\t'\"-]+
QUOTEDTEXT=('([^'\\]|\\.)*'|\"([^\"\\]|\\.)*\")

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return WHITE_SPACE; }

  "&"                { return AND; }
  "|"                { return OR; }
  "("                { return LP; }
  ")"                { return RP; }
  ":"                { return COLLON; }
  "-"                { return MINUS; }
  "tag:"             { return TAG; }
  "-tag:"            { return NTAG; }
  "tag~:"            { return RTAG; }
  "-tag~:"           { return NRTAG; }
  "app:"             { return APP; }
  "-app:"            { return NAPP; }
  "app~:"            { return RAPP; }
  "-app~:"           { return NRAPP; }

  {SPACE}            { return SPACE; }
  {LITERALTEXT}      { return LITERALTEXT; }
  {QUOTEDTEXT}       { return QUOTEDTEXT; }

}

[^] { return BAD_CHARACTER; }
