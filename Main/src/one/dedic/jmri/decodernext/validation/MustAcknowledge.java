/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.decodernext.validation;

/**
 * If implemented by the {@link ValidationMessage}, the UI will require the 
 * user to acknowledge the message before proceeding. In a sence, a message with
 * no acknowledgment will act as an error, although the presentation / severity
 * will be a warning.
 * 
 * @author sdedic
 */
public interface MustAcknowledge {
    /**
     * @return a persistent key to track acknowledgement.
     */
    public Object   getAcknowledgeKey();
}
