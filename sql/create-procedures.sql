-- //Sample Procedures 
-- SELECT 'Dropping procedure RAISE_PRICE' AS ' '|
-- drop procedure if exisnexts RAISE_PRICE|


-- SELECT 'Creating procedure SHOW_SUPPLIERS' AS ' '|
-- create procedure SHOW_SUPPLIERS()
--   begin
--     select SUPPLIERS.SUP_NAME, COFFEES.COF_NAME
--     from SUPPLIERS, COFFEES
--     where SUPPLIERS.SUP_ID = COFFEES.SUP_ID
--     order by SUP_NAME;
--   end|

-- SELECT 'Creating procedure GET_SUPPLIER_OF_COFFEE' AS ' '|  
-- create procedure GET_SUPPLIER_OF_COFFEE(IN coffeeName varchar(32), OUT supplierName varchar(40))
--   begin
--     select SUPPLIERS.SUP_NAME into supplierName
--       from SUPPLIERS, COFFEES
--       where SUPPLIERS.SUP_ID = COFFEES.SUP_ID
--       and coffeeName = COFFEES.COF_NAME;
--     select supplierName;
--   end|
 
/* Every day at midnight scheduler job */
BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'job_midnight_daily',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN publicationDueCalculator; dueDateReminder; billDueDateReminder; END;',
    start_date      => '06-NOV-15 01.00.00 AM America/New_York',
    repeat_interval => 'freq=daily;',
    enabled         => TRUE);
END;
/

-- Procedures which every day at midnight

/* Publication due calcuator procedure to calculate late fees for unreturned publications.  Fine - $2/day */

CREATE or replace Procedure publicationDueCalculator
IS
   dueAmount number(10);
   dummy number(1);
   cursor c1 is
     SELECT *
     FROM checkout
     WHERE return_date IS NULL AND (sysdate - end_date) > 0;

BEGIN
   FOR rec in c1
   LOOP
      dueAmount := floor(sysdate - rec.end_date)*2;
begin	
      select 1 into dummy from bill where c_id = rec.c_id;
      update bill b set amount = dueAmount where b.c_id = rec.c_id;
      EXCEPTION WHEN no_data_found THEN
	      insert into bill(b_id,c_id,amount,b_date) values(seq_bill.nextval,rec.c_id,dueAmount,sysdate);
end;
   END LOOP;
commit;
END;
/


/* Procedure which sends due date reminders about unpaid bills.
 30/60/90 day reminders and account gets suspended after 90 days */

create or replace Procedure billDueDateReminder
IS
  rid number(10);
  pid varchar2(10);
  description varchar2(150);
  pastDueDate number(2);

  cursor c1 is 
  select * 
  from bill
  where active = '1' and (ceil(sysdate-b_date) = 30 or ceil(sysdate-b_date) = 60);
  
  cursor c2 is
  select * 
  from bill
  where active = '1' and ceil(sysdate-b_date) = 90;
  
BEGIN
  FOR rec in c1
  LOOP
    pastDueDate:= ceil(sysdate-rec.b_date);
    select r_id,p_id into rid,pid from checkout where c_id = rec.c_id;
    description:= 'Your bill with B_ID = '||to_char(rec.b_id)||' and amount = '||to_char(rec.amount)||' has not been paid for '||to_char(pastDueDate)||' days.';
    insert into reminder values(seq_reminder.nextval,rid,pid,description,sysdate);
  END LOOP;
  
  FOR rec in c2
  LOOP
    select r_id,p_id into rid,pid from checkout where c_id = rec.c_id;
    description:= 'Your account has been temporarily SUSPENDED, since your bill with B_ID = '||to_char(rec.b_id)||' and amount = '||to_char(rec.amount)||' has not been paid for 90 days.';
    insert into reminder values(seq_reminder.nextval,rid,pid,description,sysdate);
    update patron set suspended='1' where p_id = pid;
    insert into reg_hold values(pid,'BILL_DUE');
  END LOOP;

commit;
END;
/

/* Procedure which sends out due dates for publications. 24hours/3days reminders */

create or replace Procedure dueDateReminder
IS
  r_type varchar2(30);
  description varchar2(100);

  cursor c1 is 
  select * 
  from checkout
  where return_date is null and ceil(end_date-sysdate) = 1;
  
  cursor c2 is
  select * 
  from checkout
  where return_date is null and ceil(end_date-sysdate) = 3;
  
BEGIN
  FOR rec in c1
  LOOP
    select resource_type into r_type from library_resource where r_id = rec.r_id;
    description:= 'Your '||r_type||' '||'with R_ID = '||to_char(rec.r_id)||' '||'is due in 24 hours.';
    insert into reminder values(seq_reminder.nextval,rec.r_id,rec.p_id,description,sysdate);
  END LOOP;
  
  FOR rec in c2
  LOOP
    select resource_type into r_type from library_resource where r_id = rec.r_id; 
    description:= 'Your '||r_type||' '||'with R_ID = '||to_char(rec.r_id)||' '||'is due in 3 days.';
    insert into reminder values(seq_reminder.nextval,rec.r_id,rec.p_id,description,sysdate);
  END LOOP;

commit;
END;
/

/* Every hour from midnight scheduler job */

BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'job_midnight_hourly',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN cameraDueCalculator; END;',
    start_date      => '06-NOV-15 01.00.00 AM America/New_York',
    repeat_interval => 'freq=hourly;',
    enabled         => TRUE);
