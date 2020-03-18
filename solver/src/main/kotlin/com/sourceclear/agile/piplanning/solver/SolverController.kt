/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.solver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.sourceclear.agile.piplanning.objects.Problem
import com.sourceclear.agile.piplanning.objects.Soln
import com.sourceclear.agile.piplanning.service.services.ClingoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.IOException

@RestController
open class SolverController @Autowired constructor(
  private val clingoService: ClingoService,
  private val clingoServiceNew: ClingoService
) {

  private val mapper = ObjectMapper()
      .registerModule(KotlinModule()).registerModule(Jdk8Module())

  @PostMapping("/solve")
  open fun solve(@RequestBody problem: Problem): ResponseEntity<Set<Soln>> {
    val solve = clingoService.solve(problem)

    return ResponseEntity.ok(solve)
  }

  @PostMapping("/solve/stream")
  open fun solveStream(@RequestBody problem: Problem): StreamingResponseBody {
    return StreamingResponseBody { o ->
      o.use {
        clingoServiceNew.solveIncrementally(problem) { answers ->
          try {
            o.write(mapper.writeValueAsBytes(answers))
            o.write('\n'.toInt())
            true
          } catch (e: IOException) {
            false
          }
        }
      }
    }
  }
}
