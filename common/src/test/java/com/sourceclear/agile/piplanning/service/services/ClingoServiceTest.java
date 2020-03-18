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

  private static final ClingoService clingo = new ClingoServiceImpl(30, false);
  private static final ClingoService clingo1 = new ClingoServiceImpl(30, true);

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

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

  @Test
  public void testSimpleAssignment() throws IOException {
    testSimpleAssignment(clingo);
    testSimpleAssignment(clingo1);
  }

  public static void testSimpleAssignment(ClingoService clingoService) throws IOException {
    String solve = clingoService.solve(
        "task(1). sprint(2). epic(3). " +
            "task_weight(1,1). sprint_capacity(2,1). " +
            "epic_priority(3,1). epic_task(3,1).");
    assertThat(solve, CoreMatchers.containsString("assign(1,2)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testSimpleUnassigned() throws IOException {
    testSimpleUnassigned(clingo);
    testSimpleUnassigned(clingo1);
  }

  public static void testSimpleUnassigned(ClingoService clingoService) throws IOException {
    String solve = clingoService.solve(
        "task(1). sprint(2). epic(3). " +
            "task_weight(1,2). sprint_capacity(2,1). " +
            "epic_priority(3,1). epic_task(3,1).");
    assertThat(solve, CoreMatchers.containsString("assign(1,unassigned)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testSimpleDependencies() throws IOException {
    testSimpleDependencies(clingo);
    testSimpleDependencies(clingo1);
  }

  public static void testSimpleDependencies(ClingoService clingoService) throws IOException {
    String solve = clingoService.solve(
        "task(1). task(2). sprint(3). sprint(5). epic(4). " +
            "task_weight(1,2). task_weight(2,2). sprint_capacity(3,3). sprint_capacity(5,3). " +
            "epic_priority(4,1). epic_task(4,1). epic_task(4,2). task_depends_on(1,2).");
    assertThat(solve, CoreMatchers.containsString("assign(1,5)"));
    assertThat(solve, CoreMatchers.containsString("assign(2,3)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testDependenciesUnassigned() throws IOException {
    testDependenciesUnassigned(clingo);
    testDependenciesUnassigned(clingo1);
  }

  public static void testDependenciesUnassigned(ClingoService clingoService) throws IOException {
    String solve = clingoService.solve(
        "task(1). task(2). sprint(3). epic(4). " +
            "task_weight(1,2). task_weight(2,2). sprint_capacity(3,3). " +
            "epic_priority(4,1). epic_task(4,1). epic_task(4,2). task_depends_on(1,2).");
    assertThat(solve, CoreMatchers.containsString("assign(2,3)"));
    assertThat(solve, CoreMatchers.containsString("assign(1,unassigned)"));
    assertThat(solve, CoreMatchers.containsString("OPTIMUM FOUND"));
  }

  @Test
  public void testUnsatIsPossiblePins() throws IOException {
    testUnsatIsPossiblePins(clingo);
    testUnsatIsPossiblePins(clingo1);
  }

  public static void testUnsatIsPossiblePins(ClingoService clingoService) throws IOException {
    String solve = clingoService.solve(
        "task(1). sprint(3). epic(4). " +
            "task_weight(1,2). sprint_capacity(3,1). " +
            "epic_priority(4,1). epic_task(4,1). :- not assign(1,3).");
    assertThat(solve, CoreMatchers.containsString("UNSATISFIABLE"));
  }

  @Test
  public void testEpicPriority() throws IOException {
    String base = "sprint(1..2).\n" +
        "task(1..2).\n" +
        "epic(1..2).\n" +
        "task_weight(1,8).\n" +
        "task_weight(2,8).\n" +
        "sprint_capacity(1,8).\n" +
        "sprint_capacity(2,8).\n" +
        "epic_priority(1,2).\n" +
        "epic_priority(2,1).\n" +
        "epic_task(1,1).\n" +
        "epic_task(2,2).\n" +
        "#show epic_priority_violations/1.\n";

    String solution = clingo1.solve(base);

    assertThat(solution, CoreMatchers.containsString("epic_priority_violations(0)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 0 0"));

    solution = clingo1.solve(base +
        ":- assign(1,2).\n");

    assertThat(solution, CoreMatchers.containsString("epic_priority_violations(1)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 0 1"));
  }

  @Test
  public void testParallelism() throws IOException {
    String base = "sprint(1..2).\n" +
        "task(1..2).\n" +
        "epic(1..2).\n" +
        "task_weight(1,1).\n" +
        "task_weight(2,1).\n" +
        "sprint_capacity(1,2).\n" +
        "sprint_capacity(2,2).\n" +
        "task_depends_on(2,1).\n" +
        "#show dependencies_in_same_sprint/1.\n";

    String solution = clingo1.solve(base);

    assertThat(solution, CoreMatchers.containsString("dependencies_in_same_sprint(0)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 1 0"));

    solution = clingo1.solve(base +
        ":- assign(2,2).\n");

    assertThat(solution, CoreMatchers.containsString("dependencies_in_same_sprint(1)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 1 0 0"));
  }

  @Test
  public void testDependencyDistance() throws IOException {
    String base = "sprint(1..3).\n" +
        "task(1..2).\n" +
        "task_weight(1,1).\n" +
        "task_weight(2,1).\n" +
        "sprint_capacity(1,1).\n" +
        "sprint_capacity(2,1).\n" +
        "sprint_capacity(3,1).\n" +
        "task_depends_on(2,1).\n" +
        "#show dependency_distance/1.\n";

    String solution = clingo1.solve(base);

    assertThat(solution, CoreMatchers.containsString("dependency_distance(1)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 1 0"));
    assertThat(solution, CoreMatchers.containsString("assign(1,1)"));
    assertThat(solution, CoreMatchers.containsString("assign(2,2)"));

    solution = clingo1.solve(base +
        ":- assign(1,1).\n");

    assertThat(solution, CoreMatchers.containsString("dependency_distance(1)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 1 0"));
    assertThat(solution, CoreMatchers.containsString("assign(1,2)"));
    assertThat(solution, CoreMatchers.containsString("assign(2,3)"));

    solution = clingo1.solve(base +
        ":- assign(1,2).\n" +
        ":- assign(2,2).\n");

    assertThat(solution, CoreMatchers.containsString("dependency_distance(2)"));
    assertThat(solution, CoreMatchers.containsString("Optimization: 0 0 2 0"));
    assertThat(solution, CoreMatchers.containsString("assign(1,1)"));
    assertThat(solution, CoreMatchers.containsString("assign(2,3)"));
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

}
