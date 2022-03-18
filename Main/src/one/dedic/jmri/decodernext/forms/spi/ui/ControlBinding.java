/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.spi.ui;

import java.util.Set;
import one.dedic.jmri.decodernext.forms.api.model.DataItem;
import one.dedic.jmri.decodernext.forms.api.model.EntryDescriptor;

/**
 *
 * @author sdedic
 */
public final class ControlBinding {
    private String id;
    private Set<String> requiredData;
    private Set<String> acceptedData;
    private EntryDescriptor descriptor;
    private DataItem    dataItem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getRequiredData() {
        return requiredData;
    }

    public void setRequiredData(Set<String> requiredData) {
        this.requiredData = requiredData;
    }

    public Set<String> getAcceptedData() {
        return acceptedData;
    }

    public void setAcceptedData(Set<String> acceptedData) {
        this.acceptedData = acceptedData;
    }

    public EntryDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(EntryDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(DataItem dataItem) {
        this.dataItem = dataItem;
    }
}
