-- No longer needed
ALTER TABLE tickets
    DROP COLUMN home_board_id;

-- Housekeeping, since we do generate tons of solutions
ALTER TABLE solutions
    ALTER COLUMN id SET DATA TYPE BIGINT;
ALTER SEQUENCE solutions_id_seq AS BIGINT;

CREATE TABLE story_requests
(
    id                    BIGSERIAL PRIMARY KEY,
    state                 TEXT   NOT NULL,
    from_board_id         BIGINT NOT NULL,
    to_board_id           BIGINT NOT NULL,
    from_ticket_id        BIGINT NOT NULL,
    to_ticket_id          BIGINT,
    to_ticket_description TEXT,
    to_ticket_weight      INT,
    to_ticket_epic_id     BIGINT,
    to_ticket_sprint_id   BIGINT,
    notes                 TEXT   NOT NULL DEFAULT '',
    FOREIGN KEY (to_board_id) REFERENCES boards (id) ON DELETE CASCADE,
    FOREIGN KEY (from_ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    FOREIGN KEY (to_ticket_id) REFERENCES tickets (id) ON DELETE CASCADE,
    FOREIGN KEY (to_ticket_epic_id) REFERENCES epics (id) ON DELETE CASCADE,
    FOREIGN KEY (to_ticket_sprint_id) REFERENCES sprints (id) ON DELETE CASCADE
);

CREATE INDEX ON story_requests (from_ticket_id);
CREATE INDEX ON story_requests (to_ticket_id);
