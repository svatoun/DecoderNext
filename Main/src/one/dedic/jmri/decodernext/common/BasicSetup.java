/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.common;

/**
 *
 * @author sdedic
 */
public class BasicSetup extends javax.swing.JPanel {

    /**
     * Creates new form BasicSetup
     */
    public BasicSetup() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        address = new javax.swing.JSpinner();
        lAddress = new javax.swing.JLabel();
        longAddressAllowed = new javax.swing.JCheckBox();
        speedSteps = new javax.swing.JComboBox<>();
        lSpeedSteps = new javax.swing.JLabel();
        reverseDirection = new javax.swing.JCheckBox();
        analogOperation = new javax.swing.JCheckBox();
        vAddress = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 223));

        lAddress.setLabelFor(address);
        org.openide.awt.Mnemonics.setLocalizedText(lAddress, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.lAddress.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(longAddressAllowed, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.longAddressAllowed.text")); // NOI18N

        lSpeedSteps.setLabelFor(speedSteps);
        org.openide.awt.Mnemonics.setLocalizedText(lSpeedSteps, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.lSpeedSteps.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(reverseDirection, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.reverseDirection.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(analogOperation, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.analogOperation.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(vAddress, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.vAddress.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(BasicSetup.class, "BasicSetup.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lSpeedSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(longAddressAllowed)
                    .addComponent(reverseDirection)
                    .addComponent(analogOperation)
                    .addComponent(speedSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(107, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lAddress)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vAddress)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(longAddressAllowed)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reverseDirection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(analogOperation)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(speedSteps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lSpeedSteps))
                .addContainerGap(39, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner address;
    private javax.swing.JCheckBox analogOperation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lAddress;
    private javax.swing.JLabel lSpeedSteps;
    private javax.swing.JCheckBox longAddressAllowed;
    private javax.swing.JCheckBox reverseDirection;
    private javax.swing.JComboBox<String> speedSteps;
    private javax.swing.JLabel vAddress;
    // End of variables declaration//GEN-END:variables
}