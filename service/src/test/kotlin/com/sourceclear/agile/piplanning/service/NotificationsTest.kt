/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service

import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.service.services.NotificationsImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsTest {

  val notifications = NotificationsImpl()

  @Test
  fun testDeserialization() {
    val value = NotificationO.IncomingStoryRequest(1L, 2L, "sender", "sprint", "epic", "descripton", 1, "notes")
    val s = notifications.mapper.writeValueAsString(value)
    val value1 = notifications.mapper.readValue<NotificationO.IncomingStoryRequest>(s)
    assertTrue(s.contains("\"type\":\"" + NotificationO.IncomingStoryRequest::class.simpleName + "\""))
    assertEquals(value, value1)
  }
}