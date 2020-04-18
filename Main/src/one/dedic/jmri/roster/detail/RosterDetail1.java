/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.detail;

import one.dedic.jmri.decodernext.ui.ExplicitLayoutFocusPolicy;
import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 *
 * @author sdedic
 */
public class RosterDetail1 extends javax.swing.JPanel {
    private final EntryModel model;
    private final RosterEntry entry;
    private Roster roster;
    
    private ComboBoxCompleter roadNameCompleter;
    private ComboBoxCompleter roadNumberCompleter;
    private ComboBoxCompleter mfgCompleter;
    private ComboBoxCompleter modelTypeCompleter;
    private ComboBoxCompleter ownerCompleter;
    
    private List<Component>   focusOrder = new ArrayList<>();
    
    /**
     * Creates new form RosterDetail
     */
    public RosterDetail1(EntryModel model) {
        this.model = model;
        this.entry = model.getEntry();
        initComponents();
        
        focusOrder.addAll(
            Arrays.asList(id, roadName, roadNumber, 
                manufacturer, type, 
                description, 
                owner, 
                decoderFamily, decoderModel,
                notes
            )
        );
        roadNameCompleter = new ComboBoxCompleter(roadName);
        roadNumberCompleter = new ComboBoxCompleter(roadNumber);
        mfgCompleter = new ComboBoxCompleter(manufacturer);
        modelTypeCompleter = new ComboBoxCompleter(type);
        ownerCompleter = new ComboBoxCompleter(owner);
        
        FocusTraversalPolicy p = getFocusTraversalPolicy();
        
        setFocusTraversalPolicyProvider(true);
        setFocusTraversalPolicy(new ExplicitLayoutFocusPolicy(this, focusOrder));
    }
    
    public void setRoster(Roster roster) {
        this.roster = roster;
    }
    
    public void load() throws IOException {
        id.setText(entry.getId());
        description.setText(entry.getComment());
        decoderFamily.setText(entry.getDecoderFamily());
        decoderModel.setText(entry.getDecoderModel());
        notes.setText(entry.getDecoderComment());

        initUsedValues(roadName, RosterEntry::getRoadName);
        initUsedValues(roadNumber, RosterEntry::getRoadNumber);
        initUsedValues(manufacturer, RosterEntry::getMfg);
        initUsedValues(type, RosterEntry::getModel);
        initUsedValues(owner, RosterEntry::getOwner);
    }
    
    private void initUsedValues(JComboBox box, Function<RosterEntry,String> itemFactory) {
        String initValue = itemFactory.apply(entry);
        ComboBoxModel model = createModel(itemFactory);
        model.setSelectedItem(initValue);
        box.setModel(model);
        box.getEditor().setItem(initValue);
    }
    
