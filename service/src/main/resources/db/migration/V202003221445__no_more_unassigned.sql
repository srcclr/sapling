ALTER TABLE solutions DROP CONSTRAINT solutions_unassigned;
DELETE FROM solutions WHERE unassigned = true and sprint_id is null;
ALTER TABLE solutions DROP COLUMN unassigned;
ALTER TABLE solutions ALTER COLUMN sprint_id SET NOT NULL;
