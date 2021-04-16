package com.github.sdnwiselab.sdnwise.application;

import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.loader.SdnWise;
import com.github.sdnwiselab.sdnwise.packet.DataPacket;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.packet.ReportPacket;
import static com.github.sdnwiselab.sdnwise.application.ConstantsApplication.*;
import java.nio.charset.Charset;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.github.sdnwiselab.sdnwise.packet.ConfigTimerPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;



public class ApplicationAirPure extends AbstractApplication{

    public static float temp;
    public static float umid;
    public static float tvoc;
    public static float co2;
    public static String mote;
    public Date dataColeta;
    

    public static String dataReceive;
    public static String[] arrayDataMotes;
    ConfigTimerPacket timerPacket;
    
    public HashMap<String, LinkedList<Float>> dataMoteAirPure;
    
    public ApplicationAirPure(Controller ctrl) {
        super(ctrl);
        dataMoteAirPure = new HashMap<>();
    }

    

    @Override
    public void receivePacket(DataPacket data){
        
        dataReceive = new String(data.getPayload(),Charset.forName("UTF-8"));
       
        
        dataColeta = new Date();
        String hora = new SimpleDateFormat("HH:mm:ss").format(dataColeta);
        mote = "" + data.getSrc();
        System.out.println("data Receive: " + dataReceive + " em: " + hora + " do Mote: " + mote + "\n");
        
        /*
        arrayDataMotes = dataReceive.split(" , ");        
        int idInfo = Integer.parseInt(arrayDataMotes[0]);
        

        switch(idInfo){
            case 1:                 
                verificaQualidadeAr(arrayDataMotes, mote);
                break;
            case 12:
            case 13:
            case 14:
            case 15:
                verificaControleAmbiente(dataReceive);
                break;
        } */ 
    }
   


    public void verificaQualidadeAr(String[] arrayDataMotes, String mote){
        
        temp = Float.parseFloat(arrayDataMotes[1]);
        umid = Float.parseFloat(arrayDataMotes[2]);
        tvoc = Float.parseFloat(arrayDataMotes[3]);
        co2  = Float.parseFloat(arrayDataMotes[4]);
        LinkedList<Float> dadosMote = new LinkedList<>();
        dadosMote.add(temp);
        dadosMote.add(umid);
        dadosMote.add(tvoc);
        dadosMote.add(co2);
        
        dataMoteAirPure.put(mote, dadosMote);
        System.out.println("dataMoteAirPure: " + dataMoteAirPure);
        
        
        System.out.println("\nDados do Mote: " + mote + "\nTemperatura: " + temp + " Graus;" + " Umidade: "+ umid + "%;" 
                            + " TVOC: " + tvoc + "ppb;" + " CO2: " + co2 + "ppm. \n");
        
        verificaTemperatura(temp, umid, tvoc, mote);
        verificaUmidade(umid, temp, mote);
        verificaTvoc(tvoc, mote);
        verificaCO2(co2, mote);

    }
    
    public String verificaTemperatura(float temperatura, float umidade, float tvoc, String mote){
        
        if(temperatura > MAX_TEMP ){
        
            System.out.println("Temperatura: [" + temperatura + " Graus] ACIMA do permitido!"
                    + "\nSolicitando analise do TVOC e da Umidade...\n");
            
            System.out.println("nivel do TVOC: " + analisaTVOC(tvoc));
            
            if(analisaTVOC(tvoc).equals(CRITICO)){
             SdnWise.message = CONTROLAR_TVOC;
            }
            
            else if(umidade < MIN_UMID){
                return "Temperatura elevada, Umidade abaixo do permitido (risco de problemas respiratorios)";
            }
            else {
             SdnWise.message = CONTROLAR_TEMP;
                return "Controle de Temperatura solicitado...";
            }
        }
        if(temperatura < MIN_TEMP){
            SdnWise.message = CONTROLAR_TEMP;
            return "Temperatura abaixo do permitido - solicitando controle de temperatura...";
        }

      SdnWise.message = QUALIDADE_AR;

      return DENTRO_LIMITE;
    }
    
    
    public String verificaUmidade(float umidade, float temperatura, String mote){

        if(umidade > MAX_UMID){
            
            System.out.println("Umidade: ["+ umidade +" %] ACIMA do permitido! "
                    + "\nSolicitando Controle da Umidade do ambiente...\n");
            SdnWise.message = CONTROLAR_UMID;
             
        }
        else if(umidade < MIN_UMID){
            
            System.out.println("Umidade: ["+ umidade +" %] ABAIXO do permitido! "
                    + "\nSolicitando verificacao da Temperatura do ambiente...\n");
            
            if(temp > MAX_TEMP){
                SdnWise.message = CONTROLAR_TEMP;
            }
        }
     SdnWise.message = QUALIDADE_AR;
     
     return DENTRO_LIMITE;
    }

    
    public String verificaTvoc(float tvoc, String mote){

        if(tvoc > MAX_TVOC){
            System.out.println("TVOC: ["+ tvoc +" ppb] ACIMA do permitido!"
                    + "\n Solicitando analise CO2 e da Temperatura no ambiente...");
            
            if(co2 > MAX_CO2 || temp > MAX_TEMP){
                verificaCO2(co2, mote);
                verificaTemperatura(temp, umid, tvoc, mote);
            }
            else{
                System.out.println("");
            }
        }
       SdnWise.message = QUALIDADE_AR;
       
       return "Situacao TVOC: " + analisaTVOC(tvoc);
    }
    
