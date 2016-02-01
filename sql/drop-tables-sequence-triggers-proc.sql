/* Run EACH seperately * /
/* Use / after each end manually to run the above script */
/* Drop All Views */


BEGIN 
FOR i IN (SELECT view_name FROM user_views) 
LOOP 
EXECUTE IMMEDIATE('DROP VIEW ' || user || '.' || i.view_name); 
END LOOP; 
END;

/* Drop All Triggers  */

BEGIN 
FOR i IN (SELECT trigger_name FROM user_triggers) 
LOOP 
EXECUTE IMMEDIATE('DROP TRIGGER ' || user || '.' || i.trigger_name); 
END LOOP; 
END;

/* Drop All Sequences  */

BEGIN 
FOR i IN (SELECT sequence_name FROM user_sequences) 
LOOP 
EXECUTE IMMEDIATE('DROP SEQUENCE ' || user || '.' || i.sequence_name); 
END LOOP; 
END;

/* Drop All Tables  */

BEGIN 
FOR i IN (SELECT table_name FROM user_tables) 
LOOP 
EXECUTE IMMEDIATE('DROP TABLE ' || user || '.' || i.table_name || ' CASCADE CONSTRAINTS'); 
END LOOP; 
END;

