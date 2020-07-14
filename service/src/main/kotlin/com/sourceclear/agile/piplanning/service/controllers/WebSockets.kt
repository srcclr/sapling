package com.sourceclear.agile.piplanning.service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.BoardO
import com.sourceclear.agile.piplanning.objects.MessageReq
import com.sourceclear.agile.piplanning.objects.MessageRes
import com.sourceclear.agile.piplanning.service.services.Boards
import org.aspectj.bridge.Message
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

@Component
class WebSockets @Autowired constructor(
    val create: DefaultDSLContext,
    val boards: Boards
) : AbstractWebSocketHandler() {

  val lock = Object()
  val connections = mutableMapOf<Long, MutableList<WebSocketSession>>()
  val objectMapper = ObjectMapper().registerModule(KotlinModule())!!

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    synchronized(lock) {
      connections.forEach { e ->
        e.value.removeIf { s ->
          s.id != session.id
        }
      }
    }
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val message = objectMapper.readValue<MessageReq>(message.asBytes())
    when (message) {
      is MessageReq.OpenedBoard -> {
        synchronized(lock) {
          connections.getOrPut(message.board) { mutableListOf() }.add(session)
        }
        create.transaction { _ ->
          broadcastBoardUpdate(boards.useCurrentSolution(message.board))
        }
      }
      is MessageReq.EditingSprint -> TODO()
      is MessageReq.EditingStory -> TODO()
      is MessageReq.EditingEpic -> TODO()
    }
  }

  fun broadcastBoardUpdate(board: BoardO) {
    connections.get(board.id)!!.forEach { session ->
      session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(MessageRes.Board(board))))
    }
  }
}
