

CREATE TABLE student_requests
(
    requestId bigserial NOT NULL,
    studentId int NOT NULL,
    subject varchar(255) NOT NULL,
    grade varchar(50) NOT NULL,
    teaching_level varchar(255) NOT NULL,
    meeting_type varchar(50) NOT NULL,
    dateFrom date NOT NULL,
    dateTo date NOT NULL,
    status varchar(50) NOT NULL DEFAULT('ACTIVE'),
    offerid_accepted int,

    CONSTRAINT student_requests_pk PRIMARY KEY (requestId)
);

ALTER TABLE student_requests ADD CONSTRAINT student_requests_studentId_fk FOREIGN KEY(studentId) REFERENCES tutorup_user(id);
ALTER TABLE student_requests ADD CONSTRAINT student_requests_offerid_accepted_fk FOREIGN KEY(offerid_accepted) REFERENCES tutor_offers(offerid);

CREATE INDEX student_requests_studentId_idx ON student_requests(studentId);