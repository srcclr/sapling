/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.solver

import com.sourceclear.agile.piplanning.objects.Problem
import com.sourceclear.agile.piplanning.objects.Soln
import com.sourceclear.agile.piplanning.service.services.ClingoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
open class SolverController @Autowired constructor(
  private val clingoService: ClingoService
) {

  @PostMapping("/solve")
  open fun solve(@RequestBody problem: Problem): ResponseEntity<Set<Soln>> {
    val solve = clingoService.solve(problem)

    return ResponseEntity.ok(solve)
  }
}
