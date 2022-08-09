package com.croh.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.croh.utils.SFTPUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import java.io.File;


@Service
public class UploadService {
	private String folder="cargas//";
	private final Logger logg = LoggerFactory.getLogger(UploadService.class);
	
	public String save(MultipartFile file) {
		if (!file.isEmpty()) {
			try {
				byte [] bytes= file.getBytes();
				Path path = Paths.get( folder+file.getOriginalFilename() );
				Files.write(path, bytes);				
				logg.info("Archivo guardado");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "Archivo guardado correctamente";
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

    //Clase interna para generar nombre del archivo
    public String  createName(String itemCode,  String companyCode, String serviceCode, String fileName){

        System.out.println("createName ch");
		System.out.println(companyCode);	
		
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

    public String  createPlot(String base, String servideCode){
        String plot = "02";
        try{        
            String[] partsBase = base.split(",");   
            //Código de Beneficiario
            String beneficiaryCode = partsBase[2];

            //Tipo de Documento de pago
            String typePaymentDoc = " ";
            String data1= stripAccents(partsBase[10]);       
            if(servideCode.equals("03") || servideCode.equals("05")){
                     
                if(data1.contains("credito")){
                    typePaymentDoc="C";
                }else if(data1.contains("debito")){
                    typePaymentDoc="D";
                }else{
                    typePaymentDoc="F";
                }
            }

            //Número de documento
            String nDoc = "                    ";
            if(servideCode.equals("03") || servideCode.equals("05")) {nDoc = partsBase[11];}

            //fecha de vencimiento
            String expirationDate = "        ";
            if(servideCode.equals("03") || servideCode.equals("05")) { expirationDate = partsBase[12];}

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
            String ctsFiller = ""; //hay logica para cts
            if(servideCode.equals("03") || servideCode.equals("05")) { ctsFiller = "                       ";}
            else if(servideCode.equals("04") ) { ctsFiller = "  000000000000000    ";}
            else{
                String dataCTS1= stripAccents(partsBase[16]);       
                String chargeAccountCurrencyCTS = dataCTS1.contains("sol")? "01" : "10"; 
                String amountCTS = formatAmount(partsBase[17], 15);
                ctsFiller = chargeAccountCurrencyCTS + amountCTS + " ";

            }

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

    public String download(){
        String ga="Hola";
        System.out.println("si llegó:::::::::::::::::::::::::");
        return ga;
    }
    //Generar tramas
    public String readTxt(MultipartFile fileO, String user, String password){

		System.out.println("readTxt:::::::::::::::::::::::::");
		System.out.println(fileO);

		String salida = "";
        //Arreglos de las tramas
        ArrayList<String> nuevasTramas = new ArrayList<String>();
        ArrayList<String> errores = new ArrayList<String>();
        try{
            //Leer txt de la carpeta de origen
            String headerBase = "";

            List<String> list = new ArrayList<String>();
			try (BufferedReader br
				= new BufferedReader(new InputStreamReader(fileO.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {			
					list.add(line);
				}
			}
			System.out.println("list:::::::::::::::::::::::::");
			System.out.println(list);

            //generar nombre del archivo
			String nametext = fileO.getOriginalFilename() + "t";
			System.out.println("originalName:::::::::::::::::::::::::");
			System.out.println(nametext);
			
			String[] divided = nametext.split("\\.");  
			System.out.println("divided:::::::::::::::::::::::::");
			System.out.println(divided[0]);  

            String[] partsName = divided[0].split("_");  

			System.out.println("partsName:::::::::::::::::::::::::");
			System.out.println(partsName.length);  
            System.out.println(partsName[0]);  
            System.out.println(partsName[1]);
            System.out.println(partsName[2]);
            System.out.println(partsName[3]);
			
            //String newName = createName(partsName[2], partsName[0], partsName[1], partsName[3]);
			String newName = createName(partsName[2], partsName[0], partsName[1], partsName[3]);
            String serviceCode =  partsName[1];
            System.out.println("newName");
            System.out.println(newName);
      
            //generar tramas y calcular montos (dolares y soles)
            Double amountDollars = 0.00;
            Double amountSoles = 0.00;
            for(int x = 0; x < list.size(); x++){
                String currency = getCurrency(list.get(x));
                String[] partsBase = list.get(x).split(",");   
                String newPLot = createPlot(list.get(x), serviceCode);
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
                 //String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\"+newName+".txt";
                 //File file = new File(ruta);
				 File file = File.createTempFile("temp", null); //newName,"txt");
                 System.out.println("soda:::::::::::::::::::::::::::::::::");
                 //System.out.println(newName);
				 
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

				 salida = senddFile(file, newName, user, password);
				 file.deleteOnExit();



            }
            System.out.println("=====================");
            System.out.println(nuevasTramas);
            System.out.println("=====================");
            System.out.println(errores);

        }catch(Exception ex){
            System.out.println("error general 1");
			System.out.println(ex.getMessage());
			salida = ex.getMessage();
        }
        return salida;
        
    }    

    public String senddFile(File file, String newfile, String user, String password){
       
        String salida = "";   
        try{
                SFTPUtils sftp = new SFTPUtils();
                sftp.setHostName("192.168.0.110");
                sftp.setHostPort("22");
                sftp.setUserName(user);
                sftp.setPassWord(password);
                //sftp.setDestinationDir( "/C:/Users/pruebassh" );
                sftp.setDestinationDir( "/C:/Users/" + user.toLowerCase() + "/IN" );
                //sftp.uploadFileToFTP("20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt", "C:/Users/cobesohi/Desktop/Origen/20190307151055_03_4222_01_L123456789012345678901234_20190307150301_OK.txt", false);

                System.out.println("sendfile:::::::::::");
                System.out.println(file.getName());
                System.out.println(file.getAbsolutePath());

				//sftp.uploadFileToFTP(newfile, file.getAbsolutePath(), false); 

                String messageUpload = sftp.uploadFileToFTP(newfile, file.getAbsolutePath(), false); 
                if(messageUpload.contains("fail")){
                    salida = "Autenticación fallida";
                }else{
                    salida = "Archivo subido correctamente";
                }

        }catch(Exception ex){
            System.out.println("error sendfile");
			System.out.println(ex.getMessage());
			salida = ex.getMessage();
        }
        return (salida);
    }

	
	
}
