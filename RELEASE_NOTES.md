# css4j-dom4j version 3.9 Release Notes

### October 19, 2022

<br/>

## Release Highlights

### Add `rebuildCascade()` to `XHTMLDocument`

Gives a native, more efficient implementation to css4j's `CSSDocument.rebuildCascade()` default method.

## Detail of changes

- Check for read-only in `setAttributeNode()`.
- Prevent NPE in `setTargetMedium(String)`.
- Upgrade to css4j 3.9.0
- Upgrade to `extra-java-module-info` 1.0
- Gradle: prefer version 2.0.3 of `slf4j-api`.

## Project Sites

Project home: https://css4j.github.io/

Development site: https://github.com/css4j/css4j-dom4j
