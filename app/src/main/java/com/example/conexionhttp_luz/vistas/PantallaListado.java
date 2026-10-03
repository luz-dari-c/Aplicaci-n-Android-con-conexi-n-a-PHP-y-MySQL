package com.example.conexionhttp_luz.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conexionhttp_luz.R;
import com.example.conexionhttp_luz.controladores.ConexionHttpPostServer;
import com.example.conexionhttp_luz.datos.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.httpclient.NameValuePair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PantallaListado extends AppCompatActivity {

    private ConexionHttpPostServer conexionServidor;
    private ListView listaUsuariosView;
    private Button btnGuardar;
    private Button btnCerrarSesion;
    private List<Usuario> listaUsuarios;
    private ArrayAdapter<String> items;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pantalla_listado);

        listaUsuariosView = findViewById(R.id.listaUsuarios);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        items = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1
        );
        listaUsuariosView.setAdapter(items);
        conexionServidor = new ConexionHttpPostServer();

        btnGuardar.setOnClickListener(v -> {
            Intent intento = new Intent(PantallaListado.this, PantallaCrudUsuario.class);
            intento.putExtra("sesion", PantallaInicial.fulanito);
            startActivity(intento);
        });

        btnCerrarSesion.setOnClickListener(v -> {
            PantallaInicial.fulanito = null;

            Intent intento = new Intent(PantallaListado.this, PantallaInicial.class);

            intento.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intento);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarUsuarios();
    }

    private void cargarUsuarios() {
        Toast.makeText(this, "Cargando usuarios...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            boolean resultado = procesarRespuestaPeticion();

            runOnUiThread(() -> {
                if (resultado) {
                    mostrarUsuariosEnLista();
                } else {
                    Toast.makeText(this, "No fue posible obtener los usuarios", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean procesarRespuestaPeticion() {
        ArrayList<NameValuePair> listaParametros = new ArrayList<>();
        listaParametros.add(new NameValuePair("accion", "listar"));

        try {
            String resultadoDelServidor = conexionServidor.conexionConElServidor(
                    listaParametros,
                    ConexionHttpPostServer.direccionDelServidor
            );

            if (resultadoDelServidor == null || resultadoDelServidor.trim().isEmpty()) {
                return false;
            }

            if (resultadoDelServidor.trim().startsWith("{")) {
                return false;
            }

            Gson json = new Gson();

            Type lista = new TypeToken<List<Usuario>>() {
            }.getType();

            listaUsuarios = json.fromJson(resultadoDelServidor, lista);

            return listaUsuarios != null;

        } catch (Exception error) {
            error.printStackTrace();
            return false;
        }
    }

    private void mostrarUsuariosEnLista() {
        items.clear();

        if (listaUsuarios == null) {
            return;
        }

        for (Usuario alguien : listaUsuarios) {
            String elemento = alguien.getEmail() + " - " + alguien.getNombre();
            items.add(elemento);
        }

        items.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

}