/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.Paths;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import one.dedic.jmri.roster.detail.EntryModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author sdedic
 */
public class DecoderTopComponent extends CloneableTopComponent implements LookupListener {
    private final InstanceContent lookupContent;
    
    /**
     * The roster file.
     */
    private FileObject  rosterFile;
    
    /**
     * The represented roster Entry. Will be initialized only after
     * Roster comes on line.
     */
    private RosterEntry rosterEntry;
    
    private EntryModel  model;

    /**
     * Constructor for deserialization only
     */
    public DecoderTopComponent() {
        this.lookupContent = deserializedContent.get();
        associateLookup(new AbstractLookup(deserializedContent.get()));
        deserializedContent.remove();
        setLayout(new BorderLayout());
        add(createInterimContent(), BorderLayout.CENTER);
    }
    
    static ThreadLocal<InstanceContent> deserializedContent = new ThreadLocal<InstanceContent>() {
        @Override
        protected InstanceContent initialValue() {
            return new InstanceContent();
        }
    };
    
    public DecoderTopComponent(FileObject rosterFile, RosterEntry entry) {
        this();
        this.rosterEntry = entry;
        this.rosterFile = rosterFile;
        rosterEntryConnected();
    }
    
    @NbBundle.Messages({
        "MSG_RosterEntryNotConnected=Please white while JMRI Roster is initializing..."
    })
    JPanel createInterimContent() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel(Bundle.MSG_RosterEntryNotConnected()), BorderLayout.CENTER);
        return p;
    }
    
    private Lookup.Result<RosterEntry> waitResult;
    
    private void maybeConnect() {
        synchronized (this) {
            if (rosterEntry != null) {
                waitResult = null;
                return;
            }
            RosterEntry en = rosterFile.getLookup().lookup(RosterEntry.class);
            if (en == rosterEntry) {
                if (en == null && waitResult == null) {
                    waitResult = rosterFile.getLookup().lookupResult(RosterEntry.class);
                    waitResult.addLookupListener(WeakListeners.create(LookupListener.class, this, waitResult));
                }
                return;
            }
            this.rosterEntry = en;
            waitResult = null;
        }
        SwingUtilities.invokeLater(this::rosterEntryConnected);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        if (ev.getSource() == waitResult) {
            maybeConnect();
        }
    }

    private void rosterEntryConnected() {
        setName(rosterEntry.getId());
        Roster r = Lookup.getDefault().lookup(Roster.class);
        model = new EntryModel(r, rosterEntry);
        
        for (Component c : getComponents()) {
            remove(c);
        }
        
        DecoderTopLayout topLayout = new DecoderTopLayout(model);
        add(topLayout, BorderLayout.CENTER);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in); 
        
        String filePath = in.readUTF();
        File f = Paths.get(filePath).toFile();
        FileObject fob = FileUtil.toFileObject(f);
        if (fob != null) {
            rosterFile = fob;
        } else {
            throw new FileNotFoundException(filePath);
        }
        maybeConnect();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        // write the file's path as the identification.
        out.writeUTF(rosterFile.getPath());
    }
}
