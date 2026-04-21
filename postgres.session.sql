-- UPDATE reports
-- SET assigned_driver_id = 'ae15367c-57b3-4c48-8d6c-37f71c72c426'::uuid,
--     assigned_at = now(),
--     status = 'ASSIGNED',
--     updated_at = now()
-- WHERE id = '4add0380-ec99-4840-9844-88fc313fff2c'::uuid
--   AND status = 'PENDING'
--   AND assigned_driver_id IS NULL
-- RETURNING id, assigned_driver_id, status, assigned_at, updated_at;

SELECT * from users;


-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns
-- WHERE table_name = 'drivers';

-- SELECT conname, pg_get_constraintdef(oid)
-- FROM pg_constraint
-- WHERE conrelid = 'reports'::regclass;

-- ALTER TABLE reports DROP CONSTRAINT reports_status_check;

-- ALTER TABLE reports ADD CONSTRAINT reports_status_check
-- CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED'));