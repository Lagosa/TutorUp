ALTER TABLE tutorup_user DROP COLUMN user_recommend;

ALTER TABLE tutorup_user ADD phone_number varchar(15);
ALTER TABLE tutorup_user ADD skill varchar(512);
ALTER TABLE tutorup_user ADD job varchar(512);
ALTER TABLE tutorup_user ADD city varchar(50);
ALTER TABLE tutorup_user ADD lang varchar(200);
ALTER TABLE tutorup_user ADD picture_url varchar(1000);
ALTER TABLE tutorup_user ADD biography varchar;
ALTER TABLE tutorup_user ADD educational_background varchar;






