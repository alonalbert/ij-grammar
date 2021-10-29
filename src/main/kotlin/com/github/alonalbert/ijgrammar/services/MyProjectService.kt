package com.github.alonalbert.ijgrammar.services

import com.intellij.openapi.project.Project
import com.github.alonalbert.ijgrammar.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
