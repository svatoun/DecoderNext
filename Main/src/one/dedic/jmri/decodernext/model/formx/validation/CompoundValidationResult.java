/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Compound result, that consists of results for multiple IDs.
 * @author sdedic
 */
public final class CompoundValidationResult {
    private Map<String, ValidationResult> itemResults = new HashMap<>();
    private ValidationResult combined;
    
    private CompoundValidationResult(Map<String, ValidationResult> initial) {
        this.itemResults = initial;
    }
    
    private boolean doSetResults(Map<String, ValidationResult> newResults) {
        boolean ch = itemResults.keySet().removeAll(newResults.keySet());
        itemResults.putAll(newResults);
        return ch;
    }
    
    public ValidationResult getCombinedResult() {
        if (combined == null) {
            ValidationResult c = new ValidationResult();
            itemResults.values().forEach(x -> { c.addAllFrom(x); });
            combined = c;
        }
        return combined;
    }
    
    public CompoundValidationResult subresult(Collection<String> ids) {
        Map<String, ValidationResult> r = new HashMap<>();
        for (String id : ids) {
            ValidationResult vr = itemResults.get(id);
            if (vr != null) {
                r.put(id, vr);
            }
        }
        return new CompoundValidationResult(r);
    }
    
    public static Builder from(CompoundValidationResult previous) {
        return new Builder(previous);
    }
    
    public static class Builder {
        private final CompoundValidationResult instance;

        public Builder() {
            instance = new CompoundValidationResult(new HashMap<>());
        }
        
        public Builder(CompoundValidationResult r) {
            instance = new CompoundValidationResult(new HashMap<>(r.itemResults));
        }
        
        public boolean setResults(Map<String, ValidationResult> newResults) {
            return instance.doSetResults(newResults);
        }
        
        public CompoundValidationResult build() {
            return instance;
        }
    }
}
