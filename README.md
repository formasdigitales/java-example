# java-example
Ejemplo de timbrado con java

# Requerimientos
* Java 1.8
* Netbeans 8+ (opcional)


En el proyecto encontraremos una clase que se llama Principal

```java

    private final String usuario = "";
    private final String pass = "";
    
    private final String tempXml = "com/resource/tmpxml.xml";
    private final String certPruebas = "com/resource/CSD_Pruebas_CFDI_LAN7008173R5.cer";
    private final String keyPruebas = "com/resource/CSD_Pruebas_CFDI_LAN7008173R5.key";
    private final String passKey = "12345678a";
    
    

    public static void main(String args[]) {
        Principal principal = new Principal();
        String cfdi = principal.generateXml();
        String cfdiTimbrado = principal.timbrar(cfdi);
        System.out.print(cfdiTimbrado);
    }
 ```

Las variables usuario y pass son para el servidor de pruebas, si tienes los tuyos podrias cambiarlos.

La variable tempXml es el archivo cfdi que no esta timbrado.

Las variables certPruebas, keyPruebas y passKey la ruta de nuestro certificado, key y el password de la key, que nos servira para generar nuestro sello digital, para posteriormente enviarlo a nuestro servicio web.


Para generar las clases de nuestro servicio web, utilizamos netbeans, abrimos el proyecto y nos vamos a la pestaña de servicios y ahi al menu de webservices damos click derecho y seleccionamos Agregar servicio web(Add webservice) nos saldra un cuadro de dialogo y escribimos la url del servicio web y el nombre del paquete escribimos com.wsdlcliente para que genere automaticamente las clases.

* Primero Generamos el cfdi que enviaremos al servicio web, esto se hace en el metodo generateXml. ahi generemos 
  * La cadena original
  * El numero de certificado
  * El certificado en base64. 
