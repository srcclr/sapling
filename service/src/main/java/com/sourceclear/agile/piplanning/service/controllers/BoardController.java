/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.sourceclear.agile.piplanning.objects.BoardI;
import com.sourceclear.agile.piplanning.objects.BoardL;
import com.sourceclear.agile.piplanning.objects.BoardO;
import com.sourceclear.agile.piplanning.objects.Soln;
import com.sourceclear.agile.piplanning.objects.SprintE;
import com.sourceclear.agile.piplanning.objects.SprintO;
import com.sourceclear.agile.piplanning.objects.TicketCD;
import com.sourceclear.agile.piplanning.objects.TicketCU;
import com.sourceclear.agile.piplanning.objects.TicketI;
import com.sourceclear.agile.piplanning.objects.TicketO;
import com.sourceclear.agile.piplanning.service.components.SolverProperties;
import com.sourceclear.agile.piplanning.service.entities.BaseEntity;
import com.sourceclear.agile.piplanning.service.entities.Board;
import com.sourceclear.agile.piplanning.service.entities.Epic;
import com.sourceclear.agile.piplanning.service.entities.Solution;
import com.sourceclear.agile.piplanning.service.entities.Sprint;
import com.sourceclear.agile.piplanning.service.entities.Ticket;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.repositories.BoardRepository;
import com.sourceclear.agile.piplanning.service.repositories.EpicRepository;
import com.sourceclear.agile.piplanning.service.repositories.SolutionRepository;
import com.sourceclear.agile.piplanning.service.repositories.SprintRepository;
import com.sourceclear.agile.piplanning.service.repositories.TicketRepository;
import com.sourceclear.agile.piplanning.service.services.Boards;
import com.sourceclear.agile.piplanning.service.services.ClingoService;
import kotlin.Pair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DefaultDSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.sourceclear.agile.piplanning.service.configs.Exceptions.Try;
import static com.sourceclear.agile.piplanning.service.configs.Exceptions.badRequest;
import static com.sourceclear.agile.piplanning.service.configs.Exceptions.internalServerError;
import static com.sourceclear.agile.piplanning.service.configs.Exceptions.notAcceptable;
import static com.sourceclear.agile.piplanning.service.configs.Exceptions.notFound;
import static com.sourceclear.agile.piplanning.service.jooq.tables.JiraCsv.JIRA_CSV;
import static com.sourceclear.agile.piplanning.service.jooq.tables.StoryRequests.STORY_REQUESTS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;

