package com.stream_suite.link.service.jobs

import org.junit.Test

import org.junit.Assert.*

class ScheduledMessageJobTest : com.stream_suite.link.MessengerSuite() {

    @Test
    fun ordersMessagesWithClosestTimestampFirst() {
        val list = listOf(4, 5, 1, 3, 4, 7, 6)
                .sortedBy { it }

        assertEquals(list, listOf(1,3,4,4,5,6,7))
    }
}