/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.Icon;

/**
 * The model represents groups of panes presented in the decoder's view. Panes are
 * organized into Categories, each of which must have a label and icon.
 * 
 * @author sdedic
 */
public class PaneGroupModel {
    /**
     * Ordered list of main categories.
     */
    private final List<GroupDescription>  categories = new ArrayList<>();
    
    private final Map<GroupDescription, List<PanelProvider>>   panels = new HashMap<>();

    
    /**
     * Describes a category / group of panels. Categories are typically displayed as
     * top navigation with large icons, small icons should be provided for tree-like
     * displays.
     */
    public static class GroupDescription {
        private final String id;
        private final String displayName;
        private final Icon   icon;
        private final Icon   smallIcon;

        GroupDescription(String id, String displayName, Icon smallIcon, Icon icon) {
            this.id = id;
            this.displayName = displayName;
            this.icon = icon;
            this.smallIcon = smallIcon;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getSmallIcon() {
            return smallIcon;
        }
        
        public Icon getIcon() {
            return icon;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final GroupDescription other = (GroupDescription) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }
    }
    
    public GroupDescription findCategory(String id) {
        for (GroupDescription d : categories) {
            if (d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }
    
    public List<GroupDescription> getCategories() {
        return new ArrayList<>(categories);
    }
    
    public void addCategory(GroupDescription desc) {
        assert desc.getId() != null;
        assert findCategory(desc.getId()) == null;
        categories.add(desc);
    }
    
    public void addCategoryPanel(String categoryId, PanelProvider item) {
        assert categoryId != null;
        GroupDescription d = findCategory(categoryId);
        if (d == null) {
            throw new IllegalArgumentException(categoryId);
        }
        addCategoryPanel(d, item);
    }
    
    public void addCategoryPanel(GroupDescription cat, PanelProvider item) {
        assert cat != null;
        if (item == null) {
            return;
        }
        panels.computeIfAbsent(cat, (c) -> new ArrayList<>()).add(item);
    }
}
