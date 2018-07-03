CREATE SEQUENCE alias_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE alias (
  id           BIGINT                    NOT NULL PRIMARY KEY DEFAULT nextval('alias_id_seq'::regclass),
  namespace_id BIGINT                    NOT NULL REFERENCES namespace ON DELETE RESTRICT,
  link         CHARACTER VARYING(128)    NOT NULL,
  redirect_url CHARACTER VARYING(2048)   NOT NULL,
  description  CHARACTER VARYING(512)    NOT NULL DEFAULT ''
);

CREATE UNIQUE INDEX alias_ns_id_link on alias (namespace_id, link);