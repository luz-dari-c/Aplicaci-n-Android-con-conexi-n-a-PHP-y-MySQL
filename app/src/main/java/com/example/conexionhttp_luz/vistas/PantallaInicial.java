package com.example.conexionhttp_luz.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conexionhttp_luz.R;
import com.example.conexionhttp_luz.controladores.ConexionHttpPostServer;
import com.example.conexionhttp_luz.datos.Usuario;
import com.google.gson.Gson;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.apache.commons.httpclient.NameValuePair;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaInicial extends AppCompatActivity {

    private EditText campoEmail;
    private EditText campoPassword;
    private Button btnLogin;
    private Button botonRegistrar;
    private ConexionHttpPostServer conexionServidor;
    public static Usuario fulanito;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_inicial);

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_LOCAL_NETWORK},
                    100
            );
        }
        campoEmail = findViewById(R.id.campoEmail);
        campoPassword = findViewById(R.id.campoClave);
        btnLogin = findViewById(R.id.btnIniciarSesion);
        botonRegistrar = findViewById(R.id.btnCancelar);
        conexionServidor = new ConexionHttpPostServer();

        botonRegistrar.setOnClickListener(v -> {
            fulanito = null;
            Intent intento = new Intent(PantallaInicial.this, PantallaCrudUsuario.class);
            startActivity(intento);
        });

        btnLogin.setOnClickListener(v -> {
            if (verificarDatos()) {
                String codigo = campoEmail.getText().toString().trim();
                String password = campoPassword.getText().toString();
                ejecutarLogin(codigo, password);
            }
        });
    }

    private boolean verificarDatos() {
        String codigo = campoEmail.getText().toString().trim();
        String password = campoPassword.getText().toString();

        if (codigo.isEmpty()) {
            Toast.makeText(this, "Debe ingresar el email", Toast.LENGTH_LONG).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Debe ingresar la contraseña", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void ejecutarLogin(String codigo, String password) {
        Toast.makeText(this, "Conectando...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            Object resp = iniciarSesion(codigo, password);

            runOnUiThread(() -> {
                if (resp instanceof Usuario) {
                    fulanito = (Usuario) resp;
                    mostrarDatosDeUsuarioEnOtraActividad();
                } else if (resp instanceof Mensaje) {
                    Toast.makeText(this, ((Mensaje) resp).getMensaje(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private Object iniciarSesion(String codigo, String password) {
        fulanito = null;

        ArrayList<NameValuePair> listaDeParametros = new ArrayList<>();
        listaDeParametros.add(new NameValuePair("email", codigo));
        listaDeParametros.add(new NameValuePair("psw", password));
        listaDeParametros.add(new NameValuePair("accion", "login"));

        try {
            String respuestaDelServidorEnJson = conexionServidor.conexionConElServidor(
                    listaDeParametros,
                    ConexionHttpPostServer.direccionDelServidor
            );

            if (respuestaDelServidorEnJson == null || respuestaDelServidorEnJson.isEmpty()) {
                return null;
            }

            Gson formatoJson = new Gson();
            Usuario usuario = formatoJson.fromJson(respuestaDelServidorEnJson, Usuario.class);

            if (usuario != null && usuario.getEmail() != null) {
                return usuario;
            }

            return formatoJson.fromJson(respuestaDelServidorEnJson, Mensaje.class);

        } catch (Exception error) {
            Mensaje mensaje = new Mensaje();
            mensaje.setMensaje(error.getMessage());
            return mensaje;
        }
    }

    private void mostrarDatosDeUsuarioEnOtraActividad() {
        if (fulanito != null) {
            Intent intento = new Intent(PantallaInicial.this, PantallaListado.class);
            intento.putExtra("sesion", fulanito);
            startActivity(intento);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public static class Mensaje {
        private String mensaje;

        public String getMensaje() {
            return mensaje;
        }

        public void setMensaje(String mensaje) {
            this.mensaje = mensaje;
        }
    }
}