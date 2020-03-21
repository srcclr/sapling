CREATE TABLE notifications
(
    id               BIGSERIAL PRIMARY KEY,
    recipient_id     BIGINT    NOT NULL,
    type             TEXT      NOT NULL,
    story_request_id BIGINT,
    acknowledged     BOOLEAN   NOT NULL DEFAULT false,
    FOREIGN KEY (story_request_id) REFERENCES story_requests (id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES boards (id) ON DELETE CASCADE
);
