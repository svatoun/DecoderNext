/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.input;

import java.util.Date;
import java.util.concurrent.Executor;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public interface InputContext extends Lookup.Provider {
    public Executor getValidationExecutor();
    
    public Object getSubject();
    public <T> T  findObject(String id);
    
    public void addObject(String id, Object subject, String... alternativeIds);
    public void addObjectAlias(String id, String... aliases);
    
    public void addInputContextListener(InputContextListener l);
    public void removeInputContextListener(InputContextListener l);
    
    public enum Kind {
        /**
         * Currently edited values, possibly transient.
         */
        EDITED,
        
        /**
         * Original values.
         */
        ORIGINAL,
        
        /**
         * Values programmed into devices.
         */
        PROGRAMMED,
        
        /**
         * Values saved in configuration or file.
         */
        SAVED,
        
        /**
         * Default or factory default values.
         */
        DEFAULT,
        
        /**
         * A specific version / point in time.
         */
        VERSIONED
    }
    
    public final class Version {
        String  id;
        Date    date;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }
    
    public interface Selector {
        public boolean        accepts(Kind kind);
        public InputContext   findVersion(Kind kind, Version v);
    }
}
