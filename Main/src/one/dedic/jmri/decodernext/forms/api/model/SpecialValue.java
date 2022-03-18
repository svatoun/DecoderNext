/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

/**
 *
 * @author sdedic
 */
public final class SpecialValue {
    public static final SpecialValue UNDEFINED = new SpecialValue();
    public static final SpecialValue MULTIPLE = new SpecialValue(null);
    
    public static final SpecialValue ERRONEOUS = new SpecialValue(false);
    public static final SpecialValue NORMAL = new SpecialValue();
    
    private final boolean valid;
    private final boolean common;
    private final Object commonValue;
    
    private SpecialValue(boolean valid) {
        this.valid = valid;
        this.common = false;
        this.commonValue = null;
    }
    
    private SpecialValue() {
        this(true);
    }
    
    private SpecialValue(Object common) {
        this.commonValue = common;
        this.valid = true;
        this.common = true;
    }
    
    public boolean isCommon() {
        return common && commonValue != null;
    }

    public boolean isValid() {
        return valid;
    }

    public Object getCommonValue() {
        return commonValue;
    }

    public static SpecialValue common(Object v) {
        return new SpecialValue(v);
    }
}
