package com.example.counter_app.data

import java.util.Date

enum class EventType {
    ENTRY, EXIT
}

data class CounterEvent(
    val type: EventType,
    val timestamp: Date
)
