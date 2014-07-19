package fr.javatic.gradle.release

import org.gradle.api.Project

class ReleaseExtension {
    String commitAuthorName = "release"
    String commitAuthorMail = "release@unknown.tld"

    String branchPrefix = "release_"
    String tagPrefix = "release_"

    String pushTo = "origin"

    File pushKey

    ReleaseExtension(Project project) {
        pushKey = project.file("id_rsa")
    }
}
