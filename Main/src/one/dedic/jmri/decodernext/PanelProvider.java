/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext;

import javax.swing.JPanel;

/**
 *
 * @author sdedic
 */
public interface PanelProvider {

    /**
     * @return id of the pane, locale-independent.
     */
    public String getId();

    /**
     * @return display name, localizd
     */
    public String getDisplayName();

    /**
     * @return will create the pane instance.
     */
    public JPanel createPanel();
    
}
