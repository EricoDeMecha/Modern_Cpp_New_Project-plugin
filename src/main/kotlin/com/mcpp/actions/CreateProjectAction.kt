package com.mcpp.actions

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.util.concurrent.CountDownLatch

/**
 * Create project action
 *
 * @constructor Create empty Create project action
 */
class CreateProjectAction : AnAction() {
    private val cache_dir = System.getProperty("user.home") + "/.cache/mcpp"
    override fun actionPerformed(e: AnActionEvent) {
        val m_dialog = MCppDialogWrapper()
        lateinit var templateCache: TemplateCache
        val latch = CountDownLatch(1) /* avoid race condition templateCache*/
        if (m_dialog.showAndGet()) {
            Files.createDirectories(Paths.get(cache_dir))
            e.project?.let {
                runInBackgroundWithProgress(it, "Cloning Template", true) { indicator ->
                    indicator.fraction = 0.5
                    templateCache = m_dialog.selectedTemplate?.let { it1 -> TemplateCache(it1, cache_dir, indicator) }!!
                    latch.countDown()
                }
            }
        }
        latch.await()
        ApplicationManager.getApplication().invokeLater {
            val src: String? = templateCache.getTemplatePath()
            val dest: String = m_dialog.location_info_label.text
            try {
                // create directory in the project location
                Files.createDirectories(Paths.get(dest))
                if (templateCache.isSuccess()) {
                    src?.let { Paths.get(it) }?.let { templateCache.copyDir(it, Paths.get(dest)) }
                    templateCache.deleteDir(Paths.get("$dest/.git/"))
                    openProject(dest)
                }
            } catch (e: IOException) {
                println(e.message)
            }
        }
    }

    /**
     * Run in background with progress
     *
     * @param project
     * @param title
     * @param canBeCancelled
     * @param func
     * @receiver
     */
    fun runInBackgroundWithProgress(
        project: Project,
        title: String,
        canBeCancelled: Boolean,
        func: (ProgressIndicator) -> Unit
    ) {
        object : Task.Backgroundable(project, title, canBeCancelled) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = false
                func(indicator)
                indicator.fraction = 1.0
            }
        }.queue()
    }

    /**
     * Open project
     *
     * @param folderPath
     */
    fun openProject(folderPath: String) {
        ApplicationManager.getApplication().invokeLater {
            val file = File(folderPath)
            ProjectUtil.openOrImport(file.toPath(), null, false)
        }
    }
}

