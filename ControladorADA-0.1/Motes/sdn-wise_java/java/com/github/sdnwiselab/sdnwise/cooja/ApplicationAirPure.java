
package com.github.sdnwiselab.sdnwise.cooja;

import com.github.sdnwiselab.sdnwise.packet.*;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import com.github.sdnwiselab.sdnwise.cooja.Mote;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class ApplicationAirPure {

//declaração dos atributos da classe ApplicationAirPure

  //public static String solicitacao;
  //public static String solicitacaoControlador;
  public static String hora;

  public static int temperatura;
  public static int umidade;
  public static int tvoc;
  public static int co2;
  public final Date dataColeta = new Date();
  //Mote mote;

  Random rand = new Random();
  SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm:ss");
  GregorianCalendar gc = new GregorianCalendar();

//implementação dos métodos da classe ApplicationAirPure

  public void gerarDadosAirPure(){

      temperatura = rand.nextInt(21) + 10;
      umidade = rand.nextInt(90) + 10;
      tvoc = rand.nextInt(500) + 1;
      co2 = rand.nextInt(2000) + 400;

  }

  public void dataMote(DataPacket packet, Mote mote){

      String solicitacaoControlador = new String(packet.getPayload(),Charset.forName("UTF-8"));
      mote.log("\n" + "Controlador solicita informacao sobre: " + solicitacaoControlador + "\n");
      verificaSolicitacao(packet, solicitacaoControlador, mote);

      //packet.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
      //mote.runFlowMatch(packet);   //método de constroe a regra da tabela de fluxo
  }


  public void verificaSolicitacao(DataPacket packet, String solicitacao, Mote mote){
    SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm:ss");
    GregorianCalendar gc = new GregorianCalendar();
    mote.log("string: " + solicitacao);

    try{
      switch(solicitacao){
        case "Qualidade do Ar":

          while(true){
            mote.log("Aqui");
            //try{

            DataPacket packet2 = new DataPacket(enviarDadosControlador(1).getBytes(Charset.forName("UTF-8")));
            packet2.setPayload(enviarDadosControlador(1).getBytes(Charset.forName("UTF-8")));
            packet2.setSrc(mote.addr).setDst(mote.getActualSinkAddress()).setTtl((byte) mote.ttl_max);
            mote.runFlowMatch(packet2);   //método de constroe a regra da tabela de fluxo
            Thread.sleep(800);

          //  }catch(Exception ex){
          //    mote.log("Error");
          // }

          }
          //break;
        case "Temperatura":
          packet.setPayload(enviarDadosControlador(2).getBytes(Charset.forName("UTF-8")));
          break;
        case "Umidade":
          packet.setPayload(enviarDadosControlador(3).getBytes(Charset.forName("UTF-8")));
          break;
        case "TVOC":
          packet.setPayload(enviarDadosControlador(4).getBytes(Charset.forName("UTF-8")));
          break;
        case "CO2":
          packet.setPayload(enviarDadosControlador(5).getBytes(Charset.forName("UTF-8")));
          break;
        case "controleTemperatura":
          packet.setPayload(controlarFatores(12).getBytes(Charset.forName("UTF-8")));
        case "controleUmidade":
          packet.setPayload(controlarFatores(13).getBytes(Charset.forName("UTF-8")));
        case "controleTVOC":
          packet.setPayload(controlarFatores(14).getBytes(Charset.forName("UTF-8")));
        case "controleCO2":
          packet.setPayload(controlarFatores(15).getBytes(Charset.forName("UTF-8")));
        default:
          packet.setPayload("Entrada Invalida!".getBytes(Charset.forName("UTF-8")));
          break;
      }
   }
   catch (Exception e) {
     String messageError = "Erro ao receber solicitacao do controlador";
     packet.setPayload(messageError.getBytes(Charset.forName("UTF-8")));
   }
 }

 public String enviarDadosControlador(int tipoDado){
   gerarDadosAirPure();
   SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm:ss");
   GregorianCalendar gc = new GregorianCalendar();
   hora = sdf.format(gc.getTime());

        try{
           switch(tipoDado){
             case 1:
               return tipoDado + " , " + temperatura + " , " + umidade + " , " + tvoc + " , " + co2 + " , " + hora;
             case 2:
               return tipoDado + " , " + temperatura + " , " + hora;
             case 3:
               return tipoDado + " , " + umidade + " , " + hora;
             case 4:
               return tipoDado + " , " +  tvoc + " , " + hora;
             case 5:
               return tipoDado + " ," +  co2 + " , " + hora;
             default:
               break;
           }
         }
         catch (Exception e) {
           String messageError = "Erro ao enviar solicitacao do controlador";
         }
    return null;
  }

  public String controlarFatores(int idFator){
      gerarDadosAirPure();

      switch(idFator){
        case 12:
          return idFator + " , " + (temperatura - (temperatura*0.1));
        case 13:
          return idFator + " , " + (umidade - (umidade*0.1));
        case 14:
          return idFator + " , " +  (tvoc - (tvoc*0.2));
        case 15:
          return idFator + " ," +  (co2 - (co2*0.2));
        default:
          return "Entrada Invalida!";
      }
  }


  public void enviarQualidadeAr(DataPacket pck, Mote mote) {        //executado no callback do Sink
    gerarDadosAirPure();
    SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm:ss");
    GregorianCalendar gc = new GregorianCalendar();
    hora = sdf.format(gc.getTime());

    try {
        Thread.sleep(5000);  // 5 segundos e enviar
        String enviarQualidade = 1 + " , " + temperatura + " , " + umidade + " , " + tvoc + " , " + co2 + " , " + pck.getSrc() + hora;
        pck.setPayload(enviarQualidade.getBytes(Charset.forName("UTF-8")));

    } catch (Exception ex) {
        mote.log("Error enviarQualidadeAr");
    }
  }

} // fim da classe ApplicationAirPure
