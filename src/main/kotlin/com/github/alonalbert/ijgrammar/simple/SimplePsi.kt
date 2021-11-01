package com.github.alonalbert.ijgrammar.simple

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

internal class SimpleLanguage : Language("Simple") {
  companion object {
    @JvmField
    val INSTANCE = SimpleLanguage()
  }
}

internal class SimpleFileType private constructor() : LanguageFileType(SimpleLanguage.INSTANCE) {
  override fun getName() = "Simple File"
  override fun getDescription() = "Simple language file"
  override fun getDefaultExtension() = "simple"
  override fun getIcon(): Icon = IconLoader.getIcon("/icon.svg", javaClass)

  companion object {
    @JvmField
    val INSTANCE = SimpleFileType()
  }
}

internal class SimpleTokenType(debugName: String) : IElementType(debugName, SimpleLanguage.INSTANCE) {
  override fun toString() = "SimpleTokenType.${super.toString()}"
}

internal class SimpleElementType(debugName: String) : IElementType(debugName, SimpleLanguage.INSTANCE)

internal class SimpleLexerAdapter : FlexAdapter(SimpleLexer(null))

internal class SimpleFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SimpleLanguage.INSTANCE) {
  override fun getFileType() = SimpleFileType.INSTANCE
  override fun toString() = "Simple File"
}

internal class SimpleParserDefinition : ParserDefinition {
  override fun createLexer(project: Project) = SimpleLexerAdapter()

  override fun createParser(project: Project) = SimpleParser()

  override fun getFileNodeType() = FILE

  override fun getCommentTokens(): TokenSet = TokenSet.EMPTY

  override fun getWhitespaceTokens() = WHITE_SPACES

  override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

  override fun createElement(node: ASTNode): PsiElement = SimpleTypes.Factory.createElement(node)

  override fun createFile(viewProvider: FileViewProvider) = SimpleFile(viewProvider)

  override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?) = ParserDefinition.SpaceRequirements.MAY

  companion object {
    val FILE = IFileElementType(SimpleLanguage.INSTANCE)
    val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
  }
}
