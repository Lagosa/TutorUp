
ALTER TABLE tutor_offers ADD COLUMN created_at timestamp without time zone NOT NULL DEFAULT now();

ALTER TABLE student_requests ADD COLUMN created_at timestamp without time zone NOT NULL DEFAULT now();

--randomize the created_at dates
update tutor_offers set created_at=now() - ((offerid*1817 || ' minutes')::interval);

--add random profile pictures
update tutorup_user set picture_url='user' || (id % 4)+1 ||'.jpg' where picture_url is null;
