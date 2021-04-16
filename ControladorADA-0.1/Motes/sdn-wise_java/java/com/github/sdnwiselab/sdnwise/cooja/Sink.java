/*
 * Copyright (C) 2015 SDN-WISE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.sdnwiselab.sdnwise.cooja;

import com.github.sdnwiselab.sdnwise.flowtable.*;
import static com.github.sdnwiselab.sdnwise.flowtable.Window.*;
import com.github.sdnwiselab.sdnwise.packet.*;
import static com.github.sdnwiselab.sdnwise.packet.NetworkPacket.SDN_WISE_DST_H;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import org.contikios.cooja.*;
import java.nio.charset.Charset;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import com.github.sdnwiselab.sdnwise.cooja.ApplicationAirPure;


/**
 *
 * @author Sebastiano Milardo
 */
public class Sink extends AbstractMote {

    ApplicationAirPure appAirPure = new ApplicationAirPure();
    private final String addrController;
    private final int portController;
    private Socket tcpSocket;
    private DataOutputStream inviaOBJ;
    private DataInputStream riceviOBJ;

    BufferedReader buffer = null;
    String linha = "";
    String csvDivisor = ",";
    String dataAirQuality;

    String line = null;
    int lineNumber = 0;
    HashMap<Integer, String> linesDataAirPure = new HashMap<Integer, String>();


    public Sink() {
        super();
        String[] tmp = getControllerIpPort();
        addrController = tmp[0];
        portController = Integer.parseInt(tmp[1]);
    }

    public Sink(MoteType moteType, Simulation simulation) {
        super(moteType, simulation);
        String[] tmp = getControllerIpPort();
        addrController = tmp[0];
        portController = Integer.parseInt(tmp[1]);
    }

    @Override
    public final void initSdnWise() {
        super.initSdnWise();
        commonConstructor();
        setDistanceFromSink(0);
        setRssiSink(255);
        this.setSemaphore(1);
    }


    private String[] getControllerIpPort(){
        String s = (String)JOptionPane.showInputDialog(null,
                "Please insert the IP address and TCP port of the controller:",
                "SDN-WISE Sink",
                JOptionPane.QUESTION_MESSAGE,null,null,"localhost:9991");

        String[] tmp = s.split(":");

        if (tmp.length != 2){
            return getControllerIpPort();
        } else {
            return tmp;
        }
    }

    @Override
    public final void controllerTX(NetworkPacket pck) {   //recebe o conteúdo do nó e envia para o controlador

        try {
            if (tcpSocket != null) {
                inviaOBJ = new DataOutputStream(tcpSocket.getOutputStream());
                inviaOBJ.write(pck.toByteArray());
                log("Tipo NetworkPacket: " + pck.getClass().getSimpleName() + "Mote: " + pck.getSrc() + "\n");
            }
        } catch (IOException ex) {
            log("controllerTX: " + ex.getLocalizedMessage());

        }
    }

    @Override
    public void SDN_WISE_Callback(DataPacket packet) {
      //try{
      //  appAirPure.enviarQualidadeAr(packet);
        controllerTX(packet);
      //} catch (Exception ex) {
      //    log("Sink Callback: " + ex.getLocalizedMessage());
      //}

    }

    @Override
    public void rxConfig(ConfigPacket packet) {
        NodeAddress dest = packet.getDst();
        NodeAddress src = packet.getSrc();

        if (!dest.equals(addr)) {
            runFlowMatch(packet);
        } else {
            if (!src.equals(addr)) {
                controllerTX(packet);
            } else {
                if (marshalPacket(packet) != 0) {
                    controllerTX(packet);
                }
            }
        }
    }

    @Override
    public NodeAddress getActualSinkAddress() {
        return addr;
    }

    private void commonConstructor() {
        this.battery = new SinkBattery();

        FlowTableEntry toSink = new FlowTableEntry();
        toSink.addWindow(new Window()
                .setOperator(SDN_WISE_EQUAL)
                .setSize(SDN_WISE_SIZE_2)
                .setLhsLocation(SDN_WISE_PACKET)
                .setLhs(SDN_WISE_DST_H)
                .setRhsLocation(SDN_WISE_CONST)
                .setRhs(this.addr.intValue()));
        toSink.addWindow(Window.fromString("P.TYPE > 127"));
        toSink.addAction(new ForwardUnicastAction().setNextHop(addr));
        toSink.getStats().setPermanent();
        flowTable.set(0, toSink);
        startListening();

        InetSocketAddress iAddr;
        iAddr = new InetSocketAddress(addrController, port);
        RegProxyPacket rpp = new RegProxyPacket(1, addr, "mah", "00:00:00:00:00:00", 1, iAddr);
        controllerTX(rpp);
    }

    private void startListening() {
        try {
            tcpSocket = new Socket(addrController, portController);
            Thread th = new Thread(new TcpListener());
            th.start();
        } catch (IOException ex) {
            log("StrartListening: "+ ex.getLocalizedMessage() + "\n" + addrController + "\n" + portController);
        }
    }

    private class TcpListener implements Runnable {
        @Override
        public void run() {
            try {
                riceviOBJ = new DataInputStream(tcpSocket.getInputStream());
                while (true) {
                    int len = riceviOBJ.read();
                    if (len > 0) {
                        byte[] packet = new byte[len];
                        packet[0] = (byte) len;
                        riceviOBJ.read(packet, 1, len - 1);
                        NetworkPacket np = new NetworkPacket(packet);

                        //log("Tipo do pacote TCP: " + np.getClass().getSimpleName() + "\n");
                        //String npTcpListener = new String(np.getPayload(),Charset.forName("UTF-8"));
                        //log("Solicitação do Controllador: " + npTcpListener + "\n");

                        flowTableQueue.put(np);
                    }
                }
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Sink.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
