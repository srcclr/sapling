package com.sourceclear.agile.piplanning.objects

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

// WebSocket messages sent from the front end
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageReq.OpenedBoard::class, name = "OpenedBoard"),
    JsonSubTypes.Type(value = MessageReq.EditingSprint::class, name = "EditingSprint"),
    JsonSubTypes.Type(value = MessageReq.EditingEpic::class, name = "EditingEpic"),
    JsonSubTypes.Type(value = MessageReq.EditingStory::class, name = "EditingStory"))
sealed class MessageReq {

  data class OpenedBoard(
      val board: Long) : MessageReq()

  data class EditingSprint(
      val sprint: Long) : MessageReq()

  data class EditingStory(
      val story: Long) : MessageReq()

  data class EditingEpic(
      val epic: Long) : MessageReq()
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
    JsonSubTypes.Type(value = MessageRes.Board::class, name = "Board"))
sealed class MessageRes {

  @JsonTypeName("Board")
  data class Board(
      val board: BoardO) : MessageRes()
}
