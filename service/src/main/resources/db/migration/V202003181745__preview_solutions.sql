ALTER TABLE solutions DROP CONSTRAINT solutions_board_id_ticket_id_sprint_id_key;
ALTER TABLE solutions ADD COLUMN preview BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE solutions ADD UNIQUE (board_id, ticket_id, sprint_id, preview);
