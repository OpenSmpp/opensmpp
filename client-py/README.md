# Python client for the OpenSMPP Simulator

Basic Python client for OpenSMPP Simulator to test the connection mostly.

Connects to `127.0.0.1` on port 2775 with `smppclient1` as login name and
`password` as password.

    $ make venv
    $ make run

Or interactively:

    $ make venv
    $ poetry shell
    $ python
    Python 3.12.0 (main, Oct  2 2023, 12:03:24) [Clang 15.0.0 (clang-1500.0.40.1)] on darwin
    >>> from smsc_client import app2
    >>> app2.send(sender='123', recipient='321', message='Hi 1000')
    >>> ^D ^C

Uses [smpplib](https://github.com/python-smpplib/python-smpplib).
