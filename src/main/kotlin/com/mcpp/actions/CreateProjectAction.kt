package com.mcpp.actions

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.util.io.isDirectory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

/**
 * Create project action
 *
 * @constructor Create empty Create project action
 */
class CreateProjectAction : AnAction() {
    private val cache_dir = System.getProperty("user.home") + "/.cache/mcpp"
    override fun actionPerformed(e: AnActionEvent) {
        val m_dialog = MCppDialogWrapper()
        var clone_flag: Boolean = false
        if (m_dialog.showAndGet()) {
            Files.createDirectories(Paths.get(cache_dir))
            e.project?.let {
                runInBackgroundWithProgress(it, "Cloning Template", true) { indicator ->
                    indicator.fraction = 0.5
                    clone_flag = m_dialog.selectedTemplate?.let { cloneTemplate(it, Paths.get(cache_dir)) } == true
                }
            }
        }
        ApplicationManager.getApplication().invokeLater {
            val src: String = cache_dir + "/" + m_dialog.selectedTemplate?.let { extractRemoteRepo(it) }
            val dest: String = m_dialog.location_info_label.text
            try {
                // create directory in the project location
                Files.createDirectories(Paths.get(dest))
                if (clone_flag) {
                    copyDir(Paths.get(src), Paths.get(dest))
                    deleteDir(Paths.get("$dest/.git/"))
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
     * Extract remote repo
     *
     * @param repo_uri
     * @return
     */
    fun extractRemoteRepo(repo_uri: String): String {
        try {
            val start_of_name: Int = repo_uri.lastIndexOf('/')
            val end_of_name: Int = repo_uri.lastIndexOf('.')
            val name: String = repo_uri.substring(start_of_name, end_of_name)
            return name
        } catch (e: StringIndexOutOfBoundsException) {
            Messages.showErrorDialog("Bad template", "Error")
        }
        return ""
    }

    /**
     * Clone template
     *
     * @param template
     * @param path
     * @return
     */
    fun cloneTemplate(template: String, path: Path): Boolean {
        if (!path.isDirectory()) return false
        val name: String = extractRemoteRepo(template)
        Files.createDirectories(path)
        val full_path: File = File("$path/$name")
        if (full_path.exists()) {
            return true
        } else {
            try {
                Git.cloneRepository()
                    .setURI(template)
                    .setDirectory(full_path)
                    .setCloneAllBranches(true)
                    .setCloneSubmodules(true)
                    .call()
            } catch (e: GitAPIException) {
                println(e.message)
                return false
            }
        }
        return true
    }

    /**
     * Copy dir
     *
     * @param src
     * @param dest
     */
    fun copyDir(src: Path, dest: Path) {
        Files.walkFileTree(src, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                if (file != null) {
                    Files.copy(file, dest.resolve(src.relativize(file)))
                }
                return FileVisitResult.CONTINUE
            }

            override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                dir?.let { src.relativize(it) }?.let { dest.resolve(it) }?.let { Files.createDirectories(it) }
                return FileVisitResult.CONTINUE
            }
        })
    }

    /**
     * Delete dir
     *
     * @param path
     */
    fun deleteDir(path: Path) {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                if (file != null) {
                    Files.delete(file)
                }
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path?, exc: IOException?): FileVisitResult {
                if (dir != null) {
                    Files.delete(dir)
                }
                return FileVisitResult.CONTINUE
            }
        })
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

