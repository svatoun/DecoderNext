/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Validates uniqueness of a roster entry's property. Alternatively makes an 'info' message
 * if a roster entry is unique - assuming there's only a limited number of modeled road names
 * or vehicle manufacturers, adding a new one is worth extra checking if the user did not
 * make a typo.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - property value",
    "ValidationError_ValueIsUsed=Value must be unique, it is already used.",
    "# {0} - property label",
    "# {1} - property value",
    "ValidationError_ValueIsUsed2={0} must be unique, \"{1}\" is already used.",
    "# {0} - property label",
    "ValidationError_WarnShouldBeUnique={0} should be unique.",
    "# {0} - property label",
    "# {1} - property value",
    "ValidationError_WarnShouldBeUnique2={0} should be unique.",
    "# {0} - property label",
    "# {1} - property value",
    "ValidationError_InfoNewValue2=A new {0} will be introduced. Check spelling.",
    "# {0} - property label",
    "ValidationError_InfoNewValue=A new entry \"{0}\" will be introduced. Check spelling."
})
public class RosterPropertyValidator implements ValidatorSetup, ContextValidator {
    private Object messageKey;
    private String label;
    private final Function<RosterEntry, String> extractor;
    private RosterEntry entry;
    private Roster roster;
    private boolean unique;
    private boolean warnDuplicate;
    private boolean ignoreCase;
    
    public RosterPropertyValidator(Function<RosterEntry, String> extractor) {
        this(extractor, null, null);
    }
    
    public RosterPropertyValidator(Function<RosterEntry, String> extractor, Object key, String propertyLabel) {
        this.messageKey = key;
        this.label = propertyLabel;
        this.extractor = extractor;
    }

    public Object getMessageKey() {
        return messageKey;
    }

    @Override
    public void setMessageKey(Object messageKey) {
        this.messageKey = messageKey;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String propertyLabel) {
        this.label = propertyLabel;
    }
    
    public RosterPropertyValidator ignoreCase() {
        this.ignoreCase = true;
        return this;
    }
    
    public RosterPropertyValidator requireUnique() {
        unique = true;
        return this;
    }
    
    public RosterPropertyValidator warnDuplicate() {
        warnDuplicate = true;
        return this;
    }

    @Override
    public ValidationResult validate(Object validationTarget) {
        if (roster == null || entry == null) {
            return null;
        }
        Object o;
        if (validationTarget instanceof Supplier) {
            o = ((Supplier)validationTarget).get();
        } else {
            o = validationTarget;
        }
        if (o == null) {
            return null;
        }
        String s = o.toString().trim();
        boolean found = false;
        for (RosterEntry en : roster.getAllEntries()) {
            String n = extractor.apply(en);
            if (n == null) {
                continue;
            } else {
                n = n.trim();
            }
            if ("".equals(n)) {
                continue;
            }
            boolean match = ignoreCase ? s.equalsIgnoreCase(n) : s.equals(n);
            if (match) {
                found = true;
                if (en != entry && !Objects.equals(entry.getId(), en.getId())) {
                    if (unique) {
                        return new ValidationResult().
                                add(warnDuplicate ?
                                        new SimpleValidationMessage(
                                            label == null ? 
                                                Bundle.ValidationError_WarnShouldBeUnique(s) : 
                                                Bundle.ValidationError_WarnShouldBeUnique2(label, s), 
                                            Severity.WARNING, messageKey) :
                                        new SimpleValidationMessage(
                                            label == null ? 
                                                Bundle.ValidationError_ValueIsUsed(s): 
                                                Bundle.ValidationError_ValueIsUsed2(label, s), 
                                            Severity.ERROR, messageKey)
                                );
                    } else {
                        return null;
                    }
                }
            }
        }
        if (unique || found) {
            return null;
        }
        if (s.isEmpty()) {
            return null;
        }
        return new ValidationResult().
                addInfo(
                    label == null ?
                        Bundle.ValidationError_InfoNewValue(s) :
                        Bundle.ValidationError_InfoNewValue2(label, s), 
                    messageKey);
    }

    @Override
    public void attach(Lookup context) {
        roster = context.lookup(Roster.class);
        entry = context.lookup(RosterEntry.class);
    }
}
