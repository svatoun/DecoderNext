/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package one.dedic.jmri.decodernext.forms.api.model;

/**
 *
 * @author sdedic
 */
public interface ErrorMessageProducer {
    public String   createMessage(String original, Throwable error, EntryDescriptor description);
}
