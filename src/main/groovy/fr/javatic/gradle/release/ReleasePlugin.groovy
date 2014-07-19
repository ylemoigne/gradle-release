/*
 * Copyright 2014 Yann Le Moigne
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.javatic.gradle.release

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import org.gradle.api.resources.MissingResourceException

class ReleasePlugin implements Plugin<Project> {
    private final static String PROPERTY_VERSION_MAJOR = "majorVersion"
    private final static String PROPERTY_VERSION_MINOR = "minorVersion"

    void apply(Project project) {
        project.extensions.create("release", ReleaseExtension, project)

        applyVersion(project)

        GitManager gitManager = new GitManager(project)

        project.task('prepareRelease') {
            doLast {
                if (!project.hasProperty('majorVersion')) {
                    throw new RuntimeException("prepareRelease task need 'versionMajor' property. Example: gradle -majorVersion=2 prepareRelease")
                }

                String majorVersion = project.majorVersion
                String branchName = "${project.release.branchPrefix}${majorVersion}"

                gitManager.createBranch(branchName)
                gitManager.checkout(branchName)

                Properties p = readVersionProperties(project)
                p.setProperty(PROPERTY_VERSION_MAJOR, majorVersion)
                p.setProperty(PROPERTY_VERSION_MINOR, '0')
                writeVersionProperties(project, p)

                gitManager.commit(project.release.commitAuthorName, project.release.commitAuthorMail, "PrepareRelease: set 'majorVersion' to $majorVersion and reset 'minorVersion' to 0")
                if (project.release.pushTo != null) {
                    gitManager.push(project.release.pushTo)
                }
            }
        }

        project.task('release') << {
            Properties p = readVersionProperties(project)
            String major = p.getProperty(PROPERTY_VERSION_MAJOR)
            String minor = p.getProperty(PROPERTY_VERSION_MINOR)

            String tag = "${project.release.tagPrefix}${major}.${minor}"
            gitManager.tag(tag)
            if (project.release.pushTo != null) {
                gitManager.pushTag(project.release.pushTo)
            }

            p.setProperty(PROPERTY_VERSION_MINOR, String.valueOf(Integer.parseInt(mineur) + 1))
            writeVersionProperties(project, p)

            gitManager.commit(project.release.commitAuthorName, project.release.commitAuthorMail, "Release: update 'minorVersion'")
            if (project.release.pushTo != null) {
                gitManager.push(project.release.pushTo)
            }
        }
    }

    private static void applyVersion(Project project) {
        Properties p = readVersionProperties(project)
        String major = p.getProperty(PROPERTY_VERSION_MAJOR)
        String minor = p.getProperty(PROPERTY_VERSION_MINOR)

        String version = "$major.$minor"

        project.gradle.taskGraph.addTaskExecutionGraphListener(new TaskExecutionGraphListener() {
            @Override
            void graphPopulated(TaskExecutionGraph taskExecutionGraph) {
                boolean release = taskExecutionGraph.hasTask("release")

                if (!release) {
                    version += '-SNAPSHOT'
                }

                project.allprojects { pr ->
                    pr.version = version
                }
            }
        })
    }

    private static File getVersionPropertiesFile(Project project) {
        File versionPropertiesFile = project.file('version.properties')
        if (!versionPropertiesFile.exists()) {
            throw new MissingResourceException("version.properties file not found")
        }

        return versionPropertiesFile
    }

    private static Properties readVersionProperties(Project project) {
        Properties p = new Properties()
        FileInputStream fis = new FileInputStream(getVersionPropertiesFile(project))
        try {
            p.load(fis)
        } finally {
            fis.close()
        }
        return p
    }

    private static void writeVersionProperties(Project project, Properties p) {
        FileOutputStream fos = new FileOutputStream(getVersionPropertiesFile(project))
        try {
            p.store(fos, "Version file")
            fos.flush()
        } finally {
            fos.close()
        }
    }
}