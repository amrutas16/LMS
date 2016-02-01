
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author amol
 */
public class ConferencePaperDetails {
    public int rid;
    public String confNum;
    public String cTitle;
    public String cYear;
    public String cConfName;
    public String pType;
    public String cAvail;
    public String cName;
    public ArrayList<String> authors;
    
    public String cCheckoutDate;
    public String cReturnDate;
    public String cDueDate;

    public ConferencePaperDetails(){
        authors = new ArrayList<String>();
    }
}
