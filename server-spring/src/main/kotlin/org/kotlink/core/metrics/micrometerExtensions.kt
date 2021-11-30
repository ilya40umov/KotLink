package org.kotlink.core.metrics

import io.micrometer.core.instrument.Timer

fun <T> Timer.recording(call: () -> T): T {
    return this.record(call)!!
}