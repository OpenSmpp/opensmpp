---
title: Downloads - OpenSmpp
layout: default
---

<a href="https://github.com/OpenSmpp/opensmpp">
	<img style="position: absolute; top: 0; right: 0; border: 0;" src="https://s3.amazonaws.com/github/ribbons/forkme_right_red_aa0000.png" alt="Fork me on GitHub">
</a>

# Downloads

## Maven

The core library is contained in `opensmpp-core`.

{% highlight xml %}
<dependency>
	<groupId>org.opensmpp</groupId>
	<artifactId>opensmpp-core</artifactId>
	<version>3.0.0</version>
</dependency>
{% endhighlight %}

The simulator and client are within `opensmpp-sim` and `opensmpp-client`, respectively.

{% highlight xml %}
<dependency>
	<groupId>org.opensmpp</groupId>
	<artifactId>opensmpp-sim</artifactId>
	<version>3.0.0</version>
</dependency>

<dependency>
	<groupId>org.opensmpp</groupId>
	<artifactId>opensmpp-client</artifactId>
	<version>3.0.0</version>
</dependency>
{% endhighlight %}

Maven SNAPSHOT hosting is gratiously provided by [Sonatype](http://www.sonatype.com).

{% highlight xml %}
<repository>
	<id>sonatype-oss</id>
	<url>https://oss.sonatype.org/content/groups/public</url>
</repository>
{% endhighlight %}

{% highlight xml %}
<dependency>
	<groupId>org.opensmpp</groupId>
	<artifactId>opensmpp-core</artifactId>
	<version>3.0.1-SNAPSHOT</version>
</dependency>
{% endhighlight %}

## JARs

 *  [opensmpp-core](https://repo1.maven.org/maven2/org/opensmpp/opensmpp-core/3.0.0/opensmpp-core-3.0.0.jar)
    Requires `opensmpp-charset`
 *  [opensmpp-charset](https://repo1.maven.org/maven2/org/opensmpp/opensmpp-charset/3.0.0/opensmpp-charset-3.0.0.jar)
 *  [opensmpp-client](https://repo1.maven.org/maven2/org/opensmpp/opensmpp-client/3.0.0/opensmpp-client-3.0.0.jar)
    Requires `opensmpp-core`
 *  [opensmpp-sim](https://repo1.maven.org/maven2/org/opensmpp/opensmpp-sim/3.0.0/opensmpp-3.0.0.jar)
    Requires `opensmpp-core`

## Source

The source code is hosted on [GitHub](https://github.com).

See [https://github.com/OpenSmpp/opensmpp](https://github.com/OpenSmpp/opensmpp)

Please feel free to fork the repository and submit pull requests.

A ZIP file of the current `master` branch can be downloaded
[here](https://github.com/OpenSmpp/opensmpp/archive/master.zip).

