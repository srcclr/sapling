ALTER TABLE story_requests
    ADD CONSTRAINT story_requests_from_board_id_fkey FOREIGN KEY (from_board_id)
        REFERENCES boards (id)
        ON DELETE CASCADE;