END;
/

-- Procedures which runs every hour
/* Camera due calculator procedure to calculate late fees for unreturned cameras. Fine - $2/hour */

CREATE or replace Procedure cameraDueCalculator
IS
   dueAmount number(4);
   dummy number(1);
   cursor c1 is
     SELECT *
     FROM camera_checkout
     WHERE return_date IS NULL AND checked_out = '1' AND (sysdate - due_date) > 0;
   cursor c2 is
      SELECT *
      FROM camera_checkout
      WHERE return_date is NULL AND checked_out = '1' AND (floor((sysdate - due_date)*24)) = 1;
 
BEGIN
   FOR rec in c1
   LOOP
      dueAmount := floor((sysdate - rec.due_date)*24);
begin	
      select 1 into dummy from bill where c_id = rec.c_id;
      update bill b set amount = dueAmount where b.c_id = rec.c_id;
      EXCEPTION WHEN no_data_found THEN
	      insert into bill(b_id,c_id,amount,b_date) values(seq_bill.nextval,rec.c_id,dueAmount,sysdate);
end;
   END LOOP;
  FOR rec in c2
     LOOP
     	insert into reminder values(seq_reminder.nextval,rec.r_id,rec.p_id,'Late penalty of $2/hour will charged until you return camera '||rec.r_id,sysdate);
     END LOOP;
commit;
END;
/

BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'job_half_hourly',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN roomcheckoutInspector; END;',
    start_date      => '06-NOV-15 01.00.00 AM America/New_York',
    repeat_interval => 'freq=minutely; interval=30; bysecond=0;',
    enabled         => TRUE);
END;
/

-- Procedures which runs every half hour

/* Find unchecked room reservations within one hour and make it back available for other people to reserve
   and rooms that were not checked back in will be done every half hour*/

CREATE or replace Procedure roomcheckoutInspector
IS
BEGIN
  update room_checkout set checkedout = '3' where checkedout='0' AND (floor((sysdate - start_date)*24) = 1);
  update room_checkout set checkedout = '4' where checkedout='1' AND sysdate>end_date;
commit;
END;
/

/* clearBill procedure which clears all the active bills for a p_id after checking if/she 
has returned all resources */

CREATE or replace Procedure clearBill(PID in VARCHAR2)
IS
a number(2);
RESOURCE_NOT_RETURNED Exception;	

BEGIN

Select Count(*) into a from checkout c, camera_checkout cc where (c.p_id = PID and c.return_date IS NULL and sysdate>c.end_date) or (cc.p_id = PID and cc.return_date IS NULL and sysdate>cc.due_date);

if a=0 then
  	Update bill set active='0',return_date=sysdate where c_id in (select c_id from checkout where p_id=PID) or c_id in (select c_id from 	camera_checkout where p_id=PID) and active='1';
	update patron set suspended='0' where p_id = PID;
	delete from reg_hold where s_number=PID and hold_type='BILL_DUE';

else
	RAISE RESOURCE_NOT_RETURNED;
END if;
Exception
	When RESOURCE_NOT_RETURNED then
	Raise_Application_Error (-20002, 'Resource not returned, cannot clear Bill');
	dbms_output.put_line ('RESOURCE_NOT_RETURNED');
commit;
END;
/

// camera procedures

/* proc1 - which will insert into cam_queue */

create or replace procedure cam_queue_insert(Res_ID IN NUMBER,PAT_ID IN VARCHAR2,CHDATE IN DATE)
As
COUNT_QUEUE number(10);
QUEUE_FULL EXCEPTION;
BOOL CHAR;
X VARCHAR2(10);
Y NUMBER(10);
ACCT_SUSPENDED EXCEPTION;
ACCT_HOLD EXCEPTION;
Begin
-- count no of instances in queue for that R_ID
cam_queue_count(Res_ID,CHDATE,COUNT_QUEUE);


--CHECK IF STUDENT IS ALLOWED TO CHECKOUT THE CAMERA(HOLD etc)
SELECT SUSPENDED, P_TYPE
	INTO BOOL, X
	FROM PATRON
	WHERE P_ID = PAT_ID;
	
	IF BOOL = '1' THEN
		RAISE ACCT_SUSPENDED;
	END IF;
	
	-- For students, check if they are on hold
	
	IF X = 'Student' THEN
		SELECT COUNT(*)
		INTO Y
		FROM REG_HOLD
		WHERE S_NUMBER = PAT_ID;
		
		IF Y > 0 THEN
			RAISE ACCT_HOLD;
		END IF;
	END IF;

IF COUNT_QUEUE >=3 THEN
-- NOW RETURN ERROR BY RAISING EXCEPTION
	RAISE QUEUE_FULL;
ELSE
	COUNT_QUEUE := COUNT_QUEUE +1;
	DBMS_OUTPUT.PUT_LINE('You are at queue number ' || COUNT_QUEUE);
END IF;

/* Handle exceptions */
	EXCEPTION
		WHEN ACCT_SUSPENDED THEN
			Raise_Application_Error (-20002, 'Account suspended');
			dbms_output.put_line ('ACCT_SUSPENDED');
		WHEN ACCT_HOLD THEN
			Raise_Application_Error (-20002, 'Student account is on registration hold');
			dbms_output.put_line ('ACCT_HOLD');
		WHEN QUEUE_FULL THEN
			Raise_Application_Error (-20002, 'Queue is full.');
			dbms_output.put_line ('Queue is Full');
