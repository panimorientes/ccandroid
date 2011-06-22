/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.cc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author daniel
 */
public class preguntar extends Thread {

    public String host;
    public int puertohost;
    public int puertoweb;
    public boolean matar;
    public boolean notif;
    public HashMap alertas = new HashMap();
    public HashMap camaras = new HashMap();
    
    @Override
    public void run(){
        while(matar){
            if(notif){
                conectar();
            }
        }
        
    }
    
        public HashMap conectar() {
        try {
            Socket s = new Socket(host, puertohost);
            ObjectInputStream entrada = new ObjectInputStream(s.getInputStream());
            camaras = (HashMap) entrada.readObject();
            alertas = (HashMap) entrada.readObject();
            entrada.close();
            s.close();
        } catch (OptionalDataException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
        }
        return new HashMap();



    }

    
}
