 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.impl;

import java.io.File;
import java.io.IOException;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import one.dedic.jmri.bridges.AppsBaseBridge;
import org.netbeans.api.actions.Closable;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author sdedic
 */
@MIMEResolver.NamespaceRegistration(
        displayName = "#MIME_RosterEntry", 
        elementName = "locomotive-config",
        elementNS = "",
        mimeType = "text/xml+x-locomotive-config")
@NbBundle.Messages("MIME_RosterEntry=Roster Entry")
@DataObject.Registration(
        displayName = "#MIME_RosterEntry",
        mimeType = "text/xml+x-locomotive-config"        
)
public class RosterEntryDataObject extends MultiDataObject implements Openable, Closable{
    private final InstanceContent lookupContent;
    private final Lookup lkp;
    
    private TN      tempNode;
    private transient Roster  roster;
    private transient RosterEntry entry;
    
    public RosterEntryDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo, loader);
        lookupContent = new InstanceContent();
//        lookupContent.add(RosterEntry.class, new InstanceContent.Convertor<Class, RosterEntry>() {
//            @Override
//            public RosterEntry convert(Class t) {
//                return createRosterEntry();
//            }
//
//            @Override
//            public Class<? extends RosterEntry> type(Class t) {
//                return RosterEntry.class;
//            }
//
//            @Override
//            public String id(Class t) {
//                return getPrimaryFile().getPath();
//            }
//
//            @Override
//            public String displayName(Class t) {
//                return Bundle.MIME_RosterEntry();
//            }
//        });
//        
        lkp = new ProxyLookup(new AbstractLookup(lookupContent), super.getLookup());
        
        AppsBaseBridge.getInstance().whenReady(this::connect, false);
    }
    
    private void connect() {
        roster = Lookup.getDefault().lookup(Roster.class);
        assert roster != null;
        TN tn;
        RosterEntry e = createRosterEntry();
        synchronized (this) {
            tn = tempNode;
            entry = e;
        }
        lookupContent.add(e);
        if (tn != null) {
            Node n = RosterEntryNode.create(roster, entry);
            tn.replace(n);
        }
    }

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    protected Node createNodeDelegate() {
        Roster r = Lookup.getDefault().lookup(Roster.class);
        if (r == null || entry == null) {
            AbstractNode dummy = new AbstractNode(Children.LEAF);
            dummy.setDisplayName(getPrimaryFile().getName());
            tempNode = new TN(dummy);
            return tempNode;
        } else {
            // entry initialized
            return RosterEntryNode.create(r, entry);
        }
    }
    
    

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    @Override
    public boolean isDeleteAllowed() {
        return false;
    }

    @Override
    public boolean isCopyAllowed() {
        return false;
    }

    @Override
    public boolean isMoveAllowed() {
        return false;
    }

    @Override
    public boolean isRenameAllowed() {
        return false;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx("jmri.roster.entrydataobject");
    }

    @Override
    protected DataObject handleCopy(DataFolder df) throws IOException {
        return null;
    }

    @Override
    protected void handleDelete() throws IOException {
    }

    @Override
    protected FileObject handleRename(String string) throws IOException {
        return null;
    }

    @Override
    protected FileObject handleMove(DataFolder df) throws IOException {
        return null;
    }

    @Override
    protected DataObject handleCreateFromTemplate(DataFolder df, String string) throws IOException {
        return null;
    }

    private RosterEntry createRosterEntry() {
        synchronized (this) {
            if (entry != null) {
                return entry;
            }
        }
        roster =  Lookup.getDefault().lookup(Roster.class);
        if (roster == null) {
            return null;
        }
        RosterEntry found = null;
        
        for (RosterEntry e : roster.getAllEntries()) {
            String fn = e.getFileName();
            FileObject f = FileUtil.toFileObject(new File(roster.getRosterFilesLocation() + fn));
            if (f == getPrimaryFile()) {
                found = e;
                break;
            }
        }
        if (found == null) {
            return null;
        }
        synchronized (this) {
            if (entry != null) {
                return entry;
            }
            entry = found;
        }
        lookupContent.add(found);
        return found;
    }

    @Override
    public void open() {
        TopComponent tc = new DecoderTopComponent(getPrimaryFile(), entry);
        tc.open();
        tc.requestActive();
    }

    @Override
    public boolean close() {
        return false;
    }
    
    /**
     * Just allow to replace the node with a real one, 
     * after the Roster initializes.
     */
    static class TN extends FilterNode {
        public TN(Node original) {
            super(original, Children.LEAF);
        }
        
        public void replace(Node n) {
            super.changeOriginal(n, false);
        }
    }
}
