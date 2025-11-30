import asyncio

main_event_loop=asyncio.new_event_loop()
asyncio.set_event_loop(main_event_loop)

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