package fr.javatic.gradle.release

class ReleaseExtension {
    String commitAuthorName = "release"
    String commitAuthorMail = "release@unknown.tld"

    String branchPrefix = "release_"
    String tagPrefix = "release_"

    String pushTo = "origin"
}
