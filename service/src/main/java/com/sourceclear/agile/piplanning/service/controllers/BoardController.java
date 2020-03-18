/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.controllers;

import com.sourceclear.agile.piplanning.objects.BoardI;
import com.sourceclear.agile.piplanning.objects.BoardL;
import com.sourceclear.agile.piplanning.objects.BoardO;
import com.sourceclear.agile.piplanning.objects.Soln;
import com.sourceclear.agile.piplanning.objects.SprintE;
import com.sourceclear.agile.piplanning.objects.SprintI;
import com.sourceclear.agile.piplanning.objects.SprintO;
import com.sourceclear.agile.piplanning.objects.TicketCD;
import com.sourceclear.agile.piplanning.objects.TicketCU;
import com.sourceclear.agile.piplanning.objects.TicketI;
import com.sourceclear.agile.piplanning.objects.TicketO;
import com.sourceclear.agile.piplanning.service.components.SolverProperties;
import com.sourceclear.agile.piplanning.service.entities.BaseEntity;
import com.sourceclear.agile.piplanning.service.entities.Board;
import com.sourceclear.agile.piplanning.service.entities.Dependency;
import com.sourceclear.agile.piplanning.service.entities.Epic;
import com.sourceclear.agile.piplanning.service.entities.Pin;
import com.sourceclear.agile.piplanning.service.entities.Solution;
import com.sourceclear.agile.piplanning.service.entities.Sprint;
import com.sourceclear.agile.piplanning.service.entities.Ticket;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.repositories.BoardRepository;
import com.sourceclear.agile.piplanning.service.repositories.EpicRepository;
import com.sourceclear.agile.piplanning.service.repositories.SolutionRepository;
import com.sourceclear.agile.piplanning.service.repositories.SprintRepository;
import com.sourceclear.agile.piplanning.service.repositories.TicketRepository;
import com.sourceclear.agile.piplanning.service.services.ClingoService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RestController
public class BoardController {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(BoardController.class);

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final BoardRepository boardRepository;
  private final EpicRepository epicRepository;
  private final SolutionRepository solutionRepository;
  private final SprintRepository sprintRepository;
  private final TicketRepository ticketRepository;
  private final ClingoService clingoService;
  private final SolverProperties solverProperties;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public BoardController(BoardRepository boardRepository, EpicRepository epicRepository, SolutionRepository solutionRepository, SprintRepository sprintRepository, TicketRepository ticketRepository, ClingoService clingoService, SolverProperties solverProperties) {
    this.boardRepository = boardRepository;
    this.epicRepository = epicRepository;
    this.solutionRepository = solutionRepository;
    this.sprintRepository = sprintRepository;
    this.ticketRepository = ticketRepository;
    this.clingoService = clingoService;
    this.solverProperties = solverProperties;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @PutMapping("/sprint/{sprintId}")
  @Transactional
  public void updateSprint(@Valid @RequestBody SprintE sprint,
                           @PathVariable long sprintId) {
    Sprint sprint1 = sprintRepository.findById(sprintId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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
  public ResponseEntity<SprintO> createSprint(@Valid @RequestBody SprintI sprint,
                                              @PathVariable long boardId) {
    Board board = boardRepository.findToSolve(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    Sprint s = new Sprint();
    s.setCapacity(sprint.getCapacity());
    s.setName(sprint.getName());
    s.setBoard(board);
    s.setGoal("");

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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    sprintRepository.deleteById(sprintId);
  }

  @PutMapping("/ticket/{ticketId}")
  @Transactional
  public void updateTicket(@Valid @RequestBody TicketI ticket,
                           @PathVariable long ticketId) {
    Ticket ticket1 = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // Moving to another board is done in another endpoint
    ticket1.setDescription(ticket.getDescription());

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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    Epic epic = epicRepository.findById(epicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    Ticket t = new Ticket(board, epic, ticket);
    t = ticketRepository.save(t);

    solutionRepository.save(new Solution(board, t, true));

    return ResponseEntity.ok(new TicketO(t.getId(), t.getDescription(), t.getWeight(), true, epic.getId(),
        null, new HashSet<>()));
  }

  @DeleteMapping("/ticket/{ticketId}")
  @Transactional
  public void deleteTicket(@PathVariable long ticketId) {

    // Why does deletion return 500...?
    ticketRepository.findById(ticketId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    ticketRepository.deleteById(ticketId);
  }

  @GetMapping("/board/{boardId}")
  @Transactional(readOnly = true)
  public ResponseEntity<BoardO> getSolution(@PathVariable long boardId) {
    return ResponseEntity.ok(useCurrentSolution(boardId));
  }

  @GetMapping(value = "/board/{boardId}/csv", produces = "text/csv")
  @Transactional(readOnly = true)
  public void getCsv(@PathVariable long boardId, HttpServletResponse response) throws Exception {
    BoardO solution = useCurrentSolution(boardId);

    Map<Long, String> epicNames = boardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
        .getEpics()
        .stream()
        .collect(toMap(BaseEntity::getId, Epic::getName));

    List<TicketCD> result = Stream.of(solution).flatMap(so ->
        Stream.concat(
            so.getSprints().stream().flatMap(s ->
                s.getTickets().stream().map(t ->
                    new TicketCD(t.getDescription(), t.getWeight(), s.getName(), epicNames.get(t.getEpic())))),
            so.getUnassigned().stream().map(t ->
                new TicketCD(t.getDescription(), t.getWeight(), "unassigned", epicNames.get(t.getEpic())))
        )).collect(Collectors.toList());

    final String SUMMARY = "Summary";
    final String STORY_POINTS = "Story Points";
    final String SPRINT = "Sprint";
    final String EPIC = "Epic";

    try (BufferedWriter out = new BufferedWriter(response.getWriter());
         final CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
             .withHeader(SUMMARY, STORY_POINTS, SPRINT, EPIC))) {
      for (TicketCD r : result) {
        printer.printRecord(r.getSummary(), r.getPoints(), r.getSprint(), r.getEpic());
      }
    }
  }

  @PostMapping(value = "/board/{boardId}/csv")
  @Transactional
  public Map<String, Integer> uploadCsv(@PathVariable long boardId, @RequestParam("file") MultipartFile file) throws Exception {

    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // More queries
    int[] sprintCount = { board.getSprints().size() };
    int[] priority = { board.getEpics().stream().map(Epic::getPriority).max(Comparator.comparingInt(i -> i)).orElse(1) };

    final String SUMMARY = "Summary";
    final String STORY_POINTS = "Custom field (Story Points)";
    final String SPRINT = "Sprint";
    final String EPIC = "Custom field (Epic Link)";

    // The file is already in memory
    try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(file.getBytes()))) {

      int[] errors = { 0 };
      Set<TicketCU> tickets = StreamSupport.stream(CSVFormat.DEFAULT
          .withFirstRecordAsHeader()
          .withAllowDuplicateHeaderNames() // because JIRA
          .withRecordSeparator(System.lineSeparator())
          .parse(reader).spliterator(), false)
          .flatMap(r -> {
            try {
              // Do this manually because inputs are probably barely CSV and need fiddling with
              return Stream.of(
                  new TicketCU(
                      r.get(SUMMARY),
                      // JIRA...
                      Math.round(Float.parseFloat(StringUtils.defaultIfBlank(r.get(STORY_POINTS), "0"))),
                      r.get(SPRINT),
                      r.get(EPIC)));
            } catch (Exception e) {
              LOGGER.trace("failed to parse csv record", e);
              ++errors[0];
              return Stream.of();
            }
          }).collect(Collectors.toSet());

      // Epics are given arbitrary but differing priorities
      var epics = tickets.stream().map(TicketCU::getEpic)
          .distinct()
          .collect(Collectors.toMap(e -> e, e -> new Epic(e, priority[0]++, board)));
      board.getEpics().addAll(epics.values());

      // Sprints are ordered similarly and given an arbitrary capacity
      var sprints = tickets.stream().map(TicketCU::getSprint)
          .distinct()
          .map(s -> new Sprint(board, s, "", 20, sprintCount[0]++))
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
      solutionRepository.saveAll(tix.stream().map(t -> new Solution(board, t, true))::iterator);

      LOGGER.debug("added {} epics, {} sprints, {} tickets; {} failed to parse",
          epics.size(), sprints.size(), tickets.size(), errors[0]);

      return Map.of("epics", epics.size(), "sprints", sprints.size(), "tickets", tickets.size(), "errors", errors[0]);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      LOGGER.debug("failed to import", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/board/{boardId}")
  @Transactional
  public ResponseEntity<BoardO> compute(@PathVariable long boardId) {
    return computeNewSolution(boardId);
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))));
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
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    if (soln.isEmpty()) {
      // Unsat is still possible because of pins or having no tickets
      throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
    }

    Set<Solution> solution = soln.stream()
        .map(s ->
            new Solution(board,
                s.getSprintId().map(sprintsById::get).orElse(null),
                ticketsById.get(s.getTicketId()),
                s.getUnassigned()))
        .collect(Collectors.toSet());
    solutionRepository.deleteSolution(board);
    solutionRepository.saveAll(solution);

    return respond(solution, board);
  }

  /**
   * Returns the current solution for the given board
   */
  private BoardO useCurrentSolution(long boardId) {
    Set<Solution> rawAssignments = solutionRepository.findCurrentSolution(boardId);

    Board board;
    if (rawAssignments.isEmpty()) {
      // It's only possible that there's no solution when the board is empty.
      // In that case, there won't be any associated things.
      board = boardRepository.findById(boardId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    } else {
      board = rawAssignments.iterator().next().getBoard();
    }
    return respond(rawAssignments, board);
  }

  /**
   * Given a solution for the given board in flattened form, constructs the nested version
   */
  private BoardO respond(Set<Solution> rawAssignments, Board board) {
    var resultSprints = new ArrayList<SprintO>();
    var unassigned = new ArrayList<TicketO>();
    var result = new BoardO(board.getId(), board.getName(), board.getOwner().getUsername(),
        resultSprints, unassigned);

    // Duplicates are identical so we can discard them.
    var ticketsById = rawAssignments.stream()
        .filter(s -> s.getSprint().isPresent())
        .map(Solution::getTicket)
        .collect(Collectors.toMap(BaseEntity::getId, t -> t, (a, b) -> a));

    Map<Long, List<Long>> assignments = rawAssignments.stream()
        .filter(s -> s.getSprint().isPresent())
        .collect(
            groupingBy(a -> a.getSprint().get().getId(),
                mapping(a -> a.getTicket().getId(), toList())));

    // Shouldn't require a group by if invariants hold
    Map<Long, Long> pins = board.getPins().stream().collect(Collectors.toMap(Pin::getTicketId, Pin::getSprintId));

    Map<Long, Set<Long>> deps = board.getDeps().stream()
        .collect(Collectors.groupingBy(Dependency::getFromTicketId,
            Collectors.mapping(Dependency::getToTicketId, Collectors.toSet())));

    List<SprintO> sprints = board.getSprints().stream()
        // Correct order
        .sorted(Comparator.comparingInt(Sprint::getOrdinal))
        .map(sprint -> new SprintO(sprint.getId(), sprint.getName(), sprint.getGoal(), sprint.getCapacity(),
            assignments.getOrDefault(sprint.getId(), Collections.emptyList()).stream()
            .map(ticketsById::get)
            .map(t -> t.toModel(pins.get(t.getId()), deps.getOrDefault(t.getId(), new HashSet<>())))
            // Ensure ordering remains stable
            .sorted(Comparator.comparingLong(TicketO::getId))
            .collect(toList())))
        .collect(toList());

    resultSprints.addAll(sprints);

    rawAssignments.stream()
        .filter(s -> s.getSprint().isEmpty())
        .map(Solution::getTicket)
        .map(t -> t.toModel(pins.get(t.getId()), deps.getOrDefault(t.getId(), new HashSet<>())))
        .sorted(Comparator.comparingLong(TicketO::getId))
        .forEach(unassigned::add);

    return result;
  }

  //---------------------------- Property Methods -----------------------------

}
