package com.sourceclear.agile.piplanning.service.services

import com.sourceclear.agile.piplanning.objects.BoardO
import com.sourceclear.agile.piplanning.objects.EpicO
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.objects.SprintO
import com.sourceclear.agile.piplanning.objects.StoryRequestO
import com.sourceclear.agile.piplanning.objects.TicketO
import com.sourceclear.agile.piplanning.service.configs.Exceptions
import com.sourceclear.agile.piplanning.service.entities.Solution
import com.sourceclear.agile.piplanning.service.jooq.tables.Boards.BOARDS
import com.sourceclear.agile.piplanning.service.jooq.tables.Epics
import com.sourceclear.agile.piplanning.service.jooq.tables.Epics.EPICS
import com.sourceclear.agile.piplanning.service.jooq.tables.Notifications.NOTIFICATIONS
import com.sourceclear.agile.piplanning.service.jooq.tables.Sprints.SPRINTS
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS
import com.sourceclear.agile.piplanning.service.jooq.tables.TicketDeps.TICKET_DEPS
import com.sourceclear.agile.piplanning.service.jooq.tables.TicketPins.TICKET_PINS
import com.sourceclear.agile.piplanning.service.jooq.tables.Tickets.TICKETS
import com.sourceclear.agile.piplanning.service.jooq.tables.Users.USERS
import com.sourceclear.agile.piplanning.service.jooq.tables.records.StoryRequestsRecord
import com.sourceclear.agile.piplanning.service.jooq.tables.records.TicketsRecord
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface Boards {
  fun reconstruct(rawAssignments: Set<Solution>, boardId: Long): BoardO
}

@Service
open class BoardsImpl @Autowired constructor(
    private val create: DefaultDSLContext,
    private val notifications: Notifications) : Boards {

  /**
   * Given a solution for the given board in flattened form, constructs the nested version
   */
  override fun reconstruct(rawAssignments: Set<Solution>, boardId: Long): BoardO {

    // Load everything for the given board and index it by the thing it will be nested in

    val board = create.selectFrom(BOARDS).where(BOARDS.ID.eq(boardId)).fetchOne() ?: throw Exceptions.notFound;
    val (owner) = create.select(USERS.EMAIL).from(USERS).where(USERS.ID.eq(board.owner)).fetchOne()
        ?: throw Exceptions.notFound;
    val ticketsById = create.selectFrom(TICKETS).where(TICKETS.BOARD_ID.eq(boardId)).fetch().intoMap { it.id }
    val sprints = create.selectFrom(SPRINTS).where(SPRINTS.BOARD_ID.eq(boardId)).fetch()
    val pinsByTicketId = create.selectFrom(TICKET_PINS).where(TICKET_PINS.BOARD_ID.eq(boardId)).fetch()
        .intoMap({ it.ticketId }) { it.sprintId }
    val depsByTicketId = create.selectFrom(TICKET_DEPS).where(TICKET_DEPS.BOARD_ID.eq(boardId)).fetch()
        .groupBy({ it.fromTicketId }) { it.toTicketId }.mapValues { it.value.toSet() }
    val storyRequestsbyTicketId = create.selectFrom(STORY_REQUESTS)
        .where(STORY_REQUESTS.FROM_TICKET_ID.`in`(ticketsById.keys))
        .groupBy { it.fromTicketId }
    val ticketsBlockingCrossBoard = create.select(STORY_REQUESTS.TO_TICKET_ID)
        .from(STORY_REQUESTS)
        .where(STORY_REQUESTS.TO_TICKET_ID.`in`(ticketsById.keys))
        .fetch { (id) -> id }

    // Derived

    val epics = create.selectFrom(EPICS)
        .where(EPICS.BOARD_ID.eq(boardId))
        .orderBy(EPICS.PRIORITY, EPICS.ID)
        .fetch { r -> EpicO(id = r.id, name = r.name, priority = r.priority) }

    val assignmentsBySprintId = rawAssignments
        .filter { it.sprint.isPresent }
        .groupBy({ it.sprint.get().id!! }) { it.ticket.id!! }

    val assignedSprints =
        sprints.sortedBy { it.ordinal }.map {
          SprintO(id = it.id,
              name = it.name,
              goal = it.goal,
              capacity = it.capacity,
              tickets = assignmentsBySprintId.getOrDefault(it.id, listOf())
                  .map { ticketsById[it]!! }
                  .map {
                    createTicket(ticket = it, pin = pinsByTicketId[it.id],
                        deps = depsByTicketId.getOrDefault(it.id, setOf()),
                        blocking = ticketsBlockingCrossBoard.contains(it.id),
                        storyRequests =
                        storyRequestsbyTicketId.getOrDefault(it.id, listOf()))
                  })

        }

    val backlog = (ticketsById.keys subtract rawAssignments.map { it.ticket.id!! })
        .map {
          createTicket(ticket = ticketsById.getValue(it),
              pin = pinsByTicketId[it],
              deps = depsByTicketId.getOrDefault(it, setOf()),
              blocking = ticketsBlockingCrossBoard.contains(it),
              storyRequests = storyRequestsbyTicketId.getOrDefault(it, listOf()))
        }

    val notifications: List<NotificationO> = create.selectFrom(NOTIFICATIONS)
        .where(NOTIFICATIONS.RECIPIENT_ID.eq(boardId).and(NOTIFICATIONS.ACKNOWLEDGED.isFalse))
        .fetch { r -> notifications.create(r, create) }

    return BoardO(
        id = boardId,
        name = board.name,
        owner = owner,
        epics = epics,
        sprints = assignedSprints,
        unassigned = backlog,
        notifications = notifications)
  }

  private fun createTicket(ticket: TicketsRecord,
                           pin: Long?,
                           deps: Set<Long>,
                           storyRequests: List<StoryRequestsRecord>,
                           blocking: Boolean): TicketO {

    val requests = storyRequests
        .map { s ->
          StoryRequestO(
              id = s.id,
              state = s.state,
              boardId = s.toBoardId,
              storyId = s.toTicketId,
              storyDescription = s.toTicketDescription,
              storyPoints = s.toTicketWeight,
              storyEpicId = s.toTicketEpicId,
              storySprintId = s.toTicketSprintId,
              notes = s.notes)
        }.toSet()

    return TicketO(
        id = ticket.id,
        description = ticket.description,
        weight = ticket.weight,
        epic = ticket.epicId,
        pin = pin,
        dependencies = deps,
        crossBoardDependencies = requests,
        crossBoardDependents = blocking)
  }
}