    public String analisaTVOC(float tvoc){
 
        if(tvoc > 500 ){
            return CRITICO;
        }
        if(tvoc <= 500 && tvoc >= 400){
            return ALARMANTE;
        }
        if(tvoc < 400 && tvoc >= 300){
            return REGULAR;
        }
        if(tvoc < 300){
            return BOM;
        }
        
      return "Valor TVOC nao analisado!";
    }

    public void verificaCO2(float co2, String mote){

        if(co2 > MAX_CO2){
            
            System.out.println("CO2: ["+ co2 +" ppm] ACIMA do permitido! "
                    + "\nSolicitando analise de: Temperatura e Umidade...\n");
            verificaTemperatura(temp, umid, tvoc, mote);
            
        }else if (co2 < MAX_CO2){
            analisaCO2(co2);
        }
        else {
            SdnWise.message = QUALIDADE_AR;
        }
    }
    
     public String analisaCO2(float co2){
        
        if(co2 > 1500){
            return CRITICO;
        }
        if(co2 <= 1500 && co2 >= 1000){
            return ALARMANTE;
        }
        if(co2 < 1000 && co2 >= 850){
            return REGULAR;
        }
        if(co2 < 850){
            return BOM;
        }
      
      return "Valor CO2 nao analisado!"; 
    }
    
    public String verificaControleAmbiente(String fatorControlado){
        
        String[] verificar = fatorControlado.split(" , ");
        int id = Integer.parseInt(verificar[0]);
        
        switch(id){
            case 22: //Temperatura
                temp = Float.parseFloat(verificar[1]);
                //System.out.println("temp " +  temp);
                if (temp > MAX_TEMP || temp < MIN_TEMP){
                    
                    System.out.println("Solicitar controle de Temperatura novamente!\n");
                    SdnWise.message = CONTROLAR_TEMP;
                }
                else{
                    System.out.println("Temperatura Controlada para: "+ temp +" Graus\n");
                   SdnWise.message = QUALIDADE_AR;
                }   
            case 33: //Umidade
                umid = Float.parseFloat(verificar[1]);
                if (umid > MAX_UMID || umid < MIN_UMID){
                    
                    System.out.println("Solicitar controle de Umidade novamente!\n");
                    SdnWise.message = CONTROLAR_UMID;
                }
                else{
                    System.out.println("Umidade Controlada para: "+ umid + "%\n");
                    SdnWise.message = QUALIDADE_AR;
                }
            case 44: //TVOC
                tvoc = Float.parseFloat(verificar[1]);
                System.out.println("situacao TVOC: " + analisaTVOC(tvoc));
          
            case 55: //CO2
                co2 = Float.parseFloat(verificar[1]);
                if (co2 > MAX_CO2){
                    return "Solicitar controle de CO2 novamente!";
                }
                else{
                    SdnWise.message = QUALIDADE_AR;
                    return "CO2 dentro do limite!";
                }
            default:
                System.out.println("Valor Invalido!");
        }
       return null;
        
    }
    
    public void calcularIAQ(int temperatura, int umidade, int tvoc, int co2){
  
    }
    
    @Override
    public void receiveMetrics(NetworkPacket netPacket){
     
        ReportPacket rp = new ReportPacket(netPacket);
        
        String mote = netPacket.getSrc().toString();
        String ttl = String.valueOf(netPacket.getTtl());
        String bateria = String.valueOf(rp.getBatt());
        String vizinhos = rp.getNeighborsHashMap().toString();
        
        String dadosColetados = dataColeta + "," + mote + "," + ttl + "," + bateria + "," + vizinhos;
        gerarCsv(dadosColetados);
        
        System.out.println("\n*****************************************************************");
        System.out.println("Informacoes do Mote: " + mote);
        System.out.println("TTL: " + ttl);
        System.out.println("Bateria: " + bateria);
        System.out.println("Vizinhos (Hash) : " + vizinhos);
        System.out.println("*****************************************************************\n");     
  
    }
    
    public void gerarCsv(String dadosColetados){
        
        String[] dados = new String[]{dadosColetados};
        String path = "/home/bruna/infoMotes.csv";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
                        
            for (String line : dados) {
                bw.write(line);
                bw.newLine();
            }
            
        } catch (IOException e) {
            System.out.println("Erro ao inserir dados no csv");
        } 
    }
    
    public void sleep(int i){
        try {
            Thread.sleep(i);
        } catch (InterruptedException ex) {
            System.out.println("Error - sleep");
        }
        
    }
           
}