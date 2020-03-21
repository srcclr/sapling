/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.services

import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.service.controllers.Notifications
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationsTest {
  @Test
  fun testDeserialization() {
    val value = NotificationO.StoryRequest(1L, "sender", "sprint", "epic", "descripton", 1, "notes")
    val s = Notifications.mapper.writeValueAsString(value)
    val value1 = Notifications.mapper.readValue<NotificationO.StoryRequest>(s)
    assertTrue(s.contains("\"@type\":\"" + NotificationO.StoryRequest::class.simpleName + "\""))
    assertEquals(value, value1)
  }
}