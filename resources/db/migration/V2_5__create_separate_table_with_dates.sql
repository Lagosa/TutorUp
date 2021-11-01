DELETE FROM tutor_offers;

ALTER TABLE tutor_offers DROP COLUMN dateFrom;
ALTER TABLE tutor_offers DROP COLUMN dateTo;

ALTER TABLE tutor_offers DROP COLUMN parentOfferID;
ALTER TABLE tutor_offers DROP COLUMN inSeriesNumber;

CREATE TABLE tutor_offers_dates
(
    dateId bigserial NOT NULL,
    offerId int NOT NULL,
    dateFrom DATE NOT NULL,
    dateTo DATE NOT NULL,
    hourFrom TIME(7) NOT NULL,
    hourTo TIME(7) NOT NULL,
    status varchar(50) NOT NULL DEFAULT('ACTIVE')
);

ALTER TABLE tutor_offers_dates ADD CONSTRAINT tutor_offers_dates_offerId_fk FOREIGN KEY(offerId) REFERENCES tutor_offers(offerid);

CREATE INDEX tutor_offers_dates_offerId_idx ON tutor_offers_dates(offerId);
CREATE INDEX tutor_offers_dates_dateFrom_idx ON tutor_offers_dates(dateFrom);
CREATE INDEX tutor_offers_dates_dateTo_idx ON tutor_offers_dates(dateTo);
CREATE INDEX tutor_offers_dates_status_idx ON tutor_offers_dates(status);