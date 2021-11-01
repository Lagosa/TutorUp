CREATE TABLE interactions(
    id bigserial NOT NULL,
    senderid int NOT NULL,
    receiverid int NOT NULL,
    status varchar(50) DEFAULT 'PENDING' NOT NULL
);
ALTER TABLE interactions ADD CONSTRAINT interactions_senderid FOREIGN KEY (senderid) REFERENCES tutorup_user(id);
ALTER TABLE interactions ADD CONSTRAINT interactions_receiverid FOREIGN KEY (receiverid) REFERENCES tutorup_user(id);

CREATE INDEX interactions_senderid_idx ON interactions(senderid);
CREATE INDEX interactions_receiverid_idx ON interactions(receiverid);