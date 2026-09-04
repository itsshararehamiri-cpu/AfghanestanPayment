package com.danesh.engine

import com.danesh.common.RawMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpHostTimeSynchronizer @Inject constructor() : HostTimeSynchronizer {
    override suspend fun syncFromResponse(response: RawMessage) = Unit
}
