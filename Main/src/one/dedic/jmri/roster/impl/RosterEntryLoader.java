/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.roster.impl;

import java.io.IOException;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Loads roster entry objects, from the roster folder only.
 * @author sdedic
 */
/*
@MIMEResolver.NamespaceRegistration(
        mimeType = "text/xml+locomotive-config",
        displayName = "#MIME_RosterEntryFile",
        elementName = "locomotive-config",
        elementNS = ""
)
@NbBundle.Messages(
    "MIME_RosterEntryFile=JMRI Roster Entry"
)
*/
public class RosterEntryLoader extends UniFileLoader {

    public RosterEntryLoader(String string) {
        super(RosterEntryDataObject.class.getName());
        
        ExtensionList ext = new ExtensionList();
        ext.addMimeType("text/xml+locomotive-config");
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject fo) throws DataObjectExistsException, IOException {
        return new RosterEntryDataObject(fo, this);
    }
}
