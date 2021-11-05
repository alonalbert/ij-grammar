package com.android.tools.idea.logcat.filters.parser

import java.io.CharArrayReader
import java.io.StringReader

fun main() {
  val lexer = TestLexer(
    StringReader(
      """
        If If_this_is_an_identifier > 0 Then read(b); Endif
        c := "If I were...";
        While While_this_is_also_an_identifier > 5 Do d := d + 1 Endwhile
      """
        .trimIndent()
    )
  )

  lexer.yylex()
}
