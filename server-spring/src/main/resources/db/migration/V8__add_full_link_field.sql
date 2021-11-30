ALTER TABLE alias
    ADD COLUMN full_link CHARACTER VARYING(256) NULL;

UPDATE alias
SET full_link = trim(concat(namespace.keyword, ' ', alias.link)) FROM namespace
WHERE alias.namespace_id = namespace.id;

ALTER TABLE alias
    ALTER COLUMN full_link SET NOT NULL;