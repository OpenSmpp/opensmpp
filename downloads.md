---
title: Downloads - OpenSmpp
layout: default
---

<a href="https://github.com/ptomli/opensmpp">
	<img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png" alt="Fork me on GitHub">
</a>

# Downloads

## Maven

Maven SNAPSHOT hosting is gratiously provided by [Sonatype](http://www.sonatype.com).
Once there is a 3.0.0 RELEASE we will get the artifacts into Central.

In the meantime, add a repository for Sonatype OSS

	<repository>
		<id>sonatype-oss</id>
		<url>https://oss.sonatype.org/content/groups/public</url>
	</repository>

Add a dependency for `opensmpp-core`

    <dependency>
    	<groupId>org.opensmpp</groupId>
    	<artifactId>opensmpp-core</artifactId>
    	<version>3.0.0-SNAPSHOT</version>
    </dependency>

## JARs

 *  [opensmpp-core](https://oss.sonatype.org/content/groups/public/org/opensmpp/opensmpp-core/3.0.0-SNAPSHOT/)
    Requires `opensmpp-charset`
 *  [opensmpp-charset](https://oss.sonatype.org/content/groups/public/org/opensmpp/opensmpp-charset/3.0.0-SNAPSHOT/)
 *  [opensmpp-client](https://oss.sonatype.org/content/groups/public/org/opensmpp/opensmpp-client/3.0.0-SNAPSHOT/)
    Requires `opensmpp-core`
 *  [opensmpp-sim](https://oss.sonatype.org/content/groups/public/org/opensmpp/opensmpp-sim/3.0.0-SNAPSHOT/)
    Requires `opensmpp-core`

## Source

The source code is hosted on [GitHub](https://github.com).

See [https://github.com/ptomli/opensmpp](https://github.com/ptomli/opensmpp)

Please feel free to fork the repository and submit pull requests.

A ZIP file of the current `master` branch can be downloaded
[here](https://github.com/ptomli/opensmpp/archive/master.zip).

