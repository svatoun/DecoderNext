/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class TreeCollectorEvent<T> extends EventObject {
    private final Collection<T> added;
    private final Collection<T> removed;
    private final List<T> current;

    public TreeCollectorEvent(Object source, Collection<T> added, Collection<T> removed, List<T> current) {
        super(source);
        this.added = new ArrayList<>(added);
        this.removed = new ArrayList<>(removed);
        this.current = new ArrayList<>(current);
    }

    public Collection<T> getAdded() {
        return added;
    }

    public Collection<T> getRemoved() {
        return removed;
    }

    public List<T> getCurrent() {
        return current;
    }
}
