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

import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.JschConfigSessionFactory
import org.eclipse.jgit.transport.OpenSshConfig
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.util.FS
import org.gradle.api.Project

class GitManager {
    private final Git git

    GitManager(Project project) {
        SshSessionFactory.setInstance(new CustomConfigSessionFactory(project))
        git = new Git(new FileRepositoryBuilder().setWorkTree(project.rootProject.rootDir)
                .setMustExist(true)
                .build())
    }

    void createBranch(String name) {
        git.branchCreate().setName(name).call()
    }

    void checkout(String name) {
        git.checkout().setName(name).call()
    }

    void commit(String author, String email, String message) {
        git.commit().setAuthor(author, email).setAll(true).setMessage(message).call()
    }

    void push(String remote) {
        git.push().setRemote(remote).call()
    }

    void tag(String tag) {
        git.tag().setName(tag).call()
    }

    void pushTag(String remote, String tag) {
        git.push().setRemote(remote).add(tag).call()
    }

    class CustomConfigSessionFactory extends JschConfigSessionFactory {
        private Project project

        CustomConfigSessionFactory(Project project) {
            this.project = project
        }

        @Override
        protected void configure(OpenSshConfig.Host hc, Session session) {
            session.setConfig("StrictHostKeyChecking", "no")
        }

        @Override
        protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
            JSch jsch = super.getJSch(hc, fs)
            jsch.removeAllIdentity()
            jsch.addIdentity(project.release.pushKey.toString())
            return jsch
        }
    }
}
