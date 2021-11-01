package com.github.alonalbert.ijgrammar

import com.github.alonalbert.ijgrammar.simple.SimpleFileType
import com.github.alonalbert.ijgrammar.simple.SimpleLanguage
import com.github.alonalbert.ijgrammar.simple.SimpleParserDefinition
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.hasErrorElementInRange
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

private val simpleParserDefinition = SimpleParserDefinition()

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

  fun testName() {
    val text = "foo: bar"
    val psiFile = parse(text)
    println(psiFile.firstChild)
  }

  @Suppress("SameParameterValue")
  private fun parse(text: String): PsiFile =
    PsiFileFactory.getInstance(project).createFileFromText("temp.simple", SimpleFileType.INSTANCE, text)
}
