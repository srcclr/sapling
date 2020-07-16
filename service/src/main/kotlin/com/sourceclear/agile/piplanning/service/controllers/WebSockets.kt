package com.sourceclear.agile.piplanning.service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.BoardO
import com.sourceclear.agile.piplanning.objects.Element
import com.sourceclear.agile.piplanning.objects.Interaction
import com.sourceclear.agile.piplanning.objects.MessageReq
import com.sourceclear.agile.piplanning.objects.MessageRes
import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider
import com.sourceclear.agile.piplanning.service.exceptions.JwtAuthenticationException
import com.sourceclear.agile.piplanning.service.services.Boards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

data class ConnectedClient(val session: WebSocketSession, val email: String)

@Component
class WebSockets @Autowired constructor(
    private val boards: Boards,
    private val jwtTokenProvider: JwtTokenProvider
) : AbstractWebSocketHandler() {

  private val LOGGER = LoggerFactory.getLogger(WebSockets::class.java)
  private val lock = Object()
  private val objectMapper = ObjectMapper().registerModule(KotlinModule())!!

  // Prototype implementation and won't scale. The actual thing would put state
  // in postgres and use LISTEN/NOTIFY.
  private val connectionsByBoard = mutableMapOf<Long, MutableList<ConnectedClient>>()
  private val boardListConnections = mutableListOf<ConnectedClient>()
  private val locked = mutableMapOf<Long, MutableList<Interaction>>()

  override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
    forgetSession(session)
  }

  override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
    val msg = objectMapper.readValue<MessageReq>(message.asBytes())
    val email =
        try {
          jwtTokenProvider.getEmail(msg.token)
        } catch (e: JwtAuthenticationException) {
          // TODO return something?
          return
        }

    when (msg) {
      is MessageReq.OpenedBoard -> {
        synchronized(lock) {
          forgetSession(session)
          connectionsByBoard.getOrPut(msg.board) { mutableListOf() }.add(ConnectedClient(session, email))
          LOGGER.debug("user connected to board {}", msg.board)
          broadcastBoardUpdate(msg.board)
        }
      }
      is MessageReq.OpenedBoardList -> {
        synchronized(lock) {
          forgetSession(session)
          boardListConnections.add(ConnectedClient(session, email))
          session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(MessageRes.BoardList(boards.getBoardList()))))
        }
      }
      is MessageReq.EditingSprint -> {
        synchronized(lock) {
          val interactions = locked.getOrDefault(msg.board, mutableListOf())
          if (msg.done) {
            interactions.removeIf {
              it.uuid == session.id && it.element.let { e -> e is Element.Sprint && e.sprint == msg.sprint }
            }
          } else {
            interactions.add(Interaction(email, session.id, Element.Sprint(msg.board, msg.sprint)))
          }
        }
      }
      is MessageReq.EditingStory -> {
        synchronized(lock) {
          val interactions = locked.getOrDefault(msg.board, mutableListOf())
          if (msg.done) {
            interactions.removeIf {
              it.uuid == session.id && it.element.let { e -> e is Element.Story && e.story == msg.story }
            }
          } else {
            interactions.add(Interaction(email, session.id, Element.Story(msg.board, msg.story)))
          }
        }
      }
      is MessageReq.EditingEpic -> {
        synchronized(lock) {
          val interactions = locked.getOrDefault(msg.board, mutableListOf())
          if (msg.done) {
            interactions.removeIf {
              it.uuid == session.id && it.element.let { e -> e is Element.Epic && e.epic == msg.epic }
            }
          } else {
            interactions.add(Interaction(email, session.id, Element.Epic(msg.board, msg.epic)))
          }
        }
      }
    }
  }

  fun broadcastBoardUpdate(board: Long) {
    broadcastBoardUpdate(boards.useCurrentSolution(board))
  }

  fun broadcastBoardUpdate(board: BoardO) {
    synchronized(lock) {
      val clients = connectionsByBoard.getOrDefault(board.id, mutableListOf())
      clients.forEach { client ->
        client.session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(
            MessageRes.Board(board,
                clients.map { it.email },
                locked.getOrDefault(board.id, mutableListOf())))))
      }
    }
  }

  fun broadcastBoardListUpdate() {
    synchronized(lock) {
      boardListConnections.forEach { client ->
        client.session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(
            MessageRes.BoardList(boards.getBoardList()))))
      }
    }
  }

  private fun forgetSession(session: WebSocketSession) {
    synchronized(lock) {
      connectionsByBoard.forEach { e ->
        val removed = e.value.removeIf { client ->
          client.session.id == session.id
        }
        if (removed) {
          LOGGER.debug("user disconnected from board ${e.key}")
        }
      }
      boardListConnections.removeIf { client ->
        client.session.id == session.id
      }
      locked.forEach { e ->
        e.value.removeIf { client ->
          client.uuid == session.id
        }
      }
    }
  }
}
