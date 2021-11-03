// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.android.tools.idea.logcat.filters.parser;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.android.tools.idea.logcat.filters.parser.*;
import com.intellij.psi.TokenType;

%%

%class LogcatFilterLexer
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
EQ = "="
GT = ">"
GTE = ">="
LT = "<"
LTE = "<="

UNQUOTED_VALUE      = ([^\s|&()] | "\\ ")+
SINGLE_QUOTED_VALUE = ' ([^'] | \\')* '
DOUBLE_QUOTED_VALUE = \" ([^'] | \\')* \"
VALUE               = {UNQUOTED_VALUE} | {SINGLE_QUOTED_VALUE} | {DOUBLE_QUOTED_VALUE}

STANDALONE_UNQUOTED_VALUE = [^\s:|&()<>=]+
STANDALONE_VALUE          = {STANDALONE_UNQUOTED_VALUE} | {SINGLE_QUOTED_VALUE} | {DOUBLE_QUOTED_VALUE}

KEY = "tag" | "app"

LEVEL = "level"
LEVEL_OP = {COLON} | {EQ} | {GT} | {GTE} | {LT} | {LTE}
LEVEL_VALUE
  = "V" | "VERBOSE"
  | "D" | "DEBUG"
  | "I" | "INFO"
  | "W" | "WARN"  | "WARNING"
  | "E" | "ERROR"
  | "A" | "ASSERT"

%state KEY
%state KEY_VALUE

%state LEVEL
%state LEVEL_VALUE

%%

<YYINITIAL> {
  {MINUS}? {KEY} {TILDE}?         { yybegin(KEY); return LogcatFilterTypes.KEY; }

  {LEVEL}                         { yybegin(LEVEL); return LogcatFilterTypes.LEVEL; }

  {STANDALONE_VALUE}              { return LogcatFilterTypes.VALUE; }

  {OR}                            { return LogcatFilterTypes.OR; }
  {AND}                           { return LogcatFilterTypes.AND; }
  {LPAREN}                        { return LogcatFilterTypes.LPAREN; }
  {RPAREN}                        { return LogcatFilterTypes.RPAREN; }
}

<KEY>        {COLON}              { yybegin(KEY_VALUE);   return LogcatFilterTypes.SEP; }
<KEY_VALUE>  {VALUE}              { yybegin(YYINITIAL); return LogcatFilterTypes.VALUE; }

<LEVEL>      {LEVEL_OP}           { yybegin(LEVEL_VALUE);   return LogcatFilterTypes.SEP; }
<LEVEL_VALUE>  {LEVEL_VALUE}      { yybegin(YYINITIAL); return LogcatFilterTypes.LEVEL_VALUE; }

{WHITE_SPACE}+                     { return TokenType.WHITE_SPACE; }
[^]                                { return TokenType.BAD_CHARACTER; }
