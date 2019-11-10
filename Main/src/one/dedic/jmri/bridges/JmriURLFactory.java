/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import jmri.util.FileUtil;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.Utilities;

/**
 * Registers JMRI protocol to access profile, code, scripts etc.
 * @author sdedic
 */
@URLStreamHandlerRegistration(protocol = "jmri")
public class JmriURLFactory extends URLStreamHandler{

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        String pref = u.getAuthority();
        File f = FileUtil.getFile(pref + ":" + u.getPath());
        return Utilities.toURI(f).toURL().openConnection();
    }
}
