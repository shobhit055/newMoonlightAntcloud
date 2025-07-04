package com.limelight.common

import com.limelight.common.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object ProductionDispatcherProvider : DispatcherProvider {
    override fun provideUIContext() : CoroutineContext {
        return Dispatchers.Main
    }
    override fun provideIOContext() : CoroutineContext {
        return Dispatchers.IO
    }
}
