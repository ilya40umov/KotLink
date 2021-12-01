CREATE SEQUENCE namespace_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE CACHE 1;

CREATE TABLE namespace
(
    id          BIGINT                 NOT NULL PRIMARY KEY DEFAULT nextval('namespace_id_seq'::regclass),
    keyword     CHARACTER VARYING(128) NOT NULL
        CONSTRAINT ns_kwd_unique UNIQUE,
    description CHARACTER VARYING(512) NOT NULL             DEFAULT ''
);