end;
/

/* proc2 - which will count no of instances in cam_queue for R_ID */

create or replace procedure cam_queue_count(Res_ID IN NUMBER,CHDATE IN DATE,COUNT_QUEUE OUT NUMBER)
As
BEGIN
Select count(*) 
INTO COUNT_QUEUE
from CAM_QUEUE
WHERE R_ID = Res_ID
AND FLOOR(CHECKOUT_DATE - CHDATE)=0;
END;
/

/* procedure to call the cam_8am procedure at 8am Friday */

BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'camera_8am',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN cam_8am; END;',
    start_date      => next_day(trunc(sysdate-1),'Friday')+8/24,
    repeat_interval => 'freq=weekly;',
    enabled         => TRUE);
END;
/

/* procedure which will run at 8am */

create or replace procedure cam_8am
as
-- variables
res_num number(10);
available varchar2(10);
qid number(10);
pid varchar2(10);
chdate date;
duedate date;
camid VARCHAR2(10);
mk varchar2(20);
mdl varchar2(20);
CURSOR res_cur
IS
	-- here select only those r_id for that particular date(sysdate)
	select distinct r_id from cam_queue where floor(sysdate-checkout_date)=0;
begin
OPEN res_cur;
Loop
	Fetch res_cur into res_num;
	EXIT WHEN res_cur%NOTFOUND;
	-- check if cam is available
	SELECT avail into available
	from LIBRARY_RESOURCE WHERE R_ID = res_num;
	-- if cam available then
	if available = '1' then
		-- get first inserted q_id
		select min(q_id) 
		into qid
		from cam_queue 
		where r_id = res_num
		and considered ='0'
		and floor(sysdate-checkout_date)=0;
		-- exception not needed since cursor is used
		-- change considered to 1
		update cam_queue
		set considered = '1'
		where q_id = qid;
		-- get p_id and checkoutdate for those values
		select checkout_date
		into chdate
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		select p_id
		into pid
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		-- calculate due date
		select chdate+6.75
		into duedate
		from dual;
		-- inserting values into checkout table
		insert into camera_checkout values(seq_cam_checkout.nextval,pid,res_num,chdate,duedate,0,null);
		-- notification saying cam is available and can be checked out by him
		select cam_id,make,model 
		into camid,mk,mdl
		from camera
		where r_id = res_num;
		insert into reminder values(seq_reminder.nextval,res_num,pid,'PLEASE COLLECT YOUR REQUESTED CAMERA: '  || camid ||' '|| mk ||' '|| mdl || ' before 10am. Or else your reservation will be cancelled.',chdate);
	else
		-- pop msg saying resource unavailable
		DBMS_OUTPUT.PUT_LINE('Sorry! Resource unavailable.');
		-- this should be also be in reminder
		select cam_id,make,model 
		into camid,mk,mdl
		from camera
		where r_id = res_num;
		select min(q_id) 
		into qid
		from cam_queue 
		where r_id = res_num
		and considered =0;
		select p_id
		into pid
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		-- add into reminder
		insert into reminder values(seq_reminder.nextval,res_num,pid,'YOUR REQUESTED CAMERA: ' || camid ||' '|| mk ||' '|| mdl || ' is UNAVAILABLE. Sorry',chdate);
		-- make considered equal to 1 for everyone else
		update cam_queue
		set considered = '1'
		where r_id=res_num
		and floor(sysdate-checkout_date)=0;
	end if;
end loop;
close res_cur;
end;
/

/* procedure to call the cam_10am procedure at 10am Friday */
BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'camera_10am',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN cam_10am; END;',
    start_date      => next_day(trunc(sysdate-1),'Friday')+10/24,
    repeat_interval => 'freq=weekly; ',
    enabled         => TRUE);
END;
/

/* procedure which will run at 10am */
create or replace procedure cam_10am
as
-- variables

res_num number(10);
count_qid number(10);
qid number(10);
pid varchar2(10);
chdate date;
duedate date;
camid varchar2(10);
mk varchar2(20);
mdl varchar2(20);

CURSOR cur_camera
IS
	-- check which resources have been checked out
	select distinct r_id from camera_checkout where checked_out = '0' and floor(sysdate-checkout_date)=0;
begin
-- for the ones not checked out add them in reminder,camera_checkout
OPEN cur_camera;
Loop
	Fetch cur_camera into res_num;
	EXIT WHEN cur_camera%NOTFOUND;
	select count(*) into count_qid from cam_queue where r_id = res_num and considered =0;
	if count_qid != 0 then
		select min(q_id) 
		into qid
		from cam_queue 
		where r_id = res_num
		and considered =0;
		-- exception needed if there are no more requests for that resource
		-- change considered to 1
		update cam_queue
		set considered = '1'
		where q_id = qid;
		-- get p_id and checkoutdate for those values
		select checkout_date
		into chdate
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		select p_id
		into pid
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		-- calculate due date
		select chdate+6.75
		into duedate
		from dual;
		-- inserting values into checkout table
		insert into camera_checkout values(seq_cam_checkout.nextval,pid,res_num,chdate,duedate,0,null);
		-- notification saying cam is available and can be checked out by him
		select cam_id,make,model 
		into camid,mk,mdl
		from camera
		where r_id = res_num;
		insert into reminder values(seq_reminder.nextval,res_num,pid,'PLEASE COLLECT YOUR REQUESTED CAMERA: '  || camid ||' '|| mk ||' '|| mdl || ' before 11am. Or else your reservation will be cancelled.',chdate);
	else
		DBMS_OUTPUT.PUT_LINE('No more people in queue for this resource: ' || res_num);
	end if;
