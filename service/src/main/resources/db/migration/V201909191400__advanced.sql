
CREATE TABLE epics (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  board_id BIGINT NOT NULL, FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE,
  priority INTEGER NOT NULL
);

-- A ticket only exists under an epic now.
-- This ensures that the lack of a default value in the new foreign key column is
-- fine by removing all tickets.
DELETE FROM tickets;
ALTER TABLE tickets ADD COLUMN epic_id BIGINT NOT NULL REFERENCES epics (id);

-- There's not been a way to add either of these yet.
ALTER TABLE ticket_pins ADD COLUMN board_id BIGINT NOT NULL REFERENCES boards (id);
CREATE INDEX ON ticket_pins (board_id);

ALTER TABLE ticket_deps ADD COLUMN board_id BIGINT NOT NULL REFERENCES boards (id);
CREATE INDEX ON ticket_pins (board_id);