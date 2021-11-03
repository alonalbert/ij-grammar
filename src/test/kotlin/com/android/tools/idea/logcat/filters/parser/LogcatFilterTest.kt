package com.android.tools.idea.logcat.filters.parser

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilCore
import com.intellij.psi.util.elementType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class LogcatFilterParserTest : BasePlatformTestCase() {

  fun testName() {
    val text =
      """
        tag:bar & tag:foo
      """.trim()
    val root = parse(text)


    root.children.forEach { item ->
      println(item.text)
      item.getAllChildren().forEach { child ->
        println("   $child: '${child.toText()}'")
      }
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

private fun PsiElement.getAllChildren(): Array<out PsiElement> {
  var psiChild: PsiElement? = firstChild ?: return PsiElement.EMPTY_ARRAY

  val result: MutableList<PsiElement> = ArrayList()
  while (psiChild != null) {
    result.add(psiChild)
    psiChild = psiChild.nextSibling
  }
  return PsiUtilCore.toPsiElementArray(result)
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
