/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import java.util.Map;

/**
 *
 * @author sdedic
 */
public interface EntryGroup extends AbstractEntry, EditableModel {
    
    public Map<String, InteractiveEntry>     data();
    public Map<String, EntryGroup>    groups();
}
