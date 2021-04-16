/*
 * Copyright (C) 2021 bruna
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
package com.github.sdnwiselab.sdnwise.application;

/**
 *
 * @author bruna
 */
public class ConstantsApplication {
   
    
    // analise dos fatores
    public static String CRITICO = "Critico!";
    public static String ALARMANTE = "Alarmante!";
    public static String REGULAR = "Regular!";
    public static String BOM = "Bom!";
    public static String DENTRO_LIMITE = "Valor dentro do limite estabelecido em Legislacao";
    
    //SOLICITAR DADOS AO CONTROLADOR
    public static String QUALIDADE_AR = "Qualidade do Ar";
    public static String TEMPERATURA = "Temperatura";
    public static String UMIDADE = "Umidade";
    public static String TVOC = "TVOC";
    public static String CO2 = "CO2";
    
    //SOLICITAR CONTROLE DOS FATORES
    public static String CONTROLAR_TEMP = "controleTemperatura";
    public static String CONTROLAR_UMID = "controleUmidade";
    public static String CONTROLAR_TVOC = "controleTVOC";
    public static String CONTROLAR_CO2  = "controleCO2";
    
       
    public static float temp;
    public static final float MAX_TEMP = 28;
    public static final float MIN_TEMP = 20;
    
    public static float umid;
    public static final float MAX_UMID = 65;
    public static final float MIN_UMID = 35;
    
    public static float tvoc;
    public static final float MAX_TVOC = 500;        //valor mínimo não é relevante
    public static float co2;
    public static final float MAX_CO2 = 1000;        //valor mínimo não é relevante

  
    private ConstantsApplication(){
        
    }
}
