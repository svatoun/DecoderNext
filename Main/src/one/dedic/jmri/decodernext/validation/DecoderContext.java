/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

import java.util.List;
import javax.annotation.Nonnull;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableValue;

/**
 * Uniform validation context for items on decoder screens. The context
 * should provide access to all values in the model so they can be validated
 * together.
 * @author sdedic
 */
public interface DecoderContext {
    /**
     * Determines the item being validated. Its description or tooltip may
     * be used to compose the message.
     * @return variable or {@code null} if now known
    @CheckForNull
    public VariableValue        validatedItem();
     */
    
    /**
     * Roster entry being edited.
     * @return entry instance
     */
    @Nonnull
    public RosterEntry          getRosterEntry();
    
    /**
     * Finds value for a named variable.
     * @param itemI item name
     * @return variable instance
     */
    public VariableValue        findItemVariable(String itemId);
    
    /**
     * Finds variable(s) that map to a specific CV
     * @param cvId cv ID.
     * @return variable representations.
     */
    public List<VariableValue>  findCvVariables(String cvId);
    
    /**
     * Finds the raw CV value 
     * @param cvId cv ID
     * @return CV value representatiaon
     */
    public CvValue              findCV(String cvId);
}
