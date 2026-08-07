-- Run as a database administrator before the representative's first Google
-- sign-in. Replace every value below; never commit a real email to a migration.
SET @representative_email = 'replace-with-verified-google-email@example.com';
SET @hospital_id = 1;
SET @employee_id = 'DEMO-REP-001';
SET @organization_name = 'Brahmaputra City Hospital';

INSERT INTO representative_invitations (
    id,
    email,
    hospital_id,
    employee_id,
    organization_name,
    accepted_at,
    active,
    created_at,
    updated_at
) VALUES (
    UUID(),
    LOWER(TRIM(@representative_email)),
    @hospital_id,
    @employee_id,
    @organization_name,
    NULL,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON DUPLICATE KEY UPDATE
    employee_id = VALUES(employee_id),
    organization_name = VALUES(organization_name),
    accepted_at = NULL,
    active = TRUE,
    updated_at = CURRENT_TIMESTAMP;

SELECT email, hospital_id, employee_id, organization_name, active
FROM representative_invitations
WHERE email = LOWER(TRIM(@representative_email))
  AND hospital_id = @hospital_id;
