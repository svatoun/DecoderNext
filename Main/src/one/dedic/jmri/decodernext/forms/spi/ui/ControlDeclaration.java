/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.spi.ui;

import com.jgoodies.binding.value.ComponentModel;
import com.jgoodies.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import one.dedic.jmri.decodernext.forms.api.model.BufferedModel;
import one.dedic.jmri.decodernext.forms.api.model.TypedValueModel;
import one.dedic.jmri.decodernext.forms.api.ui.ExComponentModel;
import one.dedic.jmri.decodernext.forms.api.ui.Feedback;

/**
 *
 * @author sdedic
 */
public final class ControlDeclaration<T> {
    private ControlBinding info;
    private Class<T>    type;
    private TypedValueModel<T> inputModel;
    private ExComponentModel component;
    private Validator<T> validator;
    private Feedback feedback;
    
    private ControlDeclaration() {}
    
    /**
     * Identity of the control.
     * @return 
     */
    public ControlBinding getInfo() {    
        return info;
    }

    /**
     * Type of the value transferred between the supplier
     * @return 
     */
    public Class<T> valueType() {
        return type;
    }

    /**
     * Represents the value model of the user input. The value model may implement extra
     * properties, like {@link BufferedModel}.
     * @return user input model.
     */
    public TypedValueModel<T> getInputModel() {
        return inputModel;
    }
    
    /**
     * Represents the presentation control model. Allows to enable / disable, hide the
     * presentation.
     * @return presentation model
     */
    public ComponentModel getComponent() {
        return component;
    }

    /**
     * Validator for the input model.
     * @return validator or {@code null}
     */
    public Validator<T> getValidator() {
        return validator;
    }

    public Feedback getFeedback() {
        return feedback;
    }
    
    public static <T> Builder<T> builder(Class<T> type, String id) {
        return new Builder().type(type).id(id);
    }
    
    public static <T> Builder<T> copyOf(ControlDeclaration<T> s) {
        return new Builder(s);
    }
    
    public static class Builder<T> {
        ControlDeclaration inst = new ControlDeclaration();
        Builder() {}
        Builder(ControlDeclaration s) {
            type(s.valueType());
        }
        
        public Builder type(Class<T> type) {
            inst.type = type;
            return this;
        }
        
        public Builder feedback(Feedback f) {
            inst.feedback = f;
            return this;
        }
        
        public Builder component(ExComponentModel m) {
            inst.component = m;
            return this;
        }
        
        public Builder inputModel(TypedValueModel<T> m) {
            inst.inputModel = m;
            return this;
        }
        
        public Builder validator(Validator<T> v) {
            inst.validator = v;
            return this;
        }
        
        public Builder id(String id) {
            inst.getInfo().setId(id);
            return this;
        }
        
        public Builder requireData(String... data) {
            Set<String> old = inst.getInfo().getRequiredData();
            Set<String> n = new HashSet<>(Arrays.asList(data));
            n.addAll(old);
            inst.getInfo().setRequiredData(n);
            return this;
        }
    }
}
