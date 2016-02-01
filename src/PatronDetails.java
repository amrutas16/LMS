/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author amol
 */
public class PatronDetails {
    
    static String pid = "S1";
    static String type = "faculty"; //student or faculty

    public static String getPid() {
        return pid;
    }

    public static String getType() {
        return type;
    }

    public static void setPid(String pid) {
        PatronDetails.pid = pid;
    }

    public static void setType(String type) {
        PatronDetails.type = type;
    }
 
    
    
}
