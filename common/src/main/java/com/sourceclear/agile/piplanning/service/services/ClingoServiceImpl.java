package com.sourceclear.agile.piplanning.service.services;

import com.google.common.base.Stopwatch;
import com.sourceclear.agile.piplanning.Client;
import com.sourceclear.agile.piplanning.objects.DependencyP;
import com.sourceclear.agile.piplanning.objects.EpicP;
import com.sourceclear.agile.piplanning.objects.Fact;
import com.sourceclear.agile.piplanning.objects.PinP;
import com.sourceclear.agile.piplanning.objects.Problem;
import com.sourceclear.agile.piplanning.objects.Soln;
import com.sourceclear.agile.piplanning.objects.SprintP;
import com.sourceclear.agile.piplanning.objects.TicketP;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClingoServiceImpl implements ClingoService {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(ClingoServiceImpl.class);

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Pattern UNSAT = Pattern.compile("UNSATISFIABLE");
  private static final Pattern OPTIMAL = Pattern.compile("Answer[^\n]+\n([^\n]*)\nOptimization");
  private static final Pattern FACT = Pattern.compile("(\\w+)\\(([^)]+)\\)");

  private Client client = new Client();

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  @Override
  public Set<Soln> solve(Problem problem) throws IOException {
    return interpret(parse(solve(buildProgram(problem.getEpics(), problem.getTickets(),
        problem.getSprints(), problem.getPins(), problem.getDeps()))));
  }

  @Override
  public Set<Soln> solveRemotely(URI uri, Problem problem) throws IOException {
    return client.solve(uri, problem);
  }

  @Override
  public String solve(String instance) throws IOException {
    return callSolver(instance + getEncoding());
  }

  @Override
  public List<Fact> parse(String output) {

    Matcher mUnsat = UNSAT.matcher(output);
    if (mUnsat.find()) {
      return Collections.emptyList();
    }

    // Find the last occurrence of an answer, since clingo may output intermediate optima
    List<Fact> result = new ArrayList<>();
    Matcher mOptimal = OPTIMAL.matcher(output);
    while (mOptimal.find()) {
      // Discard the previous answer
      result = new ArrayList<>();
      String rawAnswer = mOptimal.group(1);

      Matcher mFact = FACT.matcher(rawAnswer);
      while (mFact.find()) {
        String name = mFact.group(1);
        List<String> args = Arrays.stream(mFact.group(2).split(","))
            .map(String::trim)
            .collect(Collectors.toList());
        result.add(new Fact(name, args));
      }
    }

    return result;
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private String getEncoding() {
    // TODO cache
    final String encoding;
    try {
      encoding = IOUtils.toString(getClass().getResource("/encoding.lp"), StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      throw new IllegalStateException("could not read encoding program", e);
    }
    return encoding;
  }

  private Set<Soln> interpret(List<Fact> facts) {
    return facts.stream().filter(f -> f.getName().equals("assign"))
        .map(f -> {
          long ticket = Long.parseLong(f.getArgs().get(0));
          String sprint = f.getArgs().get(1);
          if (sprint.equals("unassigned")) {
            return new Soln(ticket, Optional.empty(), true);
          } else {
            return new Soln(ticket, Optional.of(Long.parseLong(sprint)), false);
          }
        })
        .collect(Collectors.toSet());
  }

  private String callSolver(String program) throws IOException {
    final ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command("clingo", "-");
    processBuilder.redirectErrorStream(true);

    final Process process = processBuilder.start();
    // This should be fine because clingo needs the entire program to start anyway
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
      writer.write(program);
    }

    Stopwatch stopwatch = Stopwatch.createStarted();
    String output;
    try (final InputStream inputStream = process.getInputStream()) {
      output = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      LOGGER.trace("input: {}, solution: {}", program.replaceAll("\\s+", ""), output.replaceAll("\\s+", " "));
    }

    try {
      process.waitFor();
      // TODO aggregate this info somewhere
      LOGGER.debug("solved in {}s", stopwatch.elapsed(TimeUnit.SECONDS));
    } catch (InterruptedException e) {
      // TODO add a timeout and generalize the thrown exception
      throw new IllegalStateException();
    }

    return output;
  }

  private String buildProgram(Set<EpicP> epics, Set<TicketP> tickets, Set<SprintP> sprints, Set<PinP> pins,
                              Set<DependencyP> deps) {

    // TODO use ((1;2))?
    // TODO maintain id mapping for a more compact program?
    final String ids =
        tickets.stream()
            .map(t -> String.format("task(%d).", t.getId()))
            .collect(Collectors.joining("\n")) +
            // TODO we use the id here, but these are actually compared
            sprints.stream()
                .map(s -> String.format("sprint(%d).", s.getId()))
                .collect(Collectors.joining("\n")) +
            epics.stream()
                .map(e -> String.format("epic(%d).", e.getId()))
                .collect(Collectors.joining("\n"));

    final String weights = tickets.stream()
        .map(t -> String.format("task_weight(%d,%d).", t.getId(), t.getWeight()))
        .collect(Collectors.joining("\n"));

    final String capacities = sprints.stream()
        .map(s -> String.format("sprint_capacity(%d,%d).", s.getId(), s.getCapacity()))
        .collect(Collectors.joining("\n"));

    final String epicPriority = epics.stream()
        .map(e -> String.format("epic_priority(%d,%d).", e.getId(), e.getPriority()))
        .collect(Collectors.joining("\n"));

    final String epicTasks = epics.stream()
        .flatMap(e -> e.getTickets().stream()
            .map(t -> String.format("epic_task(%d,%d).", e.getId(), t.getId())))
        .collect(Collectors.joining("\n"));

    final String encoding = getEncoding();

    String dependencies = deps.stream()
        .map(d -> String.format("task_depends_on(%d,%d).", d.getFromTicketId(), d.getToTicketId()))
        .collect(Collectors.joining("\n"));

    String pinsP = pins.stream()
        .map(p -> String.format(":- not assign(%d,%d).", p.getTicketId(), p.getSprintId()))
        .collect(Collectors.joining("\n"));

    return String.join("\n", ids, epicPriority, epicTasks, weights, dependencies, pinsP, capacities, encoding);
  }

  //---------------------------- Getters/Setters ------------------------------

}
