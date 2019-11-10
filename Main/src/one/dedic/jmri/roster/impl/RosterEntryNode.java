/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.impl;

import java.io.File;
import java.nio.file.Paths;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author sdedic
 */
public class RosterEntryNode extends AbstractNode {
    private final Roster            roster;
    private final RosterEntry       entry;
    private final InstanceContent   cookies;
    
    public static Node create(RosterEntry en) {
        return create(Lookup.getDefault().lookup(Roster.class), en);
    }

    public static Node create(Roster r, RosterEntry en) {
        File rosterFile = 
                Paths.get(r.getRosterFilesLocation() + en.getFileName()).toFile();
        FileObject f = FileUtil.toFileObject(rosterFile);
        return new RosterEntryNode(r, en, f, new InstanceContent());
    }
    
    private RosterEntryNode(Roster r, RosterEntry entry, FileObject f, InstanceContent content) {
        super(Children.LEAF, new ProxyLookup(new AbstractLookup(content), f.getLookup()));
        this.roster = r;
        this.entry = entry;
        this.cookies = content;
        
        content.add(entry);
        content.add(this);
        
        setDisplayName(entry.getDisplayName());
    }
}
