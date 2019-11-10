/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.impl;

import java.awt.BorderLayout;
import jmri.jmrit.roster.Roster;
import jmri.profile.ProfileManager;
import one.dedic.jmri.bridges.AppsBaseBridge;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;

/**
 *
 * @author sdedic
 */
@TopComponent.Description(preferredID = RosterContentsTopComponent.PREFERRED_ID,
    //iconBase="SET/PATH/TO/ICON/HERE", 
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = true, position=10000)
@ActionID(category = "Window", id = "one.sdedic.jmri.roster.list.RosterContentsTopComponent")
@ActionReference(path = "Menu/Window")
@TopComponent.OpenActionRegistration(displayName = "#WINDOW_RosterContents",
preferredID = "AnalysisResultTopComponent")
@NbBundle.Messages({
    "WINDOW_RosterContents=Roster"
})
@RetainLocation("listings")
public class RosterContentsTopComponent extends TopComponent {
    static final String PREFERRED_ID = "RosterContentsTopComponent";
    private RosterContentPane contentPanel;
    
    public RosterContentsTopComponent() {
        ProfileManager.getDefault().setActiveProfile("My_JMRI_Railroad.3f4f1e1a");
        setLayout(new BorderLayout());
        setName(Bundle.WINDOW_RosterContents());
    }
    
    private void initialize() {
        contentPanel = new RosterContentPane();
        AppsBaseBridge.getInstance().whenReady(contentPanel::loadJMRI, true);
        add(contentPanel, BorderLayout.CENTER);
        updateRosterGroupName();
    }
    
    private void updateRosterGroupName() {
        
    }

    @Override
    protected void componentShowing() {
        super.componentShowing(); 
        if (contentPanel == null) {
            initialize();
        }
    }
    
    private void printRoster() {
        System.err.println(Roster.getDefault().getAllEntries());
    }
}
