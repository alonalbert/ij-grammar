// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.alonalbert.ijgrammar.simple;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.alonalbert.ijgrammar.simple.*;
import com.intellij.psi.TokenType;

%%

%class SimpleLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType

WHITE_SPACE=\s

COLON = ":"
MINUS = "-"
TILDE = "~"
OR = "|"
AND = "&"
LPAREN = "("
RPAREN = ")"

UNQUOTED_VALUE      = (\S | "\\ ")+
SINGLE_QUOTED_VALUE = ' ([^'] | \\')* '
DOUBLE_QUOTED_VALUE = \" ([^'] | \\')* \"
VALUE               = {UNQUOTED_VALUE} | {SINGLE_QUOTED_VALUE} | {DOUBLE_QUOTED_VALUE}

STANDALONE_UNQUOTED_VALUE = [^\s:|&()]+
STANDALONE_VALUE          = {STANDALONE_UNQUOTED_VALUE} | {SINGLE_QUOTED_VALUE} | {DOUBLE_QUOTED_VALUE}

KEY = "tag" | "app"

%state HAS_KEY

%%

<YYINITIAL> {
  {MINUS}? {KEY} {TILDE}?         { return SimpleTypes.KEY; }
  {STANDALONE_VALUE}              { return SimpleTypes.VALUE; }
  {COLON}                         { yybegin(HAS_KEY);   return SimpleTypes.COLON; }
  {OR}                            { return SimpleTypes.OR; }
  {AND}                           { return SimpleTypes.AND; }
  {LPAREN}                        { return SimpleTypes.LPAREN; }
  {RPAREN}                        { return SimpleTypes.RPAREN; }
}

<HAS_KEY> {
  {VALUE}                          { yybegin(YYINITIAL); return SimpleTypes.VALUE; }
}

{WHITE_SPACE}+                     { return TokenType.WHITE_SPACE; }
[^]                                { return TokenType.BAD_CHARACTER; }
