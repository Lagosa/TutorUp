ALTER TABLE tutor_offers DROP CONSTRAINT tutor_offers_pk;

ALTER TABLE tutor_offers ADD offerID serial NOT NULL;
ALTER TABLE tutor_offers ADD PRIMARY KEY (offerID);
