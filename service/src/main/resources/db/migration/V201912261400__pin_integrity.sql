
-- Fixes a mistake in the advanced file
DROP INDEX ticket_pins_board_id_idx1;
CREATE INDEX ON ticket_deps (board_id);

ALTER TABLE ticket_pins ADD CONSTRAINT ticket_pins_unique UNIQUE (ticket_id, board_id);
