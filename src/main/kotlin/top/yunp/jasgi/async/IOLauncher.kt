package top.yunp.jasgi.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object IOLauncher {
    val default = CoroutineScope(Dispatchers.IO + SupervisorJob())
}