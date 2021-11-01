DELETE FROM tutor_offers;

ALTER TABLE tutor_offers DROP COLUMN date_held;

ALTER TABLE tutor_offers ADD dateFrom DATE NOT NULL;
ALTER TABLE tutor_offers ADD dateTo DATE NOT NULL;

ALTER TABLE tutor_offers ADD parentOfferID int;
ALTER TABLE tutor_offers ADD inSeriesNumber int NOT NULL DEFAULT (0);
