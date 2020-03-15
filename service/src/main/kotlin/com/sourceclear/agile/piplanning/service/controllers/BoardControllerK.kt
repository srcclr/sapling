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
import com.sourceclear.agile.piplanning.service.repositories.BoardRepository
import com.sourceclear.agile.piplanning.service.repositories.EpicRepository
import com.sourceclear.agile.piplanning.service.repositories.SprintRepository
import com.sourceclear.agile.piplanning.service.repositories.TicketRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private val boardRepository: BoardRepository
) {

  //
  // Epics
  //

  @GetMapping("/epic/{epicId}")
  @Transactional(readOnly = true)
  open fun getEpic(@PathVariable epicId : Long): ResponseEntity<EpicO> {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    return ResponseEntity.ok(EpicO(e.id, e.name, e.priority))
  }

  @GetMapping("/board/{boardId}/epics")
  @Transactional
  open fun listEpics(@PathVariable boardId : Long): List<EpicO> {
    val b = boardRepository.findById(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    return b.epics.sortedBy { it.priority }.map { EpicO(it.id, it.name, it.priority) }
  }

  @PostMapping("/board/{boardId}/epics")
  @Transactional
  open fun createEpic(@PathVariable boardId : Long, @Valid @RequestBody epic : EpicI): EpicO {
    val b = boardRepository.findById(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val e = epicRepository.save(Epic(epic.name, epic.priority, b))
    return EpicO(e.id, e.name, e.priority)
  }

  @PutMapping("/epic/{epicId}")
  @Transactional
  open fun updateEpic(@PathVariable epicId : Long, @Valid @RequestBody epic : EpicI) {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    e.name = epic.name
    e.priority = epic.priority
  }

  @DeleteMapping("/epic/{epicId}")
  @Transactional
  open fun deleteEpic(@PathVariable epicId : Long) {
    epicRepository.deleteById(epicId)
  }

  /**
   * PUTs a ticket under a new epic, hur hur
   */
  @PutMapping("/epic/{epicId}/ticket/{ticketId}")
  @Transactional
  open fun moveTicket(@PathVariable epicId : Long, @PathVariable ticketId : Long) {
    val e = epicRepository.findById(epicId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val t = ticketRepository.findById(ticketId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    t.epic = e
  }

  //
  // Pins
  //

  @PostMapping("/board/{boardId}/pins")
  @Transactional
  open fun createPin(@PathVariable boardId : Long, @Valid @RequestBody pin : PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val s = sprintRepository.findById(pin.sprintId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    val t = ticketRepository.findById(pin.ticketId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.pins.removeIf { it.ticketId == pin.ticketId }
    b.pins.add(Pin(s.id, t.id))
  }

  @DeleteMapping("/board/{boardId}/pins")
  @Transactional
  open fun deletePin(@PathVariable boardId : Long, @Valid @RequestBody pin : PinI) {
    val b = boardRepository.findWithPins(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.pins = b.pins.filterNotTo(HashSet()) { it.ticketId == pin.ticketId }
  }

  //
  // Deps
  //

  @PostMapping("/board/{boardId}/dependencies")
  @Transactional
  open fun createDep(@PathVariable boardId : Long, @Valid @RequestBody dep : DepI) {
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
  open fun deleteDep(@PathVariable boardId : Long, @Valid @RequestBody dep: DepI) {
    val b = boardRepository.findWithDeps(boardId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }!!
    b.deps.remove(Dependency(dep.fromTicketId, dep.toTicketId))
  }
}
