package com.example.conexionhttp_luz.controladores;

import org.apache.commons.httpclient.NameValuePair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

public class ConexionHttpPostServer {

    public static String direccionDelServidor =
            "http://10.0.2.2:8080/crudphpjson/crud/operacion.php";

    public String conexionConElServidor(List<NameValuePair> parametros,
                                        String rutaDeLaAplicacionWeb) throws Exception {

        HttpURLConnection conexion = null;

        try {
            URL url = new URL(rutaDeLaAplicacionWeb);
            conexion = (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(10000);
            conexion.setDoOutput(true);
            conexion.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
            );

            StringBuilder datos = new StringBuilder();

            for (NameValuePair parametro : parametros) {
                if (datos.length() > 0) {
                    datos.append("&");
                }

                datos.append(
                        URLEncoder.encode(parametro.getName(), "UTF-8")
                );

                datos.append("=");

                datos.append(
                        URLEncoder.encode(parametro.getValue(), "UTF-8")
                );
            }

            byte[] datosEnviar = datos.toString().getBytes("UTF-8");

            OutputStream salida = conexion.getOutputStream();
            salida.write(datosEnviar);
            salida.flush();
            salida.close();

            int codigoRespuesta = conexion.getResponseCode();

            if (codigoRespuesta != HttpURLConnection.HTTP_OK) {
                throw new Exception(
                        "Servidor respondió código HTTP " + codigoRespuesta
                );
            }

            return procesarRespuestaDelServidor(conexion.getInputStream());

        } catch (Exception error) {
            throw new Exception(
                    "Error de conexión: " + error.getMessage()
            );
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private String procesarRespuestaDelServidor(InputStream datosEntrada)
            throws Exception {

        BufferedReader lector = new BufferedReader(
                new InputStreamReader(datosEntrada, "UTF-8")
        );

        StringBuilder respuesta = new StringBuilder();
        String linea;

        while ((linea = lector.readLine()) != null) {
            respuesta.append(linea);
        }

        lector.close();

        return respuesta.toString();
    }
}