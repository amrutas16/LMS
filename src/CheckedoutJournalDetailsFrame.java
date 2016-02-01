
import java.util.ArrayList;
import javax.swing.DefaultListModel;
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
public class CheckedoutJournalDetailsFrame extends javax.swing.JFrame {

    /**
     * Creates new form CheckedoutJournalDetailsFrame
     */
    int rid;
    public String title;
    ArrayList<JournalDetails> journalList;
    String prevDueDate;
    JournalDetails selectedJournal;
    
    public CheckedoutJournalDetailsFrame() {
        initComponents();
        populateJournalList();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        journalRenewButton = new javax.swing.JButton();
        checkoutDateLabel = new javax.swing.JLabel();
        dueDateLabel = new javax.swing.JLabel();
        checkoutDateTxtField = new javax.swing.JTextField();
        dueDateTxtField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jList1MouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        journalRenewButton.setFont(new java.awt.Font("Bookman Old Style", 0, 14)); // NOI18N
        journalRenewButton.setText("Renew");
        journalRenewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                journalRenewButtonActionPerformed(evt);
            }
        });

        checkoutDateLabel.setFont(new java.awt.Font("Bookman Old Style", 0, 14)); // NOI18N
        checkoutDateLabel.setText("Checkout date");

        dueDateLabel.setFont(new java.awt.Font("Bookman Old Style", 0, 14)); // NOI18N
        dueDateLabel.setText("Due date");

        checkoutDateTxtField.setEditable(false);
        checkoutDateTxtField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkoutDateTxtFieldActionPerformed(evt);
            }
        });

        dueDateTxtField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dueDateTxtFieldActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton1.setText("Checkin");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(checkoutDateLabel)
                                .addGap(35, 35, 35))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dueDateLabel)
                                .addGap(73, 73, 73)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(checkoutDateTxtField, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                            .addComponent(dueDateTxtField)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(104, 104, 104)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(journalRenewButton))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 12, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkoutDateLabel)
                            .addComponent(checkoutDateTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dueDateLabel)
                            .addComponent(dueDateTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(27, 27, 27)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(journalRenewButton)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(311, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(329, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jList1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseReleased
        title = (String) jList1.getSelectedValue();
        
        for(JournalDetails journal : journalList){
            if(journal.jTitle == title){
                selectedJournal = journal;
                rid = selectedJournal.rid;
                checkoutDateTxtField.setText(journal.jCheckoutDate);
                dueDateTxtField.setText(journal.jDueDate);
                prevDueDate = journal.jDueDate;
                break;
            }
        }
        
        
        
    }//GEN-LAST:event_jList1MouseReleased

    private void checkoutDateTxtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkoutDateTxtFieldActionPerformed

    }//GEN-LAST:event_checkoutDateTxtFieldActionPerformed

    private void dueDateTxtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dueDateTxtFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dueDateTxtFieldActionPerformed

    private void journalRenewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_journalRenewButtonActionPerformed
        DatabaseHelper.renewPublication(rid,PatronDetails.pid, prevDueDate, dueDateTxtField.getText());
        populateJournalList();
    }//GEN-LAST:event_journalRenewButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        //System.out.println("rid is " + rid);
        DatabaseHelper.checkinPublication(rid,PatronDetails.pid);
        populateJournalList();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CheckedoutJournalDetailsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CheckedoutJournalDetailsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CheckedoutJournalDetailsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CheckedoutJournalDetailsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CheckedoutJournalDetailsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel checkoutDateLabel;
    protected javax.swing.JTextField checkoutDateTxtField;
    protected javax.swing.JLabel dueDateLabel;
    protected javax.swing.JTextField dueDateTxtField;
    protected javax.swing.JButton jButton1;
    protected javax.swing.JList jList1;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JButton journalRenewButton;
    // End of variables declaration//GEN-END:variables

    private void populateJournalList() {
        DefaultListModel dlm = new DefaultListModel();
        journalList =  DatabaseHelper.getcheckedOutJournals();
        for(int i=0; i< journalList.size(); i++)
        {
            dlm.addElement(journalList.get(i).jTitle);
            //JournalDetails journal = journalList.get(i);
            
           
        }
        jList1.setModel(dlm);
        System.out.println("in func populate journal list");
    }

}
