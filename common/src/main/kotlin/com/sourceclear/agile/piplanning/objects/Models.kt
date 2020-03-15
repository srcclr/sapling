package com.sourceclear.agile.piplanning.objects

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
