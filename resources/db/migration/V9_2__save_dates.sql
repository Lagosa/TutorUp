Alter table tutor_offers_dates drop constraint tutor_offers_dates_offerid_fk;


INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 1),'2019-09-15','2019-09-30','15:00:00','19:45:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 2),'2019-09-09','2019-09-28','15:00:00','19:45:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 3),'2019-10-01','2019-10-15','16:00:00','18:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 4),'2019-09-28','2019-09-30','17:00:00','21:30:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 5),'2019-10-01','2019-10-10','15:15:00','19:15:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 6),'2019-10-01','2019-10-12','15:00:00','17:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 8),'2019-10-09','2019-10-24','16:20:00','19:30:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 9),'2019-10-02','2019-10-15','17:00:00','19:15:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 10),'2019-10-04','2019-10-12','13:00:00','18:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 11),'2019-09-23','2019-09-29','14:00:00','17:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 12),'2019-10-06','2019-10-12','16:15:00','21:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 13),'2019-09-27','2019-10-30','15:00:00','19:15:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 15),'2019-10-15','2019-10-20','15:00:00','18:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 16),'2019-10-03','2019-10-18','17:15:00','20:15:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 17),'2019-09-18','2019-09-30','15:45:00','19:30:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 18),'2019-10-05','2019-10-30','15:15:00','18:45:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 19),'2019-10-10','2019-10-20','18:00:00','21:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 21),'2019-10-02','2019-10-19','16:00:00','18:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 22),'2019-10-06','2019-10-12','16:15:00','19:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 23),'2019-10-20','2019-10-25','16:00:00','20:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 24),'2019-10-14','2019-10-25','16:10:00','20:30:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 25),'2019-09-27','2019-09-30','16:00:00','18:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 26),'2019-10-10','2019-10-30','16:00:00','20:00:00','ACTIVE');

INSERT INTO tutor_offers_dates (offerid,datefrom,dateto,hourfrom,hourto,status)
VALUES ((select max(offerid) from tutor_offers where tutorid = 27),'2019-10-09','2019-10-27','18:00:00','20:30:00','ACTIVE');
