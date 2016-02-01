# UniversityLibrary
Group 18 University Library Project for CSC540 DBMS



1.Goto DBMS Group 18/sql


2. Login to sql 
3. Drop all procedures,tables,jobs,triggers,sequences

sqlplus
Enter password: *****

SQL>

@create-tables.sql
@auto-sequencers.sql
@create-views.sql
@create-procedures.sql
@create-triggers.sql
@populate-tables.sql




Run the above sql files to populate sample data with tables, procedures, triggers and views.




Run the JAR file to access the JAVA front-end.

cd DBMS Group 18\jar
java -jar LMS.jar




username : jpink

password : jpink



Access different tabs to request a resource, or reserve a room, or clear outstanding bill amount, etc. 


Close button will end the session and log the patron out. 




In the Application we use - YYYY/MM/DD HH24:Mi  format to enter dates (unless specified otherwise)


Component Tabs
1. Profile - To view and edit profile details
2. Checked out resources - To view resources checked out by a patron.
3. Resource Request - To view resources currently requested by a patron , which are not checked out.
4. Notifications - To view Notifications regarding late fees or any other reminders.
5. Balance - To view and clear bill incurred for a patron.
6. Resources - To View resources available in library and request or checkout resources.


