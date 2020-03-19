/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.controllers

import com.sourceclear.agile.piplanning.service.entities.Dependency
import com.sourceclear.agile.piplanning.service.entities.Epic
import com.sourceclear.agile.piplanning.service.entities.Pin
import com.sourceclear.agile.piplanning.objects.DepI
import com.sourceclear.agile.piplanning.objects.EpicI
import com.sourceclear.agile.piplanning.objects.EpicO
import com.sourceclear.agile.piplanning.objects.PinI
import com.sourceclear.agile.piplanning.objects.SRState.*
import com.sourceclear.agile.piplanning.objects.StateChangeInput
import com.sourceclear.agile.piplanning.objects.StoryRequestI
import com.sourceclear.agile.piplanning.service.entities.login.User
import com.sourceclear.agile.piplanning.service.jooq.tables.Boards.BOARDS
import com.sourceclear.agile.piplanning.service.jooq.tables.Epics
import com.sourceclear.agile.piplanning.service.jooq.tables.Epics.EPICS
import com.sourceclear.agile.piplanning.service.jooq.tables.Solutions.SOLUTIONS
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS
import com.sourceclear.agile.piplanning.service.jooq.tables.Tickets.TICKETS
import com.sourceclear.agile.piplanning.service.jooq.tables.records.BoardsRecord
import com.sourceclear.agile.piplanning.service.jooq.tables.records.StoryRequestsRecord
import com.sourceclear.agile.piplanning.service.repositories.BoardRepository
import com.sourceclear.agile.piplanning.service.repositories.EpicRepository
import com.sourceclear.agile.piplanning.service.repositories.SprintRepository
import com.sourceclear.agile.piplanning.service.repositories.TicketRepository
import org.jooq.impl.DSL
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
open class BoardControllerK @Autowired constructor(
    private val epicRepository: EpicRepository,
    private val sprintRepository: SprintRepository,
    private val ticketRepository: TicketRepository,
    private val boardRepository: BoardRepository,

    private val create: DefaultDSLContext
) {

  //
  // Epics
  //

  @GetMapping("/epic/{epicId}")
  @Transactional(readOnly = true)
  open fun getEpic(@PathVariable epicId: Long): ResponseEntity<EpicO> {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    return ResponseEntity.ok(EpicO(e.id, e.name, e.priority))
  }

  @GetMapping("/board/{boardId}/epics")
  @Transactional
  open fun listEpics(@PathVariable boardId: Long): List<EpicO> {
    val b = boardRepository.findById(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    return b.epics
        // ensure ordering is stable if same priority
        .sortedWith(compareBy({ it.priority }, { it.id }))
        .map { EpicO(it.id, it.name, it.priority) }
  }

  @PostMapping("/board/{boardId}/epics")
  @Transactional
  open fun createEpic(@PathVariable boardId: Long, @Valid @RequestBody epic: EpicI): EpicO {
    val b = boardRepository.findById(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val e = epicRepository.save(Epic(epic.name, epic.priority, b))
    return EpicO(e.id, e.name, e.priority)
  }

  @PutMapping("/epic/{epicId}")
  @Transactional
  open fun updateEpic(@PathVariable epicId: Long, @Valid @RequestBody epic: EpicI) {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    e.name = epic.name
    e.priority = epic.priority
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
      throw ResponseStatusException(HttpStatus.BAD_REQUEST)
    }

    epicRepository.deleteById(epicId)
  }

  /**
   * PUTs a ticket under a new epic, hur hur
   */
  @PutMapping("/epic/{epicId}/ticket/{ticketId}")
  @Transactional
  open fun moveTicket(@PathVariable epicId: Long, @PathVariable ticketId: Long) {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val t = ticketRepository.findById(ticketId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    t.epic = e
  }

  //
  // Pins
  //

  @PostMapping("/board/{boardId}/pins")
  @Transactional
  open fun createPin(@PathVariable boardId: Long, @Valid @RequestBody pin: PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val s = sprintRepository.findById(pin.sprintId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val t = ticketRepository.findById(pin.ticketId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.pins.removeIf { it.ticketId == pin.ticketId }
    b.pins.add(Pin(s.id, t.id))
  }

  @DeleteMapping("/board/{boardId}/pins")
  @Transactional
  open fun deletePin(@PathVariable boardId: Long, @Valid @RequestBody pin: PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.pins = b.pins.filterNotTo(HashSet()) { it.ticketId == pin.ticketId }
  }

  //
  // Deps
  //

  @PostMapping("/board/{boardId}/dependencies")
  @Transactional
  open fun createDep(@PathVariable boardId: Long, @Valid @RequestBody dep: DepI) {
    if (dep.fromTicketId == dep.toTicketId) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST);
    }
    val b = boardRepository.findWithDeps(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    if (b.deps.none { it.fromTicketId == dep.fromTicketId && it.toTicketId == dep.toTicketId }) {
      b.deps.add(Dependency(dep.fromTicketId, dep.toTicketId))
    }
  }

  @DeleteMapping("/board/{boardId}/dependencies")
  @Transactional
  open fun deleteDep(@PathVariable boardId: Long, @Valid @RequestBody dep: DepI) {
    val b = boardRepository.findWithDeps(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.deps.remove(Dependency(dep.fromTicketId, dep.toTicketId))
  }

  private val notFound = ResponseStatusException(HttpStatus.NOT_FOUND)
  private val badRequest = ResponseStatusException(HttpStatus.BAD_REQUEST)
  private val oopsieWoopsie = ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
  private val unauthorized = ResponseStatusException(HttpStatus.UNAUTHORIZED)

  //
  // Story requests
  //

  @PostMapping("/board/{boardId}/requests")
  @Transactional
  open fun createStoryRequest(
      @AuthenticationPrincipal user: User,
      @PathVariable boardId: Long, @Valid @RequestBody storyRequest: StoryRequestI) {
    val b = validateBoard(user, boardId)

    val s = create.newRecord(STORY_REQUESTS);
    s.state = Pending.name
    s.fromBoardId = boardId
    fromStoryRequest(s, storyRequest, b.name)
    // at this point there is no to-ticket
    s.store()
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

    val t = create.newRecord(TICKETS);
    t.boardId = s.toBoardId
    t.description = s.toTicketDescription
    t.weight = s.toTicketWeight
    t.epicId = s.toTicketEpicId
    t.store()

    s.notes = addToNote(s.notes, b.name, input.notes)

    // Get the ticket id that was just filled in
    s.toTicketId = t.id
    s.store()

    val sol = create.newRecord(SOLUTIONS);
    sol.boardId = s.toBoardId
    sol.ticketId = t.id
    sol.sprintId = s.toTicketSprintId
    sol.unassigned = false
    sol.preview = false
    sol.store()
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

    create.delete(TICKETS)
        .where(TICKETS.ID.eq(ticket))
        .execute();
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
  }

  private fun validateBoard(user: User, boardId: Long): BoardsRecord {
    val b = create.fetchOne(BOARDS, BOARDS.ID.eq(boardId))
        ?: throw notFound
    if (user.id != b.owner) {
      throw unauthorized
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
