package com.sourceclear.agile.piplanning.objects

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.Optional

data class EpicI(val name: String, val priority: Int)
data class EpicO(val id: Long, val name: String, val priority: Int)
data class TicketI(val description: String, val weight: Int)

data class TicketCD(
    val summary: String,
    val points: Int,
    val sprint: String,
    val epic: String)

data class TicketCU(
    val summary: String,
    val points: Int,
    val sprint: Optional<String>, // Java
    val epic: String)

data class BoardI(val name: String)

data class BoardL(
    val id: Long,
    val name: String,
    val owner: String)

data class BoardO(
    val id: Long,
    val name: String,
    val owner: String,
    val sprints: List<SprintO>,
    val epics: List<EpicO>,
    val unassigned: List<TicketO>,
    val notifications: List<NotificationO>)

data class SprintO(
    val id: Long,
    val name: String,
    val goal: String,
    val capacity: Int,
    val tickets: List<TicketO>)

data class TicketO(
    val id: Long,
    val description: String,
    val weight: Int,
    val epic: Long,
    val pin: Long?,
    val dependencies: Set<Long>,
    val crossBoardDependencies: Set<StoryRequestO>,
    val crossBoardDependents: Boolean
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed class NotificationO(open val id: Long) {

  @JsonTypeName("IncomingStoryRequest")
  data class IncomingStoryRequest(
      override val id: Long,
      val storyRequestId: Long,
      val sender: String,
      val sprint: String,
      val epic: String,
      val description: String,
      val points: Int,
      val notes: String) : NotificationO(id)

  @JsonTypeName("StoryRequestAccepted")
  data class StoryRequestAccepted(
      override val id: Long,
      val sender: String,
      val description: String,
      val notes: String) : NotificationO(id)

  @JsonTypeName("StoryRequestRejected")
  data class StoryRequestRejected(
      override val id: Long,
      val sender: String,
      val description: String,
      val notes: String) : NotificationO(id)

  @JsonTypeName("StoryRequestWithdrawn")
  data class StoryRequestWithdrawn(
      override val id: Long,
      val sender: String,
      val description: String,
      val notes: String) : NotificationO(id)

  // Exactly the same as incoming
  @JsonTypeName("StoryRequestResubmitted")
  data class StoryRequestResubmitted(
      override val id: Long,
      val storyRequestId: Long,
      val sender: String,
      val sprint: String,
      val epic: String,
      val description: String,
      val points: Int,
      val notes: String) : NotificationO(id)
}

data class StoryRequestO(
    val id: Long,
    val state: String,
    val boardId: Long, // TODO story, epic, board names?
    val storyId: Long?,
    val storyDescription: String,
    val storyPoints: Int,
    val storyEpicId: Long,
    val storySprintId: Long,
    val notes: String
)

data class StoryRequestI(
    val boardId: Long,
    val storyId: Long,
    val storyDescription: String,
    val storyPoints: Int,
    val storyEpicId: Long,
    val storySprintId: Long,
    val notes: String = ""
)

enum class SRState {
  Pending, Accepted, Rejected, Withdrawn
}

data class StateChangeInput(
    val notes: String)

data class SprintE(
    val name: String,
    val goal: String,
    val capacity: Int)

data class Fact(
    val name: String,
    val args: List<String>)

data class Soln(
    val ticketId: Long,
    val sprintId: Long
)

data class PinI(val ticketId: Long, val sprintId: Long)

data class DepI(val fromTicketId: Long, val toTicketId: Long)

data class BoardD(val name: String, val sprint: Int?)
data class CrossBoardDep(val from: BoardD, val to: BoardD)
data class CrossBoardDeps(val deps: List<CrossBoardDep>, val maxSprint: Int)

// For representing problems

data class EpicP(val id: Long, val tickets: Set<TicketP>, val priority: Int)
data class DependencyP(val fromTicketId: Long, val toTicketId: Long)
data class PinP(val ticketId: Long, val sprintId: Long)
data class SprintP(val id: Long, val capacity: Int)
data class TicketP(val id: Long, val weight: Int)

data class Problem(
    val epics: Set<EpicP>,
    val tickets: Set<TicketP>,
    val sprints: Set<SprintP>,
    val pins: Set<PinP>,
    val deps: Set<DependencyP>)
