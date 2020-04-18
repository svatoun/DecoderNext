/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation.data;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.Validator;
import com.jgoodies.validation.message.SimpleValidationMessage;
import java.util.function.Supplier;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "ValidationError_MustBeNumber=Number is required",
    "ValidationError_MustBeHexNumber=A hexadecimal number is required",
    "ValidationError_MustBeOctNumber=A octal number is required",
    "ValidationError_MustBeBinNumber=A binary number is required",
    "# {0} - lower bound (inclusive)",
    "ValidationError_NumberAtLeast=The value must be at least {0}",
    "# {0} - lower bound - unused",
    "# {1} - upper bound (exclusive)",
    "ValidationError_NumberAtMost=The number must be at most {1}",
    "# {0} - lower bound (inclusive)",
    "# {1} - upper bound (exclusive)",
    "ValidationError_NumberBetween=The value must fall between {0} and {1}",
})
public class MinMaxIntegerValidator implements Validator {
    private Object msgKey;
    
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    private int radix = 10;

    public MinMaxIntegerValidator(Object msgKey) {
        this.msgKey = msgKey;
    }

    public void setMsgKey(Object msgKey) {
        this.msgKey = msgKey;
    }

    public Object getMsgKey() {
        return msgKey;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getRadix() {
        return radix;
    }

    public void setRadix(int radix) {
        this.radix = radix;
    }
    
    @Override
    public ValidationResult validate(Object validationTarget) {
        int value = -1;
        boolean notNumber = false;
        if (validationTarget instanceof Supplier) {
            validationTarget = ((Supplier)validationTarget).get();
        }
        if (validationTarget instanceof Integer) {
            value = (Integer)validationTarget;
        } else if (validationTarget instanceof String) {
            String s = (String)((String) validationTarget).trim();
            if ("".equals(s)) {
                value = 0;
            } else try {
                value = Integer.parseInt((String)validationTarget);
            } catch (NumberFormatException ex) {
                notNumber = true;
            }
        } else {
            notNumber = true;
        }
        String msg = null;
        
        if (notNumber) {
            switch (radix) {
                case 10:
                    msg = Bundle.ValidationError_MustBeNumber();
                    break;
                case 16:
                    msg = Bundle.ValidationError_MustBeHexNumber();
                    break;
                case 8:
                    msg = Bundle.ValidationError_MustBeOctNumber();
                    break;
                case 2:
                    msg = Bundle.ValidationError_MustBeBinNumber();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            return new ValidationResult().add(
                    new SimpleValidationMessage(msg, Severity.ERROR, msgKey)
            );
        }
        
        if (min == Integer.MIN_VALUE) {
            if (value >= max) {
                msg = Bundle.ValidationError_NumberAtMost(-1, Integer.toString(max, radix));
            }
        } else if (max == Integer.MAX_VALUE) {
            if (value < min) {
                msg = Bundle.ValidationError_NumberAtLeast(Integer.toString(max, radix));
            }
        } else {
            if (value < min || value >= max) {
                msg = Bundle.ValidationError_NumberBetween(
                        Integer.toString(min, radix),
                        Integer.toString(max, radix)
                );
            }
        }
        if (msg != null) {
            return new ValidationResult().add(
                    new SimpleValidationMessage(msg, Severity.ERROR, msgKey)
            );
        }
        return null;
    }
    
}
