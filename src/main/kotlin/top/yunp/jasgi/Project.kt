/*
@author https://yunp.top
 */
package top.yunp.jasgi

import java.io.File

object Project {
    val codeSource = File(Project::class.java.protectionDomain.codeSource.location.toURI())

    private var _projectRoot: File? = null

    val projectRoot: File
        get() {
            if (_projectRoot == null) {
                if (codeSource.isDirectory) {
                    _projectRoot = codeSource.parentFile?.parentFile?.parentFile?.parentFile
                        ?: throw RuntimeException("Can not find the project root directory")
                } else if (codeSource.isFile && codeSource.name.lowercase().endsWith(".jar")) {
                    _projectRoot = codeSource.parentFile?.parentFile
                        ?: throw RuntimeException("Can not find the app root directory")
                } else {
                    throw RuntimeException("App environment error")
                }
            }
            return _projectRoot!!
        }
}