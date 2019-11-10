/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.ui;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 *
 * @author sdedic
 */
public interface Section {
    public Object   getKey();
    
    /**
     * Node that represents the section. The name, tooltip and possibly
     * the icon can represent the section in the container.
     * @return 
     */
    public Node   getNode();
    
    /**
     * Returns the size of the section, in cells. Wide horizontal, or tal
     * vertical Sections are supported by the container.
     * @return section size.
     */
    public Dimension    getSize();
    
    /**
     * Creates the section contents. This method is called just once; the
     * section contents may be created lazily.
     * @return 
     */
    public JPanel createComponent();
    
    /**
     * Returns the validation support, if supports validation.
     */
    public ValidatingUI getValidation();
    
    // chane listener will receive 'activated
    public void addChangeListener(ChangeListener l);
    public void removeChangeListener(ChangeListener l);
}
