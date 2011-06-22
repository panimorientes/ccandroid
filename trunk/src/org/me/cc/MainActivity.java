/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.me.cc;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;
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
public class MainActivity extends Activity {

    String host;
    int puertohost;
    int puertoweb;
    boolean aceptado;
    volatile boolean matar;
    boolean notf = false;
    Handler handler = new Handler();
    Button conexion;
    Button volver;
    ToggleButton servidor;
    ToggleButton notifi;
    ListView Lista;
    Thread hilo;
    public HashMap alertas = new HashMap();
    public HashMap camaras = new HashMap();
    ArrayList<String> lista = new ArrayList<String>();
    String ns;
    NotificationManager mg;
    Conex conex;
    Runnable Funcionar = new Runnable() {

        public void run() {
            while (matar) {
                try {
                    Thread.sleep(5000);
                    conectar();
                    listarCamaras();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }


        }
    };
    Runnable listado = new Runnable() {

        public void run() {

            Lista.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, lista));
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

            Spinner s = (Spinner) findViewById(R.id.widget40);
    ArrayAdapter adapter = ArrayAdapter.createFromResource(
            this, R.array.tiempos, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    s.setAdapter(adapter);
        
        
        conexion = (Button) findViewById(R.id.datosconex);

        servidor = (ToggleButton) findViewById(R.id.servidor);
        servidor.setTextOff("Servidor OFF");
        servidor.setTextOn("Servidor ON");
        servidor.setChecked(false);

        notifi = (ToggleButton) findViewById(R.id.notifi);
        notifi.setTextOff("Notificaciones OFF");
        notifi.setTextOn("Notificaciones ON");
        notifi.setChecked(false);

        notifi.setOnClickListener(null);
        Lista = (ListView) findViewById(R.id.Lista);

        ns = Context.NOTIFICATION_SERVICE;
        mg = (NotificationManager) getSystemService(ns);

        if (getLastNonConfigurationInstance() != null) {
            conex = (Conex) getLastNonConfigurationInstance();
        }

        conexion.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Conexion.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //startActivity(myIntent);
                startActivityForResult(myIntent, 0);
            }
        });

 
        
        servidor.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                if (!servidor.isChecked()) {
                    matar = false;
                    if (conex == null) {
                    } else {
                        conex.matar = matar;
                    }
                } else {

                    if (conex == null) {
                        matar = true;
                        conex = new Conex();
                        setConex(conex);
                        conex.execute();
                    } else {
                        matar = true;
                        setConex(conex);
                        conex.matar = matar;
                    }


                }
            }
        });

        notifi.setOnClickListener(new OnClickListener() {

            public void onClick(View view) {
                if (!notifi.isChecked()) {
                    notf = false;
                    if (conex == null) {
                    } else {
                        conex.notf = notf;
                    }
                } else {

                    notf = true;
                    if (conex == null) {
                    } else {
                        conex.notf = notf;
                    }

                }
            }
        });
    }

        @Override
           public void onBackPressed() {
           moveTaskToBack(true);
    }  
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        host = data.getStringExtra("host");
        puertohost = data.getIntExtra("puerto", 2003);
        puertoweb = data.getIntExtra("puertow", 2004);
        aceptado = data.getBooleanExtra("aceptado", false);

        if (aceptado) {
            servidor.setChecked(true);
            matar = true;
            if (conex == null) {
                conex = new Conex();
                setConex(conex);
                conex.execute();
            } else {
                setConex(conex);
                conex.matar = matar;
            }


        } else {
            matar = false;
            servidor.setChecked(false);
            if (conex == null) {

                conex = new Conex();
                setConex(conex);
                conex.execute();
            } else {
                setConex(conex);
                conex.matar = matar;
            }

        }
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        if (conex != null) // Check that the object exists
        {
            return (conex);
        }
        return super.onRetainNonConfigurationInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Store values between instances here
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Put the values from the UI

        editor.putString("ip", host); // value to store
        editor.putInt("puerto", puertohost); // value to store
        editor.putInt("puertow", puertoweb); // value to store    

        editor.putBoolean("matar", matar);
        editor.putBoolean("notf", notf);

        // Commit to storage
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        // Put the values from the UI
        matar = preferences.getBoolean("matar", false);
        notf = preferences.getBoolean("notf", false);
        puertohost = preferences.getInt("puerto", 2003);
        puertoweb = preferences.getInt("puertow", 2004);
        host = preferences.getString("ip", null);

        if (matar) {
            servidor.setChecked(true);
        } else {
            servidor.setChecked(false);
        }
        if (notf) {
            notifi.setChecked(true);
        } else {
            notifi.setChecked(false);
        }
    }

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

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mg = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.icon;
        CharSequence tickerText = "CamCenter Notificacion";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        Uri path = Uri.parse("file:///sdcard/music/cop_siren.mp3");
        notification.sound = path;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Context context = getApplicationContext();
        CharSequence contentTitle = "CamCenter";
        CharSequence contentText = msg;
        Intent notificationIntent = new Intent(this, notificacion.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("Camara", Camara);
        notificationIntent.putExtra("Fecha", Fecha);
        notificationIntent.putExtra("host", host);
        notificationIntent.putExtra("puerto", puertoweb);
        notificationIntent.putExtra("id", ID);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.contentIntent = contentIntent;
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        mg.notify(ID, notification);


    }

    public void setConex(Conex conex) {
        conex.mg = mg;
        conex.Lista = Lista;
        conex.context = getApplicationContext();
        conex.aceptado = aceptado;
        conex.host = host;
        conex.puertohost = puertohost;
        conex.puertoweb = puertoweb;
        conex.matar = matar;
        conex.notf = notf;



    }
}
