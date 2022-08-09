package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.SFTPUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/txtManagement")
public class txtManagement {

    //generar reporte en xlsx
    public static void crearAPartirDeArrayList(ArrayList<String> content, String header, String nameFile, String destinationFolder) {
        Workbook libro = new XSSFWorkbook();
        Sheet hoja = libro.createSheet("Hoja 1");
    
        int indiceFila = 0;
    
        Row fila = hoja.createRow(indiceFila);
        String[] valuesHeader = header.split(";");   
        for (int i = 0; i < valuesHeader.length; i++) {
            String encabezado = valuesHeader[i];
            Cell celda = fila.createCell(i);
            CellStyle cellStyle = celda.getCellStyle();
            if(cellStyle == null) {
                cellStyle = celda.getSheet().getWorkbook().createCellStyle();
            }        
            cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            celda.setCellStyle(cellStyle);
            celda.setCellValue(encabezado);
        }
        
        //12345678901234567890_VVPepito los palotes_Efectivo_600_VVVVVVV6003035650390_Dolares_0.22_Dolares_0.22_20160815_0000_VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVSIN OBSERVACIONES_12345678901234500000

        indiceFila++;
        for (int i = 0; i < content.size(); i++) {
            fila = hoja.createRow(indiceFila);
            String[] values = content.get(i).split("_");   
            //fila.createCell(0).setCellValue(values[0]);
            //fila.createCell(1).setCellValue(values[1]);
            //fila.createCell(2).setCellValue(values[2]);

            for (int j = 0; j < valuesHeader.length; j++) {
                fila.createCell(j).setCellValue(values[j]);
            }


            indiceFila++;
        }
    
        // Guardamos
        String ubicacion = destinationFolder + nameFile +"_report.xlsx";
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(ubicacion); 
            libro.write(outputStream);
            libro.close();
            System.out.println("Libro de personas guardado correctamente");
        } catch (FileNotFoundException ex) {
            System.out.println("Error de filenotfound");
        } catch (IOException ex) {
            System.out.println("Error de IOException");
        }
    
    }


    //Normalizar
    public static String stripAccents(String s) 
    {   s = s.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    //pasar a monedas
    public double stringToMoney(String val){
        val = new StringBuilder(val).insert(val.length()-2, ".").toString();
        String formated = ""; 
        Boolean flag = false; 
        for(int i = 0; i < val.length(); i++){            
            if(val.charAt(i) != '0'){
                flag = true;
            }else{flag = false;}            
            if(flag){
                formated += val.charAt(i);
            }
        }   
        double d=Double.parseDouble(formated);       
        return d;
    }

    //Darle formato a los montos
    public static String formatAmount(String s, int width) 
    {   
        //redondear a dos decimales
        double amount = Double.parseDouble(s);
        double roundDbl = Math.round(amount*100.0)/100.0;

        //convertir a string y retirar punto
        String rounded= (String.valueOf(roundDbl)).replace(".", "");

        //formatear 
        String formatted = String.format("%0" + width + "d", Integer.valueOf(rounded));

        return formatted;
    }

    //Obtener moneda del monto
    public String getCurrency(String s) 
    {   
        System.out.println("getCurrency"); 
        System.out.println(s); 


        String[] partsBase = s.split(","); 
        String data= stripAccents(partsBase[5]);
        String chargeAccountCurrency = data.contains("sol")? "01" : "10"; 
        return chargeAccountCurrency;
    }

    //Clase interna para generar txt
    public void createTxt(ArrayList<String> content){
    }

    //Clase interna para generar nombre del archivo
    public String  createName(String itemCode,  String companyCode, String serviceCode, String fileName){
        System.out.println("createName");
        String name = "H2HH000";
        Date date = new Date();  
        SimpleDateFormat  formatter = new SimpleDateFormat("YYYYMMDDHHMMSS");  
        String strDate = formatter.format(date); 
        name = name + itemCode + companyCode + serviceCode + fileName + "_" + strDate + ".txt";
        System.out.println(name);
        return name;
    }
    
    public String createHeader(String base, String itemCode, String companyCode, String serviceCode,String fileName, Double qLines, Double qSoles, Double qDollars ){
        String header = "01";
        try{  
            System.out.println("createHeader");
            System.out.println(base);
            String[] partsBase = base.split(",");   

            //cuenta de cargo    
            String loadAccount = (partsBase[6].toString()).replace("-", ""); 

            //Tipo de cuenta de cargo        
            String data1= partsBase[5].toString().toLowerCase();
            boolean isSaving = data1.indexOf("ahorro") !=-1? true: false;
            String chargeAccountType = isSaving == true? "002" : "001";

            //Moneda de la cuenta de cargo
            String chargeAccountCurrency = data1.contains("sol") ? "01" : "10";        
            
            //Fecha y hora de creación del txt
            Date date = new Date();  
            SimpleDateFormat  formatter = new SimpleDateFormat("YYYYMMDD");  
            String strDate = formatter.format(date); 

            //tipo de proceso
            String proccessType =  (partsBase[8].toString().toLowerCase()).equals("en diferido") ? "1" :"0";

            //fecha de proceso
            SimpleDateFormat  formatter2 = new SimpleDateFormat("YYYYMMDDHHMMSS");  
            String proccessDate = proccessType.equals("0") ?  formatter2.format(date) : partsBase[9] ;

            //Formatear cantidad y montos
            String fQLines = formatAmount (String.valueOf(qLines) , 6);
            String fQSoles = formatAmount(String.valueOf(qSoles),15);
            String fQDollars = formatAmount(String.valueOf(qDollars), 15);



            //header = header + itemCode + companyCode + ";" + serviceCode + ";" + loadAccount + ";"  + chargeAccountType + ";" +  chargeAccountCurrency + ";"  + fileName + ";"  +  strDate + ";"  + proccessType + ";"  + proccessDate + ";"  + qLines + ";"  + qSoles + ";"  + qDollars + ";"  + "MC001";
            header = header + itemCode + companyCode + serviceCode +  loadAccount +  chargeAccountType +  chargeAccountCurrency + fileName +  strDate + proccessType + proccessDate + fQLines + fQSoles + fQDollars + "MC001";

            //System.out.println("header generado");
            //System.out.println(header);
        }catch(Exception ex){
            System.out.println("error header");
            header= ex.getMessage();
        }
        return header;
    }

    public String  createPlot(String base){
        String plot = "02";
        try{        
            String[] partsBase = base.split(",");   
            //Código de Beneficiario
            String beneficiaryCode = partsBase[2];
            //Tipo de Documento de pago
            String data1= stripAccents(partsBase[10]); 
            String typePaymentDoc = "";
            if(data1.contains("credito")){
                typePaymentDoc="C";
            }else if(data1.contains("debito")){
                typePaymentDoc="D";
            }else{
                typePaymentDoc="F";
            }
            //Número de documento
            String nDoc = partsBase[11];
            //fecha de vencimiento
            String expirationDate = partsBase[12];
            //Tipo de moneda
            String data2= stripAccents(partsBase[5]);
            String chargeAccountCurrency = data2.contains("sol")? "01" : "10"; 
            //Monto
            String amount = formatAmount(partsBase[15], 15);
            //Tipo de abono
            String data2_1= stripAccents(partsBase[4]);
            String typeOfPayment =  "00";
            if(data2_1.contains("abono")){
                typeOfPayment = "09";
            }else if(data2_1.contains("cheque")){
                typeOfPayment = "11";
            }else if(data2_1.contains("interbancario")){
                typeOfPayment = "99";
            }
            //Tipo de cuenta
            String accountType = "007"; 
            if(data2.contains("corriente")){
                accountType = "001";
            }else if(data2.contains("ahorro")){
                accountType = "002"; 
            }
            // Moneda de la cuenta  
            String chargeAccountCurrencyR = data1.contains("sol")? "01" : "10"; 
            // Oficina de la cuenta  
            String accountOffice =  partsBase[6].substring(0,3); 
            // Número de cuenta  
            String account = partsBase[6].substring(4); 
            // Tipo de persona
            String data3 = stripAccents(partsBase[3]);
            String personType = data3.equals("natural")? "P":"C"; 
            // Tipo de documento de identidad 
            String typeDoc = "03";
            String data4 = stripAccents(partsBase[1]);
            if(data4.equals("dni")){
                typeDoc = "01";            
            }else if(data4.equals("ruc")){
                typeDoc = "02";        
            }else if(data4.equals("pasaporte")){
                typeDoc = "05";        
            }
            // Número de documento de identidad
            String doc = partsBase[2];   
            // Nombre del Beneficiario
            String[] parts = partsBase[0].split(" ");
            String beneficiaryName = parts[1] + ";" + parts[2] + ";" + parts[0];
            // Moneda monto intangible CTS + Monto Intangible CTS  + Filler
            String ctsFiller = "   "; //hay logica para cts
            // Número Celular
            String phone = partsBase[13];
            // Correo Electrónico
            String email = partsBase[14];

            //concatenar todo
            //plot = plot + beneficiaryCode + typePaymentDoc + nDoc + expirationDate + chargeAccountCurrency + amount + " " + typeOfPayment + accountType + chargeAccountCurrencyR + accountOffice + account + personType + typeDoc +  doc +  beneficiaryName + ctsFiller + phone + email;
            plot = plot + "_" + beneficiaryCode + "_" + typePaymentDoc + "_" + nDoc + "_" + expirationDate + "_" + chargeAccountCurrency + "_" + amount + "_" + " " + "_" + typeOfPayment + "_" + accountType + "_" + chargeAccountCurrencyR + "_" + accountOffice + "_" + account + "_" + personType + "_" + typeDoc + "_" +  doc + "_" +  beneficiaryName + "_" + ctsFiller + "_" + phone + "_" + email;

        
        }catch(Exception ex){
            System.out.println("error plot");
            plot= ex.getMessage();
        }
        return plot ;
    }

    

    /////////////////////////////////////////////

    //Clase interna para generar nombre del archivo de salida
    public String  createNameOutput(String originName){
        System.out.println("createNameOutput");
        String name = "";
        String[] partsBase = originName.split("_");   
        String creationDate = partsBase[0];
        String itemCode = partsBase[1];
        String companyCode = partsBase[2];
        String serviceCode = partsBase[3];
        String plotID = partsBase[4];
        String creationDateFile = partsBase[5];
        String flagResult = partsBase[6];
        name = creationDate + "_H2HH000" + itemCode + companyCode + serviceCode + plotID + creationDateFile + "_" + flagResult  ;
        return name;
    }

    //Generar contenido de respuesta
    public String  createAns(String base){
        System.out.println("base::::::::");
        System.out.println(base);
        String beneficiaryId = base.substring(0, 20); //String beneficiaryId = base.substring(0, 19);
        String beneficiaryName = base.substring(20, 40); //String beneficiaryName = base.substring(20, 39);
        
        String data1 = base.substring(40, 42);                
        String bonusType = "Efectivo"; //00=Efectivo; 09=Abono en cuenta; 11=Cheque de Gerencia y 99=Interbancario
        if(data1.equals("09")){
            bonusType = "Abono en cuenta";
        }else if(data1.contains("11")){
            bonusType = "Cheque de Gerencia";
        }else if(data1.contains("99")){
            bonusType = "Interbancario";
        }

        String office = base.substring(42, 45); //String office = base.substring(42, 44);
        String paymentAccountNumber = base.substring(45, 65); 

        String data2 = base.substring(65, 67);
        String paymentAccountCurrency = data2.equals("01") ? "Soles": "Dolares" ;

        double amount = stringToMoney(base.substring(67, 80)); //String amount = base.substring(67, 79);

        String data3 = base.substring(80, 82); //base.substring(80, 82);
        String intangibleAmountCurrency =   data3.equals("01") ? "Soles": "Dolares" ;  

        double intangibleAmount = stringToMoney(base.substring(82, 95)); //String intangibleAmount = base.substring(84, 96);

        String expirationDate = base.substring(95, 103); //String expirationDate = base.substring(99, 106);
        String paymentStatus = base.substring(103, 107); //String paymentStatus = base.substring(107, 110);
        String reasonObservation = base.substring(107, 177); //String reasonObservation = base.substring(111, 180);
        String commercialDocumentNumber = base.substring(177, 197); //


        System.out.println("resultado::::::::");
        System.out.println(beneficiaryId);
        System.out.println(beneficiaryName);
        System.out.println(bonusType );
        System.out.println(office);
        System.out.println(paymentAccountNumber );
        System.out.println(paymentAccountCurrency);
        System.out.println(amount );
        System.out.println(intangibleAmountCurrency);
        System.out.println(intangibleAmount);
        System.out.println(expirationDate);
        System.out.println(paymentStatus);
        System.out.println(reasonObservation);
        System.out.println(commercialDocumentNumber);

        
        //String values = beneficiaryId + beneficiaryName + bonusType + office + paymentAccountNumber + paymentAccountCurrency + amount + intangibleAmountCurrency + intangibleAmount + expirationDate + paymentStatus + reasonObservation + commercialDocumentNumber; 
        String values = beneficiaryId + "_" + beneficiaryName + "_" + bonusType + "_" + office + "_" + paymentAccountNumber + "_" + paymentAccountCurrency + "_" + amount + "_" + intangibleAmountCurrency + "_" + intangibleAmount + "_" + expirationDate + "_" + paymentStatus + "_" + reasonObservation + "_" + commercialDocumentNumber  + "#" + amount + "#" + data2 ; 
        return values; 
    }

    //Generar tramas
    @PostMapping(value = "/readTxt")
    public ResponseEntity<Map<String, Object>> readTxt(@RequestBody Map<String, String> myJsonRequest){
        String origin = myJsonRequest.get("origin");
        Map<String, Object> salida = new HashMap<>();   
        //Arreglos de las tramas
        ArrayList<String> nuevasTramas = new ArrayList<String>();
        ArrayList<String> errores = new ArrayList<String>();
        try{
            //Leer txt de la carpeta de origen
            FileReader fr =  new FileReader(origin);
            String headerBase = "";

            List<String> list = Files.readAllLines(new File(origin).toPath(), Charset.defaultCharset() );


            //generar nombre del archivo
            String[] partsName = myJsonRequest.get("fileOrigin").split("_");      
            String newName = createName(partsName[2], partsName[0], partsName[1], partsName[3]);
            System.out.println("newName");
            System.out.println(newName);
      
            //generar tramas y calcular montos (dolares y soles)
            Double amountDollars = 0.00;
            Double amountSoles = 0.00;
            for(int x = 0; x < list.size(); x++){
                String currency = getCurrency(list.get(x));
                String[] partsBase = list.get(x).split(",");   
                String newPLot = createPlot(list.get(x));
                nuevasTramas.add(newPLot);

                //Calcular montos generales
                Double amount =  Double.parseDouble(partsBase[15]);
                if(currency.equals("01")){
                    amountSoles = amountSoles + amount;
                }else{
                    amountDollars = amountDollars + amount;
                }
            }

            //generar cabecera
            Double lines = Double.valueOf(list.size()) ;
            headerBase = list.get(0);
            System.out.println("headerBase");
            System.out.println(headerBase);
            System.out.println(lines);
            System.out.println(amountSoles);
            System.out.println(amountDollars);
           
            String header = createHeader(headerBase, partsName[2], partsName[0], partsName[1], partsName[3], lines, amountSoles, amountDollars);
            nuevasTramas.add( 0, header);

            //crear archivos txt en la carpeta de destino 
            if(nuevasTramas.size() > 0){
                 //Date date = new Date();  
                 //SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
                 //String strDate = formatter.format(date);  
                 String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\"+newName+".txt";
                 File file = new File(ruta);
                 // Si el archivo no existe es creado
                 if (!file.exists()) {
                     file.createNewFile();
                 }
                 FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw);
                 for (int i = 0; i < nuevasTramas.size() ; i++) {
                     bw.write(nuevasTramas.get(i));
                     bw.newLine();
                 }             
                 bw.close();
            }


            // if(errores.size() > 0 ){
            //     Date date = new Date();  
            //     SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
            //     String strDate = formatter.format(date);  
            //     String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\Error-"+strDate+".txt";
            //     File file = new File(ruta);
            //     // Si el archivo no existe es creado
            //     if (!file.exists()) {
            //         file.createNewFile();
            //     }
            //     FileWriter fw = new FileWriter(file);
            //     BufferedWriter bw = new BufferedWriter(fw);
            //     for (int i = 0; i < errores.size() ; i++) {
            //         bw.write(errores.get(i));
            //         bw.newLine();
            //     }             
            //     bw.close();
            // }


            System.out.println("=====================");
            System.out.println(nuevasTramas);
            System.out.println("=====================");
            System.out.println(errores);
            salida.put("cant_lineas_recorridas", nuevasTramas.size() + errores.size());
            salida.put("cant_tramas_generadas", nuevasTramas.size());
            salida.put("cant_errores", errores.size());

        }catch(Exception ex){
            System.out.println("error general 1");

        }
        return ResponseEntity.ok(salida);
        
    }    

    @PostMapping(value = "/sendfile")
    public ResponseEntity<Map<String, Object>> senddFile(){
       
        Map<String, Object> salida = new HashMap<>();   
        try{
                SFTPUtils sftp = new SFTPUtils();
                sftp.setHostName("192.168.0.110");
                sftp.setHostPort("22");
                sftp.setUserName("PruebaSSH");
                sftp.setPassWord("User1");
                sftp.setDestinationDir( "/C:/Users/pruebassh" );
                //sftp.uploadFileToFTP("C:/Users/cobesohi/Desktop/Origen/20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt" , 
                //"20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt", false);

                sftp.uploadFileToFTP("20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt", "C:/Users/cobesohi/Desktop/Origen/20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt", false);

                salida.put("result", "enviado correctamente");

        }catch(Exception ex){
            System.out.println("error general 1");
            salida.put("result", ex);
        }
        return ResponseEntity.ok(salida);
    }


    //192.168.0.110	
    //PruebaSSH	User1
    //  /C:/Users/pruebassh/Documents

    //Traducir rpta
    @PostMapping(value = "/generateOutput")
    public ResponseEntity<Map<String, Object>> generateOutput(@RequestBody Map<String, String> myJsonRequest){
        String origin = myJsonRequest.get("origin");
        Map<String, Object> salida = new HashMap<>();   
        //Contenido
        ArrayList<String> content = new ArrayList<String>();
        try{
            //Leer txt de la carpeta de origen
            String headerBase = "";

            String[] partsName = myJsonRequest.get("file").split("_");     

            List<String> list = Files.readAllLines(new File(origin).toPath(), Charset.defaultCharset() );

            //generar nombre del archivo
            String file = myJsonRequest.get("file");
            String newName = createNameOutput(file);
            System.out.println("newName output");
            System.out.println(newName);
      
            //leer tramas, divdirlas y traducirlas
            Double amountDollars = 0.00;
            Double amountSoles = 0.00;

            for(int x = 1; x < list.size(); x++){
                
                String value = createAns(list.get(x));
                String[] partsValue = value.split("#"); 
                System.out.println("new value");
                System.out.println(value);
                content.add(partsValue[0]);
                //Calcular montos generales
                
                Double amount =  Double.parseDouble(partsValue[1]);
                if(partsValue[2].equals("01")){
                    amountSoles = amountSoles + amount;
                }else{
                    amountDollars = amountDollars + amount;
                }
            }

            //generar cabecera
            Double lines = Double.valueOf(list.size()) ;
            headerBase = list.get(1);
            System.out.println("headerBase");
            System.out.println(headerBase);
            System.out.println(lines);
            System.out.println(amountSoles);
            System.out.println(amountDollars);


           
            //String header = createHeader(headerBase, partsName[3], partsName[2], partsName[1], partsName[4], lines, amountSoles, amountDollars);
            //content.add( 0, header);

            //crear archivos txt en la carpeta de destino 
            if(content.size() > 0){
                 //Date date = new Date();  
                 //SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
                 //String strDate = formatter.format(date);  
                 String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\"+newName +".txt";
                 File filetxt = new File(ruta);
                 // Si el archivo no existe es creado
                 if (!filetxt.exists()) {
                    filetxt.createNewFile();
                 }
                 FileWriter fw = new FileWriter(filetxt);
                 BufferedWriter bw = new BufferedWriter(fw);
                 for (int i = 0; i < content.size() ; i++) {
                     bw.write(content.get(i));
                     bw.newLine();
                 }             
                 bw.close();
            }

            //Crear archivo xlsx
            String headerXlsx = "Identificador del Beneficiario;Nombre del Beneficiario;Tipo de abono;Tienda;Nro de cuenta de abono;Moneda de la cuenta de abono;Monto del abono;Moneda de monto intangible;Monto Intangible;Fecha de vencimiento;Estado del pago;Motivo de la observación;Número de documento comercial";
            crearAPartirDeArrayList(content, headerXlsx, newName,  "C:\\Users\\cobesohi\\Desktop\\Destino\\");

            System.out.println("=====================");
            System.out.println(content);
            System.out.println("=====================");
            //System.out.println(errores);
            //salida.put("cant_lineas_recorridas", content.size() + errores.size());
            salida.put("cant_tramas_generadas", content.size());
            //salida.put("cant_errores", errores.size());
            

        }catch(Exception ex){
            System.out.println("primer error");
            salida.put("error", ex);
        }
        return ResponseEntity.ok(salida);
    }
}
