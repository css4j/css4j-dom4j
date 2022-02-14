# css4j - dom4j module

Subclasses several [dom4j](https://dom4j.github.io/) classes and provides CSS functionality to it.

[License](LICENSE.txt) is BSD 3-clause.

See the [latest Release Notes](RELEASE_NOTES.md).

## Java™ Runtime Environment requirements
All the classes in the binary package have been compiled with a [Java compiler](https://adoptium.net/)
set to 1.7 compiler compliance level, except the `module-info.java` file.

Building the library requires JDK 11 or higher.

<br/>

## Build from source
To build css4j-dom4j from the code that is currently at the Git repository, Java
11 or later is needed, although the resulting jar files can be run with a 1.7 JRE.

You can run a variety of Gradle tasks with the Gradle wrapper (on Windows shells you can omit the `./`):

- `./gradlew build` (normal build)
- `./gradlew build publishToMavenLocal` (to install in local Maven repository)
- `./gradlew copyJars` (to copy jar files into a top-level _jar_ directory)
- `./gradlew lineEndingConversion` (to convert line endings of top-level text files to CRLF)
- `./gradlew publish` (to deploy to a Maven repository, as described in the `publishing.repositories.maven` block of
[build.gradle](https://github.com/css4j/css4j-dom4j/blob/1-stable/build.gradle))

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

## Software dependencies

In case that you do not use a Gradle or Maven build (which would manage the
dependencies according to the relevant `.module` or `.pom` files), the required
and optional library packages are the following:

### Compile-time dependencies

- The [css4j](https://github.com/css4j/css4j/releases) library (and its transitive
  dependencies); version 1.3.1 or higher (but below 2.0) is recommended.

- The [css4j-agent](https://github.com/css4j/css4j-agent/releases) library;
  version 1.3.0 or higher (but below 2.0) is recommended.
  **It is optional at runtime.**

- The [dom4j](https://github.com/dom4j/dom4j) JAR package (tested with 2.1.1).
  Requires at least version 2.0 to compile and 2.1.1 to run the tests, but you
  should be able to run the resulting jar file with dom4j 1.6 if you are stuck with it.

- The [XPP3 Pull Parser](https://github.com/xmlpull-xpp3/xmlpull-xpp3) (which
  can be used with this library but beware that it does not support [character
  entities](https://dev.w3.org/html5/html-author/charref)).
  **It is optional at runtime.**

### Test dependencies

- A recent version of [JUnit 4](https://junit.org/junit4/).

- The `batik-css`, `batik-util`and `batik-i18n` artifacts from
  [Apache Batik](https://xmlgraphics.apache.org/batik/).

- [Jaxen](https://github.com/jaxen-xpath/jaxen), this software was tested with
  version 1.2.0.

- [SLF4J](http://www.slf4j.org/), which is a logging package.

<br/>

## Website
For more information please visit https://css4j.github.io/
