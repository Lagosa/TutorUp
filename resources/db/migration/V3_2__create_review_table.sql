CREATE TABLE tutor_review(
    reviewId bigserial NOT NULL,
    reviewedUserID int NOT NULL,
    reviewerUserID int NOT NULL,
    rating int NOT NULL,
    remark varchar,
    dateWhenReviewed timestamp NOT NULL,

    CONSTRAINT tutor_review_pk PRIMARY KEY (reviewedUserID, reviewerUserID)
);

ALTER TABLE tutor_review ADD CONSTRAINT tutor_review_reviewedID FOREIGN KEY (reviewedUserID) REFERENCES tutorup_user(id);
ALTER TABLE tutor_review ADD CONSTRAINT tutor_review_reviewerID FOREIGN KEY (reviewerUserID) REFERENCES tutorup_user(id);

CREATE VIEW tutor_review_view AS
SELECT reviewedUserId, avg(rating)
FROM tutor_review
GROUP BY reviewedUserId;