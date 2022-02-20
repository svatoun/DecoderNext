/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.input;

import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public interface InputContext<T> extends Lookup.Provider {
    public T     getMasterBean();
    public <T> T findNamedBean(String id);
    
    public enum Kind {
        EDITED,
        ORIGINAL,
        PROGRAMMED,
        SAVED,
        VERSIONED
    }
    
    public interface History {
        public boolean              accepts(Kind kind);
        public <T> InputContext<T>  findVersion(Kind kind);
    }
}
