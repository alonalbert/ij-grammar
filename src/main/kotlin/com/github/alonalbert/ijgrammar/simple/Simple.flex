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

SEPARATOR = ":"
MINUS = "-"
TILDE = "~"
OR = "|"

VALUE_CHAR=\S | "\\ "
SINGLE_QUOTED_VALUE_CHAR=[^'] | \\'
KEY_TAG = "tag" | "app"

%state WAITING_VALUE

%%

<YYINITIAL> {MINUS}? {KEY_TAG} {TILDE}?                     { yybegin(YYINITIAL);     return SimpleTypes.KEY; }
<YYINITIAL> {SEPARATOR}                                     { yybegin(WAITING_VALUE); return SimpleTypes.SEPARATOR; }
<YYINITIAL> {OR}                                            { yybegin(YYINITIAL);     return SimpleTypes.OR; }
<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return TokenType.WHITE_SPACE; }
<WAITING_VALUE> {VALUE_CHAR}+                               { yybegin(YYINITIAL);     return SimpleTypes.VALUE; }
<WAITING_VALUE> ' {SINGLE_QUOTED_VALUE_CHAR}+ '             { yybegin(YYINITIAL);     return SimpleTypes.VALUE; }

{WHITE_SPACE}+                                              { yybegin(YYINITIAL);     return TokenType.WHITE_SPACE; }

[^]                                                         {                         return TokenType.BAD_CHARACTER; }
