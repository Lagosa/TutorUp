ALTER TABLE interactions DROP COLUMN description;

ALTER TABLE interactions ADD description varchar(500);