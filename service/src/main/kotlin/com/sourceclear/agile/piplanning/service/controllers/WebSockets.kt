package com.sourceclear.agile.piplanning.service.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.BoardO
import com.sourceclear.agile.piplanning.objects.ConnectedUser
import com.sourceclear.agile.piplanning.objects.Element
import com.sourceclear.agile.piplanning.objects.Interaction
import com.sourceclear.agile.piplanning.objects.MessageReq
import com.sourceclear.agile.piplanning.objects.MessageRes
import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider
import com.sourceclear.agile.piplanning.service.exceptions.JwtAuthenticationException
import com.sourceclear.agile.piplanning.service.services.Boards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

data class ConnectedClient(val session: WebSocketSession, val email: String)

@Component
open class WebSockets @Autowired constructor(
    // Lazy has to be here instead of in Boards.
    // There's some silly non-symmetry around this not having an interface because
    // it has to inherit that abstract class below.
    @Lazy private val boards: Boards,
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
    try {
      val msg = objectMapper.readValue<MessageReq>(message.asBytes())
      val email =
          try {
            jwtTokenProvider.getEmail(msg.token)
          } catch (e: JwtAuthenticationException) { // 401
            return
          }
      reallyHandleTextMessage(session, email, msg)
    } catch (e: JsonProcessingException) { // 400ish
    } catch (e: Exception) { // 500ish
      LOGGER.error("error occurred", e)
      // TODO return something?
    }
  }

  private fun reallyHandleTextMessage(session: WebSocketSession, email: String, message: MessageReq) {
    when (message) {
      is MessageReq.OpenedBoard -> {
        synchronized(lock) {
          forgetSession(session)
          connectionsByBoard.getOrPut(message.board) { mutableListOf() }.add(
              ConnectedClient(session = session, email = email))
          LOGGER.debug("user connected to board {}", message.board)
          broadcastBoardUpdate(message.board)
        }
      }
      is MessageReq.OpenedBoardList -> {
        synchronized(lock) {
          forgetSession(session)
          boardListConnections.add(ConnectedClient(session = session, email = email))
          session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(MessageRes.BoardList(boards.getBoardList()))))
        }
      }
      is MessageReq.EditingSprint -> {
        synchronized(lock) {
          val interactions = locked.getOrPut(message.board) { mutableListOf() }
          if (message.done) {
            interactions.removeIf {
              it.user.uuid == session.id && it.element.let { e -> e is Element.Sprint && e.sprint == message.sprint }
            }
          } else {
            interactions.add(Interaction(
                ConnectedUser(email = email, uuid = session.id),
                Element.Sprint(board = message.board, sprint = message.sprint)))
          }
          broadcastBoardUpdate(message.board)
        }
      }
      is MessageReq.EditingStory -> {
        synchronized(lock) {
          val interactions = locked.getOrPut(message.board) { mutableListOf() }
          if (message.done) {
            interactions.removeIf {
              it.user.uuid == session.id && it.element.let { e -> e is Element.Story && e.story == message.story }
            }
          } else {
            interactions.add(Interaction(
                ConnectedUser(email = email, uuid = session.id),
                Element.Story(board = message.board, story = message.story)))
          }
          broadcastBoardUpdate(message.board)
        }
      }
      is MessageReq.EditingEpic -> {
        synchronized(lock) {
          val interactions = locked.getOrPut(message.board) { mutableListOf() }
          if (message.done) {
            interactions.removeIf {
              it.user.uuid == session.id && it.element.let { e -> e is Element.Epic && e.epic == message.epic }
            }
          } else {
            interactions.add(Interaction(
                ConnectedUser(email = email, uuid = session.id),
                Element.Epic(board = message.board, epic = message.epic)))
          }
          broadcastBoardUpdate(message.board)
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
        val b = MessageRes.Board(board,
            clients.map { ConnectedUser(email = it.email, uuid = it.session.id) },
            locked.getOrDefault(board.id, mutableListOf()).filter { it.user.uuid != client.session.id })
        client.session.sendMessage(TextMessage(objectMapper.writeValueAsBytes(b)))
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
        e.value.removeIf { i ->
          i.user.uuid == session.id
        }
      }
    }
  }
}
