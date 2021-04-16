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
package com.github.sdnwiselab.sdnwise.loader;

import com.github.sdnwiselab.sdnwise.application.ApplicationAirPure;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.controller.ControllerFactory;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SdnWise class of the SDN-WISE project. It loads the configuration file and
 * starts the the Controller.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public class SdnWise {

    public static String message = "Qualidade do Ar";
    public Controller controller;
    public int qntNosRede = 12;
    //ApplicationAirPure appAirPure = new ApplicationAirPure();
   
/**
     * Starts the components of the SDN-WISE Controller.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        new SdnWise().startExample();
    }

    /**
     * Starts the Controller layer of the SDN-WISE network. The path to the
     * configurations are specified in the configFilePath String. The options to
     * be specified in this file are: a "lower" Adapter, in order to communicate
     * with the flowVisor (See the Adapter javadoc for more info), an
     * "algorithm" for calculating the shortest path in the network. The only
     * supported at the moment is "DIJKSTRA". A "map" which contains
     * informations regarding the "TIMEOUT" in order to remove a non responding
     * node from the topology, a "RSSI_RESOLUTION" value that triggers an event
     * when a link rssi value changes more than the set threshold.
     *
     * @param configFilePath a String that specifies the path to the
     * configuration file.
     * @return the Controller layer of the current SDN-WISE network.
     */
    public Controller startController(String configFilePath) {
        InputStream configFileURI = null;
        if (configFilePath == null || configFilePath.isEmpty()) {
            configFileURI = this.getClass().getResourceAsStream("/config.ini");
        } else {
            try {
                configFileURI = new FileInputStream(configFilePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Configurator conf = Configurator.load(configFileURI);
        controller = new ControllerFactory().getController(conf.getController());
        new Thread(controller).start();
        return controller;
    }


    public void startExample() {
        controller = startController("");
        ApplicationAirPure appAirPure = new ApplicationAirPure(controller);
        
        System.out.println("\nControlador ADA executando....\n");
        DataPacket dp;
        int netId = 1;
        NodeAddress dst;
        NodeAddress src;
        
        try {
            Thread.sleep(40000);  //30 segundos
        
           // while (true){    
                for (int i = 2; i < 5; i++){   //start na rede...

                    dst = new NodeAddress(4);
                    src = new NodeAddress(1);
                    dp  = new DataPacket(netId,src,dst);

                    dp.setNxhop(src);
                    dp.setPayload(message.getBytes(Charset.forName("UTF-8")));
                    controller.sendNetworkPacket(dp);
                    System.out.println("Solicitando: " + message);
                    Thread.sleep(2000); 
                }

          //}
        } catch (InterruptedException ex) {
            Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }


}
/*

main () {

        application.send("Qualidade do ar", BROAD_CAST);
        Response response = application.recv();
        AirPureData data = parse.parseAirPure(response);

        if (data.temperature > 28) {
            application.send("TVOC", 4);
            Response resp2 = application.recv();
            AirPureData data2 = parse.parseAirpure(resp2);
            data2.tvoc
        }

return 0;
}

mediator.defineIntention("intention.xml");
application.send("Qualidade do ar", "REGION_A"");

*/