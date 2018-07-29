CREATE SEQUENCE user_account_id_seq
  START WITH 1
  INCREMENT BY 1
  NO MINVALUE
  NO MAXVALUE
  CACHE 1;

CREATE TABLE user_account (
  id           BIGINT                    NOT NULL PRIMARY KEY DEFAULT nextval('user_account_id_seq'::regclass),
  email        CHARACTER VARYING(1024)   NOT NULL
);

CREATE UNIQUE INDEX user_account_email ON user_account (email);