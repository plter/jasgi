import asyncio
from asgiref.sync import sync_to_async, async_to_sync


def completable_to_awaitable(completable_future):
    loop = asyncio.get_event_loop()
    future = loop.create_future()

    def complete_task(result):
        if not future.done():
            future.set_result(result)

    def complete_exceptionally(error):
        if not future.done():
            future.set_exception(error)

    completable_future.thenAccept(complete_task)
    completable_future.exceptionally(complete_exceptionally)

    return future


def str2bstr(s):
    return s.encode("utf8")


def bstr2str(bs):
    return bs.decode("utf8")


def bytesj2p(bs):
    return bytes(b % 256 for b in bs)


def wrap_no_arg_java_function(jf):
    def f():
        return jf(None)

    return f
