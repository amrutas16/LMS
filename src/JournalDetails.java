
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
public class JournalDetails {
    public int rid;
    public String issn;
    public String jTitle;
    public String jYear;
    public String jPublisher;
    public String pType;
    public String jAvail;
    public String jCheckoutDate, jReturnDate, jDueDate;
    public ArrayList<String> authors;

    public JournalDetails(){
        authors = new ArrayList<String>();
    }
}
