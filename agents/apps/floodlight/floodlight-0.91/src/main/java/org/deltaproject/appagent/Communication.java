package org.deltaproject.appagent;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Communication extends Thread {
    int result = 1;

    private AppAgent app;

    private Socket socket;
    private InputStream in;
    private DataInputStream dis;
    private OutputStream out;
    private DataOutputStream dos;

    private String serverIP;
    private int serverPort;

    // private DataFuzzing fuzzing;

    public Communication(AppAgent in) {
        this.app = in;
    }

    public void setServerAddr() {
        // for static
        this.serverIP = "10.0.2.2";
        this.serverPort = 3366;
    }

    public void connectServer(String agent) {
        try {
            socket = new Socket(serverIP, serverPort);
            in = socket.getInputStream();
            dis = new DataInputStream(in);
            out = socket.getOutputStream();
            dos = new DataOutputStream(out);

            dos.writeUTF(agent);
            dos.flush();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void write(String in) {
        try {
            dos.writeUTF(in);
            dos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void replayingKnownAttack(String recv) throws IOException {
        String result = "";

        if (recv.equals("3.1.020")) {
            app.Set_Control_Message_Drop();
            result = app.Control_Message_Drop();
            dos.writeUTF(result);
        } else if (recv.equals("3.1.030")) {
            app.Set_Infinite_Loop();
            return;
        } else if (recv.equals("3.1.040")) {
            result = app.Internal_Storage_Abuse();
            dos.writeUTF(result);
        } else if (recv.equals("3.1.070")) {
            result = app.Flow_Rule_Modification();
            dos.writeUTF(result);
        } else if (recv.contains("3.1.080")) {
            if (recv.contains("false"))
                app.Flow_Table_Clearance(false); // only once
            else
                app.Flow_Table_Clearance(true); // infinite
            return;
        } else if (recv.equals("3.1.090")) {
            result = app.Event_Listener_Unsubscription();
            dos.writeUTF(result);
        } else if (recv.equals("3.1.110")) {
            app.Resource_Exhaustion_Mem();
            return;
        } else if (recv.equals("3.1.120")) {
            app.Resource_Exhaustion_CPU();
            return;
        } else if (recv.equals("3.1.130")) {
            app.System_Variable_Manipulation();
            return;
        } else if (recv.equals("3.1.140")) {
            app.System_Command_Execution();
            return;
        } else if (recv.equals("3.1.160")) {
            result = app.LinkFabrication();
            dos.writeUTF(result);
        } else if (recv.equals("3.1.190")) {
            app.Flow_Rule_Flooding();
            return;
        } else if (recv.equals("3.1.200")) {
            result = app.Switch_Firmware_Misuse();
            dos.writeUTF(result);
        } else if (recv.contains("2.1.060")) {
            result = app.sendUnFlaggedFlowRemoveMsg();
            dos.writeUTF(result);
        }

        dos.flush();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        String recv = "";

        try {
            while ((recv = dis.readUTF()) != null) {
                // reads characters encoded with modified UTF-8
                replayingKnownAttack(recv);
            }
        } catch (Exception e) {
            // if any error occurs
            e.printStackTrace();
        } finally {
            try {
                dis.close();
                dos.close();
            } catch (IOException e) {

                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
