/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import jmri.util.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = URLMapper.class)
public class JmriURLmapper extends URLMapper {

    ThreadLocal<Boolean> inURLMapper = new ThreadLocal<>();
    
    @Override
    public URL getURL(FileObject fo, int type) {
        if (Boolean.TRUE.equals(inURLMapper.get())) {
            return null;
        }
        try {
            inURLMapper.set(true);
            File f = org.openide.filesystems.FileUtil.toFile(fo);
            if (f == null) {
                return null;
            }
            String portable = FileUtil.getPortableFilename(f);
            URI u;
            try {
                u = new URI(portable);
                if (!u.isAbsolute()) {
                    return null;
                }
                return new URL("jmri:" + portable);
            } catch (URISyntaxException | MalformedURLException ex) {
                return null;
            }
        } finally {
            inURLMapper.remove();
        }
    }

    @Override
    public FileObject[] getFileObjects(URL url) {
        String proto = url.getProtocol();
        if (!"jmri".equals(proto)) {
            return null;
        }
        String pref = url.getAuthority();
        try {
            File f = FileUtil.getFile(pref + ":" + url.getPath());
            FileObject fo = org.openide.filesystems.FileUtil.toFileObject(f);
            if (fo == null) {
                return null;
            } else {
                return new FileObject[] { fo };
            }
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    
}
