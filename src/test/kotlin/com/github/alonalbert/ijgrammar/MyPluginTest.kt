package com.github.alonalbert.ijgrammar

import com.github.alonalbert.ijgrammar.logcatfilter.*
import com.google.common.truth.Truth.assertThat
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.thoughtworks.qdox.model.expression.Not

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

  fun testName() {
    val text = "tag~: 1 & -tag:2 & tag:3 | -app~: 1 & app:2 & app:3 | tag: 10 & tag:20 & tag:30"
    val root = parse(text)

    val expressions = PsiTreeUtil.getChildrenOfType(root, LogcatFilterExpression::class.java)

    val filter = createFilter(expressions)

    assertThat(filter).isEqualTo(
      OrFilter(
        AndFilter(
          TagRegexFilter("1"),
          NotFilter(TagFilter("2")),
          TagFilter("3"),
        ),
        AndFilter(
          NotFilter(AppRegexFilter("1")),
          AppFilter("2"),
          AppFilter("3"),
        ),
        AndFilter(
          TagFilter("10"),
          TagFilter("20"),
          TagFilter("30"),
        ),
      )
    )
  }

  private fun createFilter(expressions: Array<LogcatFilterExpression>?): Filter {
    val filters = expressions?.map { it.toFilter() }
    return when {
      filters == null -> EmptyFilter()
      filters.size == 1 -> filters[0]
      else -> AndFilter(filters)
    }
  }

  @Suppress("SameParameterValue")
  private fun parse(text: String): PsiFile {
    val psiFile =
      PsiFileFactory.getInstance(project).createFileFromText("temp.simple", LogcatFilterFileType.INSTANCE, text)
    if (PsiTreeUtil.hasErrorElements(psiFile)) {
      val errorElement = PsiTreeUtil.findChildOfType(psiFile, PsiErrorElement::class.java) as PsiErrorElement
      throw IllegalArgumentException(errorElement.errorDescription)
    }
    return psiFile
  }
}

private fun LogcatFilterExpression.toFilter(): Filter {
  return when (this) {
    is LogcatFilterLiteralExpression -> when (key.firstChild.elementType) {
      LogcatFilterTypes.TAG -> TagFilter(value.text)
      LogcatFilterTypes.RTAG -> TagRegexFilter(value.text)
      LogcatFilterTypes.NTAG -> NotFilter(TagFilter(value.text))
      LogcatFilterTypes.NRTAG -> NotFilter(TagRegexFilter(value.text))
      LogcatFilterTypes.APP -> AppFilter(value.text)
      LogcatFilterTypes.RAPP -> AppRegexFilter(value.text)
      LogcatFilterTypes.NAPP -> NotFilter(AppFilter(value.text))
      LogcatFilterTypes.NRAPP -> NotFilter(AppRegexFilter(value.text))
      else -> throw IllegalArgumentException()
    }
    is LogcatFilterAndExpression -> AndFilter(flattenAndExpression(this).map { it.toFilter() })
    is LogcatFilterOrExpression -> OrFilter(flattenOrExpression(this).map { it.toFilter() })
    else -> throw IllegalArgumentException()
  }
}

private interface Filter

private class EmptyFilter : Filter
private data class TagFilter(val tag: String) : Filter
private data class TagRegexFilter(val tag: String) : Filter
private data class AppFilter(val app: String) : Filter
private data class AppRegexFilter(val app: String) : Filter
private data class NotFilter(val filter: Filter) : Filter
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
    flattenAndExpression(expression.expressionList[0]) + flattenOrExpression(expression.expressionList[1])
  } else {
    listOf(expression)
  }

