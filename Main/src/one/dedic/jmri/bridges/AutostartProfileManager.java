/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package one.dedic.jmri.bridges;

import java.io.IOException;
import jmri.profile.ProfileManager;

/**
 *
 * @author sdedic
 */
public class AutostartProfileManager extends ProfileManager {

    @Override
    public void readActiveProfile() throws IOException {
        super.readActiveProfile();
        setAutoStartActiveProfile(true);
    }
    
}