end loop;
CLOSE cur_camera;
end;
/

/* procedure to call the cam_11am procedure at 11am Friday */

BEGIN
  DBMS_SCHEDULER.create_job (
    job_name        => 'camera_11am',
    job_type        => 'PLSQL_BLOCK',
    job_action      => 'BEGIN cam_11am; END;',
    start_date      => next_day(trunc(sysdate-1),'Friday')+11/24,
    repeat_interval => 'freq=weekly;',
    enabled         => TRUE);
END;
/

/* procedure which will run  at 11am */

create or replace procedure cam_11am
as
-- variables

res_num number(10);
count_qid number(10);
qid number(10);
pid varchar2(10);
chdate date;
duedate date;
camid varchar2(10);
mk varchar2(20);
mdl varchar2(20);

CURSOR cur_camera
IS
	-- check which resources have been checked out
	select distinct r_id from camera_checkout where r_id not in (select distinct r_id from camera_checkout where checked_out = '1' and floor(sysdate-checkout_date)=0);
begin
-- for the ones not checked out add them in reminder,camera_checkout
OPEN cur_camera;
Loop
	Fetch cur_camera into res_num;
	EXIT WHEN cur_camera%NOTFOUND;
	select count(*) into count_qid from cam_queue where r_id = res_num and considered =0;
	if count_qid != 0 then
		select min(q_id) 
		into qid
		from cam_queue 
		where r_id = res_num
		and considered =0;
		-- exception needed if there are no more requests for that resource
		-- change considered to 1
		update cam_queue
		set considered = '1'
		where q_id = qid;
		-- get p_id and checkoutdate for those values
		select checkout_date
		into chdate
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		select p_id
		into pid
		from cam_queue
		where q_id = qid
		and r_id = res_num;
		-- calculate due date
		select chdate+6.75
		into duedate
		from dual;
		-- inserting values into checkout table
		insert into camera_checkout values(seq_cam_checkout.nextval,pid,res_num,chdate,duedate,0,null);
		-- notification saying cam is available and can be checked out by him
		select cam_id,make,model 
		into camid,mk,mdl
		from camera
		where r_id = res_num;
		insert into reminder values(seq_reminder.nextval,res_num,pid,'PLEASE COLLECT YOUR REQUESTED CAMERA: '  || camid ||' '|| mk ||' '|| mdl || ' before 12am. Or else your reservation will be cancelled.',chdate);
	else
		DBMS_OUTPUT.PUT_LINE('No more people in queue for this resource: ' || res_num);
	end if;
end loop;
CLOSE cur_camera;
end;
/

/* remind next person in queue */

create or replace procedure next_person_in_queue(RID IN NUMBER,next_person OUT VARCHAR2)
As
PID VARCHAR2(10);
QID NUMBER(10);
Ptype varchar2(10);
Cursor reminder_cur
IS
	select Q_ID,P_ID from queue where R_ID=RID order by Q_ID;
begin
next_person := ' ';
OPEN reminder_cur;
Loop
	Fetch reminder_cur into QID,PID;
	EXIT WHEN reminder_cur%NOTFOUND;
	select p_type into ptype from patron where p_ID = pid;
	if reminder_cur%ROWCOUNT = 1 then
		next_person:=pid;
		dbms_output.put_line('First patron is '||pid);
	end if;
	if ptype='Faculty' then
		next_person:=pid;
		dbms_output.put_line('Faculty in Queue' || pid);
		EXIT;
	end if;
end loop;
close reminder_cur;
end;
/


/* Procedure to make resource available when checked in */
CREATE OR REPLACE PROCEDURE RESOURCE_AVAILABLE(RES_ID IN NUMBER)
AS
BEGIN
UPDATE LIBRARY_RESOURCE
SET AVAIL='1'
WHERE R_ID=RES_ID;
END;
/







/* 1. resource availability*/
CREATE OR REPLACE procedure resource_avail(RES_ID IN NUMBER, STR_STA_DATE IN VARCHAR2, STR_RET_DATE IN VARCHAR2, AVAIL OUT CHAR)
IS
	X VARCHAR2(10);
	Y NUMBER(10);
	Z VARCHAR2(30);
	PUB_TYPE VARCHAR2(15);
	STA_DATE DATE;
	RET_DATE DATE;
	INVALID_RESOURCE EXCEPTION;
