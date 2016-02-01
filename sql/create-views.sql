/* Active bill each patron has to pay */

/* CREATE VIEW v_BILL_FOR_EACH_PATRON AS 
select P_ID,SUM(AMOUNT) AS AMOUNT from BILL where BILL.STATUS='Y' GROUP BY P_ID;
 */

CREATE VIEW v_BILL_FOR_EACH_PATRON AS 
select c.P_ID,c.C_ID,SUM(b.AMOUNT) AS AMOUNT FROM bill b,checkout c where b.C_ID=c.C_ID  and b.ACTIVE=1 group by P_ID,c.C_ID;

CREATE VIEW v_BILL AS 
select l.RESOURCE_TYPE,l.R_ID,temp.P_ID,temp.AMOUNT from library_resource l,(select c.C_ID,c.R_ID,b.P_ID,b.AMOUNT from checkout c,v_BILL_FOR_EACH_PATRON b where c.C_ID=b.C_ID) temp where temp.R_ID=l.R_ID;

/* Create sample Query Views Given by TA */
CREATE VIEW v_Requested_Resource AS 
select q.R_ID,q.P_ID,r.RESOURCE_TYPE from queue q ,library_resource r where q.R_ID=r.R_ID;

/* sample 

Requested Publications 

CREATE VIEW v_Req_Publication AS 
select P_TITLE from PUBLICATION where Publication_ID in (select ISSN from Journal where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='Journal' and P_ID='S1') ) UNION select P_TITLE from PUBLICATION where Publication_ID in (select ISBN from Book where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='Book' and P_ID='S1') ) UNION select P_TITLE from PUBLICATION where Publication_ID in (select conf_num from CONF_PROCEEDINGS where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='ConferenceProceedings' and P_ID='S1') );
/*
/*
select * from v_Requested_Resource where RESOURCE_TYPE='Journal' or RESOURCE_TYPE='ConferenceProceeding' or RESOURCE_TYPE='Room' ;

*/

/* Requested Rooms */
CREATE VIEW v_Req_Rooms As 
select r.R_ID,r.ROOM_NO,r.CAPACITY,rc.TYPE,rc.LIBRARY_NAME,temp.P_ID from Room r,Room_constraint rc ,(select q.R_ID,q.P_ID from queue q ,library_resource r where q.R_ID=r.R_ID and r.RESOURCE_TYPE='Room') temp where r.RC_ID=rc.RC_ID and  r.R_ID=temp.R_ID ;


/* Requested Camera */
CREATE VIEW v_Req_Cameras As 
select c.R_ID,c.CAM_ID,c.MODEL,c.LENS_CONFIG,c.MEMORY,cq.P_ID from CAMERA c,CAM_QUEUE cq where c.R_ID=cq.R_ID and (cq.checkout_date-sysdate >=0) and considered=0;


/* For GUI Book */

/* Needs change on available */
CREATE VIEW v_BOOK AS 
	SELECT b.R_ID,b.ISBN,b.B_EDITION,b.B_PUBLISHER,b.P_TYPE,p.PUBLICATION_ID,p.P_TITLE,p.YEAR_PUBLISHED,'Yes' B_AVAIL 
	FROM BOOK b,PUBLICATION p 
	WHERE b.ISBN=p.PUBLICATION_ID;

/* For GUI Conf_proceedings */

/* Needs change on available */
CREATE VIEW v_CONF_PROCEEDINGS AS 
	SELECT c.R_ID,c.CONF_NUM,c.NAME_OF_CONFERENCE,c.P_TYPE,p.PUBLICATION_ID,p.P_TITLE,p.YEAR_PUBLISHED,'Yes' B_AVAIL 
	FROM CONF_PROCEEDINGS c,PUBLICATION p
	WHERE c.CONF_NUM=p.PUBLICATION_ID;
	
	
/* For GUI journal */

/* Needs change on available */
CREATE VIEW v_JOURNAL AS 
	SELECT j.R_ID,j.ISSN,j.P_TYPE,p.PUBLICATION_ID,p.P_TITLE,p.YEAR_PUBLISHED,'Yes' B_AVAIL 
	FROM JOURNAL j,PUBLICATION p
	WHERE j.ISSN=p.PUBLICATION_ID;
