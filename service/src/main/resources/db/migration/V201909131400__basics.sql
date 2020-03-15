CREATE TABLE boards (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  owner BIGINT NOT NULL, FOREIGN KEY (owner) REFERENCES users (id) ON DELETE CASCADE
);

-- Sprints are tickets are unrelated until a solution is found.
CREATE TABLE sprints (
  id BIGSERIAL PRIMARY KEY,
  board_id BIGINT NOT NULL, FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  capacity INTEGER NOT NULL,
  ordinal INTEGER NOT NULL
);
CREATE INDEX ON sprints (board_id);

-- A ticket may appear on a board different from the one it was created on.
CREATE TABLE tickets (
  id BIGSERIAL PRIMARY KEY,
  home_board_id BIGINT NOT NULL, FOREIGN KEY (home_board_id) REFERENCES boards (id) ON DELETE CASCADE,
  board_id BIGINT NOT NULL, FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
  description TEXT NOT NULL,
  weight INTEGER NOT NULL
);

-- This models a set of ticket-sprint pairs per board.
CREATE TABLE solutions (
  board_id BIGINT NOT NULL,
  ticket_id BIGINT NOT NULL,
  sprint_id BIGINT NOT NULL,
  FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
  FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
  FOREIGN KEY (sprint_id) REFERENCES sprints (id) ON DELETE CASCADE,
  PRIMARY KEY (board_id, ticket_id, sprint_id)
);
CREATE INDEX ON solutions (board_id);

CREATE TABLE ticket_deps (
  from_ticket_id BIGINT NOT NULL,
  to_ticket_id BIGINT NOT NULL,
  FOREIGN KEY (from_ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
  FOREIGN KEY (to_ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
  PRIMARY KEY (from_ticket_id, to_ticket_id)
);

CREATE TABLE ticket_pins (
  ticket_id BIGINT NOT NULL,
  sprint_id BIGINT NOT NULL,
  FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
  FOREIGN KEY (sprint_id) REFERENCES sprints (id) ON DELETE CASCADE,
  PRIMARY KEY (ticket_id, sprint_id)
);

