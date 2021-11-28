# css4j-dom4j version 3.6.1 Release Notes

### November 28, 2021

<br/>

## Release Highlights

### DOM conformance.

  - `XHTMLDocument` has gained `getCompatMode()`.

  - `XHTMLDocument` now returns sane default values for `Document.inputEncoding`,
    `xmlEncoding`, `xmlStandalone` and `xmlVersion` attributes (instead of throwing an
    exception). Thanks to that, css4j-dom4j documents can now be used with a
    wider XML infrastructure like `javax.xml.transform.Transformer`.

### Build & CI.

  - Miscellaneous build improvements.

  - Added a CI workflow.

  - New workflow to automatically publish in Github packages on release.


## Project Sites

Project home: https://css4j.github.io/

Development site: https://github.com/css4j/css4j-dom4j
