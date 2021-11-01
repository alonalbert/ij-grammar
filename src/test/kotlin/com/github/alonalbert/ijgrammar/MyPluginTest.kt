package com.github.alonalbert.ijgrammar

import com.github.alonalbert.ijgrammar.logcatfilter.*
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

  fun testName() {
    val text = "tag: bar1 | app:foo1 | tag:bar2"
    val root = parse(text)

    val expressions = PsiTreeUtil.getChildrenOfType(root, LogcatFilterExpression::class.java)

    val filter = createFilter(expressions)

    println(filter)
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
    is LogcatFilterLiteralExpression -> {
      when (key.firstChild.elementType) {
        LogcatFilterTypes.TAG -> TagFilter(value.text)
        LogcatFilterTypes.APP -> AppFilter(value.text)
        else -> throw IllegalArgumentException()
      }
    }
    is LogcatFilterAndExpression -> AndFilter(expressionList.map { it.toFilter() })
    is LogcatFilterOrExpression -> OrFilter(expressionList.map { it.toFilter() })
    else -> throw IllegalArgumentException()
  }
}


private interface Filter

private class EmptyFilter : Filter
private data class TagFilter(val tag: String) : Filter
private data class AppFilter(val app: String) : Filter
private data class AndFilter(val filters: List<Filter>) : Filter
private data class OrFilter(val filters: List<Filter>) : Filter