BEGIN
	STA_DATE := TO_DATE(STR_STA_DATE, 'yyyy/mm/dd hh24:mi');
	RET_DATE := TO_DATE(STR_RET_DATE, 'yyyy/mm/dd hh24:mi');
	SELECT RESOURCE_TYPE
	INTO Z
	FROM LIBRARY_RESOURCE
	WHERE R_ID = RES_ID;
	
	IF Z = 'Book' OR Z = 'Journal' OR Z = 'ConferenceProceeding' THEN
		CASE Z
			WHEN 'Book' THEN
				SELECT P_TYPE
				INTO PUB_TYPE
				FROM BOOK
				WHERE R_ID = RES_ID;
			WHEN 'Journal' THEN
				SELECT P_TYPE
				INTO PUB_TYPE
				FROM JOURNAL
				WHERE R_ID = RES_ID;
			WHEN 'ConferenceProceeding' THEN
				SELECT P_TYPE
				INTO PUB_TYPE
				FROM CONF_PROCEEDINGS
				WHERE R_ID = RES_ID;
		END CASE;
		
		/*Check if it's an electronic copy*/
		IF PUB_TYPE = 'Electronic' THEN
			AVAIL := '1';
		ELSE
			SELECT COUNT(*)
			INTO Y
			FROM CHECKOUT
			WHERE	(R_ID = RES_ID) AND
					((START_DATE < STA_DATE AND END_DATE > RET_DATE) OR
					(START_DATE BETWEEN STA_DATE AND RET_DATE) OR
					(END_DATE BETWEEN STA_DATE AND RET_DATE) OR 
					(RETURN_DATE IS NULL));
			
			IF Y > 0 THEN
				AVAIL := '0';
			ELSE
				AVAIL := '1';
			END IF;
		END IF;
	ELSE
		RAISE INVALID_RESOURCE;
	END IF;
	
	EXCEPTION
		WHEN INVALID_RESOURCE THEN
		Raise_Application_Error (-20002, 'Unexpected resource type');
		dbms_output.put_line ('INVALID_RESOURCE');
END;
/

/*room reservation*/
CREATE OR REPLACE procedure checkout_room(RES_ID IN NUMBER, PAT_ID IN VARCHAR2, STR_STA_DATE VARCHAR2, STR_RET_DATE IN VARCHAR2)
IS
	BOOL CHAR;
	X VARCHAR2(10);
	Y NUMBER(10);
	STA_DATE DATE;
	RET_DATE DATE;
	RES_UNAVAILABLE EXCEPTION;
	ACCT_SUSPENDED EXCEPTION;
	ACCT_HOLD EXCEPTION;
	INVALID_END_DATE EXCEPTION;
BEGIN
	/*Check availability*/

	STA_DATE := TO_DATE(STR_STA_DATE, 'yyyy/mm/dd hh24:mi');
	RET_DATE := TO_DATE(STR_RET_DATE, 'yyyy/mm/dd hh24:mi');
	/*Check if account is suspended*/
	SELECT SUSPENDED, P_TYPE
	INTO BOOL, X
	FROM PATRON
	WHERE P_ID = PAT_ID;
	
	IF BOOL = '1' THEN
		RAISE ACCT_SUSPENDED;
	END IF;
	
	/*For students, check if they are on hold*/
	IF X = 'Student' THEN
		SELECT COUNT(*)
		INTO Y
		FROM REG_HOLD
		WHERE S_NUMBER = PAT_ID;
		
		IF Y > 0 THEN
			RAISE ACCT_HOLD;
		END IF;
	END IF;
	
	/*Check if return date is valid*/
	IF RET_DATE - STA_DATE > 1/8 THEN
		RAISE INVALID_END_DATE;
	END IF;
	
	/*Insert into room checkout table*/
	INSERT INTO ROOM_CHECKOUT(C_ID, R_ID, P_ID, START_DATE, END_DATE)
	VALUES(seq_room_checkout.nextval, RES_ID, PAT_ID, STA_DATE, RET_DATE);

	
	/*Handle exceptions*/
	EXCEPTION
		WHEN RES_UNAVAILABLE THEN
			Raise_Application_Error (-20002, 'Resource is unavailable');
		WHEN ACCT_SUSPENDED THEN
			Raise_Application_Error (-20002, 'Account suspended');
		WHEN ACCT_HOLD THEN
			Raise_Application_Error (-20002, 'Student account is on registration hold');
		WHEN INVALID_END_DATE THEN
			Raise_Application_Error (-20002, 'The end time entered exceeds the allowed limit (3 hours)');

END;
/

/* Procedure to check out a room after reservation starts*/
CREATE OR REPLACE procedure check_out_room(RES_ID IN NUMBER, PAT_ID IN VARCHAR2)
IS
	CID NUMBER(10);
	INVALID_CHECKOUT_TIME EXCEPTION;
BEGIN
	/*Check if check-in time is valid*/
	BEGIN
		SELECT C_ID
		INTO CID
		FROM ROOM_CHECKOUT
		WHERE	P_ID = PAT_ID 
			AND R_ID = RES_ID
			AND SYSDATE BETWEEN START_DATE AND END_DATE;
	EXCEPTION
	WHEN NO_DATA_FOUND THEN
		CID := NULL;
	END;

	/*If request is valid, update the room checkout entry*/
	IF CID IS NOT NULL THEN
		UPDATE ROOM_CHECKOUT
				SET CHECKEDOUT = '1'
				WHERE C_ID = CID;
	ELSE
		RAISE INVALID_CHECKOUT_TIME;
	END IF;
	
	/*Handle exceptions*/
	EXCEPTION
		WHEN INVALID_CHECKOUT_TIME THEN
			Raise_Application_Error (-20002, 'Check out time provided does not match reservation time');
END;
/

/* Procedure to check in a room after checking out*/
CREATE OR REPLACE procedure check_in_room(RES_ID IN NUMBER, PAT_ID IN VARCHAR2)
IS
	CID NUMBER(10);
	INVALID_CHECKIN_TIME EXCEPTION;
