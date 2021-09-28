# css4j - dom4j module

Subclasses several [dom4j](https://dom4j.github.io/) classes and provides CSS functionality to it.
Licence is BSD 3-clause.

## Build from source
To build css4j-dom4j from the code that is currently at the Git repository, Java 11 or later is needed.
You can run a variety of Gradle tasks with the Gradle wrapper (on Windows shells you can omit the `./`):

- `./gradlew build` (normal build)
- `./gradlew build publishToMavenLocal` (to install in local Maven repository)
- `./gradlew copyJars` (to copy jar files into a top-level _jar_ directory)
- `./gradlew lineEndingConversion` (to convert line endings of top-level text files to CRLF)
- `./gradlew publish` (to deploy to a Maven repository, as described in the `publishing.repositories.maven` block of
[build.gradle](https://github.com/css4j/css4j/blob/master/build.gradle))

<br/>

## Usage from a Gradle project
If your Gradle project depends on css4j-dom4j, you can use this project's own Maven repository in a `repositories` section of
your build file:
```groovy
repositories {
    maven {
        url "https://css4j.github.io/maven/"
        mavenContent {
            releasesOnly()
        }
        content {
            includeGroup 'io.sf.carte'
            includeGroup 'io.sf.jclf'
            includeGroup 'xmlpull'
            includeGroup 'xpp3'
        }
    }
}
```
please use this repository **only** for the artifact groups listed in the `includeGroup` statements.

Then, in your `build.gradle` file:
```groovy
dependencies {
    api "io.sf.carte:css4j-dom4j:${css4jDom4jVersion}"
}
```
where `css4jDom4jVersion` would be defined in a `gradle.properties` file.

<br/>

## Website
For more information please visit https://css4j.github.io/
