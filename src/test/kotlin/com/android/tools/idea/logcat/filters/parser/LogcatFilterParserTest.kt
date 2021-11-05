@file:Suppress("UnstableApiUsage")

package com.android.tools.idea.logcat.filters.parser

import com.google.common.truth.Truth.assertThat
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.text.ParseException

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class LogcatFilterParserTest : BasePlatformTestCase() {

  fun testImplicitAnd() {
    val psi = parse("foo tag: bar app: foobar")

    assertThat(psi.toFilter()).isEqualTo(
      AndFilter(
        TopFilter("foo"),
        KeyFilter("tag", "bar"),
        KeyFilter("app", "foobar"),
      )
    )
  }

  fun testAnd() {
    val psi = parse("tag: bar & foo & app: foobar")

    assertThat(psi.toFilter()).isEqualTo(
      AndFilter(
        KeyFilter("tag", "bar"),
        TopFilter("foo"),
        KeyFilter("app", "foobar"),
      )
    )
  }

  fun testOr() {
    val psi = parse("tag: bar | foo | app: foobar")

    assertThat(psi.toFilter()).isEqualTo(
      OrFilter(
        KeyFilter("tag", "bar"),
        TopFilter("foo"),
        KeyFilter("app", "foobar"),
      )
    )
  }

  fun testOperatorPrecedence() {
    val psi = parse("f1 & f2 | f3 & f4")

    assertThat(psi.toFilter()).isEqualTo(
      OrFilter(
        AndFilter(
          TopFilter("f1"),
          TopFilter("f2"),
        ),
        AndFilter(
          TopFilter("f3"),
          TopFilter("f4"),
        ),
      )
    )
  }

  fun testParens() {
    val psi = parse("f1 & ( f2 | f3 ) & f4")

    assertThat(psi.toFilter()).isEqualTo(
      AndFilter(
        TopFilter("f1"),
        OrFilter(
          TopFilter("f2"),
          TopFilter("f3"),
        ),
        TopFilter("f4"),
      )
    )
  }

  fun testParensWithKey() {
    val psi = parse("f1 & (tag: foo | app: 'bar') & f4")

    assertThat(psi.toFilter()).isEqualTo(
      AndFilter(
        TopFilter("f1"),
        OrFilter(
          KeyFilter("tag", "foo"),
          KeyFilter("app", "bar"),
        ),
        TopFilter("f4"),
      )
    )
  }

  fun testLevel() {
    val psi = parse("level: V | fromLevel: DEBUG")

    assertThat(psi.toFilter()).isEqualTo(
      OrFilter(
        KeyFilter("level", "V"),
        KeyFilter("fromLevel", "DEBUG"),
      )
    )
  }

  fun test() {
    val lexer = TestLexer(null)
    val text = "If If_this_is_an_identifier"
    lexer.reset(text, 0, text.length, 0)

    while (true) {
      lexer.yylex() ?: break
    }
  }

  fun test2() {
    val lexer = LogcatFilterLexer(null)
    val text = "level: V | fromLevel: DEBUG"
    lexer.reset(text, 0, text.length, 0)

    while (true) {
      val element = lexer.advance() ?: break
      println("""${element.debugName}: "${lexer.yytext()}"""")
    }
  }

  private fun parse(text: String): PsiFile {
    val psi = PsiFileFactory.getInstance(project).createFileFromText("temp.simple", LogcatFilterFileType.INSTANCE, text)
    if (PsiTreeUtil.hasErrorElements(psi)) {
      val errorElement = PsiTreeUtil.findChildOfType(psi, PsiErrorElement::class.java) as PsiErrorElement
      throw IllegalArgumentException(errorElement.errorDescription)
    }
    return psi
  }
}

private fun PsiFile.toFilter(): Filter {
  val expressions = PsiTreeUtil.getChildrenOfType(this, LogcatFilterExpression::class.java)

  val filters = expressions?.map { it.toFilter() }
  return when {
    filters == null -> EmptyFilter()
    filters.size == 1 -> filters[0]
    else -> AndFilter(filters)
  }

}

private fun LogcatFilterExpression.toFilter(): Filter {
  return when (this) {
    is LogcatFilterLiteralExpression -> {
      this.literalToFilter()
    }
    is LogcatFilterParenExpression -> expression!!.toFilter()
    is LogcatFilterAndExpression -> AndFilter(flattenAndExpression(this).map { it.toFilter() })
    is LogcatFilterOrExpression -> OrFilter(flattenOrExpression(this).map { it.toFilter() })
    else -> throw ParseException("Unexpected element: ${this::class.simpleName}", -1)
  }
}

private fun LogcatFilterLiteralExpression.literalToFilter() =
  when (firstChild.elementType) {
    LogcatFilterTypes.VALUE -> TopFilter(firstChild.toText())
    LogcatFilterTypes.KEY, LogcatFilterTypes.LEVEL_KEY -> KeyFilter(this)
    else -> throw ParseException("Unexpected elementType: $firstChild.elementType", -1)
  }

private fun PsiElement.toText(): String {
  return when (elementType) {
    LogcatFilterTypes.VALUE -> {
      when (text.first()) {
        '\'' -> text.substring(1, textLength - 1).replace("\\'", "'")
        '"' -> text.substring(1, textLength - 1).replace("\\\"", "\"")
        else -> text.replace("\\ ", " ")
      }
    }
    else -> {
      text
    }
  }
}

private interface Filter

private class EmptyFilter : Filter

private data class TopFilter(val text: String) : Filter

private data class KeyFilter(
  val key: String,
  val text: String,
  val isNegated: Boolean = false,
  val isRegex: Boolean = false
) : Filter {
  constructor(element: PsiElement) : this(
    element.firstChild.text.trimEnd(':'),
    element.lastChild.toText(),
    element.firstChild.text.startsWith('-'),
    element.firstChild.text.endsWith("~:")
  )
}

private data class AndFilter(val filters: List<Filter>) : Filter {
  constructor(vararg filters: Filter) : this(filters.asList())
}

private data class OrFilter(val filters: List<Filter>) : Filter {
  constructor(vararg filters: Filter) : this(filters.asList())
}

private fun flattenOrExpression(expression: LogcatFilterExpression): List<LogcatFilterExpression> =
  if (expression is LogcatFilterOrExpression) {
    flattenOrExpression(expression.expressionList[0]) + flattenOrExpression(expression.expressionList[1])
  } else {
    listOf(expression)
  }

private fun flattenAndExpression(expression: LogcatFilterExpression): List<LogcatFilterExpression> =
  if (expression is LogcatFilterAndExpression) {
    flattenAndExpression(expression.expressionList[0]) + flattenAndExpression(expression.expressionList[1])
  } else {
    listOf(expression)
  }