    private ComboBoxModel<String> createModel(Function<RosterEntry,String> itemFactory) {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        Set<String> unique = new HashSet<>();
        for (RosterEntry e : roster.getAllEntries()) {
            String item = itemFactory.apply(e);
            if (item != null && !item.isEmpty()) {
                unique.add(item);
            }
        }
        List<String> sorted = new ArrayList<>(unique);
        Collections.sort(sorted);
        for (String s : sorted) {
            m.addElement(s);
        }
        return m;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lModelCaption = new javax.swing.JLabel();
        lID = new javax.swing.JLabel();
        lRoadName = new javax.swing.JLabel();
        lRoadNumber = new javax.swing.JLabel();
        lDescription = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        roadName = new javax.swing.JComboBox<>();
        roadNumber = new javax.swing.JComboBox<>();
        stID = new javax.swing.JLabel();
        stRoadName = new javax.swing.JLabel();
        stRoadNumber = new javax.swing.JLabel();
        lManufacturer = new javax.swing.JLabel();
        lType = new javax.swing.JLabel();
        manufacturer = new javax.swing.JComboBox<>();
        type = new javax.swing.JComboBox<>();
        stManufacturer = new javax.swing.JLabel();
        stModelType = new javax.swing.JLabel();
        descriptionScroll = new javax.swing.JScrollPane();
        description = new javax.swing.JTextArea();
        lOwner = new javax.swing.JLabel();
        owner = new javax.swing.JComboBox<>();
        stOwner = new javax.swing.JLabel();
        lDecoderCaption = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        lFamily = new javax.swing.JLabel();
        decoderFamily = new javax.swing.JTextField();
        lModel = new javax.swing.JLabel();
        decoderModel = new javax.swing.JTextField();
        lNotes = new javax.swing.JLabel();
        notesScroll = new javax.swing.JScrollPane();
        notes = new javax.swing.JTextArea();

        lModelCaption.setFont(lModelCaption.getFont().deriveFont((lModelCaption.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(lModelCaption, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lModelCaption.text_3")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lID, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lID.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lRoadName, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lRoadName.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lRoadNumber, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lRoadNumber.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lDescription, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lDescription.text_3_1")); // NOI18N

        id.setText(org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.id.text_3_1")); // NOI18N

        roadName.setEditable(true);

        roadNumber.setEditable(true);

        stID.setLabelFor(id);
        org.openide.awt.Mnemonics.setLocalizedText(stID, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stID.text_3_1")); // NOI18N

        stRoadName.setLabelFor(id);
        org.openide.awt.Mnemonics.setLocalizedText(stRoadName, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stRoadName.text")); // NOI18N

        stRoadNumber.setLabelFor(roadNumber);
        org.openide.awt.Mnemonics.setLocalizedText(stRoadNumber, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stRoadNumber.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lManufacturer, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lManufacturer.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lType, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lType.text_3_1")); // NOI18N

        manufacturer.setEditable(true);

        type.setEditable(true);

        stManufacturer.setLabelFor(roadNumber);
        org.openide.awt.Mnemonics.setLocalizedText(stManufacturer, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stManufacturer.text")); // NOI18N

        stModelType.setLabelFor(roadNumber);
        org.openide.awt.Mnemonics.setLocalizedText(stModelType, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stModelType.text")); // NOI18N

        description.setColumns(20);
        description.setLineWrap(true);
        description.setRows(5);
        descriptionScroll.setViewportView(description);

        org.openide.awt.Mnemonics.setLocalizedText(lOwner, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lOwner.text_3_1")); // NOI18N

        owner.setEditable(true);

        stOwner.setLabelFor(owner);
        org.openide.awt.Mnemonics.setLocalizedText(stOwner, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.stOwner.text_2_1")); // NOI18N

        lDecoderCaption.setFont(lDecoderCaption.getFont().deriveFont((lDecoderCaption.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(lDecoderCaption, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lDecoderCaption.text_3")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lFamily, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lFamily.text_3_1")); // NOI18N

        decoderFamily.setEditable(false);
        decoderFamily.setBackground(getBackground());
        decoderFamily.setText(org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.decoderFamily.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lModel, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lModel.text_3_1")); // NOI18N

        decoderModel.setEditable(false);
        decoderModel.setBackground(getBackground());
        decoderModel.setText(org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.decoderModel.text_3_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lNotes, org.openide.util.NbBundle.getMessage(RosterDetail1.class, "RosterDetail1.lNotes.text_3_1")); // NOI18N

        notes.setColumns(20);
        notes.setRows(5);
        notesScroll.setViewportView(notes);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lNotes)
                                    .addComponent(lFamily)
                                    .addComponent(lOwner)
                                    .addComponent(lDescription)
                                    .addComponent(lRoadName)
                                    .addComponent(lRoadNumber)
                                    .addComponent(lID))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(owner, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(roadNumber, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(roadName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(id, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                                            .addComponent(decoderFamily))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(stOwner)
                                            .addComponent(stRoadNumber)
                                            .addComponent(stRoadName)
                                            .addComponent(stID))
                                        .addGap(50, 50, 50)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lModel)
                                            .addComponent(lType)
                                            .addComponent(lManufacturer))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(decoderModel)
                                            .addComponent(type, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(manufacturer, 0, 200, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(stManufacturer)
                                            .addComponent(stModelType)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(notesScroll, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(descriptionScroll))
                                        .addGap(24, 24, 24))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lDecoderCaption)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 670, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lModelCaption)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {decoderFamily, id, owner, roadName, roadNumber});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {manufacturer, type});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(lModelCaption)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lID)
                    .addComponent(id, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stID))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roadName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lRoadName)
                    .addComponent(stRoadName)
                    .addComponent(lManufacturer)
                    .addComponent(manufacturer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stManufacturer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(roadNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lRoadNumber)
                    .addComponent(stRoadNumber)
                    .addComponent(lType)
                    .addComponent(type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stModelType))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lDescription)
                    .addComponent(descriptionScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(owner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lOwner)
                    .addComponent(stOwner))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lDecoderCaption)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lFamily)
                    .addComponent(decoderFamily, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decoderModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lModel))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesScroll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lNotes))
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField decoderFamily;
    private javax.swing.JTextField decoderModel;
    private javax.swing.JTextArea description;
    private javax.swing.JScrollPane descriptionScroll;
    private javax.swing.JTextField id;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lDecoderCaption;
    private javax.swing.JLabel lDescription;
    private javax.swing.JLabel lFamily;
    private javax.swing.JLabel lID;
    private javax.swing.JLabel lManufacturer;
    private javax.swing.JLabel lModel;
    private javax.swing.JLabel lModelCaption;
    private javax.swing.JLabel lNotes;
    private javax.swing.JLabel lOwner;
    private javax.swing.JLabel lRoadName;
    private javax.swing.JLabel lRoadNumber;
    private javax.swing.JLabel lType;
    private javax.swing.JComboBox<String> manufacturer;
    private javax.swing.JTextArea notes;
    private javax.swing.JScrollPane notesScroll;
    private javax.swing.JComboBox<String> owner;
    private javax.swing.JComboBox<String> roadName;
    private javax.swing.JComboBox<String> roadNumber;
    private javax.swing.JLabel stID;
    private javax.swing.JLabel stManufacturer;
    private javax.swing.JLabel stModelType;
    private javax.swing.JLabel stOwner;
    private javax.swing.JLabel stRoadName;
    private javax.swing.JLabel stRoadNumber;
    private javax.swing.JComboBox<String> type;
    // End of variables declaration//GEN-END:variables
}
