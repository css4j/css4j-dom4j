# css4j-dom4j version 5.3 Release Notes

### March 31, 2025

NOTE: This new version is not required to run with css4j 5.3, but if you use this
version of css4j-dom4j, then you need css4j 5.3 or later.

<br/>

## Release Highlights

### Update to latest changes in css4j 5.3

This version no longer calls an agent method which was deprecated by css4j 5.3.

### java.net.URL constructor deprecations

The library no longer uses the deprecated java.net.URL constructors. To achieve
this, it is calling a method from css4j 5.3, which makes the build dependent on
5.3 and later.

<br/>

## Detail of changes

- Deprecation cleanup: do not use java.net.URL constructors (requires css4j 5.3 or later).
- Update to latest changes in css4j.
- Upgrade to css4j 5.3.
- Upgrade to css4j-agent 5.3.
- Test with Jaxen 2.0.0.
- Upgrade Gradle wrapper to 8.13.
- Use SLF4J 2.0.17 in tests.
- Upgrade to JUnit 5.12.1.
- Upgrade to extra-java-module-info 1.11.
- Run CI with Java 11 and 21.

<br/>

## Project Sites

Project home: https://css4j.github.io/

Development site: https://github.com/css4j/css4j-dom4j
