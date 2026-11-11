package dev.dantech.klokk.persist

internal const val DATA_STORE_FILE = "klokk.preferences_pb"

expect fun defaultKlokkRepository(): KlokkRepository
