/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.MessageReq
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.service.services.NotificationsImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessagesTest {

  @Test
  fun messages() {
    val mapper = ObjectMapper().registerModule(KotlinModule())
    val a = MessageReq.OpenedBoard(1L)
    val s = mapper.writeValueAsString(a)
    val b = mapper.readValue<MessageReq>(s) // polymorphic
    assertEquals(a, b)
  }
}