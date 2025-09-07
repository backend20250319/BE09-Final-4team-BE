-- PostgreSQL UTC migration: convert LocalDateTime-like columns to timestamptz assuming existing values are Asia/Seoul local times

-- schedules
ALTER TABLE schedules
  ALTER COLUMN created_at TYPE timestamptz USING (created_at AT TIME ZONE 'Asia/Seoul'),
  ALTER COLUMN updated_at TYPE timestamptz USING (updated_at AT TIME ZONE 'Asia/Seoul');

-- work_policy
ALTER TABLE work_policy
  ALTER COLUMN created_at TYPE timestamptz USING (created_at AT TIME ZONE 'Asia/Seoul'),
  ALTER COLUMN updated_at TYPE timestamptz USING (updated_at AT TIME ZONE 'Asia/Seoul');

-- annual_leave
ALTER TABLE annual_leave
  ALTER COLUMN created_at TYPE timestamptz USING (created_at AT TIME ZONE 'Asia/Seoul'),
  ALTER COLUMN updated_at TYPE timestamptz USING (updated_at AT TIME ZONE 'Asia/Seoul');

-- work_time_adjustments
ALTER TABLE work_time_adjustments
  ALTER COLUMN created_at TYPE timestamptz USING (created_at AT TIME ZONE 'Asia/Seoul'),
  ALTER COLUMN updated_at TYPE timestamptz USING (updated_at AT TIME ZONE 'Asia/Seoul');

-- leave_requests
ALTER TABLE leave_requests
  ALTER COLUMN requested_at TYPE timestamptz USING (requested_at AT TIME ZONE 'Asia/Seoul'),
  ALTER COLUMN approved_at  TYPE timestamptz USING (approved_at  AT TIME ZONE 'Asia/Seoul');

-- attendance (only if stored as local datetime previously)
-- ALTER TABLE attendance
--   ALTER COLUMN check_in  TYPE timestamptz USING (check_in  AT TIME ZONE 'Asia/Seoul'),
--   ALTER COLUMN check_out TYPE timestamptz USING (check_out AT TIME ZONE 'Asia/Seoul'); 