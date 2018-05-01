CREATE SEQUENCE namespace_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE namespace (
  id        BIGINT                    PRIMARY KEY NOT NULL DEFAULT nextval('namespace_id_seq'::regclass),
  keyword   CHARACTER VARYING(128)    NOT NULL
);

CREATE INDEX namespace_keyword on namespace (keyword);