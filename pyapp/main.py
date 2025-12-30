from mysite.asgi import application as app
import traceback

async def application(*args, **kwargs):
    try:
        return await app(*args, **kwargs)
    except:
        traceback.print_exc()