@RestController
public class BoardController {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class);

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .registerModule(new KotlinModule()).registerModule(new Jdk8Module());

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final BoardRepository boardRepository;
  private final EpicRepository epicRepository;
  private final SolutionRepository solutionRepository;
  private final SprintRepository sprintRepository;
  private final TicketRepository ticketRepository;
  private final ClingoService clingoService;
  private final ClingoService clingoServiceNew;
  private final SolverProperties solverProperties;
  private final DefaultDSLContext create;
  private final Boards boards;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public BoardController(BoardRepository boardRepository,
                         EpicRepository epicRepository,
                         SolutionRepository solutionRepository,
                         SprintRepository sprintRepository,
                         TicketRepository ticketRepository,
                         ClingoService clingoService,
                         ClingoService clingoServiceNew,
                         SolverProperties solverProperties,
                         DefaultDSLContext create,
                         Boards boards) {
    this.boardRepository = boardRepository;
    this.epicRepository = epicRepository;
    this.solutionRepository = solutionRepository;
    this.sprintRepository = sprintRepository;
    this.ticketRepository = ticketRepository;
    this.clingoService = clingoService;
    this.clingoServiceNew = clingoServiceNew;
    this.solverProperties = solverProperties;
    this.create = create;
    this.boards = boards;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @PutMapping("/sprint/{sprintId}")
  @Transactional
  public void updateSprint(@Valid @RequestBody SprintE sprint,
                           @PathVariable long sprintId) {
    Sprint sprint1 = sprintRepository.findById(sprintId)
        .orElseThrow(notFound);

    // Removing tickets is done in another endpoint
    sprint1.setName(sprint.getName());
    sprint1.setGoal(sprint.getGoal());

    if (sprint1.getCapacity() != sprint.getCapacity()) {
      sprint1.setCapacity(sprint.getCapacity());
    }

    // TODO fix ordinal?
  }

  @PostMapping("/board/{boardId}/sprints")
  @Transactional
  public ResponseEntity<SprintO> createSprint(@Valid @RequestBody SprintE sprint,
                                              @PathVariable long boardId) {
    Board board = boardRepository.findToSolve(boardId)
        .orElseThrow(notFound);

    Sprint s = new Sprint();
    s.setCapacity(sprint.getCapacity());
    s.setName(sprint.getName());
    s.setBoard(board);
    s.setGoal(sprint.getGoal());

    s.setOrdinal(1 + board.getSprints().stream()
        .map(Sprint::getOrdinal)
        .max(Comparator.naturalOrder())
        .orElse(0));

    s = sprintRepository.save(s);
    board.getSprints().add(s);
    return ResponseEntity.ok(new SprintO(s.getId(), s.getName(), s.getGoal(), s.getCapacity(), new ArrayList<>()));
  }

  @DeleteMapping("/sprint/{sprintId}")
  @Transactional
  public void deleteSprint(@PathVariable long sprintId) {
    // Why does deletion return 500...?
    sprintRepository.findById(sprintId)
        .orElseThrow(notFound);

    sprintRepository.deleteById(sprintId);
  }

  @PutMapping("/ticket/{ticketId}")
  @Transactional
  public void updateTicket(@Valid @RequestBody TicketI ticket,
                           @PathVariable long ticketId) {
    Ticket ticket1 = ticketRepository.findById(ticketId)
        .orElseThrow(notFound);

    // Changing epic is done elsewhere

    boolean notOurTicket = create.fetchExists(
        selectOne().from(STORY_REQUESTS)
            .where(STORY_REQUESTS.TO_TICKET_ID.eq(ticketId)));

    if (!notOurTicket) {
      ticket1.setDescription(ticket.getDescription());
      // otherwise attempts to change the description will be ignored
    }

    if (ticket1.getWeight() != ticket.getWeight()) {
      ticket1.setWeight(ticket.getWeight());
    }
  }

  @PostMapping("/board/{boardId}/epic/{epicId}/tickets")
  @Transactional
  public ResponseEntity<TicketO> createTicket(@Valid @RequestBody TicketI ticket,
                                              @PathVariable long epicId,
                                              @PathVariable long boardId) {
    Board board = boardRepository.findById(boardId)
        .orElseThrow(notFound);
    Epic epic = epicRepository.findById(epicId)
        .orElseThrow(notFound);
    Ticket t = new Ticket(board, epic, ticket);
    t = ticketRepository.save(t);

    solutionRepository.save(new Solution(board, t, false));

    return ResponseEntity.ok(new TicketO(t.getId(), t.getDescription(), t.getWeight(), epic.getId(),
        null, new HashSet<>(), new HashSet<>(), false));
  }

  @DeleteMapping("/ticket/{ticketId}")
  @Transactional
  public void deleteTicket(@PathVariable long ticketId) {

    boolean isInvolvedInAStoryRequest = create.fetchExists(
        selectOne().from(STORY_REQUESTS)
            .where(STORY_REQUESTS.TO_TICKET_ID.eq(ticketId)
                .or(STORY_REQUESTS.FROM_TICKET_ID.eq(ticketId))));

    if (isInvolvedInAStoryRequest) {
      throw badRequest;
    }

    // Why does deletion return 500...?
    ticketRepository.findById(ticketId)
        .orElseThrow(notFound);

    ticketRepository.deleteById(ticketId);
  }

  private static final String INPUT_ISSUE_KEY = "Issue key";
  private static final String INPUT_SUMMARY = "Summary";
  private static final String INPUT_STORY_POINTS = "Custom field (Story Points)";
  private static final String INPUT_SPRINT = "Sprint";
  private static final String INPUT_EPIC = "Custom field (Epic Link)";

  private static final String OUTPUT_SUMMARY = "Summary";
  private static final String OUTPUT_STORY_POINTS = "Story Points";
  private static final String OUTPUT_SPRINT = "Sprint";
  private static final String OUTPUT_EPIC = "Epic";

  @GetMapping(value = "/board/{boardId}/csv/jira", produces = "text/csv")
  @Transactional(readOnly = true)
  public void getCsvJira(@PathVariable long boardId, HttpServletResponse response) throws Exception {
    List<TicketCD> result = loadCsvOutput(boardId);

    // Grab the CSV file we saved and mutate it
    String csv = create.fetchOne(select(JIRA_CSV.CSV).from(JIRA_CSV).where(JIRA_CSV.BOARD_ID.eq(boardId))).component1();
    if (csv == null) {
      throw badRequest;
    }

    Map<String, Map<String, String>> records;
    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(csv.getBytes()))) {
      records = parseCsv(reader).getRecords().stream()
          // Deal with mutable maps
          .map(CSVRecord::toMap)
          .collect(toMap(r -> r.get(INPUT_ISSUE_KEY), r -> r,
              // Assume the keys are unique and not crash, i.e. garbage in, garbage out
              (a, b) -> b));
    }

    // There must be at least one record, as we check on upload
    String[] header = records.values().iterator().next().keySet().toArray(new String[]{});

    List<Map<String, String>> newRecords = new ArrayList<>();

    for (TicketCD r : result) {
      String summary = r.getSummary();
      // If the same key appears in more than one ticket, we'll clobber it
      var key = splitIssueKey(summary);

      // Check if it's an existing ticket
      Map<String, String> record = null;
      if (key.isPresent()) {
        String issueKey = key.get().getFirst();
        if (records.containsKey(issueKey)) {
          // Could be associated with an existing ticket
          record = records.get(issueKey);
          summary = key.get().getSecond();
        }
      }

      // Could not be associated; create a new ticket instead
      if (record == null) {
        var r1 = record = new HashMap<>();
        Arrays.stream(header).forEach(h -> r1.put(h, null));
        newRecords.add(r1);
      }

      // Read using the input columns
      record.put(INPUT_EPIC, r.getEpic());
      record.put(INPUT_SUMMARY, summary);
      record.put(INPUT_STORY_POINTS, Integer.toString(r.getPoints()));
      record.put(INPUT_SPRINT, r.getSprint());
    }

    try (BufferedWriter out = new BufferedWriter(response.getWriter());
         CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(header))) {

      for (Map<String, String> r : records.values()) {
        // Make sure header ordering is preserved
        Object[] values = Arrays.stream(header).map(r::get).toArray();
        printer.printRecord(values);
      }

      for (Map<String, String> r : newRecords) {
        Object[] values = Arrays.stream(header).map(r::get).toArray();
        printer.printRecord(values);
      }
    }
  }

  @GetMapping(value = "/board/{boardId}/csv", produces = "text/csv")
  @Transactional(readOnly = true)
  public void getCsv(@PathVariable long boardId, HttpServletResponse response) throws Exception {
    List<TicketCD> result = loadCsvOutput(boardId);

    try (BufferedWriter out = new BufferedWriter(response.getWriter());
         CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
             .withHeader(OUTPUT_SUMMARY, OUTPUT_STORY_POINTS, OUTPUT_SPRINT, OUTPUT_EPIC))) {
      for (TicketCD r : result) {
        printer.printRecord(r.getSummary(), r.getPoints(), r.getSprint(), r.getEpic());
      }
    }
  }

  @PostMapping(value = "/board/{boardId}/csv")
  @Transactional
  public Map<String, Integer> uploadCsv(@PathVariable long boardId, @RequestParam("file") MultipartFile file) throws IOException {
    // The file is already in memory
    return importCsv(file.getBytes(), boardId, false);
  }

  @PostMapping(value = "/board/{boardId}/csv/jira")
  @Transactional
  public Map<String, Integer> uploadCsvJira(@PathVariable long boardId, @RequestParam("file") MultipartFile file) throws IOException {

    // Save the CSV file separately
    create.mergeInto(JIRA_CSV, JIRA_CSV.BOARD_ID, JIRA_CSV.CSV)
        .values(boardId, new String(file.getBytes()))
        .execute();

    // Do exactly the same thing but ensure the issue key is present
    return importCsv(file.getBytes(), boardId, true);
  }

  @GetMapping("/board/{boardId}")
  @Transactional(readOnly = true)
  public ResponseEntity<BoardO> getSolution(@PathVariable long boardId) {
    return ResponseEntity.ok(useCurrentSolution(boardId));
  }

  @PostMapping("/board/{boardId}")
  @Transactional
  public ResponseEntity<BoardO> compute(@PathVariable long boardId) {
    return computeNewSolution(boardId);
  }

  @GetMapping("/board/{boardId}/preview")
  @Transactional(readOnly = true)
  public ResponseEntity<BoardO> getPreview(@PathVariable long boardId) {
    return ResponseEntity.ok(useCurrentPreview(boardId));
  }

  @PostMapping(value = "/board/{boardId}/preview", produces = "application/x-ndjson")
  @CrossOrigin
  @Transactional // this is held open for the duration of the solver/async request timeout
  public StreamingResponseBody preview(@PathVariable long boardId) {
    // TODO lock board?
    return out -> {
      Board board = boardRepository.findToSolve(boardId)
          .orElseThrow(notFound);
      computePreviewSolutions(board, answer -> {
        try {
          out.write(MAPPER.writeValueAsBytes(answer));
          out.write('\n');
          out.flush();
          LOGGER.trace("written answer back to client");
          return true;
        } catch (IOException e) {
          // TODO an IllegalStateException is still printed from within Tomcat.
          //  We can try upgrading Tomcat/Spring Boot or switching to Undertow.
          //  See https://github.com/spring-projects/spring-boot/issues/15057#issuecomment-435310964

          LOGGER.trace("error occurred writing answer; connection closed?", e);
          // This usually means that the connection was closed, most probably the client aborting.
          return false;
        }
      });
    };
  }

  @PostMapping(value = "/board/{boardId}/preview/accept")
  @CrossOrigin
  @Transactional
  public void acceptPreview(@PathVariable long boardId) {
    Board board = boardRepository.findById(boardId)
        .orElseThrow(notFound);
    solutionRepository.deleteRealSolution(board);
    solutionRepository.acceptPreviewSolution(board);
  }

  @DeleteMapping(value = "/board/{boardId}/preview")
  @CrossOrigin
  @Transactional
  public void deletePreview(@PathVariable long boardId) {
    Board board = boardRepository.findById(boardId)
        .orElseThrow(notFound);
    solutionRepository.deletePreviewSolution(board);
  }

  @GetMapping("/boards")
  @Transactional(readOnly = true)
  public ResponseEntity<List<BoardL>> listBoards() {
    var all = boardRepository.findAllWithOwner().stream()
        .map(b -> new BoardL(b.getId(), b.getName(), b.getOwner().getUsername()))
        .sorted(Comparator.comparingLong(BoardL::getId))
        .collect(Collectors.toList());
    return ResponseEntity.ok(all);
  }

  @PostMapping("/boards")
  @Transactional
  public void createBoard(@Valid @RequestBody BoardI board,
                          @AuthenticationPrincipal User user) {
    Board b = new Board();
    // Only metadata can be set here
    b.setName(board.getName());
    b.setOwner(user);
    boardRepository.save(b);
  }

  @PutMapping("/board/{boardId}")
  @Transactional
  public void updateBoard(@PathVariable long boardId,
                          @Valid @RequestBody BoardI board) {
    Board b = boardRepository.findById(boardId)
        .orElseThrow(notFound);
    // Only metadata can be updated here
    b.setName(board.getName());
  }

  @DeleteMapping("/board/{boardId}")
  @Transactional
  public void deleteBoard(@PathVariable long boardId) {
    boardRepository.deleteById(boardId);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  /**
   * Validates the id, then computes, saves, and returns a new solution for the given board.
   */
  private ResponseEntity<BoardO> computeNewSolution(long boardId) {
    return ResponseEntity.ok(computeNewSolution(
        boardRepository.findToSolve(boardId)
            .orElseThrow(notFound)));
  }

  private void computePreviewSolutions(Board board, Function<BoardO, Boolean> answers) {

    var sprintsById = board.getSprints().stream().collect(Collectors.toMap(BaseEntity::getId, s -> s));
    var tickets = board.getTickets();
    var ticketsById = tickets.stream().collect(Collectors.toMap(BaseEntity::getId, t -> t));

    try {
      if (solverProperties.isRemote()) {
        LOGGER.trace("solving incrementally remotely");
        clingoService.solveIncrementallyRemotely(solverProperties.getUri(), board.toProblem(),
            handlePreviewSolution(board, ticketsById, sprintsById, answers));
      } else {
        LOGGER.trace("solving incrementally locally");
        clingoServiceNew.solveIncrementally(board.toProblem(),
            handlePreviewSolution(board, ticketsById, sprintsById, answers));
      }
    } catch (Exception e) {
      LOGGER.error("Failed to run solver {}",
          solverProperties.isRemote() ? "remotely" : "locally", e);
      throw internalServerError;
    }
  }

  private Function<Set<Soln>, Boolean> handlePreviewSolution(Board board, Map<Long, Ticket> ticketsById,
                                                             Map<Long, Sprint> sprintsById,
                                                             Function<BoardO, Boolean> answers) {

    return soln -> {
      checkUnsat(soln);

      LOGGER.trace("got set of solutions");

      Set<Solution> solution = soln.stream()
          .map(s ->
              new Solution(board,
                  s.getSprintId().map(sprintsById::get).orElse(null),
                  ticketsById.get(s.getTicketId()),
                  s.getUnassigned(),
                  true))
          .collect(Collectors.toSet());

      solutionRepository.deletePreviewSolution(board);
      solutionRepository.saveAll(solution);

      return answers.apply(boards.reconstruct(solution, board.getId()));
    };
  }

  /**
   * Computes, saves, and returns a new solution for the given board.
   */
  private BoardO computeNewSolution(Board board) {

    var sprintsById = board.getSprints().stream().collect(Collectors.toMap(BaseEntity::getId, s -> s));
    var tickets = board.getTickets();
    var ticketsById = tickets.stream().collect(Collectors.toMap(BaseEntity::getId, t -> t));

    Set<Soln> soln;
    try {
      if (solverProperties.isRemote()) {
        soln = clingoService.solveRemotely(solverProperties.getUri(), board.toProblem());
      } else {
        soln = clingoService.solve(board.toProblem());
      }
    } catch (Exception e) {
      LOGGER.error("Failed to run solver", e);
      throw internalServerError;
    }

    checkUnsat(soln);

    Set<Solution> solution = soln.stream()
        .map(s ->
            new Solution(board,
                s.getSprintId().map(sprintsById::get).orElse(null),
                ticketsById.get(s.getTicketId()),
                s.getUnassigned(),
                false))
        .collect(Collectors.toSet());
    solutionRepository.deleteRealSolution(board);
    solutionRepository.saveAll(solution);

    return boards.reconstruct(solution, board.getId());
  }

  private void checkUnsat(Set<Soln> soln) {
    if (soln.isEmpty()) {
      // Unsat is still possible because of pins or having no tickets
      throw notAcceptable;
    }
  }

  private BoardO useCurrentSolution(long boardId) {
    return useCurrentBoard(boardId, solutionRepository.findCurrentSolution(boardId));
  }

  private BoardO useCurrentPreview(long boardId) {
    return useCurrentBoard(boardId, solutionRepository.findCurrentPreview(boardId));
  }

  @NotNull
  private BoardO useCurrentBoard(long boardId, Set<Solution> rawAssignments) {
    Board board;
    if (rawAssignments.isEmpty()) {
      // It's only possible that there's no solution when the board is empty.
      // In that case, there won't be any associated things.
      board = boardRepository.findById(boardId)
          .orElseThrow(notFound);
    } else {
      board = rawAssignments.iterator().next().getBoard();
    }
    return boards.reconstruct(rawAssignments, board.getId());
  }


  private Map<String, Integer> importCsv(byte[] csv, long boardId, boolean keepIssueKey) {

    Board board = boardRepository.findById(boardId)
        .orElseThrow(notFound);

    // More queries
    int[] ordinal = {board.getSprints().stream().map(Sprint::getOrdinal).max(Comparator.comparingInt(i -> i)).orElse(1)};
    int[] priority = {board.getEpics().stream().map(Epic::getPriority).max(Comparator.comparingInt(i -> i)).orElse(1)};

    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(csv))) {

      int[] errors = {0};
      Set<TicketCU> tickets = StreamSupport.stream(parseCsv(reader).spliterator(), false)
          .flatMap(r -> {
            try {
              // Do this manually because inputs are probably barely CSV and need fiddling with
              return Stream.of(
                  new TicketCU(
                      (keepIssueKey ? r.get(INPUT_ISSUE_KEY) + " " : "") + r.get(INPUT_SUMMARY),
                      // JIRA...
                      Math.round(Float.parseFloat(StringUtils.defaultIfBlank(r.get(INPUT_STORY_POINTS), "0"))),
                      Try(() -> Optional.of(r.get(INPUT_SPRINT)), Optional.empty()),
                      r.get(INPUT_EPIC)));
            } catch (Exception e) {
              LOGGER.debug("failed to parse csv record", e);
              ++errors[0];
              return Stream.of();
            }
          }).collect(Collectors.toSet());

      if (tickets.isEmpty()) {
        throw new IllegalArgumentException("no stories could be parsed from csv file");
      }

      // Epics are given arbitrary but differing priorities
      var epics = tickets.stream().map(TicketCU::getEpic)
          .distinct()
          .collect(Collectors.toMap(e -> e, e -> new Epic(e, ++priority[0], board)));
      board.getEpics().addAll(epics.values());

      // Sprints are ordered similarly and given an arbitrary capacity
      var sprints = tickets.stream().map(TicketCU::getSprint)
          .flatMap(Optional::stream)
          .distinct()
          .map(s -> new Sprint(board, s, "", 20, ++ordinal[0]))
          .collect(Collectors.toList());
      board.getSprints().addAll(sprints);

      // Silently drop tickets with invalid epics
      List<Ticket> tix = tickets.stream().filter(t -> epics.containsKey(t.getEpic()))
          .map(t -> new Ticket(board, epics.get(t.getEpic()), t))
          .collect(toList());

      board.getTickets().addAll(tix);

      // Make sure they show up as unassigned
      epicRepository.saveAll(epics.values());
      ticketRepository.saveAll(tix); // or Hibernate complains
      solutionRepository.saveAll(tix.stream().map(t -> new Solution(board, t, false))::iterator);

      LOGGER.debug("added {} epics, {} sprints, {} tickets; {} failed to parse",
          epics.size(), sprints.size(), tickets.size(), errors[0]);

      return Map.of("epics", epics.size(), "sprints", sprints.size(), "tickets", tickets.size(), "errors", errors[0]);
    } catch (IllegalArgumentException e) {
      throw badRequest.apply(e.getMessage());
    } catch (Exception e) {
      LOGGER.debug("failed to import", e);
      throw internalServerError;
    }
  }

  private static CSVParser parseCsv(InputStreamReader reader) throws IOException {
    return CSVFormat.DEFAULT
        .withFirstRecordAsHeader()
        .withAllowDuplicateHeaderNames() // because JIRA
        .withRecordSeparator(System.lineSeparator())
        .parse(reader);
  }

  private List<TicketCD> loadCsvOutput(@PathVariable long boardId) {
    BoardO solution = useCurrentSolution(boardId);

    Map<Long, String> epicNames = boardRepository.findById(boardId)
        .orElseThrow(notFound)
        .getEpics()
        .stream()
        .collect(toMap(BaseEntity::getId, Epic::getName));

    return Stream.of(solution).flatMap(so ->
        Stream.concat(
            so.getSprints().stream().flatMap(s ->
                s.getTickets().stream().map(t ->
                    new TicketCD(t.getDescription(), t.getWeight(), s.getName(), epicNames.get(t.getEpic())))),
            so.getUnassigned().stream().map(t ->
                new TicketCD(t.getDescription(), t.getWeight(), "unassigned", epicNames.get(t.getEpic())))
        )).collect(Collectors.toList());
  }

  public static Optional<Pair<String, String>> splitIssueKey(String text) {
    String[] split = text.split(" ");
    if (split.length > 0) {
      String issueKey = split[0];
      String rest = Arrays.stream(split).skip(1).collect(Collectors.joining(" "));
      return Optional.of(new Pair<>(issueKey, rest));
    } else {
      return Optional.empty();
    }
  }

  //---------------------------- Property Methods -----------------------------

}
