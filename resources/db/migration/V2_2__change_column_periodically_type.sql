ALTER TABLE tutor_offers DROP COLUMN periodically;

ALTER TABLE tutor_offers ADD periodically int NOT NULL;
