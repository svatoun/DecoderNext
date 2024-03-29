/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

/**
 * Mixin interface that marks a model which transforms the value before returning
 * it.
 * @author sdedic
 */
public interface TransformModel {
    public Object transform(Object v);
}
