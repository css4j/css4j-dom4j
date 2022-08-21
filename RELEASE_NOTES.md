# css4j-dom4j version 3.8 Release Notes

### August 21, 2022

<br/>

## Release Highlights

### Use `<style>` elements from any namespace for computing styles

In style computation, inline styles that were in a non-HTML namespace were not
being used. This is what some browsers did in the past but that is no longer the
case.

Now it uses style elements having any namespace, which matches the current
behaviour of web browsers and makes it easier to support inlined SVG.

## Detail of changes

- Use `<style>` elements from any namespace for computing styles.
- Javadoc: add a module description.
- Fix javadoc warning.
- Gradle: upgrade wrapper to 7.5.1.
- Upgrade `extra-java-module-info` plugin to 0.15.
- Actions: switch from 'adopt' distribution to 'temurin', setup-java@v2 to 3.
- Actions: upgrade actions/checkout to v3.

## Project Sites

Project home: https://css4j.github.io/

Development site: https://github.com/css4j/css4j-dom4j
