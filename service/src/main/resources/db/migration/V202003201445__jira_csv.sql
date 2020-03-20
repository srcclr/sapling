CREATE TABLE jira_csv
(
    board_id BIGINT NOT NULL,
    csv      TEXT   NOT NULL,
    PRIMARY KEY (board_id),
    FOREIGN KEY (board_id) REFERENCES boards (id) ON DELETE CASCADE
);

