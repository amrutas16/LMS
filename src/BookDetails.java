
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
public class BookDetails {
    public int rid;
    public String isbn;
    public String bTitle;
    public String bEdition;
    public String bYear;
    public String bPublisher;
    public String pType;
    public String bAvail;
    public String bCheckoutDate;
    public String bReturnDate;
    public String bDueDate;
    public ArrayList<String> authors;

    public BookDetails(){
        authors = new ArrayList<String>();
    }
    
}


