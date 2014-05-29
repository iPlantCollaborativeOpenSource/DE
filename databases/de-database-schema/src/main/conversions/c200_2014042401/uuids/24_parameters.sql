SET search_path = public, pg_catalog;

--
-- Updates parameters uuid foreign keys.
-- Adds temporary indexes to help speed up the conversion.
--
CREATE INDEX parameters_validator_idx ON parameters(validator);
CREATE INDEX validator_rule_validator_id_idx ON validator_rule_v187(validator_id);
CREATE INDEX validator_rule_rule_id_idx ON validator_rule_v187(rule_id);

UPDATE validation_rules r SET parameter_id =
    (SELECT p.id FROM parameters p
     LEFT JOIN validator_rule_v187 vr ON vr.validator_id = p.validator
     WHERE r.hid = vr.rule_id);

-- Drop temporary indexes.
DROP INDEX parameters_validator_idx;
DROP INDEX validator_rule_validator_id_idx;
DROP INDEX validator_rule_rule_id_idx;

-- Cleanup rows with NULL foreign keys.
ALTER TABLE validation_rule_arguments DROP CONSTRAINT rule_argument_rule_id_fkey;
ALTER TABLE validator_rule_v187 DROP CONSTRAINT validator_rule_rule_id_fkey;
DELETE FROM validation_rules WHERE parameter_id IS NULL;

-- Add NOT NULL constraints on foreign key columns.
ALTER TABLE ONLY validation_rules ALTER COLUMN parameter_id SET NOT NULL;

