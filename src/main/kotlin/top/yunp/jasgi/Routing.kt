package top.yunp.jasgi

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/{...}") { ASGIBridge(this).handle() }
        post("/{...}") { ASGIBridge(this).handle() }
        // Static plugin. Try to access `/static/index.html`
        staticResources("/static", "static")
    }
}
