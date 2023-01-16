package com.mcpp.actions

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProgressIndicator
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import ru.nsk.kstatemachine.*
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

object SwitchEvent : Event

/**
 * States
 *
 * @constructor Create empty States
 */
sealed class States : DefaultState() {
    object ExtractedRepo : States()
    object ValidRepo : States()
    object UpdatedRepo : States(), FinalState
}

/**
 * Template cache
 *
 * @property git_repo
 * @property path
 * @constructor Create empty Template cache
 */
class TemplateCache(val git_repo: String, val path: Path, indicator: ProgressIndicator) {
    private var repo_name: String? = null
    private var full_path: String? = null
    private var is_valid = false

    constructor(git_repo: String, path: String, indicator: ProgressIndicator) : this(git_repo, Paths.get(path), indicator)

    private val machine = createStateMachine {
        var is_success = false
        addInitialState(States.ExtractedRepo) {
            onEntry {
                // extract  repo name from the string path
                repo_name = extractRemoteRepo(git_repo)
            }
            onExit {
                if (repo_name == null) {
//                    println("Invalid git repo")
                    indicator.thisLogger().error("Invalid git repo")
                }
            }
            transition<SwitchEvent> {
                targetState = States.ValidRepo
                onTriggered {
//                    println("Validating repo...")
                    indicator.thisLogger().info("Validating repo...")
                    indicator.fraction  = 0.3
                }
            }
        }
        addState(States.ValidRepo) {
            onEntry {
                full_path = "$path$repo_name"
                if (!full_path?.let { it1 -> File(it1).exists() }!!) {
                    full_path?.let { it1 -> Paths.get(it1) }?.let { it2 -> Files.createDirectories(it2) }
                } else {
                    is_valid = try {
                        Git.open(full_path?.let { it1 -> File(it1) })
                        true
                    } catch (e: RepositoryNotFoundException) {
                        false
                    }
                }
            }
            onExit {
                if (!is_valid) {
                    //delete dir in the path.
                    full_path?.let { it1 -> Paths.get(it1) }?.let { it2 -> deleteDir(it2) }
                }
            }
            transition<SwitchEvent> {
                targetState = States.UpdatedRepo
                onTriggered {
//                    println("Updating repository...")
                    indicator.thisLogger().info("Updating repository...")
                    indicator.fraction = 0.6
                }
            }
        }
        addFinalState(States.UpdatedRepo) {
            var is_updated = false
            var is_cloned = false
            var git: Git
            onEntry {
                if (is_valid) {
                    //check to see if we need to update
                    git = Git.open(full_path?.let { it1 -> File(it1) })
                    git.fetch().call()
                    val status = git.status().call()
                    is_updated = status.isClean
                    is_success = true // it is valid git repo
                } else {
                    // clone the git url to  path
                    is_cloned = try {
                        Git.cloneRepository()
                            .setURI(git_repo)
                            .setDirectory(full_path?.let { it1 -> File(it1) })
                            .setCloneAllBranches(true)
                            .setCloneSubmodules(true)
                            .call()
                        git = Git.open(full_path?.let { it1 -> File(it1) })// just in case is not complete
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }

            onExit {
                println(is_success)
                if (!is_updated) {
                    // update
                    git = Git.open(full_path?.let { it1 -> File(it1) })
                    git.pull().call()
                }
                if (!is_cloned) {
                    // delete and reclone
                    is_success = try {
                        Git.cloneRepository()
                            .setURI(git_repo)
                            .setDirectory(full_path?.let { it1 -> File(it1) })
                            .setCloneAllBranches(true)
                            .setCloneSubmodules(true)
                            .call()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                indicator.thisLogger().info("Finished...")
            }
        }
        onFinished {
            if (!is_success) {
                indicator.thisLogger().error("Error caching template")
//                println("Error caching template")
            }
        }
    }

    init {
        // process the three states
        machine.processEvent(SwitchEvent)
        machine.processEvent(SwitchEvent)
        machine.processEvent(SwitchEvent)
    }


    /**
     * Extract remote repo
     *
     * @param repo_uri
     * @return
     */
    private fun extractRemoteRepo(repo_uri: String): String? {
        try {
            val start_of_name: Int = repo_uri.lastIndexOf('/')
            val end_of_name: Int = repo_uri.lastIndexOf('.')
            val name: String = repo_uri.substring(start_of_name, end_of_name)
            return name
        } catch (e: StringIndexOutOfBoundsException) {
            println("Bad template...")
        }
        return null
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
     * Is success
     *
     * @return
     */
    fun isSuccess(): Boolean {
        return machine.isFinished
    }

    /**
     * Get template path
     *
     * @return
     */
    fun getTemplatePath(): String? {
        return full_path
    }
}