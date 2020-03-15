/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.objects.Fact;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThat;

public class ClingoServiceTest {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final ClingoService clingo = new ClingoServiceImpl();

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Test
  public void testSimpleAssignment() throws IOException {
    String solve = clingo.solve(
        "task(1). sprint(2). epic(3). " +
            "task_weight(1,1). sprint_capacity(2,1). " +
            "epic_priority(3,1). epic_task(3,1).");
    assertThat(solve, CoreMatchers.containsString("assign(1,2)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testSimpleUnassigned() throws IOException {
    String solve = clingo.solve(
        "task(1). sprint(2). epic(3). " +
            "task_weight(1,2). sprint_capacity(2,1). " +
            "epic_priority(3,1). epic_task(3,1).");
    assertThat(solve, CoreMatchers.containsString("assign(1,unassigned)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testSimpleDependencies() throws IOException {
    String solve = clingo.solve(
        "task(1). task(2). sprint(3). sprint(5). epic(4). " +
            "task_weight(1,2). task_weight(2,2). sprint_capacity(3,3). sprint_capacity(5,3). " +
            "epic_priority(4,1). epic_task(4,1). epic_task(4,2). task_depends_on(1,2).");
    assertThat(solve, CoreMatchers.containsString("assign(1,5)"));
    assertThat(solve, CoreMatchers.containsString("assign(2,3)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testDependenciesUnassigned() throws IOException {
    String solve = clingo.solve(
        "task(1). task(2). sprint(3). epic(4). " +
            "task_weight(1,2). task_weight(2,2). sprint_capacity(3,3). " +
            "epic_priority(4,1). epic_task(4,1). epic_task(4,2). task_depends_on(1,2).");
    assertThat(solve, CoreMatchers.containsString("assign(2,3)"));
    assertThat(solve, CoreMatchers.containsString("assign(1,unassigned)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testUnsatIsPossiblePins() throws IOException {
    String solve = clingo.solve(
        "task(1). sprint(3). epic(4). " +
            "task_weight(1,2). sprint_capacity(3,1). " +
            "epic_priority(4,1). epic_task(4,1). :- not assign(1,3).");
    assertThat(solve, CoreMatchers.containsString("UNSATISFIABLE"));
  }

  @Test
  public void testEpicPriority() throws IOException {
    String solve = clingo.solve(
        "sprint(1). " +
            "task(1..2). " +
            "epic(1..2). " +
            "task_weight(1,8). " +
            "task_weight(2,8). " +
            "sprint_capacity(1,8). " +
            "epic_priority(1,1). " +
            "epic_priority(2,2). " +
            "epic_task(1,1). " +
            "epic_task(2,2). ");

    assertThat(solve, CoreMatchers.containsString("assign(1,1)"));
    assertThat(solve, CoreMatchers.containsString("assign(2,unassigned)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testParse() {
    List<Fact> facts = clingo.parse(
        "Answer: 1\n" +
            "sprint_total(110,0) assign(137,unassigned) sprint_total(unassigned,2)\n" +
            "Optimization: 1\n" +
            "Answer: 2\n" +
            "assign(137,110) sprint_total(110,2) sprint_total(unassigned,0)\n" +
            "Optimization: 0" +
            "OPTIMUM FOUND");
    assertThat(facts, Matchers.contains(
        new Fact("assign", Arrays.asList("137", "110")),
        new Fact("sprint_total", Arrays.asList("110", "2")),
        new Fact("sprint_total", Arrays.asList("unassigned", "0"))
    ));
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

}
