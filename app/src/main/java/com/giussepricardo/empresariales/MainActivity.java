package com.giussepricardo.empresariales;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements TextInputEditText.OnEditorActionListener {
    TextInputEditText txtOrigen, txtDestino;
    TextView textViewDistancia;
    ProgressDialog progressDialog;

    /**
     * Todo Desarrollar una aplicación que realice lo siguiente:
     * <p>
     * 1.  Mostrar en pantalla dos cajas de texto donde se va a ingresar una lugar origen y un lugar destino Ejem: txt1 =  “Cl 26 # 59 – 15” y txt2 = “Centro comercial Titán Plaza”
     * <p>
     * 2. Mostrar en pantalla un botón que se llame “Calcular Distancia”
     * <p>
     * 3. Conectarse al API de google “distance-matrix” y enviarle por petición GET con el parámetro de la información contenida en las cajas de texto 1 y 2.
     * <p>
     * 4. Una vez el API responda con la información se debe mostrar en pantalla el resultado de la distancia entre los dos puntos suministrados.
     * <p>
     * Documentación necesaria:
     * https://developers.google.com/maps/documentation/distance-matrix/start?hl=es-419
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtOrigen = findViewById(R.id.txt_origen);
        txtDestino = findViewById(R.id.txt_destino);
        txtDestino.setOnEditorActionListener(this);
        textViewDistancia = findViewById(R.id.textView_resultado);
    }

    public void btn_calcular_pressed(View view) {
        if (isEmpty(txtOrigen)) {
            Toast.makeText(this, "El campo origen no puede estar vacío.", Toast.LENGTH_SHORT).show();
        } else if (isEmpty(txtDestino)) {
            Toast.makeText(this, "El campo destino no puede estar vacío", Toast.LENGTH_SHORT).show();
        } else {
            obtenerDistancia();
        }
    }

    void obtenerDistancia() {
        progressDialog = ProgressDialog.show(this, "Calculando distancia", "Por favor espere...", true, false);

        // Instanciamos el RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String rawStringOrigen = txtOrigen.getText().toString();
        String rawStringDestino = txtDestino.getText().toString();

        String encodeOrigen, encodeDestino;

        try {
            encodeOrigen = URLEncoder.encode(rawStringOrigen, "UTF-8");
            encodeDestino = URLEncoder.encode(rawStringDestino, "UTF-8");

            String url = String.format("https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=%s,DC&destinations=%s&key=AIzaSyC60yXlwuNrgppmEJMkqtrwLNJpRZag5QA",
                    encodeOrigen,
                    encodeDestino
            );

            // Hacemos la petición del JSON a la API Distance Matrix by Google
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i("JSONRESPONSE", "onResponse: " + response);
                            try {
                                // Obtenemos el JSONArray 'rows' del JSON
                                JSONArray jsonArrayRows = response.getJSONArray("rows");

                                //Obtenemos el primer objeto del JSONARRAY rows
                                JSONObject jsonObjectRows = jsonArrayRows.getJSONObject(0);

                                // Obtenemos JSONArray 'elements'
                                JSONArray jsonArrayElements = jsonObjectRows.getJSONArray("elements");

                                //Obtenemos el primer objeto del JSONArray elements
                                JSONObject jsonObjectElements = jsonArrayElements.getJSONObject(0);

                                // Validamos si ese objeto tiene el key distance
                                String texto_distancia = "";

                                if (!jsonObjectElements.getString("status").equals("OK")) {
                                    texto_distancia = jsonObjectElements.getString("status");
                                } else if (jsonObjectElements.has("distance")) {
                                    // Obtenemos el JSON distance
                                    JSONObject jsonObjectDistance = jsonObjectElements.getJSONObject("distance");

                                    //Mostramos el valor de la distancia
                                    texto_distancia = String.format("Distancia: %s.", jsonObjectDistance.getString("text"));

                                    if (jsonObjectElements.has("duration")) {
                                        //Obtenemos el JSON duration
                                        JSONObject jsonObjectDuration = jsonObjectElements.getJSONObject("duration");

                                        texto_distancia += (String.format("\nAproximadamente: %s", jsonObjectDuration.getString("text")));
                                    }
                                }
                                progressDialog.dismiss();
                                InputMethodManager inputManager =
                                        (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(
                                        MainActivity.this.getCurrentFocus().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                                textViewDistancia.setText(texto_distancia);

                            } catch (JSONException e) {
                                progressDialog.dismiss();
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Log.e("JSONRESPONSE", "onErrorResponse: ", error);
                            Toast.makeText(MainActivity.this, "Ha ocurrido un error al obtener distancia, por favor inténtalo nuevamente. 😕", Toast.LENGTH_SHORT).show();
                        }
                    });

            requestQueue.add(jsonObjectRequest);
        } catch (UnsupportedEncodingException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error al hacer encode a textos Origen o Destino", Toast.LENGTH_SHORT).show();
        }
    }

    boolean isEmpty(EditText editText) {
        CharSequence str = editText.getText().toString();
        return TextUtils.isEmpty(str);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            obtenerDistancia();
            return true;
        }
        return false;
    }
}
