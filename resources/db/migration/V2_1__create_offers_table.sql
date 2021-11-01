CREATE TABLE tutor_offers
(
tutorID int NOT NULL,
subject varchar(255) NOT NULL,
teaching_level varchar(255) NOT NULL,
grade varchar(50) NOT NULL,
number_of_students int,
meeting_type varchar(50),
location varchar(100),
price int NOT NULL,
periodically bit NOT NULL,
date_held date NOT NULL,
hourFrom float NOT NULL,
hourTo float NOT NULL,
studentID_accepted varchar(255),
status varchar(25) NOT NULL DEFAULT 'ACTIVE',

CONSTRAINT tutor_offers_pk PRIMARY KEY (tutorID)
);

CREATE INDEX tutor_offers_studentID_idx ON tutor_offers(studentID_accepted) WHERE studentID_accepted IS NOT NULL;
CREATE INDEX tutor_offers_date_held_idx ON tutor_offers(date_held);
CREATE INDEX tutor_offers_subject_idx ON tutor_offers(subject);
CREATE INDEX tutor_offers_grade_idx ON tutor_offers(grade);