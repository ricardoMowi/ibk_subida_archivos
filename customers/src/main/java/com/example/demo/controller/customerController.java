package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.customer;
import com.example.demo.repository.customerRepo;
import com.example.demo.repository.customerRepository;
import com.example.demo.service.customerService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/customer")

public class customerController {
    @Autowired
    private customerService service;

    @Autowired
    private customerRepository repo;

    @Autowired
    private customerRepo rep;   

    //CRUD
    @GetMapping(value = "/all")
    public Flux<customer> getAll() {
        log.info("lista todos");
        return service.getAll();
    }  

    @PostMapping(value = "/create")
    public Mono<String> createCustomer(@RequestBody customer new_client){       
        return service.createCustomer(new_client);
    }

    @PostMapping(value = "/save")
    public Mono<customer> saveCustomer(@RequestBody customer new_client){       
        return service.saveCustomer(new_client);
    }

    @GetMapping("getClient/{id}")
    @ResponseBody
    public Mono<customer> getCustomerData(@PathVariable("id") String id){      
    	return service.getById(id);   
    }


    //Clase interna para validar cliente 
    public HashMap<String, Object> validateCustomer(String ruc) {        
        HashMap<String, Object> map = new HashMap<>();
        
        Optional<customer> client_doc = rep.findByRUC(ruc);           
        if (client_doc.isPresent()) {
            //Armar hashmap - probar customerType
            map.put("message", "Cliente válido");
            map.put("IdCustomer", client_doc.get().getId());
        } else{
            map.put("message", "RUC no válido");
        }
        return map;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Normalizar
    public static String stripAccents(String s) 
    {   s = s.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    //Clase interna para generar txt
    public void createTxt(ArrayList<String> content){
    }

    //Clase interna para generar nombre del archivo
        //Código del rubro, proporcionado por Interbank (itemCode)
        //Código de Empresa. Proporcionado por Interbank (companyCode)
        //Código del servicio. Proporcionado por Interbank (serviceCode)
        //Código único que identifica al lote (generado por la Empresa) (fileName)
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

    public String createHeader(String base, String itemCode, String companyCode, String serviceCode,String fileName, String qLines, String qSoles, String qDollars ){
        String header = "01";
       
        String[] partsBase = base.split(",");   

        //cuenta de cargo    
        String loadAccount = (partsBase[6].toString()).replace("-", ""); 

        //Tipo de cuenta de cargo        
        String data1= partsBase[5].toString().toLowerCase();
        boolean isSaving = data1.indexOf("ahorro") !=-1? true: false;
        String chargeAccountType = isSaving == true? "002" : "001";

        //Moneda de la cuenta de cargo
        boolean isSoles = data1.indexOf("sol") !=-1? true: false;
        String chargeAccountCurrency = isSoles == true? "01" : "10";
        
        //Fecha y hora de creación del txt
        Date date = new Date();  
        SimpleDateFormat  formatter = new SimpleDateFormat("YYYYMMDD");  
        String strDate = formatter.format(date); 

        //tipo de proceso
        String proccessType =  (partsBase[8].toString().toLowerCase()).equals("en diferido") ? "1" :"0";

        //fecha de proceso
        SimpleDateFormat  formatter2 = new SimpleDateFormat("YYYYMMDDHHMMSS");  
        String proccessDate = proccessType.equals("0") ?  formatter2.format(date) : partsBase[9] ;

        //header = header + itemCode + companyCode + ";" + serviceCode + ";" + loadAccount + ";"  + chargeAccountType + ";" +  chargeAccountCurrency + ";"  + fileName + ";"  +  strDate + ";"  + proccessType + ";"  + proccessDate + ";"  + qLines + ";"  + qSoles + ";"  + qDollars + ";"  + "MC001";
        header = header + itemCode + companyCode + serviceCode +  loadAccount +  chargeAccountType +  chargeAccountCurrency + fileName +  strDate + proccessType + proccessDate + qLines + qSoles + qDollars + "MC001";

        System.out.println("header generado");
        System.out.println(header);
        return header;
    }

    public String  createPlot(String base){
        String plot = "02";
        try{
        System.out.println("entro a createPlot");
        System.out.println(base);
        
        String[] partsBase = base.split(",");   
        //System.out.println(partsBase[0]);
        //Código de Beneficiario
        String beneficiaryCode = partsBase[2];
        //Tipo de Documento de pago
        String data1= stripAccents(partsBase[10]); //partsBase[10].toString().toLowerCase();
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
        String chargeAccountCurrency = data1.contains("sol")? "01" : "10"; 
        //Monto
        String amount = partsBase[15];
        //Tipo de abono
        String typeOfPayment = "00";
        if(data1.contains("abono")){
            typeOfPayment = "09";
        }else if(data1.contains("cheque")){
            typeOfPayment = "11";
        }else if(data1.contains("interbancario")){
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
        System.out.println("llegó a nombre");
        String[] parts = partsBase[0].split(" ");
        System.out.println(parts[0]);
        System.out.println(parts[1]);
        //System.out.println(parts[2]);


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

        //
        }catch(Exception ex){
            plot= ex.getMessage();
        }
        return plot ;
    }

    @PostMapping(value = "/readTxt")
    //public String  readTxt(@RequestBody String origin){      
    //public ResponseEntity<Map<String, Object>> createProduct(@RequestBody String origin){
    public ResponseEntity<Map<String, Object>> readTxt(@RequestBody Map<String, String> myJsonRequest){
        System.out.println("readTxt:::::::::::::::::::::::");
        String origin = myJsonRequest.get("origin");
        Map<String, Object> salida = new HashMap<>();   
        try{
            //Leer txt de la carpeta de origen
            FileReader fr =  new FileReader(origin);
            BufferedReader br = new BufferedReader(fr);
            String cadena;
            String headerBase = "";

            //generar nombre
            String[] partsName = myJsonRequest.get("fileOrigin").split("_");      
            System.out.println("partsName:::::::::::::::::::::::");
            System.out.println(partsName[0]);
            System.out.println(partsName[1]);
            System.out.println(partsName[2]);
            System.out.println(partsName[3]);
            String newName = createName(partsName[2], partsName[0], partsName[1], partsName[3]);
            System.out.println("newName");
            System.out.println(newName);
      
            //generar cabecera
            for(int x = 0; x < 2; x++){
                //headerBase = br.readLine();
            }
            
            System.out.println("headerBase");
            System.out.println(headerBase);
            String header = "hola"; // createHeader(headerBase, partsName[2], partsName[0], partsName[1], partsName[3], "000001", "000000000001000", "000000000000000");
            System.out.println("header final");
            System.out.println(header);

            //Arreglos de las tramas
            //ArrayList<String> nuevasTramas = new ArrayList<String>();
            //ArrayList<String> errores = new ArrayList<String>();

            while((cadena = br.readLine()) != null){ 
                System.out.println("entro while");
                //Dividir cadena 
                String[] parts = cadena.split(","); 
                for (int i = 0; i < parts.length; i++) {
                     //System.out.println(parts[i]);
                }
     
                String newPLot = createPlot(cadena);
                System.out.println("trama generada");
                System.out.println(newPLot);
 
                // for (int i = 0; i < parts.length; i++) {
                //     System.out.println(parts[i]);
                // }



                // //Realizar validaciones
                // //Buscar customer
                // System.out.println("log1 ");
                // System.out.println(parts[0]);
                // HashMap<String, Object> data_client = validateCustomer(parts[0]);  
                // String message = (data_client.get("message")).toString();

                // if(message.equals("RUC no válido")){
                //     String codeError = "ERR-1"; 
                //     String RUCerror = parts[0];
                //     String tramaError = codeError+"%%"+RUCerror;
                //     errores.add(tramaError);

                // }else{

                //     //Si existe, crear nueva trama                    
                //     //
                //     String idCustomer = (data_client.get("IdCustomer")).toString();
                //     //
                //     String type = parts[2].equals("CAR") ? "TIPO01" : "TIPO02";
                //     //
                //     Date date = new Date();  
                //     SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyy");  
                //     String strDate = formatter.format(date);  
                //     //unir
                //     String nueva_trama = idCustomer + type + strDate; 
                //     //Agregar a nuevastramas
                //     nuevasTramas.add(nueva_trama);
                // }

            }

            // //crear archivos txt en la carpeta de destino 
            // if(nuevasTramas.size() > 0){
            //     Date date = new Date();  
            //     SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
            //     String strDate = formatter.format(date);  
            //     String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\"+strDate+".txt";
            //     File file = new File(ruta);
            //     // Si el archivo no existe es creado
            //     if (!file.exists()) {
            //         file.createNewFile();
            //     }
            //     FileWriter fw = new FileWriter(file);
            //     BufferedWriter bw = new BufferedWriter(fw);
            //     for (int i = 0; i < nuevasTramas.size() ; i++) {
            //         bw.write(nuevasTramas.get(i));
            //         bw.newLine();
            //     }             
            //     bw.close();

            // }
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


            // System.out.println("=====================");
            // System.out.println(nuevasTramas);
            // System.out.println("=====================");
            // System.out.println(errores);
            // salida.put("cant_lineas_recorridas", nuevasTramas.size() + errores.size());
            // salida.put("cant_tramas_generadas", nuevasTramas.size());
            // salida.put("cant_errores", errores.size());

        }catch(Exception ex){

        }
        return ResponseEntity.ok(salida);
        
    }



    @PostMapping(value = "/readTxt_1")
    //public String  readTxt(@RequestBody String origin){      
    //public ResponseEntity<Map<String, Object>> createProduct(@RequestBody String origin){
    public ResponseEntity<Map<String, Object>> readTxt_1(@RequestBody Map<String, String> myJsonRequest){
        
        String origin = myJsonRequest.get("origin");
        Map<String, Object> salida = new HashMap<>();   
        try{
            //Leer txt de la carpeta de origen
            FileReader fr =  new FileReader(origin);
            BufferedReader br = new BufferedReader(fr);
            String cadena;

            //Arreglos de las tramas
            ArrayList<String> nuevasTramas = new ArrayList<String>();
            ArrayList<String> errores = new ArrayList<String>();

    
            while((cadena = br.readLine()) != null){ 
                //Dividir cadena 
                String[] parts = cadena.split("%%");      

                //Realizar validaciones
                //Buscar customer
                System.out.println("log1 ");
                System.out.println(parts[0]);
                HashMap<String, Object> data_client = validateCustomer(parts[0]);  
                String message = (data_client.get("message")).toString();

                if(message.equals("RUC no válido")){
                    String codeError = "ERR-1"; 
                    String RUCerror = parts[0];
                    String tramaError = codeError+"%%"+RUCerror;
                    errores.add(tramaError);

                }else{

                    //Si existe, crear nueva trama                    
                    //
                    String idCustomer = (data_client.get("IdCustomer")).toString();
                    //
                    String type = parts[2].equals("CAR") ? "TIPO01" : "TIPO02";
                    //
                    Date date = new Date();  
                    SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyy");  
                    String strDate = formatter.format(date);  
                    //unir
                    String nueva_trama = idCustomer + type + strDate; 
                    //Agregar a nuevastramas
                    nuevasTramas.add(nueva_trama);
                }

            }

            //crear archivos txt en la carpeta de destino 
            if(nuevasTramas.size() > 0){
                Date date = new Date();  
                SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
                String strDate = formatter.format(date);  
                String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\"+strDate+".txt";
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
            if(errores.size() > 0 ){
                Date date = new Date();  
                SimpleDateFormat  formatter = new SimpleDateFormat("ddMMyyyyHHmmss");  
                String strDate = formatter.format(date);  
                String ruta = "C:\\Users\\cobesohi\\Desktop\\Destino\\Error-"+strDate+".txt";
                File file = new File(ruta);
                // Si el archivo no existe es creado
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                for (int i = 0; i < errores.size() ; i++) {
                    bw.write(errores.get(i));
                    bw.newLine();
                }             
                bw.close();
            }


            System.out.println("=====================");
            System.out.println(nuevasTramas);
            System.out.println("=====================");
            System.out.println(errores);
            salida.put("cant_lineas_recorridas", nuevasTramas.size() + errores.size());
            salida.put("cant_tramas_generadas", nuevasTramas.size());
            salida.put("cant_errores", errores.size());

        }catch(Exception ex){

        }
        return ResponseEntity.ok(salida);
        
    }






}
