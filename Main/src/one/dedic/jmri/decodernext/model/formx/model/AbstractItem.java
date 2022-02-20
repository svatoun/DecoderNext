/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.model;

import java.beans.FeatureDescriptor;

/**
 * Represents an item in the data layer. The item may be a 
 * @author sdedic
 */
public interface AbstractItem {
    /**
     * Uniquely identifies this item. Allows to present one single data
     * item more than once on the form, while validating the data, rather
     * than its appearance.
     * 
     * @return 
     */
    public String id();

    /**
     * Describes the item. Name and shortDescription should be filled.
     * @return 
     */
    public FeatureDescriptor getDescriptor();
}
