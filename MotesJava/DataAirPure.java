/* 
 * Programa para testar a leitura do arquivo csv
 * não está sendo utilizado nos codigos Sink.java, Mote.java e AbstractMote.java
 */

package com.github.sdnwiselab.sdnwise.cooja;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DataAirPure {

  String arquivoCSV = "dataAirpure.csv";
  BufferedReader br = null;
  String linha = "";
  String csvDivisor = ",";
  public String dataAirQuality;

  public String dataSensors() {

    try {
        br = new BufferedReader(new FileReader(arquivoCSV));
        while ((linha = br.readLine()) != null) {
            String[] factors = linha.split(csvDivisor);

        //dataAirQuality = factors[0] + ", " + factors[1] + ", " + factors[2] + ", "
        //                 + factors[3] + ", " + factors[4] + ", " + factors[5];

        dataAirQuality = factors[1];
        System.out.print("Dados Qualidade do Ar: " + dataAirQuality);
        }

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return dataAirQuality;
  }

/**
    public static void main(String[] args) {

      DataAirPure obj = new DataAirPure();
      obj.DataSensors();

    }
**/

}
