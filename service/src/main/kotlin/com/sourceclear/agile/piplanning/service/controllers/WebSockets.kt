package com.sourceclear.agile.piplanning.service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.BoardO
import com.sourceclear.agile.piplanning.objects.MessageReq
import com.sourceclear.agile.piplanning.objects.MessageRes
import com.sourceclear.agile.piplanning.service.services.Boards
import org.jooq.impl.DefaultDSLContext
import org.slf4j.LoggerFactory
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

  private val LOGGER = LoggerFactory.getLogger(WebSockets::class.java)
  private val lock = Object()
  private val connections = mutableMapOf<Long, MutableList<WebSocketSession>>()
  private val objectMapper = ObjectMapper().registerModule(KotlinModule())!!

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    forgetSession(session)
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    when (val msg = objectMapper.readValue<MessageReq>(message.asBytes())) {
      is MessageReq.Register -> {
        session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(MessageRes.Welcome(session.id))))
      }
      is MessageReq.OpenedBoard -> {
        synchronized(lock) {
          forgetSession(session)
          connections.getOrPut(msg.board) { mutableListOf() }.add(session)
          LOGGER.debug("user connected to board {}", msg.board)
        }
        create.transaction { _ ->
          broadcastBoardUpdate(boards.useCurrentSolution(msg.board))
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

  private fun forgetSession(session: WebSocketSession) {
    synchronized(lock) {
      connections.forEach { e ->
        val removed = e.value.removeIf { s ->
          s.id == session.id
        }
        if (removed) {
          LOGGER.debug("user disconnected from board ${e.key}")
        }
      }
    }
  }
}
