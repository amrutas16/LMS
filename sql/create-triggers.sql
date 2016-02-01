/*
create or replace trigger student_record
before insert
on student
begin
insert into patron(p_id,p_department,suspended)
select s_number,s_department,'N'
from student s
where not exists
(select * from patron p
where p.p_id = s.s_number);
end;
/

/*
insert into trig_table(name)
select t from time_temp;
*/

/* Triggers to be created:
1. Trigger on course reservation
on insert into resource

2. insert into patron
when insert on student,faculty

/*send reminder when publication returned*/

/* trigger to call the proc which will insert into cam_queue */

create or replace trigger cam_queue_insert
before insert on cam_queue
for each row
declare
Q_NO number;
begin
	cam_queue_insert(:new.R_ID,:new.P_ID,:new.CHECKOUT_DATE);
end;
/

/* trigger to remind next person in queue after a resource is returned*/

create or replace trigger remind_next
after update of return_date on checkout
for each row
declare
next varchar2(10);
begin
IF UPDATING ('RETURN_DATE') THEN 
	next_person_in_queue(:new.R_ID,next);
	if(next <> ' ') then	
		insert into reminder values(seq_Reminder.nextval,:new.R_ID,next,'Please collect your resource',sysdate);
		delete from queue where r_id=:new.R_ID and p_id=next;	
	end if;
END IF;
end;
/

