/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.cc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
 

/**
 *
 * @author daniel
 */
class Conex extends AsyncTask<Void, Void, Void> {

    boolean matar = false;
    
    boolean notf = false;
    boolean aceptado = false;
    public HashMap alertas = new HashMap();
    public HashMap camaras = new HashMap();
    ListView Lista;
    ArrayList<String> lista = new ArrayList<String>();
    String host;
    int puertohost;
    int puertoweb;
    Context context;
    Handler handler = new Handler();
    NotificationManager mg;


//    @Override
//        public void run() {
//            while (matar) {
//                try {
//                    Thread.sleep(5000);
//                    conectar();
//                    listarCamaras();
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//
//        }
    
    Runnable listado = new Runnable() {

        public void run() {

            Lista.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, lista));
        }
    };

    public HashMap conectar() {
        try {
            Socket s = new Socket(host, puertohost);
            ObjectInputStream entrada = new ObjectInputStream(s.getInputStream());
            HashMap viejas = (HashMap) alertas.clone();
            camaras = (HashMap) entrada.readObject();
            alertas = (HashMap) entrada.readObject();
            if (notf) {
                alertar(viejas);
            }

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

    public void alertar(HashMap viejas) {
        Iterator iterator = viejas.keySet().iterator();
        int cont = 1;
        while (iterator.hasNext()) {
            String camara = (String) iterator.next();
            cont++;
            if (!viejas.get(camara).equals(alertas.get(camara))) {
                displayNotification("Movimiento en la camara " + camara, cont, camara, (String) alertas.get(camara));

            }

        }

    }

    public void listarCamaras() {

        lista = new ArrayList<String>();
        Set gg = null;
        if (!camaras.isEmpty()) {
            gg = camaras.keySet();
        } else {
            gg = new HashSet();
            gg.add("Ninguna");

        }
        Iterator iterator = gg.iterator();
        while (iterator.hasNext()) {

            lista.add((String) iterator.next());

        }
        handler.post(listado);


    }

    public void displayNotification(String msg, int ID, String Camara, String Fecha) {

        int icon = R.drawable.icon;
        CharSequence tickerText = "CamCenter Notificacion";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        Uri path = Uri.parse("file:///sdcard/music/cop_siren.mp3");
        notification.sound = path;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        CharSequence contentTitle = "CamCenter";
        CharSequence contentText = msg;
        Intent notificationIntent = new Intent(context, notificacion.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("Camara", Camara);
        notificationIntent.putExtra("Fecha", Fecha);
        notificationIntent.putExtra("host", host);
        notificationIntent.putExtra("puerto", puertoweb);
        notificationIntent.putExtra("id", ID);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        mg.notify(ID, notification);
    }

    @Override
    protected Void doInBackground(Void... paramss) {

            while (matar) {
                try {
                    Thread.sleep(5000);
                    conectar();
                    listarCamaras();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        return(null);
    }
}
