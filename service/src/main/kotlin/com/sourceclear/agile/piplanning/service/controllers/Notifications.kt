package com.sourceclear.agile.piplanning.service.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS
import com.sourceclear.agile.piplanning.service.jooq.tables.records.NotificationsRecord
import org.jooq.DSLContext

object Notifications {
  val mapper = ObjectMapper().registerModule(KotlinModule())

  fun create(n: NotificationsRecord, create: DSLContext): NotificationO {
    return when (n.type) {
      NotificationO.StoryRequest::class.simpleName -> {
        create.select(
                STORY_REQUESTS.storyRequestsFromBoardIdFkey().NAME,
                STORY_REQUESTS.epics().NAME,
                STORY_REQUESTS.sprints().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.TO_TICKET_WEIGHT,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (sender, epic, sprint, desc, points, notes) ->
              NotificationO.StoryRequest(id = n.id, sender = sender, epic = epic,
                  sprint = sprint, description = desc, points = points, notes = notes)
            }
      }
      else -> throw IllegalStateException("unrecognized notification type " + n.type)
    }
  }
}
