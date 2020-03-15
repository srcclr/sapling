
ALTER TABLE solutions DROP CONSTRAINT solutions_pkey;
ALTER TABLE solutions ADD COLUMN id SERIAL PRIMARY KEY;
ALTER TABLE solutions ADD UNIQUE (board_id, ticket_id, sprint_id);

ALTER TABLE solutions ADD COLUMN unassigned BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE solutions ALTER COLUMN sprint_id DROP NOT NULL;
ALTER TABLE solutions ADD CONSTRAINT solutions_unassigned CHECK (unassigned OR sprint_id is not null);