BEGIN
	/*Check if check-in time is valid*/
	BEGIN
		SELECT C_ID
		INTO CID
		FROM ROOM_CHECKOUT
		WHERE	P_ID = PAT_ID 
			AND R_ID = RES_ID
			AND SYSDATE BETWEEN START_DATE AND END_DATE
			AND CHECKEDOUT = '1';
	EXCEPTION
	WHEN NO_DATA_FOUND THEN
		CID := NULL;
	END;

	/*If request is valid, update the room checkout entry*/
	IF CID IS NOT NULL THEN
		UPDATE ROOM_CHECKOUT
				SET CHECKEDOUT = '2'
				WHERE C_ID = CID;
	ELSE
		RAISE INVALID_CHECKIN_TIME;
	END IF;
	
	/*Handle exceptions*/
	EXCEPTION
		WHEN INVALID_CHECKIN_TIME THEN
			Raise_Application_Error (-20002, 'Check in time does not match reservation time OR Room has not been checked out');
END;
/

CREATE OR REPLACE procedure checkout_publication(RES_ID IN NUMBER, PAT_ID IN VARCHAR2, STR_STA_DATE IN VARCHAR2, STR_RET_DATE IN VARCHAR2)
IS
	BOOL CHAR;
	X VARCHAR2(10);
	Y NUMBER(10);
	Z VARCHAR2(30);
	PUB_ID VARCHAR2(15);
	REC_PUB VARCHAR2(15);
	PUB_TYPE VARCHAR2(15);
	COURSE VARCHAR2(6);
	RETURNING_DATE DATE;
	STA_DATE DATE;
	RET_DATE DATE;
	cursor user_checkouts is
		SELECT *
		FROM CHECKOUT
		WHERE P_ID = PAT_ID AND RETURN_DATE IS NULL;
	
	user_rec user_checkouts%rowtype;
	
	RES_UNAVAILABLE EXCEPTION;
	ACCT_SUSPENDED EXCEPTION;
	ACCT_HOLD EXCEPTION;
	INVALID_RESOURCE EXCEPTION;
	DUPLICATE_PUBLICATION EXCEPTION;
	STUDENT_COURSE EXCEPTION;
	INVALID_DUE_DATE EXCEPTION;
	ECOPY_DUE_DATE EXCEPTION;
	
