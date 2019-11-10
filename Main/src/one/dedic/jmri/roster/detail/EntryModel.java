/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.detail;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;

/**
 *
 * @author sdedic
 */
public class EntryModel {
    private final Roster        roster;
    private final RosterEntry   entry;
    private CvTableModel        cvModel;
    private VariableTableModel  varModel;

    public EntryModel(Roster r, RosterEntry entry) {
        this.roster = r;
        this.entry = entry;
    }

    public Roster getRoster() {
        return roster;
    }

    public RosterEntry getEntry() {
        return entry;
    }
    
    public CvTableModel getCvModel() {
        return cvModel;
    }

    public void setCvModel(CvTableModel cvModel) {
        this.cvModel = cvModel;
    }

    public VariableTableModel getVarModel() {
        return varModel;
    }

    public void setVarModel(VariableTableModel varModel) {
        this.varModel = varModel;
    }
}
