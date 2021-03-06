package com.sourceclear.agile.piplanning.service.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.sourceclear.agile.piplanning.objects.NotificationO
import com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS
import com.sourceclear.agile.piplanning.service.jooq.tables.records.NotificationsRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

interface Notifications {
  fun create(n: NotificationsRecord, create: DSLContext): NotificationO;
}

@Service
class NotificationsImpl : Notifications {
  val mapper = ObjectMapper().registerModule(KotlinModule())

  override fun create(n: NotificationsRecord, create: DSLContext): NotificationO {
    return when (n.type) {
      NotificationO.IncomingStoryRequest::class.simpleName -> {
        create.select(
                STORY_REQUESTS.ID,
                STORY_REQUESTS.storyRequestsFromBoardIdFkey().NAME,
                STORY_REQUESTS.epics().NAME,
                STORY_REQUESTS.sprints().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.TO_TICKET_WEIGHT,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (id, sender, epic, sprint, desc, points, notes) ->
              NotificationO.IncomingStoryRequest(id = n.id, storyRequestId = id, sender = sender, epic = epic,
                  sprint = sprint, description = desc, points = points, notes = notes)
            }
      }
      NotificationO.StoryRequestAccepted::class.simpleName -> {
        create.select(
                STORY_REQUESTS.storyRequestsToBoardIdFkey().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (sender, desc, notes) ->
              NotificationO.StoryRequestAccepted(id = n.id, sender = sender,
                  description = desc, notes = notes)
            }
      }
      NotificationO.StoryRequestRejected::class.simpleName -> {
        create.select(
                STORY_REQUESTS.storyRequestsToBoardIdFkey().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (sender, desc, notes) ->
              NotificationO.StoryRequestRejected(id = n.id, sender = sender,
                  description = desc, notes = notes)
            }
      }
      NotificationO.StoryRequestWithdrawn::class.simpleName -> {
        create.select(
                STORY_REQUESTS.storyRequestsFromBoardIdFkey().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (sender, desc, notes) ->
              NotificationO.StoryRequestWithdrawn(id = n.id, sender = sender,
                  description = desc, notes = notes)
            }
      }
      NotificationO.StoryRequestResubmitted::class.simpleName -> {
        create.select(
                STORY_REQUESTS.ID,
                STORY_REQUESTS.storyRequestsFromBoardIdFkey().NAME,
                STORY_REQUESTS.epics().NAME,
                STORY_REQUESTS.sprints().NAME,
                STORY_REQUESTS.TO_TICKET_DESCRIPTION,
                STORY_REQUESTS.TO_TICKET_WEIGHT,
                STORY_REQUESTS.NOTES)
            .from(STORY_REQUESTS)
            .where(STORY_REQUESTS.ID.eq(n.storyRequestId))
            .fetchOne { (id, sender, epic, sprint, desc, points, notes) ->
              // Exactly the same as incoming except for the constructor name
              NotificationO.StoryRequestResubmitted(id = n.id, storyRequestId = id, sender = sender, epic = epic,
                  sprint = sprint, description = desc, points = points, notes = notes)
            }
      }
      else -> throw IllegalStateException("unrecognized notification type " + n.type)
    }
  }
}
