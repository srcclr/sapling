/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.controllers

import com.sourceclear.agile.piplanning.objects.BoardD
import com.sourceclear.agile.piplanning.objects.CrossBoardDep
import com.sourceclear.agile.piplanning.objects.CrossBoardDeps
import com.sourceclear.agile.piplanning.objects.DepI
import com.sourceclear.agile.piplanning.objects.EpicI
import com.sourceclear.agile.piplanning.objects.EpicO
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.objects.PinI
import com.sourceclear.agile.piplanning.objects.SRState.*
import com.sourceclear.agile.piplanning.objects.StateChangeInput
import com.sourceclear.agile.piplanning.objects.StoryRequestI
import com.sourceclear.agile.piplanning.service.configs.Exceptions.*
import com.sourceclear.agile.piplanning.service.entities.Dependency
import com.sourceclear.agile.piplanning.service.entities.Epic
import com.sourceclear.agile.piplanning.service.entities.Pin
import com.sourceclear.agile.piplanning.service.entities.login.User
import com.sourceclear.agile.piplanning.service.jooq.tables.Boards.BOARDS
import com.sourceclear.agile.piplanning.service.jooq.tables.Epics.EPICS
import com.sourceclear.agile.piplanning.service.jooq.tables.Notifications.NOTIFICATIONS
import com.sourceclear.agile.piplanning.service.jooq.tables.Solutions.SOLUTIONS
import com.sourceclear.agile.piplanning.service.jooq.tables.Sprints.SPRINTS
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS
import com.sourceclear.agile.piplanning.service.jooq.tables.TicketPins.TICKET_PINS
import com.sourceclear.agile.piplanning.service.jooq.tables.Tickets.TICKETS
import com.sourceclear.agile.piplanning.service.jooq.tables.records.BoardsRecord
import com.sourceclear.agile.piplanning.service.jooq.tables.records.StoryRequestsRecord
import com.sourceclear.agile.piplanning.service.repositories.BoardRepository
import com.sourceclear.agile.piplanning.service.repositories.EpicRepository
import com.sourceclear.agile.piplanning.service.repositories.SprintRepository
import com.sourceclear.agile.piplanning.service.repositories.TicketRepository
import org.jooq.impl.DSL
import org.jooq.impl.DSL.max
import org.jooq.impl.DSL.select
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
open class BoardControllerK @Autowired constructor(
    private val epicRepository: EpicRepository,
    private val sprintRepository: SprintRepository,
    private val ticketRepository: TicketRepository,
    private val boardRepository: BoardRepository,
    private val webSockets: WebSockets,
    private val create: DefaultDSLContext
) {

  //
  // Epics
  //

  @Deprecated("never used and subsumed by main endpoint")
  @GetMapping("/epic/{epicId}")
  @Transactional(readOnly = true)
  open fun getEpic(@PathVariable epicId: Long): ResponseEntity<EpicO> {
    val e = epicRepository.findById(epicId).orElseThrow(notFound)!!
    return ResponseEntity.ok(EpicO(e.id, e.name, e.priority))
  }

  @Deprecated("subsumed by main endpoint")
  @GetMapping("/board/{boardId}/epics")
  @Transactional
  open fun listEpics(@PathVariable boardId: Long): List<EpicO> {
    val b = boardRepository.findById(boardId).orElseThrow(notFound)!!
    return b.epics
        // ensure ordering is stable if same priority
        .sortedWith(compareBy({ it.priority }, { it.id }))
        .map { EpicO(it.id, it.name, it.priority) }
  }

  @PostMapping("/board/{boardId}/epics")
  @Transactional
  open fun createEpic(@PathVariable boardId: Long, @Valid @RequestBody epic: EpicI): EpicO {
    val b = boardRepository.findById(boardId).orElseThrow(notFound)!!
    val e = epicRepository.save(Epic(epic.name, epic.priority, b))
    webSockets.broadcastBoardUpdate(boardId)
    return EpicO(e.id, e.name, e.priority)
  }

  @PutMapping("/epic/{epicId}")
  @Transactional
  open fun updateEpic(@PathVariable epicId: Long, @Valid @RequestBody epic: EpicI) {
    val e = epicRepository.findById(epicId).orElseThrow(notFound)!!
    e.name = epic.name
    e.priority = epic.priority
    webSockets.broadcastBoardUpdate(e.board.id)
  }

  @DeleteMapping("/epic/{epicId}")
  @Transactional
  open fun deleteEpic(@PathVariable epicId: Long) {

    val hasTicketInvolvedInAStoryRequest =
        create.fetchExists(
            DSL.selectOne()
                .from(EPICS
                    .join(TICKETS).onKey()
                    .join(STORY_REQUESTS)
                    .on(STORY_REQUESTS.TO_TICKET_ID.eq(TICKETS.ID).or(STORY_REQUESTS.FROM_TICKET_ID.eq(TICKETS.ID))))
                .where(EPICS.ID.eq(epicId)))

    if (hasTicketInvolvedInAStoryRequest) {
      throw badRequest
    }

    val (board) = create.select(EPICS.BOARD_ID).from(EPICS).where(EPICS.ID.eq(epicId)).fetchOne()
    epicRepository.deleteById(epicId)
    webSockets.broadcastBoardUpdate(board)
  }

  /**
   * PUTs a ticket under a new epic, hur hur
   */
  @PutMapping("/epic/{epicId}/ticket/{ticketId}")
  @Transactional
  open fun moveTicket(@PathVariable epicId: Long, @PathVariable ticketId: Long) {
    val e = epicRepository.findById(epicId).orElseThrow(notFound)!!
    val t = ticketRepository.findById(ticketId).orElseThrow(notFound)!!
    t.epic = e
    webSockets.broadcastBoardUpdate(t.board.id)
  }

  //
  // Pins
  //

  @PostMapping("/board/{boardId}/pins")
  @Transactional
  open fun createPin(@PathVariable boardId: Long, @Valid @RequestBody pin: PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow(notFound)!!
    val s = sprintRepository.findById(pin.sprintId).orElseThrow(notFound)!!
    val t = ticketRepository.findById(pin.ticketId).orElseThrow(notFound)!!
    b.pins.removeIf { it.ticketId == pin.ticketId }
    b.pins.add(Pin(s.id, t.id))
    webSockets.broadcastBoardUpdate(boardId)
  }

  @PostMapping("/board/{boardId}/pins/all")
  @Transactional
  open fun pinAll(@PathVariable boardId: Long) {
    create.fetchExists(BOARDS, BOARDS.ID.eq(boardId)) || throw notFound

    create.insertInto(TICKET_PINS, TICKET_PINS.TICKET_ID, TICKET_PINS.SPRINT_ID, TICKET_PINS.BOARD_ID)
        .select(
            select(SOLUTIONS.TICKET_ID, SOLUTIONS.SPRINT_ID, SOLUTIONS.BOARD_ID)
                .from(SOLUTIONS)
                .where(SOLUTIONS.BOARD_ID.eq(boardId)))
        .execute()
    webSockets.broadcastBoardUpdate(boardId)
  }

  @DeleteMapping("/board/{boardId}/pins/all")
  @Transactional
  open fun unpinAll(@PathVariable boardId: Long) {
    create.fetchExists(BOARDS, BOARDS.ID.eq(boardId)) || throw notFound

    create.delete(TICKET_PINS)
        .where(TICKET_PINS.BOARD_ID.eq(boardId))
        .execute()
    webSockets.broadcastBoardUpdate(boardId)
  }

  @DeleteMapping("/board/{boardId}/pins")
  @Transactional
  open fun deletePin(@PathVariable boardId: Long, @Valid @RequestBody pin: PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow(notFound)!!
    b.pins = b.pins.filterNotTo(HashSet()) { it.ticketId == pin.ticketId }
    webSockets.broadcastBoardUpdate(boardId)
  }

  //
  // Deps
  //

  @PostMapping("/board/{boardId}/dependencies")
  @Transactional
  open fun createDep(@PathVariable boardId: Long, @Valid @RequestBody dep: DepI) {
    if (dep.fromTicketId == dep.toTicketId) {
      throw badRequest
    }
    val b = boardRepository.findWithDeps(boardId).orElseThrow(notFound)!!
    if (b.deps.none { it.fromTicketId == dep.fromTicketId && it.toTicketId == dep.toTicketId }) {
      b.deps.add(Dependency(dep.fromTicketId, dep.toTicketId))
    }
    webSockets.broadcastBoardUpdate(boardId)
  }

  @DeleteMapping("/board/{boardId}/dependencies")
  @Transactional
  open fun deleteDep(@PathVariable boardId: Long, @Valid @RequestBody dep: DepI) {
    val b = boardRepository.findWithDeps(boardId).orElseThrow(notFound)!!
    b.deps.remove(Dependency(dep.fromTicketId, dep.toTicketId))
    webSockets.broadcastBoardUpdate(boardId)
  }

  //
  // Story requests
  //

  @PostMapping("/board/{boardId}/requests")
  @Transactional
  open fun createStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long, @Valid @RequestBody storyRequest: StoryRequestI) {
    val b = validateBoard(user, boardId)

    val s = create.newRecord(STORY_REQUESTS)
    s.state = Pending.name
    s.fromBoardId = boardId
    fromStoryRequest(s, storyRequest, b.name)
    // at this point there is no to-ticket
    s.store()

    create.newRecord(NOTIFICATIONS).let {
      it.type = NotificationO.IncomingStoryRequest::class.simpleName
      it.storyRequestId = s.id
      it.recipientId = s.toBoardId
      it.store()
    }

    webSockets.broadcastBoardUpdate(boardId)
  }

  @PutMapping("/board/{boardId}/request/{requestId}")
  @Transactional
  open fun editStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable requestId: Long,
      @Valid @RequestBody storyRequest: StoryRequestI) {
    val b = validateBoard(user, boardId)

    val s = create.fetchOne(STORY_REQUESTS, STORY_REQUESTS.ID.eq(requestId))
        ?: throw notFound
    // similar to create, except only the fields below can be edited
    fromStoryRequest(s, storyRequest, b.name)
    s.store()

    webSockets.broadcastBoardUpdate(boardId)
  }

  private fun fromStoryRequest(s: StoryRequestsRecord, storyRequest: StoryRequestI, boardName: String) {
    s.toBoardId = storyRequest.boardId
    s.fromTicketId = storyRequest.storyId
    s.toTicketDescription = storyRequest.storyDescription
    s.toTicketEpicId = storyRequest.storyEpicId
    s.toTicketWeight = storyRequest.storyPoints
    s.toTicketSprintId = storyRequest.storySprintId
    s.notes = addToNote(s.notes, boardName, storyRequest.notes)
  }

  /*
   digraph G {
    nonexistent -> pending [label="submitted"];
    pending -> accepted [label="accept"];
    pending -> rejected [label="reject"];
    rejected -> pending [label="resubmitted"];
    accepted -> withdrawn [label="withdraw"];
    withdrawn -> pending [label="resubmitted"];
    pending -> withdrawn [label="withdraw"];
  }
  */

  @PostMapping("/board/{boardId}/request/{requestId}/accept")
  @Transactional
  open fun acceptStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable requestId: Long,
      @Valid @RequestBody input: StateChangeInput) {
    val b = validateBoard(user, boardId)

    val s = create.fetchOne(STORY_REQUESTS, STORY_REQUESTS.ID.eq(requestId))
        ?: throw notFound
    if (boardId != s.toBoardId) { // has to be sent from the receiving board
      throw unauthorized
    }

    if (valueOf(s.state) == Pending) {
      s.state = Accepted.name
    } else {
      throw badRequest
    }

    val t = create.newRecord(TICKETS)
    t.boardId = s.toBoardId
    t.description = s.toTicketDescription
    t.weight = s.toTicketWeight
    t.epicId = s.toTicketEpicId
    t.store()

    s.notes = addToNote(s.notes, b.name, input.notes)

    // Get the ticket id that was just filled in
    s.toTicketId = t.id
    s.store()

    create.newRecord(SOLUTIONS).let {
      it.boardId = s.toBoardId
      it.ticketId = t.id
      it.sprintId = s.toTicketSprintId
      it.preview = false
      it.store()
    }

    create.newRecord(NOTIFICATIONS).let {
      it.type = NotificationO.StoryRequestAccepted::class.simpleName
      it.storyRequestId = s.id
      it.recipientId = s.fromBoardId
      it.store()
    }

    create.update(NOTIFICATIONS)
        .set(NOTIFICATIONS.ACKNOWLEDGED, true)
        .where(NOTIFICATIONS.STORY_REQUEST_ID.eq(requestId).and(NOTIFICATIONS.TYPE.eq(NotificationO.IncomingStoryRequest::class.simpleName)))
        .execute();

    webSockets.broadcastBoardUpdate(boardId)
  }

  @PostMapping("/board/{boardId}/request/{requestId}/reject")
  @Transactional
  open fun rejectStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable requestId: Long,
      @Valid @RequestBody input: StateChangeInput) {
    val b = validateBoard(user, boardId)

    val s = create.fetchOne(STORY_REQUESTS, STORY_REQUESTS.ID.eq(requestId))
        ?: throw notFound
    if (boardId != s.toBoardId) { // has to be sent from the receiving board
      throw unauthorized
    }

    if (valueOf(s.state) == Pending) {
      s.state = Rejected.name
    } else {
      throw badRequest
    }

    s.notes = addToNote(s.notes, b.name, input.notes)

    // Null this before deleting, otherwise it cascades and goes as well
    val ticket = s.toTicketId
    s.toTicketId = null
    s.store()

    if (ticket != null) {
      // If the ticket hasn't been created and the story was rejected outright
      create.delete(TICKETS)
          .where(TICKETS.ID.eq(ticket))
          .execute();
    }

    create.newRecord(NOTIFICATIONS).let {
      it.type = NotificationO.StoryRequestRejected::class.simpleName
      it.storyRequestId = s.id
      it.recipientId = s.fromBoardId
      it.store()
    }

    create.update(NOTIFICATIONS)
        .set(NOTIFICATIONS.ACKNOWLEDGED, true)
        .where(NOTIFICATIONS.STORY_REQUEST_ID.eq(requestId).and(NOTIFICATIONS.TYPE.eq(NotificationO.IncomingStoryRequest::class.simpleName)))
        .execute();

    webSockets.broadcastBoardUpdate(boardId)
  }

  @PostMapping("/board/{boardId}/request/{requestId}/withdraw")
  @Transactional
  open fun withdrawStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable requestId: Long,
      @Valid @RequestBody input: StateChangeInput) {
    val b = validateBoard(user, boardId)

    val s = create.fetchOne(STORY_REQUESTS, STORY_REQUESTS.ID.eq(requestId))
        ?: throw notFound
    if (boardId != s.fromBoardId) {
      throw unauthorized
    }

    val state = valueOf(s.state)
    if (state == Pending || state == Accepted) {
      s.state = Withdrawn.name
    } else {
      throw badRequest
    }

    s.notes = addToNote(s.notes, b.name, input.notes)

    // Null this before deleting, otherwise it cascades and goes as well
    val ticket = s.toTicketId
    s.toTicketId = null
    s.store()

    create.delete(TICKETS)
        .where(TICKETS.ID.eq(ticket))
        .execute();

    create.update((NOTIFICATIONS))
        .set(NOTIFICATIONS.ACKNOWLEDGED, true)
        .where(NOTIFICATIONS.STORY_REQUEST_ID.eq(s.id))
        .and(NOTIFICATIONS.TYPE.eq(NotificationO.IncomingStoryRequest::class.simpleName)
            .or(NOTIFICATIONS.TYPE.eq(NotificationO.StoryRequestResubmitted::class.simpleName)))
        .execute();

    create.newRecord(NOTIFICATIONS).let {
      it.type = NotificationO.StoryRequestWithdrawn::class.simpleName
      it.storyRequestId = s.id
      it.recipientId = s.toBoardId
      it.store()
    }

    webSockets.broadcastBoardUpdate(boardId)
  }

  @PostMapping("/board/{boardId}/request/{requestId}/resubmit")
  @Transactional
  open fun resubmitStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable requestId: Long,
      @Valid @RequestBody input: StateChangeInput) {
    val b = validateBoard(user, boardId)

    val s = create.fetchOne(STORY_REQUESTS, STORY_REQUESTS.ID.eq(requestId))
        ?: throw notFound
    if (boardId != s.fromBoardId) {
      throw unauthorized
    }

    val state = valueOf(s.state)
    if (state == Rejected || state == Withdrawn) {
      s.state = Pending.name
    } else {
      throw badRequest
    }
    s.notes = addToNote(s.notes, b.name, input.notes)
    s.store()

    create.newRecord(NOTIFICATIONS).let {
      it.type = NotificationO.StoryRequestResubmitted::class.simpleName
      it.storyRequestId = s.id
      it.recipientId = s.toBoardId
      it.store()
    }

    webSockets.broadcastBoardUpdate(boardId)
  }

  @GetMapping("/boards/dependencies")
  @Transactional
  open fun crossBoardDependencies(): ResponseEntity<CrossBoardDeps> {

    val b1 = BOARDS.`as`("b1")
    val b2 = BOARDS.`as`("b2")
    val sr = STORY_REQUESTS.`as`("sr")
    val s1 = SOLUTIONS.`as`("s1")
    val s2 = SOLUTIONS.`as`("s2")
    val sp1 = SPRINTS.`as`("sp1")
    val sp2 = SPRINTS.`as`("sp2")

    // Unfortunately we can't use onKey for the last two;
    // also, onKey only works when joining to the first table.
    // https://github.com/jOOQ/jOOQ/issues/7626
    val deps =
        create.select(b1.NAME, b2.NAME, sp1.ORDINAL, sp2.ORDINAL)
            .from(sr
                .join(b1).onKey(STORY_REQUESTS.FROM_BOARD_ID)
                .join(b2).onKey(STORY_REQUESTS.TO_BOARD_ID)
                .join(s1).on(b1.ID.eq(s1.BOARD_ID).and(sr.FROM_TICKET_ID.eq(s1.TICKET_ID)))
                .join(s2).on(b2.ID.eq(s2.BOARD_ID).and(sr.TO_TICKET_ID.eq(s2.TICKET_ID)))
                .join(sp1).on(s1.SPRINT_ID.eq(sp1.ID))
                .join(sp2).on(s2.SPRINT_ID.eq(sp2.ID)))
            .where(sr.STATE.eq(Accepted.name))
            .fetch { (b1, b2, sp1, sp2) ->
              CrossBoardDep(BoardD(b1, sp1), BoardD(b2, sp2))
            }

    val (maxSprint) = create.select(max(sp1.ORDINAL))
        .from(sr
            .join(b1).on(b1.ID.eq(sr.FROM_BOARD_ID).or(b1.ID.eq(sr.TO_BOARD_ID)))
            .join(sp1).on(sp1.BOARD_ID.eq(b1.ID)))
        .fetchOne()

    return ResponseEntity.ok(CrossBoardDeps(deps, maxSprint))
  }

  @PostMapping("/boards/{boardId}/notifications/{notificationId}/acknowledge")
  @Transactional
  open fun ackNotification(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long,
      @PathVariable notificationId: Long) {
    validateBoard(user, boardId)

    val n = create.selectFrom(NOTIFICATIONS)
        .where(NOTIFICATIONS.ID.eq(notificationId).and(NOTIFICATIONS.RECIPIENT_ID.eq(boardId)))
        .fetchOne()
        ?: throw badRequest

    n.acknowledged = true
    n.store()

    webSockets.broadcastBoardUpdate(boardId)
  }

  private fun validateBoard(user: User, boardId: Long): BoardsRecord {
    val b = create.fetchOne(BOARDS, BOARDS.ID.eq(boardId))
        ?: throw notFound
    if (user.id != b.owner) {
      // throw unauthorized
    }
    // TODO memberships
    return b
  }

  /**
   * Brutally practical alternative to storing the history of each state change
   */
  private fun addToNote(existing: String?, boardName: String, addition: String): String {
    val e =
        if (existing.isNullOrBlank()) {
          ""
        } else {
          existing + "\n\n";
        }
    return "$e$boardName:\n$addition"
  }
}
