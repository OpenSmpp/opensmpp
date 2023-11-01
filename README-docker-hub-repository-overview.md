<!--

The content of this file are submitted as repository overview in Docker Hub:
https://hub.docker.com/repository/docker/codcod66/opensmpp-sim

-->

Note: the description for this image can be found at https://github.com/codcod/opensmpp/blob/master/README-repository-overview.md.

# Quick reference

-   **Maintained by**:
    
    https://github.com/codcod/opensmpp

# Supported tags and respective `Dockerfile` links

-	[`0.1.0`, `latest`](https://github.com/codcod/opensmpp/blob/a70c2b567b596f8f5cf6274039b2e957a83ca9fc/sim.Dockerfile)

# What is OpenSMPP?

The OpenSmpp API is an open source Java library implementing the SMPP (Short
Message Peer to Peer) protocol. It is based on the original Logica OpenSMPP
API.

At the moment only SMPP version 3.4 is supported.

Support for SMPP used to be provided via the forums at smsforum.net (no longer
in existence); but since SMS Forum ceased operations at the end of 2007,
official support is no longer available.

> http://opensmpp.org

![logo](http://opensmpp.org/images/opensmpp.png)

# How to use this image

## start an opensmpp instance

```console
$ docker run --rm -p 2775:2775 codcod66/opensmpp-sim
```

The default users and passwords are kept [in this file](https://github.com/codcod/opensmpp/blob/a70c2b567b596f8f5cf6274039b2e957a83ca9fc/sim/users.txt).

## ... or via [`docker compose`](https://github.com/docker/compose)

Example `compose.yml`:

```yaml
version: "3.8"

services:
  sim:
    container_name: opensmpp-sim-1
    image: codcod66/opensmpp-sim
    ports:
      - "2775:2775"
```

# License

View [license](https://github.com/codcod/opensmpp/blob/a70c2b567b596f8f5cf6274039b2e957a83ca9fc/LICENSE)
[information](https://github.com/codcod/opensmpp/blob/a70c2b567b596f8f5cf6274039b2e957a83ca9fc/LICENSE_LOGICA)
for the software contained in this image.

As with all Docker images, these likely also contain other software which may
be under other licenses (such as Bash, etc from the base distribution, along
with any direct or indirect dependencies of the primary software being
contained).

As for any pre-built image usage, it is the image user's responsibility to
ensure that any use of this image complies with any relevant licenses for all
software contained within.
