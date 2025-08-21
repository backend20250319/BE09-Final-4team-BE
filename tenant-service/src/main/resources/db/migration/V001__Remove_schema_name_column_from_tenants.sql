-- Remove schema_name column from tenants table
-- This migration is part of the refactoring to generate schema names dynamically from tenant_id

-- Step 1: Remove the schema_name column from tenants table
ALTER TABLE tenants DROP COLUMN IF EXISTS schema_name;

-- Note: schema_name is now generated dynamically using the pattern: tenant_{tenant_id}
-- No data migration is needed since schema names can be regenerated from tenant_id