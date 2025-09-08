-- Add created_at and updated_at with defaults, and backfill existing rows
ALTER TABLE work_monitor
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();

-- Ensure existing nulls (if any) are set
UPDATE work_monitor
SET created_at = COALESCE(created_at, now()),
    updated_at = COALESCE(updated_at, now());

-- Optional: keep defaults for future inserts
ALTER TABLE work_monitor
  ALTER COLUMN created_at SET DEFAULT now(),
  ALTER COLUMN updated_at SET DEFAULT now(); 