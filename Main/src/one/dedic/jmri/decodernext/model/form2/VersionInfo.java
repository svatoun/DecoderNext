/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.model.form2;

import java.time.Instant;

/**
 *
 * @author sdedic
 */
public final class VersionInfo {
    public static final String CURRENT = new String("current");
    public static final String SAVED = new String("saved");
    
    private final String  id;
    private final String  message;
    private final Instant timestamp;

    public VersionInfo(String label, String message, Instant timestamp) {
        this.id = label;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
    
    public static VersionInfo current() {
        return new VersionInfo(CURRENT, null, Instant.now());
    }

    public static VersionInfo saved(Instant modTime) {
        return new VersionInfo(SAVED, null, modTime);
    }
}
