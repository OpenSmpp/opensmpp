# Roadmap

We are using [Semantic Versioning](http://semver.org) for releases, which
dictates how version numbers change with respect to the APII contract.

## 3.0.0

This will be a compatibility release, suitable for users of the library from
the [SourceForge OpenSmpp/SmsTools](http://sourceforge.net/projects/smstools/)
project.

The increase in major number is as a result of the repackaging into seperate
modules.

Following this release, there is not expected to be any major development on
this branch, though bugfixes releases may occur.

## 4.0.0

This branch will bring the API into line with current best practices, making use
of standard JDK types and Java 6+ techniques.

In order to ease the transistion of existing codebases, which may have relied on
the previous API for over ten years, it is expected that there will be a
compatibility layer to mimic the 3.0.x API.
