package com.sourceclear.agile.piplanning.objects

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class EpicI(val name: String, val priority: Int)
data class EpicO(val id: Long, val name: String, val priority: Int)
data class TicketI(val description: String, val weight: Int)
data class TicketO(val id : Long, val description: String, val weight: Int, val home : Boolean,
                   val epic : Long, val pin: Long?, val dependencies: Set<Long>)

data class TicketCD(
    val summary: String,
    val points: Int,
    val sprint: String,
    val epic: String)

data class TicketCU(
    val summary: String,
    val points: Int,
    val sprint: String,
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
    val unassigned: List<TicketO>)

data class SprintO(
    val id: Long,
    val name: String,
    val capacity: Int,
    val tickets: List<TicketO>)

data class SprintI(
    val name: String,
    val capacity: Int)

data class Fact(
    val name: String,
    val args: List<String>)

data class Soln(
    val ticketId: Long,
    val sprintId: Optional<Long>, // for Java interop
    val unassigned: Boolean)

data class PinI(val ticketId : Long, val sprintId : Long)

data class DepI(val fromTicketId : Long, val toTicketId : Long)
