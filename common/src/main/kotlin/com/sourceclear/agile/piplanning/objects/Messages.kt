package com.sourceclear.agile.piplanning.objects

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

// WebSocket messages sent from the front end
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageReq.OpenedBoard::class, name = "OpenedBoard"),
    JsonSubTypes.Type(value = MessageReq.OpenedBoardList::class, name = "OpenedBoardList"),
    JsonSubTypes.Type(value = MessageReq.EditingSprint::class, name = "EditingSprint"),
    JsonSubTypes.Type(value = MessageReq.EditingEpic::class, name = "EditingEpic"),
    JsonSubTypes.Type(value = MessageReq.EditingStory::class, name = "EditingStory"))
sealed class MessageReq(open val token: String) {

  data class OpenedBoard(
      override val token: String,
      val board: Long) : MessageReq(token)

  data class OpenedBoardList(
      override val token: String) : MessageReq(token)

  data class EditingSprint(
      override val token: String,
      val board: Long,
      val sprint: Long,
      val done: Boolean) : MessageReq(token)

  data class EditingStory(
      override val token: String,
      val board: Long,
      val story: Long,
      val done: Boolean) : MessageReq(token)

  data class EditingEpic(
      override val token: String,
      val board: Long,
      val epic: Long,
      val done: Boolean) : MessageReq(token)
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageRes.BoardList::class, name = "BoardList"),
    JsonSubTypes.Type(value = MessageRes.Board::class, name = "Board"))
sealed class MessageRes {

  @JsonTypeName("Board")
  data class Board(
      val board: BoardO,
      val clients: List<String>,
      val locked: List<Interaction>) : MessageRes()

  @JsonTypeName("BoardList")
  data class BoardList(
      val boards: List<BoardL>) : MessageRes()
}

// Elements that can be edited
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(value = Element.Sprint::class, name = "Sprint"),
    JsonSubTypes.Type(value = Element.Story::class, name = "Story"),
    JsonSubTypes.Type(value = Element.Epic::class, name = "Epic"))
sealed class Element {

  data class Sprint(
      val board: Long,
      val sprint: Long) : Element()

  data class Story(
      val board: Long,
      val story: Long) : Element()

  data class Epic(
      val board: Long,
      val epic: Long) : Element()
}

data class Interaction(val email: String, val uuid: String, val element: Element)
