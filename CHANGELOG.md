# Changelog #

## Unreleased ## 

- Upgrade [promesa](https://github.com/funcool/promesa/blob/master/CHANGELOG.md#version-402)
  to v4

## Version 1.1.0 ##

Date: 2018-06-02

- Add alternative xhr client.


## Version 1.0.0 ##

Date: 2017-05-28

- Fix incorrect body fetching inconsistencies on nodejs.
- Bump promesa to 1.8.1
- Bump aleph to 0.4.3
- Remove support for aboirt that is no longer supported by promesa.


## Version 0.6.2 ##

Date: 2016-08-26

- Add `:with-credentials?` parameter for xhr client.
- Update promesa to 1.5.0.


## Version 0.6.1 ##

Date: 2016-07-10

- Update promesa to 1.4.0 (that fixes problems with advanced compilations)


## Version 0.6.0 ##

Date: 2016-04-23

- Major nodejs client refactor (making it consistent with the rest of clients and
  may contain **breaking changes**).
- Fix consistency issues on `aleph` client.
- Add full test suite for the 3 builtin clients (not only xhr).
- Normalize error reporting: all builtin clients now uses `ex-info`
  instances with response data atteched for error reporting.
- Code cleaning
- Add `:query-params` encoding.


## Version 0.5.0 ##

Date: 2016-03-28

- Clojure compatibility
- An aleph-based Clojure client on `httpurr.client.aleph`
- Many bugfixes on xhr client.


## Version 0.3.0 ##

Date: 2016-01-08

- Upgrade dependencies.


## Version 0.2.0 ##

Date: 2015-12-03

- Upgrade dependencies.


## Version 0.1.2 ##

Date: 2015-11-12

- Add node.js client on `httpur.client.node`.
- Add a basic auth helper under `htttpurr.auth`.
- Add more examples to the documentation.


## Version 0.1.1 ##

Date: 2015-10-24

- Add missing clojure symbols exclude on httpurr.client.xhr ns.


## Version 0.1.0 ##

Date: 2015-09-27

- First relase.
