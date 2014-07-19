gradle-release
==============
Opiniated & Simple Gradle release plugin supporting Git

Sneak Peak
==========
This plugin use a very simple approach :

Version will have the form of major.minor

To prepare a new major version :

    gradle prepareRelease -PmajorVersion=2

This will create a branch named `release_2`
Then for each 2.x release, on branch `release_2` just do

    gradle release

This will tag source with version 2.0 the first time, then 2.1, 2.2, etc.

It's simple.

Usage
=====

    buildscript {
        repositories {
            mavenCentral()
        }

        dependencies {
            classpath 'fr.lactalis.gradle.plugin:gradle-release:1.0'
        }
    }

    apply plugin: 'release'

    release {
        commitAuthorName = "release"
        commitAuthorMail = "release@unknown.tld"

        branchPrefix = "release_"
        tagPrefix = "release_"

        pushTo = "origin"
    }

About the release extension :

* `commitAuthorName` Name to use for the author in git commit. Default is `release`
* `commitAuthorMail` Email to use for the author in git commit. Default is `release@unknown.tld`

* `branchPrefix` When using `prepareRelease` task, the branch name is made of $branchPrefix$majorVersion . Default is `release_` which for a majorVersion=2 will produce a branch named `release_2`
* `tagPrefix` When using `release` task, the tag name is made of $tagPrefix$majorVersion.$minorVersion. Default is `release_` which for a majorVersion=2, minorVersion=3 will produce a tag named `release_2.3`

* `pushTo` Name of the remote on to push branch & tag. Default is `origin`. Can be set to `null` to disable push.