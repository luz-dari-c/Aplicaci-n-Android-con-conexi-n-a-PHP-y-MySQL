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

import org.apache.commons.httpclient.NameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaCrudUsuario extends AppCompatActivity {

    private EditText campoCodigo;
    private EditText campoPassword;
    private EditText campoNombre;
    private Button botonModificar;
    private Button botonCancelar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_crud_usuario);

        campoCodigo = findViewById(R.id.campoCodigo2);
        campoPassword = findViewById(R.id.campoPassword2);
        campoNombre = findViewById(R.id.campoNombre);
        botonModificar = findViewById(R.id.botonModificar);
        botonCancelar = findViewById(R.id.botonCancelar2);

        cargarDatosUsuario();

        botonCancelar.setOnClickListener(v -> finish());

        botonModificar.setOnClickListener(v -> {
            if (verificarDatos()) {
                guardar();
            }
        });
    }

    private void cargarDatosUsuario() {
        Usuario usuario = null;
        Intent intento = getIntent();

        if (intento != null && intento.getExtras() != null) {
            usuario = (Usuario) intento.getSerializableExtra("sesion");
        }

        if (usuario == null) {
            usuario = PantallaInicial.fulanito;
        }

        if (usuario != null) {
            campoCodigo.setText(usuario.getEmail());
            campoPassword.setText(usuario.getPassword());
            campoNombre.setText(usuario.getNombre());
            botonModificar.setText("MODIFICAR");
        } else {
            botonModificar.setText("GUARDAR");
        }
    }

    private boolean verificarDatos() {
        if (campoCodigo.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Debe ingresar el email", Toast.LENGTH_LONG).show();
            return false;
        }

        if (campoPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe ingresar la contraseña", Toast.LENGTH_LONG).show();
            return false;
        }

        if (campoNombre.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Debe ingresar el nombre", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void guardar() {
        Toast.makeText(this, "Guardando...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            String resultado = guardarUsuario();

            runOnUiThread(() -> {
                if ("OK".equalsIgnoreCase(resultado)) {
                    Toast.makeText(this, "Usuario guardado con éxito", Toast.LENGTH_LONG).show();

                    PantallaInicial.fulanito = null;

                    Intent intento = new Intent(PantallaCrudUsuario.this, PantallaInicial.class);
                    intento.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intento);
                    finish();
                } else {
                    Toast.makeText(this, "ERROR: " + resultado, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String guardarUsuario() {
        NameValuePair parametroAccion;

        if (PantallaInicial.fulanito == null) {
            parametroAccion = new NameValuePair("accion", "Agregar");
        } else {
            parametroAccion = new NameValuePair("accion", "editar");
        }

        NameValuePair parametroEmail =
                new NameValuePair("email", campoCodigo.getText().toString().trim());

        NameValuePair parametroClave =
                new NameValuePair("psw", campoPassword.getText().toString());

        NameValuePair parametroNombre =
                new NameValuePair("nombre", campoNombre.getText().toString().trim());

        List<NameValuePair> parametros = new ArrayList<>();
        parametros.add(parametroAccion);
        parametros.add(parametroEmail);
        parametros.add(parametroClave);
        parametros.add(parametroNombre);

        ConexionHttpPostServer conexionHttp = new ConexionHttpPostServer();

        try {
            String jsonRespuesta = conexionHttp.conexionConElServidor(
                    parametros,
                    ConexionHttpPostServer.direccionDelServidor
            );

            if (jsonRespuesta != null) {
                Gson json = new Gson();
                Mensaje mensaje = json.fromJson(jsonRespuesta, Mensaje.class);
                return mensaje.getMensaje();
            }

            return "ERROR";

        } catch (Exception error) {
            return error.getMessage();
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