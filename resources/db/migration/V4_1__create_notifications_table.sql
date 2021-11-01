CREATE TABLE notifications(
    id bigserial NOT NULL,
    userid int NOT NULL,
    usernamefrom varchar(100),
    title varchar NOT NULL,
    description varchar,
    linktoopen varchar(256),
    pagetoopen varchar(256),
    dateWhenSent timestamp NOT NULL,
    status varchar(50) NOT NULL DEFAULT 'ACTIVE'
);

ALTER TABLE notifications ADD CONSTRAINT notifications_userid FOREIGN KEY (userid) REFERENCES tutorup_user(id);
CREATE INDEX notifications_userid_idx ON notifications(userid);