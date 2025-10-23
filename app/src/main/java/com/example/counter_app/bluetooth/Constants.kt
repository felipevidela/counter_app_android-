package com.example.counter_app.bluetooth

import java.util.UUID

const val USE_SIMULATED_SERVICE = true
val COUNTER_SERVICE_UUID: UUID = UUID.fromString("df801ed6-a405-11eb-bcbc-000000000000")
val COUNTER_CHARACTERISTIC_UUID: UUID = UUID.fromString("df801ed6-a405-11eb-bcbc-000000000001")
val ACTIVATOR_CHARACTERISTIC_UUID: UUID = UUID.fromString("df801ed6-a405-11eb-bcbc-000000000002")
