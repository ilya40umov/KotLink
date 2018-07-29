CREATE SEQUENCE api_secret_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE api_secret (
  id                BIGINT                    NOT NULL PRIMARY KEY DEFAULT nextval('api_secret_id_seq'::regclass),
  secret            CHARACTER VARYING(64)     NOT NULL CONSTRAINT api_secret_unique UNIQUE,
  user_account_id   BIGINT                    NOT NULL REFERENCES user_account ON DELETE RESTRICT
);

CREATE UNIQUE INDEX api_secret_account_id on api_secret (user_account_id);