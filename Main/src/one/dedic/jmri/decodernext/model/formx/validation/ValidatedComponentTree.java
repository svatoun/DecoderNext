/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.formx.validation;

import com.jgoodies.validation.ValidationResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.JComponent;
import one.dedic.jmri.decodernext.model.formx.spi.FormPart;
import static one.dedic.jmri.decodernext.model.formx.swing.SwingFormUtils.PROPERTY_FORM_PART;
import static one.dedic.jmri.decodernext.model.formx.swing.SwingFormUtils.PROPERTY_VALIDATED;
import one.dedic.jmri.decodernext.model.formx.swing.SwingTreeCollector;

/**
 *
 * @author sdedic
 */
public class ValidatedComponentTree implements Validated {
    private final JComponent target;
    private final L vl = new L();
    private final Scanner scanner;
    private boolean pending;
    private List<ValidationListener> listeners = new ArrayList<>();
    private Collection<CompletableFuture> allFutures = new LinkedHashSet<>();
    private ValidationResult pendingResult;
    private CompletableFuture<ValidationResult> result = CompletableFuture.completedFuture(ValidationResult.EMPTY);
    
    public ValidatedComponentTree(JComponent target) {
        this.target = target;
        this.scanner = new Scanner(target);
    }

    @Override
    public CompletableFuture<ValidationResult> validate(boolean force) {
        List<Validated> ll;
        CompletableFuture<ValidationResult> fake;
        synchronized (this) {
            if (result.isDone() && !force) {
                return result;
            }
            ll = new ArrayList<>(scanner.getItems());
            fake = new CompletableFuture<>();
            allFutures.add(fake);
        }
        // fire validation start...
        fireValidationPending();
        List<CompletableFuture<ValidationResult>> fts = new ArrayList<>();
        ll.forEach(v -> {
            CompletableFuture<ValidationResult> cf = v.validate(force);
            synchronized (this) {
                if (!cf.isDone()) {
                    allFutures.add(cf);
                }
            }
            cf.thenAccept((r) -> checkFinished(cf, r));
        });
        checkFinished(fake, ValidationResult.EMPTY);
        return CompletableFuture.allOf(fts.toArray(new CompletableFuture[fts.size()])).thenCompose((v) -> result);
    }

    @Override
    public void addValidationListener(ValidationListener l) {
        synchronized (this) {
            listeners.add(l);
        }
    }

    @Override
    public void removeValidationListener(ValidationListener l) {
        synchronized (this) {
            listeners.remove(l);
        }
    }

    @Override
    public ValidationResult getValidation() {
        try {
            return result.get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private void fireValidationPending() {
        List<ValidationListener> ll;
        synchronized (this) {
            if (pending) {
                return;
            }
            pending = true;
            if (listeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<>(listeners);
            pendingResult = new ValidationResult();
            result = new CompletableFuture<>();
        }
        ValidationEvent e = new ValidationEvent(this, CompletableFuture.completedFuture(null));
        ll.forEach(l -> l.validationPending(e));
    }
    
    class L implements ValidationListener {
        @Override
        public void validationPending(ValidationEvent e) {
            fireValidationPending();
            
            Validated s = (Validated)e.getSource();
            CompletableFuture<ValidationResult> rr = s.validate(false);
            synchronized (this) {
                if (!rr.isDone()) {
                    allFutures.add(rr);
                }
            }
            rr.thenAccept((v) -> checkFinished(rr, v));
        }
    }
    
    private void checkFinished(CompletableFuture<ValidationResult> cf, ValidationResult r) {
        CompletableFuture<ValidationResult> toComplete;
        ValidationResult total;
        ValidationResult prev;
        List<ValidationListener> ll;
        
        synchronized (this) {
            pendingResult.addAllFrom(r);
            allFutures.remove(cf);
            if (!allFutures.isEmpty()) {
                return;
            }
            prev = this.result.getNow(ValidationResult.EMPTY);
            total = this.pendingResult;
            pendingResult = null;
            toComplete = result;
            pending = false;

            if (listeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<>(listeners);
        }
        ValidationEvent e = new ValidationEvent(total, this);
        toComplete.complete(total);
        if (!total.equals(prev)) {
            ll.forEach(l -> l.validationResultChanged(e));
        }
        ll.forEach(l -> l.validationFinished(e));
    }
    
    class Scanner extends SwingTreeCollector<Validated> {
        public Scanner(JComponent rootComponent) {
            super(rootComponent, Validated.class);
        }

        @Override
        protected void notifyRemoved(Collection<Validated> removals) {
            removals.forEach(v -> v.removeValidationListener(vl));
            super.notifyRemoved(removals);
        }

        @Override
        protected void notifyAdded(Collection<Validated> additions) {
            super.notifyAdded(additions);
            additions.forEach(v -> v.addValidationListener(vl));
        }

        @Override
        protected Validated extract(JComponent c) {
            Object o;
            o = c.getClientProperty(PROPERTY_VALIDATED);
            if (o instanceof Validated) {
                return (Validated)o;
            }
            o = c.getClientProperty(PROPERTY_FORM_PART);
            if (o instanceof FormPart) {
                return ((FormPart)o).getValidated();
            }
            return null;
        }
    }
}
