/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.concurrent.CompletableFuture;
import one.dedic.jmri.decodernext.model.formx.validation.ValidationListener;

/**
 * Collects individual validators and informs on entry validation status changes.
 * 
 * @author sdedic
 */
public interface ValidationService {
    /**
     * Attaches a validation state listener.    
     * @param l 
     */
    public void addValidationListener(String entryId, ValidationListener l);
    
    /**
     * Removes a validation state listener
     * @param l 
     */
    public void removeValidationListener(String entryId, ValidationListener l);

    /**
     * Validates an entry, or all entries. `entryId` specifies the entry that is
     * going to be validated. If such entry does not exist, returns {@code null}.
     * If {@code null} is passed as `entryId', all entries are validated. 
     * <p>
     * If none of the entries is dirty and the `force` parameter is false, the call
     * completes immediately with the last-known validation result.
     * 
     * @param entryId entry to validate or {@code null} to validate all.
     * @param force force validation even when the status is clean.
     * @return Future that completes with the validation result.
     */
    public CompletableFuture<ValidationResult>  validate(String entryId, boolean force);
}
