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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClingoServiceImpl implements ClingoService {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(ClingoServiceImpl.class);

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Pattern UNSAT = Pattern.compile("UNSATISFIABLE");
  private static final Pattern OPTIMAL = Pattern.compile("Answer[^\n]+\n([^\n]*)\nOptimization");
  private static final Pattern FACT = Pattern.compile("(\\w+)\\(([^)]+)\\)");

  private static final Pattern ANSWER = Pattern.compile("Answer: \\d+");

  private static String ENCODING;

  private final int timeout;
  private final boolean newEncoding;

  private final Client client = new Client();

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public ClingoServiceImpl(int timeout, boolean newEncoding) {
    this.timeout = timeout;
    this.newEncoding = newEncoding;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  @Override
  public Set<Soln> solve(Problem problem) throws IOException {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("solving {}", client.getObjectMapper().writeValueAsString(problem));
    }
    return interpret(parse(solve(buildProgram(problem.getEpics(), problem.getTickets(),
        problem.getSprints(), problem.getPins(), problem.getDeps()))));
  }

  @Override
  public void solveIncrementally(Problem problem, Function<Set<Soln>, Boolean> answers) throws IOException {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("solving {}", client.getObjectMapper().writeValueAsString(problem));
    }
    String instance = buildProgram(problem.getEpics(), problem.getTickets(),
        problem.getSprints(), problem.getPins(), problem.getDeps());
    callSolverWithIntermediateAnswers(instance, a -> answers.apply(interpret(a)));
  }

  @Override
  public void solveIncrementallyRemotely(URI uri, Problem problem, Function<Set<Soln>, Boolean> answers) throws IOException {
    client.solvePreview(uri, problem, answers::apply);
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
      String rawAnswer = mOptimal.group(1);
      // Discard the previous answer
      result = parseFacts(rawAnswer);
    }

    return result;
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private void callSolverWithIntermediateAnswers(String program, Function<List<Fact>, Boolean> answers) throws IOException {
    final ProcessBuilder processBuilder = new ProcessBuilder();
    String[] cmd = buildCommand();
    processBuilder.command(cmd);
    processBuilder.redirectErrorStream(true);

    final Process process = processBuilder.start();
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
      writer.write(program);
    }

    boolean terminated = false;
    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      // We may block here, and the only way to continue is for clingo to time out
      while ((line = reader.readLine()) != null) {
        Matcher m = ANSWER.matcher(line);
        if (m.matches()) {
          if (!answers.apply(parseFacts(reader.readLine()))) {
            terminated = true;
            break;
          }
        }
      }
    }
    process.destroy();
    LOGGER.trace("solver {}", terminated ? "terminated" : "finished");
  }

  private String[] buildCommand() {
    String[] cmd;
    if (timeout == 0) {
      cmd = new String[] {"clingo", "-"};
    } else {
      cmd = new String[] {"clingo", "--time-limit", Integer.toString(timeout), "-"};
    }
    return cmd;
  }

  private synchronized String getEncoding() {
    if (ENCODING == null) {
      try {
        ENCODING = IOUtils.toString(getClass().getResource(
            String.format("/encoding%s.lp", newEncoding ? "1" : "")), StandardCharsets.UTF_8).trim();
      } catch (IOException e) {
        throw new IllegalStateException("could not read encoding program", e);
      }
    }
    return ENCODING;
  }

  /**
   * This only works if there are no functors
   */
  private static List<Fact> parseFacts(String line) {
    Matcher mFact = FACT.matcher(line);
    List<Fact> result = new ArrayList<>();
    while (mFact.find()) {
      String name = mFact.group(1);
      List<String> args = Arrays.stream(mFact.group(2).split(","))
          .map(String::trim)
          .collect(Collectors.toList());
      result.add(new Fact(name, args));
    }
    return result;
  }

  private Set<Soln> interpret(List<Fact> facts) {
    return facts.stream().filter(f -> f.getName().equals("assign"))
        .flatMap(f -> {
          long ticket = Long.parseLong(f.getArgs().get(0));
          String sprint = f.getArgs().get(1);
          if (sprint.equals("unassigned")) {
            return Stream.empty();
          } else {
            return Stream.of(new Soln(ticket, Long.parseLong(sprint)));
          }
        })
        .collect(Collectors.toSet());
  }

  private String callSolver(String program) throws IOException {
    final ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command(buildCommand());
    processBuilder.redirectErrorStream(true);

    final Process process = processBuilder.start();
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
      // A timeout won't cause this, it'll just return a partial output
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
