package com.sourceclear.agile.piplanning

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sourceclear.agile.piplanning.objects.Problem
import com.sourceclear.agile.piplanning.objects.Soln
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Client {
  private val httpClient = HttpClient.newBuilder().build()
  private val objectMapper = ObjectMapper().registerModule(KotlinModule()).registerModule(Jdk8Module())

  fun solve(uri: URI, problem: Problem): Set<Soln> {

    val request = HttpRequest.newBuilder()
        .uri(uri.resolve("/solve"))
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(problem)))
        .setHeader("Content-Type", "application/json")
        .build()

    // TODO handle streaming
    val r = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue<Set<Soln>>(r.body())
  }
}
