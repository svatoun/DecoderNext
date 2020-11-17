package one.dedic.jmri.roster.detail2;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import javax.swing.text.JTextComponent;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import one.dedic.jmri.decodernext.validation.ContextValidator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Validates roster entry name. The name must not be empty, and must be unique among
 * other roster entries.
 * 
 * @author sdedic
 */
public class RosterNameEditValidator implements Validator, ContextValidator {
    public static final String KEY_ROSTER_ID = "rosterId"; // NOI18N
    public static final String KEY_ADDRESS = "address"; // NOI18N
    
    public enum Kind {
        ID,
        NAME,
        NUMBER,
        ADDRESS
    };
    private Lookup context = Lookup.EMPTY;
    
    private final Kind kind;
    
    public RosterNameEditValidator(Kind kind) {
        this.kind = kind;
    }
    
    @Override
    public ValidationResult validate(Object validationTarget) {
        Roster roster = context.lookup(Roster.class);
        if (roster == null || (!(validationTarget instanceof JTextComponent))) {
            return ValidationResult.EMPTY;
        }
        JTextComponent text = (JTextComponent)validationTarget;
        String input = text.getText().trim();
        
        if (input.isEmpty()) {
            return reportEmpty();
        }
        
        for (RosterEntry en : roster.getAllEntries()) {
            String compare = value(kind, en);
            if (compare == null) {
                continue;
            }
            compare = compare.trim();
            if (compare.compareToIgnoreCase(input) == 0) {
                ValidationResult r =  report(en);
                if (r != null) {
                    return r;
                }
            }
        }
        return ValidationResult.EMPTY;
    }
    
    private ValidationResult reportEmpty() {
        ValidationResult r;
        switch (kind) {
            case ID:
                return new ValidationResult().addError(
                    Bundle.VALIDATE_RosterIDEmpty(),
                    KEY_ROSTER_ID
                );
        }
        return null;
    }
    
    @NbBundle.Messages({
        "VALIDATE_RosterIDEmpty=Roster ID cannot be empty.",
        "VALIDATE_RosterIDExists=Item with this ID already exist in the Roster. IDs must be unique.",
        "VALIDATE_RosterNameExists=Item with this ID already exist in the Roster. IDs must be unique.",
        "VALIDATE_RosterAddressDuplicate=The DCC address is already used. Please check that the same value is really intended.",
    })
    private ValidationResult report(RosterEntry entry) {
        RosterEntry myEntry = context.lookup(RosterEntry.class);
        switch (kind) {
            case ID:
                return new ValidationResult().
                        addError(Bundle.VALIDATE_RosterIDExists(), KEY_ROSTER_ID);
            case ADDRESS:
                return new ValidationResult().
                        addWarning(Bundle.VALIDATE_RosterIDExists(), KEY_ADDRESS);
                
        }
        return null;
    }
    
    private String value(Kind k, RosterEntry en) {
        switch (k) {
            case ADDRESS: 
                return en.getDccAddress();
            case ID:
                return en.getId();
            case NAME:
                return en.getDisplayName();
            case NUMBER:
                return en.getRoadNumber();
            default:
                throw new IllegalArgumentException(k.toString());
        }
    }

    @Override
    public void attach(Lookup context) {
        this.context = context;
    }
}
