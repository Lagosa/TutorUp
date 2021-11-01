CREATE TABLE tutorup_user
(
  id serial NOT NULL,
  username varchar(100) NOT NULL,
  password varchar(512) NOT NULL,
  first_name varchar(256) NOT NULL,
  last_name varchar(256) NOT NULL,
  email varchar(200) NOT NULL,
  dob DATE NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'NEW',
  token varchar(100),
  user_recommend  varchar(200),

  CONSTRAINT tutorup_user_pk PRIMARY KEY (id),
  CONSTRAINT tutorup_user_username_unq UNIQUE (username),
  CONSTRAINT tutorup_user_email_unq UNIQUE (email),
  CONSTRAINT tutorup_user_token_unq UNIQUE (token)
);

CREATE INDEX tutorup_user_role_idx ON tutorup_user(status);
CREATE INDEX tutorup_user_dob_idx ON tutorup_user(dob);