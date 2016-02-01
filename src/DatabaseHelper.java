
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author amol
 */

public class DatabaseHelper {
    
    static final String jdbcURL 
	= "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl";

    public static Connection conn;
    public static Statement stmt;
    public static ResultSet rs;
    
    public static void main (String args[])
    {
    	
    }
    
    public static void connect() throws  SQLException, UnsupportedEncodingException
    {
        try {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        String user = "mkadaba";	
        String passwd = new String(hexStringToByteArray("73797374656d"),"ASCII");		// Your 9 digit student ID number
        conn = null;
        stmt = null;
        rs = null;
        conn = DriverManager.getConnection(jdbcURL, user, passwd);
        stmt = conn.createStatement();
            
    }
    
    public static void close()
   {
        close(rs);
        close(stmt);
        close(conn);
   }
    static void close(Connection conn) {
        if(conn != null) {
            try { conn.close(); } catch(Throwable whatever) {}
        }
    }

    static void close(Statement st) {
        if(st != null) {
            try { st.close(); } catch(Throwable whatever) {}
        }
    }

    static void close(ResultSet rs) {
        if(rs != null) {
            try { rs.close(); } catch(Throwable whatever) {}
        }
    }
    
     private static byte[] hexStringToByteArray(String s) {
		
	   int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
    }

    public static boolean validate(String username, String password) {
        String pid = null;
        try {
            //Validates pid and password and searches if pid is student or faculty
            
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            rs = stmt.executeQuery("SELECT PASSWORD from LOGIN where user_id = " + "'" + username + "'");
            String pass=null;
            while(rs.next())
            {
                pass=rs.getString("PASSWORD");
                System.out.println(pass+"-Passcheck");
            }
            
            rs = stmt.executeQuery("select p_id from login where user_id = " + "'" + username + "'");
            
            while(rs.next())
                pid = rs.getString("p_id");
            
            if (password.equals(pass))
            {
                PatronDetails.pid = pid;   
                if(isStudent(pid) )
                        {PatronDetails.type = "student"; return true;
                        }
                else if (isFaculty(pid))
                        {PatronDetails.type = "faculty";return true;}
                else 
                    return false;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }   

    
       static boolean  isStudent(String pid){ 
        try {
       
            rs = stmt.executeQuery("SELECT * from STUDENT where S_NUMBER =" + "'" + pid + "'");
            
            int count=0;
            while(rs.next())
                count++;
             return (count>0);
             
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
         
            
       }
       static  boolean isFaculty(String pid){
              try {
       rs=stmt.executeQuery("select * from faculty where f_number=" + "'" + pid + "'");
                  //rs = stmt.executeQuery("SELECT * from FACULTY where F_NUMBER = " + pid);
         //   System.out.println(rs.getInt("F_NUMBER"));
            int count=0;
            while (rs.next())
                count++;
            return (count>0);
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
         
       }
       
       public static ArrayList<BillDetails> getBill() throws SQLException
       {
               ArrayList<BillDetails> billList = null;
        try {
           
            rs = stmt.executeQuery("select * from v_BILL where P_ID="+ "'" + PatronDetails.getPid() + "'");
            billList = new ArrayList();
            
            System.out.println("In function get Bill");
            while(rs.next()){
                BillDetails bd = new BillDetails();
                bd.rid = rs.getInt("r_id");
                //System.out.println(rs.getInt("r_id"));
              
                bd.amount = rs.getInt("AMOUNT");
                bd.resourceType = rs.getString("RESOURCE_TYPE");
               
                billList.add(bd);
                
            }

            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        return billList;
    
       }
       
       public static String getAmountDue(String pid )
       {
           try {
                rs=stmt.executeQuery("select NVL(SUM(AMOUNT),0) AS AMOUNT from v_BILL where P_ID="+ "'" + pid + "'");
                 if(rs.next())
                        return rs.getString("AMOUNT");
                 
                
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }   
             return "0";      
       }

       //REMEMBER TO keep commit() from transaction in sqlplus as well whenever u update else it hangs
    static void clearAmountDue(String pid) {
          try {
        int r=stmt.executeUpdate("update  bill set ACTIVE =0 where C_ID IN (select C_ID from checkout where P_ID="+ "'" + pid + "'" + ")");
        conn.commit();
        System.out.println(r+"rows updated in clearAmountDue");
    } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
          
         
    }

    static JList getNotification(String pid) {
        DefaultListModel listModel = new DefaultListModel();
        JList list=null;
        try {
           // System.out.println("select REMINDER_DESCRIPTION from REMINDER where P_ID ="+pid);
                rs=stmt.executeQuery("select * from REMINDER where P_ID="+ "'" + pid + "'");
                
                while (rs.next()) {
                        String remind = rs.getString("REMINDER_DESCRIPTION");
                        Date rdate = rs.getDate("REMINDER_DATE");
                      //  System.out.println("->"+remind);
                        listModel.addElement(remind + "    :   " +rdate.toString());  
                        //list.getSelectedValue();
                }
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex); 
              }
       list= new JList(listModel);
       
       
       
        return list;
   
    }
    
      static JList getRequestedPublication(String pid) {
        DefaultListModel listModel = new DefaultListModel();
        JList list=null;
        try {
           // System.out.println("select REMINDER_DESCRIPTION from REMINDER where P_ID ="+pid);
              //  rs=stmt.executeQuery("select * from REMINDER where P_ID="+ "'" + pid + "'");
                
               //  rs=stmt.executeQuery("select P_TITLE from PUBLICATION where Publication_ID in (select ISSN from Journal where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='Journal' and P_ID="+"'"+pid+"') ) UNION select P_TITLE from PUBLICATION where Publication_ID in (select ISBN from Book where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='Book' and P_ID="+"'"+pid+"') ) UNION select P_TITLE from PUBLICATION where Publication_ID in (select conf_num from CONF_PROCEEDINGS where R_ID in ( select  R_ID from v_Requested_Resource where RESOURCE_TYPE='ConferenceProceedings' and P_ID="+"'"+pid+"') )");
               rs=stmt.executeQuery("select p.p_title from publication p where publication_id in (SELECT ISBN from book where R_ID in (SELECT R_ID from queue where p_id='"+pid+"') UNION SELECT ISSN from journal where R_ID in (SELECT R_ID from queue where p_id='"+pid+"') UNION SELECT CONF_NUM from conf_proceedings where R_ID in (SELECT R_ID from queue where p_id='"+pid+"'))"
);
                
                while (rs.next()) {
                        String ptitle = rs.getString("P_TITLE");
                       
                        listModel.addElement(ptitle);  
                        //list.getSelectedValue();
                }
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex); 
              }
       list= new JList(listModel);
        return list;

    }
      static ArrayList<RoomDetails> getRequestedRooms(String pid) {
          ArrayList<RoomDetails> roomList =new ArrayList();
         
       // DefaultListModel listModel = new DefaultListModel();
        //JList list=null;
        try {
           // System.out.println("select REMINDER_DESCRIPTION from REMINDER where P_ID ="+pid);
              //  rs=stmt.executeQuery("select * from REMINDER where P_ID="+ "'" + pid + "'");
                String query = "select r.r_id, r.room_no, r.position, to_char(rch.start_date,'YYYY/MM/DD HH24:mi') start_date, to_char(rch.end_date,'YYYY/MM/DD HH24:mi') end_date from room r, room_checkout rch, room_constraint rc where r.r_id = rch.r_id and rch.checkedout = '0' and rch.p_id = '" + PatronDetails.pid + "' and r.rc_id = rc.rc_id";
                 rs=stmt.executeQuery(query);
                
                while (rs.next()) {
                     RoomDetails r=new RoomDetails();
                        r.rid= rs.getInt("R_ID");
                        r.roomNo= rs.getString("ROOM_NO");
                        r.checkoutDate=rs.getString("start_date");
                        r.dueDate=rs.getString("end_date");
                        //r.position=rs.getString();
                       // r.=rs.getString("LIBRARY_NAME");
                        //listModel.addElement(RoomNo+"   :   "+type+"   :   "+libname);  
                        //list.getSelectedValue();
                        roomList.add(r);
                }
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex); 
              }
       return roomList;
       

    }
    
      static ArrayList<CameraDetails> getRequestedCameras(String pid) {
       ArrayList<CameraDetails> camList=new  ArrayList();
          DefaultListModel listModel = new DefaultListModel();
       // JList list=null;
        try {
           // System.out.println("select REMINDER_DESCRIPTION from REMINDER where P_ID ="+pid);
              //  rs=stmt.executeQuery("select * from REMINDER where P_ID="+ "'" + pid + "'");
                
                 rs=stmt.executeQuery("select cq.R_ID,cq.P_ID,c.CAM_ID from cam_queue cq,camera c where cq.R_ID=c.R_ID and P_ID='"+pid+"'");
                //c.CAM_ID,c.MODEL,c.LENS_CONFIG,c.MEMORY,
                while (rs.next()) {
                    CameraDetails c=new CameraDetails();
                    c.rid=rs.getInt("R_ID");
                            
                       c.camId = rs.getString("CAM_ID");
                       //c=rs.getString("MODEL");
                       //String lc=rs.getString("LENS_CONFIG");
                       //String mem=rs.getString("MEMORY");
                              
                        //listModel.addElement(Camid+"   :   "+model+"   :   "+lc+"   :   "+mem);  
                        //list.getSelectedValue();
                       camList.add(c);
                }
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex); 
              }
       return camList;

    }
      
      static boolean isCameraAvailable(String pid)
      {
   
      try {
   String query="select P_ID from camera_checkout where CHECKOUT_DATE<=sysdate and DUE_DATE>=sysdate and CHECKED_OUT=0 and RETURN_DATE is null and P_ID='"+pid+"'" ;  
            //rs = stmt.executeQuery("SELECT * from STUDENT where S_NUMBER =" + "'" + pid + "'");
            rs=stmt.executeQuery(query);
            int count=0;
            while(rs.next())
                count++;
             return (count>0);
             
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
         
        
      }
    

    static ArrayList<CameraDetails> getCameras(){
       ArrayList<CameraDetails> cameraList = new ArrayList<>();
        try {
            ResultSet rs = stmt.executeQuery("select * from camera");
           
            while(rs.next())
            {
                CameraDetails cd = new CameraDetails();
                
                cd.camId = rs.getString("cam_id");
                cd.make = rs.getString("make");
                cd.memory = rs.getString("memory");
                cd.lensConfig = rs.getString("lens_config");
                cd.model = rs.getString("model");
                cd.rid = rs.getInt("r_id");
                cameraList.add(cd);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cameraList;
    }

    static void getStudentDetails(String pid) throws UnsupportedEncodingException {
        try {
        
           int enroll_id = 0;
           rs = stmt.executeQuery("SELECT * from STUDENT where s_number =" + "'" + pid + "'");
            while(rs.next())
            {
                StudentDetails.sNumber = rs.getString("s_number");
                StudentDetails.sAddress = rs.getString("s_address");
                StudentDetails.sAltPhoneNo = rs.getInt("s_alternate_phone_no");
                //StudentDetails.sDob = rs.getString("s_dob");
                StudentDetails.sSex = rs.getString("s_sex");
                StudentDetails.sNationality = rs.getString("s_nationality");
                StudentDetails.sPhoneNo = rs.getInt("s_phone_no");
                enroll_id = rs.getInt("s_enrollment_id");
                StudentDetails.sEnrollInfo = enroll_id;
                
                ResultSet rs1 = stmt.executeQuery("select to_char(s_dob, 'yyyy/mm/dd') dob from student where s_number = " + "'" + pid + "'");
                while(rs1.next()){
                    StudentDetails.sDob = rs1.getString("dob");
                }
                
                rs1 = stmt.executeQuery("select * from student_enrollment_info where S_ENROLLMENT_INFO_ID =" + enroll_id );
                while(rs1.next())
                {
                    StudentDetails.sCategory = rs1.getString("S_CATEGORY_OF_STUDENT");
                    StudentDetails.sClassification = rs1.getString("S_CLASSIFICATION");
                    StudentDetails.sDegProg = rs1.getString("S_DEGREE_PROGRAM");
                }
             
                //dept
                rs1 = stmt.executeQuery("select * from patron where p_id = " + "'" + pid + "'");
                while(rs1.next())
                {
                    StudentDetails.sDept = rs1.getString("p_department");
                    StudentDetails.sFname = rs1.getString("p_first_name");
                    StudentDetails.sLname = rs1.getString("p_last_name");
                }
                
            }   
                
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    static ArrayList<RoomDetails> getCheckedoutRooms(String roomType){
        ArrayList<RoomDetails> roomList = new ArrayList();
        try {
            
            String query = "select r.r_id, r.room_no, r.position, to_char(rch.start_date,'YYYY/MM/DD HH24:mi') start_date, to_char(rch.end_date,'YYYY/MM/DD HH24:mi') end_date from room r, room_checkout rch, room_constraint rc where r.r_id = rch.r_id and rch.checkedout = '1' and rch.p_id = '" + PatronDetails.pid + "' and r.rc_id = rc.rc_id";
            rs = stmt.executeQuery(query);
            while(rs.next()){
                System.out.println("In while");
                RoomDetails rd = new RoomDetails();
               
                rd.position = rs.getString("position");
                rd.roomNo = rs.getString("room_no");
                rd.rid = rs.getInt("r_id");
                rd.checkoutDate = rs.getString("start_date");
                rd.dueDate = rs.getString("end_date");
                roomList.add(rd);
                return roomList;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return roomList;
    }
    
    static ArrayList<RoomDetails> getRooms(int capacity, String startTime, String endTime, String startDate, String endDate, String libName) {
        ArrayList<RoomDetails> roomList = new ArrayList<>();
        String startDateTime, endDateTime;
      
        try {
        
            String enteredStartDate = startDate + " " + startTime;
            //System.out.println(enteredStartDate);
            String enteredEndDate = startDate + " " + endTime;
            //System.out.println(enteredEndDate);
            String roomType = RoomDetails.roomType;
            //System.out.println(roomType);
            //System.out.println(libName);
            
            
            //String query = "SELECT r.room_no, r.position, r.r_id FROM room r, room_constraint rc, room_checkout rch WHERE r.r_id = rch.r_id and r.rc_id = rc.rc_id and rc.type = 'Conference Room' and rc.library_name = 'J. B. Hunt' and r.room_no not in (SELECT room_no FROM room_checkout WHERE ((rch.start_date < to_date('2015/12/01 05:00','yyyy/MM/dd hh24:mi') AND rch.end_date > to_date('2015/12/01 05:00','yyyy/MM/dd hh24:mi')) OR (rch.start_date BETWEEN to_date('2015/12/01 05:00','yyyy/MM/dd hh24:mi') AND to_date('2015/12/01 05:00','yyyy/MM/dd hh24:mi')) OR (rch.end_date BETWEEN to_date('2015/12/01 05:00', 'yyyy/MM/dd hh24:mi') AND to_date('2015/12/01 05:00', 'yyyy/MM/dd hh24:mi')) AND (CHECKEDOUT <> '2' AND rch.CHECKEDOUT <> '3' AND rch.CHECKEDOUT <> '4')))";
            String query = "SELECT r.room_no, r.position, r.r_id FROM room r, room_constraint rc, room_checkout rch WHERE r.r_id = rch.r_id and r.rc_id = rc.rc_id and rc.type =" +  "'" + roomType + "'" + "and rc.library_name =" +  "'" + libName + "'" +  "and r.room_no not in (SELECT room_no FROM room_checkout WHERE ((rch.start_date < to_date('" + startDate +"','yyyy/MM/dd hh24:mi') AND rch.end_date > to_date('" + endDate + "','yyyy/MM/dd hh24:mi')) OR (rch.start_date BETWEEN to_date('" + startDate + "','yyyy/MM/dd hh24:mi') AND to_date('" + endDate + "','yyyy/MM/dd hh24:mi')) OR (rch.end_date BETWEEN to_date('" + startDate + "', 'yyyy/MM/dd hh24:mi') AND to_date('" + endDate + "', 'yyyy/MM/dd hh24:mi')) AND (CHECKEDOUT <> '2' AND rch.CHECKEDOUT <> '3' AND rch.CHECKEDOUT <> '4')))";
       
            
            
            
            rs = stmt.executeQuery(query);
            while(rs.next()){
                //JOptionPane.showMessageDialog(null, "get rooms");
                RoomDetails rd = new RoomDetails();
                
                rd.capacity = capacity;
                rd.position = rs.getString("position");
                rd.roomNo = rs.getString("room_no");
                rd.rid = rs.getInt("r_id");
                
                roomList.add(rd);
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        } 
        
        return roomList;
        
    }

    static int insertCamera(CameraDetails selectedCamera,String date ) throws SQLException {
               stmt.executeUpdate("INSERT INTO cam_queue values ( seq_cam_queue.nextVal,'"+selectedCamera.rid+"','"+PatronDetails.pid+"',TO_DATE('"+date+"','YYYY/MM/DD'),'0')");
  
           conn.commit();
            try {
               rs=stmt.executeQuery("Select count(*)  as count from CAM_QUEUE WHERE R_ID = "+selectedCamera.rid+" AND FLOOR(CHECKOUT_DATE - TO_DATE('"+date+"','YYYY/MM/DD'))=0");
                if(rs.next())
                       return rs.getInt("count");
                
               
             } catch (SQLException ex) {
           Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
             }
         
       
    
           //PreparedStatement prstmt;
           //prstmt = conn.prepareStatement("insert into cam_queue " +  "values(?,?, ?, ?, ?)");
           //pstmt.setInt(1, seq_cam_q);
           //stmt.executeQuery("insert into cam_queue " +  "values(seq_cam_queue.nextVal,selectedCamera.rid, PatronDetails.pid, cameraCheckoutDateTxtField.getText(), '0');");
       /*
           CallableStatement pstmt = conn.prepareCall("{call cam_queue_count(?,?,?)}");
           pstmt.setInt(1, selectedCamera.rid);
           /*
           SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        format.setLenient(false);
         Date date1 =format.parse(date1);
         
 
       cal.setTime(date);
          /* 
           int year=Integer.parseInt("2015");
           int month=Integer.parseInt("11");
           int day=Integer.parseInt("06");
           
           pstmt.setDate(2, new Date(year, month, day) );
           pstmt.registerOutParameter(3, Types.NUMERIC);
           pstmt.executeUpdate();
       
           int camQueueNo = pstmt.getInt(3);
           */
           return 0;
    }

    static ArrayList<BookDetails> getCheckedOutBooks() {
        ArrayList<BookDetails> cBookList = new ArrayList();
        try {
            //ResultSet rs = stmt.executeQuery("select p.p_title,c.r_id, to_date(c.start_date,'YYYY/MM/DD HH24:mi') start_date, to_date(c.end_date,'YYYY/MM/DD HH24:mi') end_date from publication p,book b,checkout c where p.PUBLICATION_ID=b.ISBN  and b.r_id=c.r_id and c.p_id =" + "'" + PatronDetails.pid + "'");
            ResultSet rs = stmt.executeQuery("select p.p_title,c.r_id, to_char(c.start_date,'YYYY/MM/DD HH24:mi') start_date, to_char(c.end_date,'YYYY/MM/DD HH24:mi') end_date from publication p,book b,checkout c where p.PUBLICATION_ID=b.ISBN and c.return_date is null and b.r_id=c.r_id and c.p_id =" + "'" + PatronDetails.pid + "'");
            while(rs.next()){
                
                BookDetails bd = new BookDetails();
                bd.rid = rs.getInt("r_id");
               
                bd.bTitle = rs.getString("p_title");
                
                bd.bCheckoutDate = rs.getString("start_date");
                bd.bDueDate = rs.getString("end_date");
                cBookList.add(bd);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cBookList;
    }

    
    static ArrayList<JournalDetails> getcheckedOutJournals() {
        ArrayList<JournalDetails> journalList = new ArrayList();
        try {
            ResultSet rs = stmt.executeQuery("select p.p_title,c.r_id, to_char(c.start_date,'yyyy/mm/dd hh24:mi') start_date, to_char(c.end_date,'yyyy/mm/dd hh24:mi') end_date from publication p, journal j,checkout c where p.PUBLICATION_ID=j.ISSN  and c.return_date is null and j.r_id=c.r_id and c.p_id =" + "'" + PatronDetails.pid + "'");
            
            while(rs.next()){
                System.out.println("checked out journals");
                JournalDetails jd = new JournalDetails();
                
                jd.rid = rs.getInt("R_ID");
                jd.jTitle = rs.getString("p_title");
                jd.jCheckoutDate = rs.getString("start_date");
                jd.jDueDate = rs.getString("end_date");
                journalList.add(jd);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return journalList;
    }

    static ArrayList<ConferencePaperDetails> getCheckedOutConfPapers() {
        ArrayList<ConferencePaperDetails> confPaperList = new ArrayList();
        try {
            ResultSet rs = stmt.executeQuery("select p.p_title,c.r_id, to_char(c.start_date,'yyyy/mm/dd hh24:mi') start_date, to_char(c.end_date,'yyyy/mm/dd hh24:mi') end_date from publication p,conf_proceedings b,checkout c where p.PUBLICATION_ID=b.conf_num and c.return_date is null  and b.r_id=c.r_id and c.p_id =" + "'" + PatronDetails.pid + "'");
            
            while(rs.next()){
                ConferencePaperDetails cpd = new ConferencePaperDetails();
                
                cpd.rid = rs.getInt("R_ID");
                cpd.cTitle = rs.getString("p_title");
                cpd.cCheckoutDate = rs.getString("start_date");
                cpd.cDueDate = rs.getString("end_date");
                confPaperList.add(cpd);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return confPaperList;
    }

    static void reserveRoom(String roomNo, String pid, String startDate, String endDate) {
        //JOptionPane.showMessageDialog(null, roomNo);
        try {
            rs = stmt.executeQuery("select r_id from room where room_no =" + "'" + roomNo + "'");
            int rid = 0;
            while(rs.next()){
                rid = rs.getInt("r_id");
                
            }
            
            CallableStatement pstmt = conn.prepareCall("{call checkout_room(?,?,?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, pid);
            pstmt.setString(3, startDate );
            pstmt.setString(4, endDate);
            
            pstmt.executeUpdate();
            
            
            conn.commit();
            JOptionPane.showMessageDialog(null, "Success");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            
        }
        
    }

    static void getFacultyDetails(String pid) {
           
        try {
            rs = stmt.executeQuery("SELECT * from faculty where f_NUMBER =" + "'" + pid + "'");
            while(rs.next())
            {
                FacultyDetails.fNationality = rs.getString("f_nationality");
                FacultyDetails.fCategory = rs.getString("f_category");
            }
            rs.close();
            
            rs = stmt.executeQuery("select * from patron where p_id = " + "'" + pid + "'");
            while(rs.next())
            {
                FacultyDetails.fDept = rs.getString("p_department");
                FacultyDetails.fFname = rs.getString("p_first_name");
                FacultyDetails.fLname = rs.getString("p_last_name");
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void updateStudent() {
        //JOptionPane.showMessageDialog(null, StudentDetails.sEnrollInfo);
        try {
            String query = "update student set s_sex = '" + StudentDetails.sSex + "', S_NATIONALITY = '" + StudentDetails.sNationality + "', S_ALTERNATE_PHONE_NO = " + StudentDetails.sAltPhoneNo
+ ", S_PHONE_NO = " + StudentDetails.sPhoneNo + ",S_ADDRESS = '" + StudentDetails.sAddress + "', S_DOB = to_date('" + StudentDetails.sDob + "', 'yyyy/mm/dd') where S_NUMBER = '" + StudentDetails.sNumber + "'";
            
            //String query1 = "update student_enrollment_info set s_classification = '" + StudentDetails.sClassification + "', s_category_of_student = '"
//+ StudentDetails.sCategory + "', s_degree_program = '" + StudentDetails.sDegProg + "' where s_enrollment_info_id = " + StudentDetails.sEnrollInfo;
            
            String query1 = "select s_enrollment_info_id from student_enrollment_info where s_classification = '" + StudentDetails.sClassification + "' and s_category_of_student = '"
+ StudentDetails.sCategory + "' and s_degree_program = '" + StudentDetails.sDegProg + "'";
            
            String query2 = "update patron set p_first_name = '" + StudentDetails.sFname + "', p_last_name = '" + StudentDetails.sLname + "', p_department = '" + StudentDetails.sDept + "' where p_id = '" + StudentDetails.sNumber + "'"; 
            
            stmt.executeQuery(query);
            stmt.executeQuery(query2);
            conn.commit();
            rs = stmt.executeQuery(query1);
            if(!rs.next()){
                JOptionPane.showMessageDialog(null, "Wrong details entered");
            }
            
            else{
                int enrollId = rs.getInt("s_enrollment_info_id");
                stmt.executeQuery("update student set s_enrollment_id = "+ enrollId + " where s_number = '" + StudentDetails.sNumber + "'");
       conn.commit();
      
                JOptionPane.showMessageDialog(null, "Success");
            }
                
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Enter in correct format");
        }
    }

    static String getResourceAvail(int rid, String checkoutDate, String dueDate) {
        String avail = "";
        try {
            System.out.println("Rid checkout date due date " + rid + " " + checkoutDate + " " + dueDate);
            CallableStatement pstmt = conn.prepareCall("{call resource_avail(?,?,?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, checkoutDate);
            pstmt.setString(3, dueDate );
            pstmt.registerOutParameter(4, Types.VARCHAR);
            
            pstmt.executeUpdate();
            conn.commit();
            avail = pstmt.getString(4);
            //System.out.println("avail is "+ avail);
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
        return avail;
    }

    static void checkoutPublication(int rid, String pid, String checkoutDate, String dueDate) {
         try {
            
            CallableStatement pstmt = conn.prepareCall("{call checkout_publication(?,?,?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, pid);
            pstmt.setString(3, checkoutDate);
            pstmt.setString(4, dueDate );
            
            pstmt.executeUpdate();
            
            //int o_empno = pstmt.getInt(2);
            //String o_ename = pstmt.getString(3);
            //float o_sal = pstmt.getFloat(4);
            conn.commit();
            JOptionPane.showMessageDialog(null, "Success");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    static void addPubToQueue(int rid, String pid) {
        try {
            rs = stmt.executeQuery("insert into queue values(seq_queue.nextval," + rid + ",'" + pid + "')");
            if(rs.next())
            //if(c > 0)
                
            conn.commit();
            JOptionPane.showMessageDialog(null, "You have been added to the queue");
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    static void renewPublication(int rid, String pid, String checkoutDate, String dueDate) {
        try {
            
            CallableStatement pstmt = conn.prepareCall("{call renew_publication(?,?,?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, pid);
            pstmt.setString(3, checkoutDate);
            pstmt.setString(4, dueDate );
            
            pstmt.executeUpdate();
            
          
            conn.commit();
            JOptionPane.showMessageDialog(null, "Success");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    static void checkinRoom(int rid, String pid) {
        try {
            CallableStatement pstmt = conn.prepareCall("{call check_in_room(?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, pid);
           
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    static ArrayList<CameraDetails> getCheckedoutCameras() {
        ArrayList<CameraDetails> cameraList = new ArrayList<>();
        try {
            
            String query = "select c.r_id, c.model, to_char(rch.checkout_date,'YYYY/MM/DD HH24:mi') start_date, to_char(rch.due_date,'YYYY/MM/DD HH24:mi') end_date,rch.P_ID from camera c, camera_checkout rch where c.r_id = rch.r_id and rch.checked_out = '1' and rch.return_date is null and rch.P_ID='"+PatronDetails.getPid()+"'";
            
              
                  rs = stmt.executeQuery(query);
            while(rs.next()){
                System.out.println("In while");
                CameraDetails cd = new CameraDetails();
               
                cd.rid = rs.getInt("r_id");
                cd.model = rs.getString("model");
                cd.checkoutDate = rs.getString("start_date");
                cd.dueDate = rs.getString("end_date");
                cameraList.add(cd);
                return cameraList;
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cameraList;
        
    }

    static void checkinPublication(int rid, String pid) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Calendar cal = Calendar.getInstance();
            System.out.println(dateFormat.format(cal.getTime()));
            //System.out.println("rid is "+rid);
            stmt.executeQuery("update checkout set return_date = sysdate where r_id = "+ rid + "and p_id = '" + pid + "'");
            conn.commit();
            JOptionPane.showMessageDialog(null, "Success");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    static void checkinCamera(int rid, String pid) {
        try {
            String query = "update camera_checkout set return_date = sysdate where r_id=" + rid + "and p_id = '"+ pid + "' and return_date is null";
            
            stmt.executeQuery(query);
            conn.commit();
            JOptionPane.showMessageDialog(null, "checkin successful");
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "checkin failed");
        }
    }

    static void checkoutRoom(int rid, String pid) throws SQLException{
        
    
            CallableStatement pstmt = conn.prepareCall("{call check_out_room(?,?)}");
            pstmt.setInt(1, rid);
            pstmt.setString(2, pid);
       
            pstmt.executeUpdate();
            
            //int o_empno = pstmt.getInt(2);
            //String o_ename = pstmt.getString(3);
            //float o_sal = pstmt.getFloat(4);
       
        
        
    }

    static void checkoutCamera(int rid, String pid) throws SQLException {
    
      
            String query = "update camera_checkout set checked_out='1' where r_id=" + rid + "and p_id = '"+ pid + "'";
            
            stmt.executeQuery(query);
            conn.commit();
            String query1 = "update library_resource set avail='0' where r_id=" + rid;
            
            stmt.executeQuery(query1);
            conn.commit();
            
            
      
        
    
    }

    static void updateFaculty() {
       
           try {
           /* String query = "update student set s_sex = '" + StudentDetails.sSex + "', S_NATIONALITY = '" + StudentDetails.sNationality + "', S_ALTERNATE_PHONE_NO = " + StudentDetails.sAltPhoneNo
+ ", S_PHONE_NO = " + StudentDetails.sPhoneNo + ",S_ADDRESS = '" + StudentDetails.sAddress + "', S_DOB = to_date('" + StudentDetails.sDob + "', 'yyyy/mm/dd') where S_NUMBER = '" + StudentDetails.sNumber + "'";
            
          
            String query1 = "select s_enrollment_info_id from student_enrollment_info where s_classification = '" + StudentDetails.sClassification + "' and s_category_of_student = '"
+ StudentDetails.sCategory + "' and s_degree_program = '" + StudentDetails.sDegProg + "'";
            
            String query2 = "update patron set p_first_name = '" + StudentDetails.sFname + "', p_last_name = '" + StudentDetails.sLname + "', p_department = '" + StudentDetails.sDept + "' where p_id = '" + StudentDetails.sNumber + "'"; 
            
            stmt.executeQuery(query);
            stmt.executeQuery(query2);
            conn.commit();
            rs = stmt.executeQuery(query1);
            if(!rs.next()){
                JOptionPane.showMessageDialog(null, "Wrong details entered");
            }
            
            else{
                int enrollId = rs.getInt("s_enrollment_info_id");
                stmt.executeQuery("update student set s_enrollment_id = "+ enrollId + " where s_number = '" + StudentDetails.sNumber + "'");
       conn.commit();
      */
                JOptionPane.showMessageDialog(null, "Success");
            
                
        
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Enter in correct format");
        }
    
        
    }
    

  
       public String getPublicationDetails()
       {
           if (PatronDetails.pid==null)
               return "0";
           try {
                rs=stmt.executeQuery("select NVL(SUM(AMOUNT),0) AS AMOUNT from v_BILL_FOR_EACH_PATRON where P_ID="+PatronDetails.pid);
                 if(rs.next())
                        return rs.getString("AMOUNT");
                 
                
              } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }   
             return "0";      
       }


       //REMEMBER TO keep commit() from transaction in sqlplus as well whenever u update else it hangs
   
    public static ArrayList getBooks(){
        ArrayList<BookDetails> bookList = new ArrayList();
        try {
           
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            rs = stmt.executeQuery("select * from v_book");
           
            System.out.println("In function get books");
            while(rs.next()){
                BookDetails bd = new BookDetails();
                bd.rid = rs.getInt("r_id");
                //System.out.println(rs.getInt("r_id"));
                bd.isbn = rs.getString("isbn");
                bd.bEdition = rs.getString("b_edition");
                bd.bYear = rs.getString("year_published");
                bd.pType = rs.getString("p_type");
                bd.bTitle = rs.getString("p_title");
                bd.bPublisher = rs.getString("b_publisher");
                
                //Author
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("select author_name from author where publication_id ="+ "'" + bd.isbn + "'");
                
                while(rs1.next())
                {
                    bd.authors.add(rs1.getString("author_name"));
                }
                rs1.close();
                //Availability
                //If availability is no, check checkout table
                System.out.println("Title is "+ bd.bTitle);
                bookList.add(bd);
                
                System.out.println("In while loop");
            }
            
            //print arralyst
            for(int i=0; i< bookList.size(); i++)
            {
                BookDetails book = bookList.get(i);
                //System.out.println(book.bTitle);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return bookList;
    
    }

    public static ArrayList<JournalDetails> getJournals() {
        ArrayList<JournalDetails> journalList = new ArrayList();
        try {
            ResultSet rs = stmt.executeQuery("select * from v_journal");
            
            while(rs.next()){
                JournalDetails jd = new JournalDetails();
                
                jd.rid = rs.getInt("R_ID");
                jd.issn = rs.getString("ISSN");
                jd.jTitle = rs.getString("P_TITLE");
                jd.jYear = rs.getString("YEAR_PUBLISHED");
                jd.pType = rs.getString("P_TYPE");
                
                //Author
                
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("select author_name from author where publication_id ="+ "'" + jd.issn + "'");
                
                while(rs1.next()){
                    jd.authors.add(rs1.getString("author_name"));
                }
                
                //Availability left

                journalList.add(jd);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return journalList;
    }

    public static ArrayList<ConferencePaperDetails> getConferencePapers() {
        ArrayList<ConferencePaperDetails> cList = new ArrayList();
        try {
            ResultSet rs = stmt.executeQuery("select * from v_conf_proceedings");
            
            while(rs.next()){
                System.out.println("In get conf papers");
                ConferencePaperDetails cd = new ConferencePaperDetails();
                
                cd.rid = rs.getInt("R_ID");
                cd.cTitle = rs.getString("p_title");
                cd.confNum = rs.getString("CONF_NUM");
                cd.cName = rs.getString("P_TITLE");
                cd.cYear = rs.getString("YEAR_PUBLISHED");
                cd.cConfName = rs.getString("NAME_OF_CONFERENCE");
                cd.pType = rs.getString("P_TYPE");
                //Author 
                
                Statement stmt1 = conn.createStatement();
                ResultSet rs1 = stmt1.executeQuery("select author_name from author where publication_id ="+ "'" + cd.confNum + "'");
                
                while(rs1.next()){
                    cd.authors.add(rs1.getString("author_name"));
                }
                
                //Availability left

                cList.add(cd);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cList;
    }
       
      
       
            
        
    
     
     
}
