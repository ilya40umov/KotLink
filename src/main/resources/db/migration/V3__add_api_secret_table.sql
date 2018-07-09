CREATE TABLE api_secret (
  secret       CHARACTER VARYING(64)     NOT NULL PRIMARY KEY,
  user_email   CHARACTER VARYING(1024)   NOT NULL
);

CREATE UNIQUE INDEX api_secret_user_email on api_secret (user_email);