BEGIN
	/*Check availability*/
	STA_DATE := TO_DATE(STR_STA_DATE, 'yyyy/mm/dd hh24:mi');
	RET_DATE := TO_DATE(STR_RET_DATE, 'yyyy/mm/dd hh24:mi');
	RESOURCE_AVAIL(RES_ID, STR_STA_DATE, STR_RET_DATE, BOOL);

	IF BOOL = '0' THEN
		RAISE RES_UNAVAILABLE;
	END IF;

	/*Check if account is suspended*/
	SELECT SUSPENDED, P_TYPE
	INTO BOOL, X
	FROM PATRON
	WHERE P_ID = PAT_ID;
	
	IF BOOL = '1' THEN
		RAISE ACCT_SUSPENDED;
	END IF;
	
	/*For students, check if they are on hold*/
	IF X = 'Student' THEN
		SELECT COUNT(*)
		INTO Y
		FROM REG_HOLD
		WHERE S_NUMBER = PAT_ID;
		
		IF Y > 0 THEN
			RAISE ACCT_HOLD;
		END IF;
	END IF;
	
	/*Check if the patron has already checked out the same publication*/
	SELECT RESOURCE_TYPE
	INTO Z
	FROM LIBRARY_RESOURCE
	WHERE R_ID = RES_ID;
	
	/*First get the PUB_ID of the publication*/
	CASE Z
		WHEN 'Book' THEN
			SELECT ISBN, P_TYPE
			INTO PUB_ID, PUB_TYPE
			FROM BOOK
			WHERE R_ID = RES_ID;
		WHEN 'Journal' THEN
			SELECT ISSN, P_TYPE
			INTO PUB_ID, PUB_TYPE
			FROM JOURNAL
			WHERE R_ID = RES_ID;
		WHEN 'ConferenceProceeding' THEN
			SELECT CONF_NUM, P_TYPE
			INTO PUB_ID, PUB_TYPE
			FROM CONF_PROCEEDINGS
			WHERE R_ID = RES_ID;
		ELSE
			RAISE INVALID_RESOURCE;
	END CASE;
	
	/*Now check if the user has currently checked out the same PUB_ID*/
	FOR user_rec in user_checkouts
	LOOP
		Y := user_rec.R_ID;
		RETURNING_DATE := user_rec.RETURN_DATE;
	
		
		SELECT RESOURCE_TYPE
		INTO Z
		FROM LIBRARY_RESOURCE
		WHERE R_ID = Y;
		
		CASE Z
			WHEN 'Book' THEN
				SELECT ISBN
				INTO REC_PUB
				FROM BOOK
				WHERE R_ID = Y;
			WHEN 'Journal' THEN
				SELECT ISSN
				INTO REC_PUB
				FROM JOURNAL
				WHERE R_ID = Y;
			WHEN 'ConferenceProceeding' THEN
				SELECT CONF_NUM
				INTO REC_PUB
				FROM CONF_PROCEEDINGS
				WHERE R_ID = Y;
			ELSE
				REC_PUB := '0';
		END CASE;
		
		IF REC_PUB <> '0' AND RETURNING_DATE IS NULL THEN
			IF REC_PUB = PUB_ID THEN
				RAISE DUPLICATE_PUBLICATION;
			END IF;
		END IF;
		
	END LOOP;
	
	/*Check if student is part of course for reserved books*/
	SELECT RESOURCE_TYPE
	INTO Z
	FROM LIBRARY_RESOURCE
	WHERE R_ID = RES_ID;	

	IF Z = 'Book' THEN
		SELECT ISBN
		INTO PUB_ID
		FROM BOOK
		WHERE R_ID = RES_ID;
		
		/*Handling NO_DATA_FOUND in case no course reservation exists*/
		BEGIN
			SELECT COURSE_ID
			INTO COURSE
			FROM COURSE_RESERVATION
			WHERE ISBN = PUB_ID;
		EXCEPTION
		WHEN NO_DATA_FOUND THEN
			COURSE := NULL;
		END;
		BOOL := '0';
		/*If course is NOT null, book is reserved for a course*/
		IF COURSE IS NOT NULL THEN
			SELECT COUNT(*)
			INTO Y
			FROM ENROLLS
			WHERE STUDENT_ID = PAT_ID AND COURSE_NO = COURSE;
			
			IF Y = 0 THEN
				RAISE STUDENT_COURSE;
			ElSE
				/*Flag signifying book is reserved for course (for handling due date)*/
				BOOL := '1';
			END IF;
		END IF;	
	END IF;
	
	IF PUB_TYPE = 'Hardcopy' THEN
		CASE Z
			WHEN 'Book' THEN
				IF BOOL = '0' THEN
					IF X = 'Student' THEN
						IF RET_DATE - STA_DATE > 14 THEN
							RAISE INVALID_DUE_DATE;
						END IF;
					END IF;
					/*1 month = 30 days?*/
					IF X = 'Faculty' THEN
						IF RET_DATE - STA_DATE > 30 THEN
							RAISE INVALID_DUE_DATE;
						END IF;
					END IF;
				ELSIF BOOL = '1' THEN
					IF RET_DATE - STA_DATE > 1/6 THEN
						RAISE INVALID_DUE_DATE;
					END IF;
				END IF;
			WHEN 'Journal' THEN
				IF RET_DATE - STA_DATE > 1/2 THEN
					RAISE INVALID_DUE_DATE;
				END IF;
			WHEN 'ConferenceProceeding' THEN
				IF RET_DATE - STA_DATE > 1/2 THEN
					RAISE INVALID_DUE_DATE;
				END IF;
		END CASE;
	END IF;
	
	/*Insert into checkout table*/
	IF PUB_TYPE = 'Hardcopy' THEN
		INSERT INTO CHECKOUT(C_ID, R_ID, P_ID, START_DATE, END_DATE)
		VALUES(seq_checkout.nextval, RES_ID, PAT_ID, STA_DATE, RET_DATE);
	ELSE
		INSERT INTO CHECKOUT(C_ID, R_ID, P_ID, START_DATE, END_DATE)
		VALUES(seq_checkout.nextval, RES_ID, PAT_ID, STA_DATE, TO_DATE('9999-12-31', 'YYYY-MM-DD'));
	END IF;
	
	/*Handle exceptions*/
	EXCEPTION
		WHEN RES_UNAVAILABLE THEN
			Raise_Application_Error (-20002, 'Resource is unavailable');
		WHEN ACCT_SUSPENDED THEN
			Raise_Application_Error (-20002, 'Account suspended');
		WHEN ACCT_HOLD THEN
			Raise_Application_Error (-20002, 'Student account is on registration hold');
		WHEN INVALID_RESOURCE THEN
			Raise_Application_Error (-20002, 'Invalid resource type');
		WHEN DUPLICATE_PUBLICATION THEN
			Raise_Application_Error (-20002, 'A copy of this publication has already been checked out by user');
		WHEN STUDENT_COURSE THEN
			Raise_Application_Error (-20002, 'This book is reserved for a course that user is not enrolled in');
		WHEN INVALID_DUE_DATE THEN
			Raise_Application_Error (-20002, 'The due date entered exceeds the allowed limit');
		WHEN ECOPY_DUE_DATE THEN
			Raise_Application_Error (-20002, 'Invalid due date for electronic copy');
END;
/

/* Procedure to renew a publication*/
CREATE OR REPLACE procedure renew_publication(RES_ID IN NUMBER, PAT_ID IN VARCHAR2, STR_STA_DATE IN VARCHAR2, STR_RET_DATE IN VARCHAR2)
IS
	BOOL CHAR;
	X VARCHAR2(10);
	Y NUMBER(10);
	Z VARCHAR2(30);
	PUB_ID VARCHAR2(15);
	PUB_TYPE VARCHAR2(15);
	COURSE VARCHAR2(6);
	STA_DATE DATE;
	RET_DATE DATE;
	
	ACCT_SUSPENDED EXCEPTION;
	ACCT_HOLD EXCEPTION;
	INVALID_RESOURCE EXCEPTION;
	DUPLICATE_PUBLICATION EXCEPTION;
	STUDENT_COURSE EXCEPTION;
	INVALID_DUE_DATE EXCEPTION;
	ECOPY_DUE_DATE EXCEPTION;
	CANNOT_RENEW_ECOPY EXCEPTION;
	START_BEFORE_END EXCEPTION;
	PUB_ALREADY_DUE EXCEPTION;
	
