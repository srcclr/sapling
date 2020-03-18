package com.sourceclear.agile.piplanning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.Problem
import com.sourceclear.agile.piplanning.objects.Soln
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class Client {
  private val httpClient = HttpClient.newBuilder().build()
  val objectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(Jdk8Module())!!
  private val logger: Logger = LoggerFactory.getLogger(Client::class.java)

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  fun solve(uri: URI, problem: Problem): Set<Soln> {

    val request = HttpRequest.newBuilder()
        .uri(uri.resolve("/solve"))
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(problem)))
        .setHeader("Content-Type", "application/json")
        .build()
    val r = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(r.body())
  }

  fun solvePreview(uri: URI, problem: Problem, answers: (Set<Soln>) -> Boolean) {
    val request = HttpRequest.newBuilder()
        .uri(uri.resolve("/solve/stream"))
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(problem)))
        .setHeader("Content-Type", "application/json")
        .build()

    val r = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
    BufferedReader(InputStreamReader(r.body())).use { i ->
      objectMapper.factory.createParser(i).use { parser ->
        try {
          while (true) {
            val v = objectMapper.readValue<Set<Soln>>(parser)
            if (!answers(v)) {
              logger.trace("client aborted")
              break
            }
          }
        } catch (e: IOException) {
          logger.trace("solver stopped")
        }
      }
    }
  }
}
