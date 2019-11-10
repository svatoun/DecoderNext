/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.ui;

import com.jgoodies.validation.ValidationResult;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author sdedic
 */
public class ComposedPane extends javax.swing.JPanel implements ContainerPanel {
    private final Map<Node, Section> sectionMap = new HashMap<>();
    private final List<Section> sectionOrder = new ArrayList<>();
    private final Map<Section, JComponent>  componentMap = new HashMap<>();
    
    private ValidationResult validation = new ValidationResult();
    private int maxColumns = 2;
    
    private static final String CLIENT_SECTION = "decoderNext.uiSection"; // NOI18N
    
    /**
     * Creates new form ComposedPane
     */
    public ComposedPane() {
        initComponents();
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public void setMaxColumns(int maxColumns) {
        this.maxColumns = maxColumns;
    }

    @Override
    public Section getSection(Node key) {
        return sectionMap.get(key);
    }

    @Override
    public void addSection(Section section) {
        if (section == null) {
            return;
        }
        if (sectionOrder.contains(section)) {
            return;
        }
        Section old = sectionMap.get(section.getNode());
        if (old != null && !old.equals(section)) {
            removeSection(old);
        }
        sectionOrder.add(section);
        sectionMap.put(section.getNode(), section);
    }

    @Override
    public void removeSection(Section section) {
        if (section == null || !sectionOrder.contains(section)) {
            return;
        }
        /*
        JComponent existing = componentMap.remove(section);
        if (existing != null) {
            // FIXME: may need actually to rebuild the layout !
            remove(existing);
        }
        */
        sectionOrder.remove(section);
        sectionMap.remove(section.getNode());
    }

    @Override
    public Node getRoot() {
        return new AbstractNode(Children.LEAF);
    }

    @Override
    public ValidationResult getValidationResult() {
        return validation;
    }

    @Override
    public ValidationResult doValidation() {
        // FIXME: no op
        return validation;
    }
    
    private int x;
    private int y;
    private int startX;
    private int gapY;
    
    /**
     * Actually materializes the UI.
     */
    public void build() {
        x = 1;
        y = 1;
        startX = 1;
        gapY = -1;
        int maxX = -1;
        int maxY = -1;
        for (Section s : sectionOrder) {
            GridBagConstraints cons = new GridBagConstraints();
            cons.anchor = GridBagConstraints.NORTHWEST;
            cons.fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
            Dimension d = s.getSize();
            if (d == null) {
                cons.gridwidth = cons.gridheight = 1;
            } else {
                cons.gridwidth = d.width;
                cons.gridheight = d.height;
            }
            
            cons.gridx = x;
            cons.gridy = y;
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            JComponent child = createChild(s);
            addHorizontalCell(child, cons);
        }
        
        GridBagConstraints consX = new GridBagConstraints();
        consX.gridx = maxX + 1;
        consX.weightx = 1;
        consX.fill = GridBagConstraints.HORIZONTAL;
        add(new JPanel(), consX);

        GridBagConstraints consY = new GridBagConstraints();
        consY.gridy = maxY + 1;
        consY.weighty = 1;
        consY.fill = GridBagConstraints.VERTICAL;
        add(new JPanel(), consY);

        invalidate();
    }
    
    void addHorizontalCell(JComponent c, GridBagConstraints g) {
        x += g.gridwidth;
        if (x > maxColumns) {
            nextGridLine();
            y++;
            x = startX;
        }
        add(c, g);
    }
    
    void nextGridLine() {
        if ((gapY == 0) || (--gapY == 0)) {
            x = startX;
        } else {
            gapY = -1;
            x = 1;
        }
    }
    
    private JComponent createChild(Section s) {
        JComponent c = componentMap.get(s);
        if (c != null) {
            return c;
        }
        c = new SectionLayout(s);
        componentMap.put(s, c);
        return c;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        build();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