BEGIN
	STA_DATE := TO_DATE(STR_STA_DATE, 'yyyy/mm/dd hh24:mi');
	RET_DATE := TO_DATE(STR_RET_DATE, 'yyyy/mm/dd hh24:mi');
	
	/*Check if end date given is valid*/
	IF RET_DATE < STA_DATE THEN
		RAISE START_BEFORE_END;
	END IF;
	
	IF STA_DATE < SYSDATE THEN
		RAISE PUB_ALREADY_DUE;
	END IF;
	
	/*Check if account is suspended*/
	SELECT SUSPENDED, P_TYPE
	INTO BOOL, X
	FROM PATRON
	WHERE P_ID = PAT_ID;
	
	IF BOOL = '1' THEN
		RAISE ACCT_SUSPENDED;
	END IF;
	
	/*For students, check if they are on hold*/
	IF X = 'Student' THEN
		SELECT COUNT(*)
		INTO Y
		FROM REG_HOLD
		WHERE S_NUMBER = PAT_ID;
		
		IF Y > 0 THEN
			RAISE ACCT_HOLD;
		END IF;
	END IF;
	
	/*Check if student is part of course for reserved books*/
	SELECT RESOURCE_TYPE
	INTO Z
	FROM LIBRARY_RESOURCE
	WHERE R_ID = RES_ID;

	IF Z = 'Book' THEN
		SELECT ISBN
		INTO PUB_ID
		FROM BOOK
		WHERE R_ID = RES_ID;
		
		/*Handling NO_DATA_FOUND in case no course reservation exists*/
		BEGIN
			SELECT COURSE_ID
			INTO COURSE
			FROM COURSE_RESERVATION
			WHERE ISBN = PUB_ID;
		EXCEPTION
		WHEN NO_DATA_FOUND THEN
			COURSE := NULL;
		END;
		BOOL := '0';
		/*If course is NOT null, book is reserved for a course*/
		IF COURSE IS NOT NULL THEN
			SELECT COUNT(*)
			INTO Y
			FROM ENROLLS
			WHERE STUDENT_ID = PAT_ID AND COURSE_NO = COURSE;
			
			IF Y = 0 THEN
				RAISE STUDENT_COURSE;
			ElSE
				/*Flag signifying book is reserved for course (for handling due date)*/
				BOOL := '1';
			END IF;
		END IF;	
	END IF;
	
	
	CASE Z
	WHEN 'Book' THEN
		SELECT P_TYPE
		INTO PUB_TYPE
		FROM BOOK
		WHERE R_ID = RES_ID;
	WHEN 'Journal' THEN
		SELECT P_TYPE
		INTO PUB_TYPE
		FROM JOURNAL
		WHERE R_ID = RES_ID;
	WHEN 'ConferenceProceeding' THEN
		SELECT P_TYPE
		INTO PUB_TYPE
		FROM CONF_PROCEEDINGS
		WHERE R_ID = RES_ID;
	ELSE
		RAISE INVALID_RESOURCE;
	END CASE;
	
	/*Check dates*/
	IF PUB_TYPE = 'Hardcopy' THEN
		CASE Z
			WHEN 'Book' THEN
				IF BOOL = '0' THEN
					IF X = 'Student' THEN
						IF RET_DATE - STA_DATE > 14 THEN
							RAISE INVALID_DUE_DATE;
						END IF;
					END IF;
					/*1 month = 30 days?*/
					IF X = 'Faculty' THEN
						IF RET_DATE - STA_DATE > 30 THEN
							RAISE INVALID_DUE_DATE;
						END IF;
					END IF;
				ELSIF BOOL = '1' THEN
					IF RET_DATE - STA_DATE > 1/6 THEN
						RAISE INVALID_DUE_DATE;
					END IF;
				END IF;
			WHEN 'Journal' THEN
				IF RET_DATE - STA_DATE > 1/2 THEN
					RAISE INVALID_DUE_DATE;
				END IF;
			WHEN 'ConferenceProceeding' THEN
				IF RET_DATE - STA_DATE > 1/2 THEN
					RAISE INVALID_DUE_DATE;
				END IF;
		END CASE;
	END IF;
	
	/*Update checkout table*/
	IF PUB_TYPE = 'Hardcopy' THEN
		UPDATE CHECKOUT
		SET END_DATE = RET_DATE
		WHERE R_ID = RES_ID AND P_ID = PAT_ID AND RETURN_DATE IS NULL;
	ELSE
		RAISE CANNOT_RENEW_ECOPY;
	END IF;
	
	/*Handle exceptions*/
	EXCEPTION
		WHEN ACCT_SUSPENDED THEN
			Raise_Application_Error (-20002, 'Account suspended');
		WHEN ACCT_HOLD THEN
			Raise_Application_Error (-20002, 'Student account is on registration hold');
		WHEN INVALID_RESOURCE THEN
			Raise_Application_Error (-20002, 'Invalid resource type');
		WHEN STUDENT_COURSE THEN
			Raise_Application_Error (-20002, 'This book is reserved for a course that user is not enrolled in');
		WHEN INVALID_DUE_DATE THEN
			Raise_Application_Error (-20002, 'The due date entered exceeds the allowed limit');
		WHEN ECOPY_DUE_DATE THEN
			Raise_Application_Error (-20002, 'Invalid due date for electronic copy');
		WHEN CANNOT_RENEW_ECOPY THEN
			Raise_Application_Error (-20002, 'An electronic publication cannot be renewed');
		WHEN START_BEFORE_END THEN
			Raise_Application_Error (-20002, 'Renewal end date before current end date');
		WHEN PUB_ALREADY_DUE THEN
			Raise_Application_Error (-20002, 'Cannot renew a publication that is currently due');
END;
/
