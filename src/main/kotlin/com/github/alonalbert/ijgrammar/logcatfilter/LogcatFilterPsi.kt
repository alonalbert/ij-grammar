package com.github.alonalbert.ijgrammar.logcatfilter

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import javax.swing.Icon

internal class LogcatFilterLanguage : Language("LogcatFilter") {
  companion object {
    @JvmField
    val INSTANCE = LogcatFilterLanguage()
  }
}

internal class LogcatFilterFileType private constructor() : LanguageFileType(LogcatFilterLanguage.INSTANCE) {
  override fun getName() = "LogcatFilter File"
  override fun getDescription() = "LogcatFilter language file"
  override fun getDefaultExtension() = "lcf"
  override fun getIcon(): Icon = IconLoader.getIcon("/icon.svg", javaClass)

  companion object {
    @JvmField
    val INSTANCE = LogcatFilterFileType()
  }
}

internal class LogcatFilterTokenType(debugName: String) : IElementType(debugName, LogcatFilterLanguage.INSTANCE) {
  override fun toString() = "LogcatFilterTokenType.${super.toString()}"
}

internal class LogcatFilterElementType(debugName: String) : IElementType(debugName, LogcatFilterLanguage.INSTANCE)

internal class LogcatFilterLexerAdapter : FlexAdapter(_LogcatFilterLexer(null))

internal class LogcatFilterFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LogcatFilterLanguage.INSTANCE) {
  override fun getFileType() = LogcatFilterFileType.INSTANCE
  override fun toString() = "LogcatFilter File"
}

internal class LogcatFilterParserDefinition : ParserDefinition {
  override fun createLexer(project: Project) = LogcatFilterLexerAdapter()

  override fun createParser(project: Project) = LogcatFilterParser()

  override fun getFileNodeType() = FILE

  override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

  override fun getWhitespaceTokens() = WHITE_SPACES

  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createElement(node: ASTNode): PsiElement = LogcatFilterTypes.Factory.createElement(node)

  override fun createFile(viewProvider: FileViewProvider) = LogcatFilterFile(viewProvider)

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = ParserDefinition.SpaceRequirements.MAY

  companion object {
    val FILE = IFileElementType(LogcatFilterLanguage.INSTANCE)
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  }
}
