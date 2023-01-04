package com.mcpp.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.util.io.isDirectory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CreateProjectAction : AnAction(){
    private val cache_dir = System.getProperty("user.home") + "/.cache/mcpp"
    override fun actionPerformed(e: AnActionEvent) {
        val m_dialog  = MCppDialogWrapper()
        if(m_dialog.showAndGet()){
            Files.createDirectories(Paths.get(cache_dir))
            e.project?.let {
                runInBackgroundWithProgress(it, "Cloning Template", true ){ indicator ->
                    indicator.fraction = 0.5
                    m_dialog.selectedTemplate?.let { cloneTemplate(it, Paths.get(cache_dir)) }
                }
            }
        }
    }
    fun runInBackgroundWithProgress(project: Project, title: String, canBeCancelled: Boolean, func: (ProgressIndicator) -> Unit){
        object : Task.Backgroundable(project, title, canBeCancelled) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                func(indicator)
                indicator.fraction = 1.0
            }
        }.queue()
    }
    fun cloneTemplate(template: String, path: Path){
        if(!path.isDirectory()) return
        val start_of_name: Int = template.lastIndexOf('/')
        val end_of_name: Int = template.lastIndexOf('.')
        val name: String = template.substring(start_of_name, end_of_name)
        Files.createDirectories(path)
        val full_path: File = File( path.toString() + "/" + name)
        if(full_path.exists()){
            TODO("1. Copy the sources to a new location \n" +
                    "2. Delete .git and open the project \n")

        }else{
            try{
                Git.cloneRepository()
                    .setURI(template)
                    .setDirectory(full_path)
                    .setCloneAllBranches(true)
                    .setCloneSubmodules(true)
                    .call()
            }catch (e: GitAPIException){
                println(e.message)
            }
        }
    }


}

