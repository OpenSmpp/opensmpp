---
title: Roadmap - OpenSmpp
layout: default
---

# Roadmap

We are using [Semantic Versioning](http://semver.org) for releases, which
dictates how version numbers change with respect to the APII contract.

## 3.0.0

This will be a compatibility release, suitable for users of the library from
the [SourceForge OpenSmpp/SmsTools](http://sourceforge.net/projects/smstools/)
project.

The increase in major number is as a result of the repackaging into seperate
modules.

Following this release, we will be bring the code in line with current best
practices, making use of stadard JDK types and Java 5+ techniques.

In order to ease the transistion of existing codebases, which may have relied on
the previous API for over ten years, there will remain a compatibility layer to
mimic the 3.0.x API.

It is expected that an improved API will be presented under the `org.opensmpp`
package and that the `org.smpp` package API will become deprecated.
