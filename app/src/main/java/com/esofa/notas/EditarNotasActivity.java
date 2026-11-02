package com.esofa.notas;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class EditarNotasActivity extends Activity {

    public static final String EXTRA_ALUMNO_ID = "alumno_id";

    private DatosAlumnos datos;
    private Alumno alumno;

    private EditText etProgramacion;
    private EditText etRedes;
    private EditText etBaseDatos;
    private EditText etDesarrollo;
    private EditText etIngles;
    private EditText etCarrera;
    private EditText etNatacion;
    private EditText etBarras;
    private EditText etAbdominales;
    private EditText etConducta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_notas);

        datos = DatosAlumnos.getInstance(this);
        int id = getIntent().getIntExtra(EXTRA_ALUMNO_ID, -1);
        alumno = datos.getAlumnoPorId(id);

        if (alumno == null) {
            Toast.makeText(this, "No se encontró al alumno.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView tvAlumno = findViewById(R.id.tvAlumnoEdicion);
        tvAlumno.setText(alumno.getNombreCompleto()
                + "\nCódigo " + alumno.getCodigo() + " · 9no curso · Informática");

        etProgramacion = findViewById(R.id.etProgramacion);
        etRedes = findViewById(R.id.etRedes);
        etBaseDatos = findViewById(R.id.etBaseDatos);
        etDesarrollo = findViewById(R.id.etDesarrollo);
        etIngles = findViewById(R.id.etIngles);
        etCarrera = findViewById(R.id.etCarrera);
        etNatacion = findViewById(R.id.etNatacion);
        etBarras = findViewById(R.id.etBarras);
        etAbdominales = findViewById(R.id.etAbdominales);
        etConducta = findViewById(R.id.etConducta);

        rellenarCampos();

        findViewById(R.id.btnGuardar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios();
            }
        });

        findViewById(R.id.btnCancelar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void rellenarCampos() {
        etProgramacion.setText(formato1(alumno.getNotaAcademica(0)));
        etRedes.setText(formato1(alumno.getNotaAcademica(1)));
        etBaseDatos.setText(formato1(alumno.getNotaAcademica(2)));
        etDesarrollo.setText(formato1(alumno.getNotaAcademica(3)));
        etIngles.setText(formato1(alumno.getNotaAcademica(4)));
        etCarrera.setText(Alumno.formatearTiempo(alumno.getTiempo1500()));
        etNatacion.setText(Alumno.formatearTiempo(alumno.getTiempoNatacion()));
        etBarras.setText(String.valueOf(alumno.getBarras()));
        etAbdominales.setText(String.valueOf(alumno.getAbdominales()));
        etConducta.setText(formato1(alumno.getConducta()));
    }

    private void guardarCambios() {
        try {
            double[] notas = {
                    leerNota(etProgramacion, "Programación"),
                    leerNota(etRedes, "Redes y Comunicaciones"),
                    leerNota(etBaseDatos, "Base de Datos"),
                    leerNota(etDesarrollo, "Desarrollo Web"),
                    leerNota(etIngles, "Inglés Técnico")
            };
            double carrera = leerTiempo(etCarrera, "Carrera 1500 m");
            double natacion = leerTiempo(etNatacion, "Natación 100 m");
            int barras = leerEntero(etBarras, "Barras", 0, 100);
            int abdominales = leerEntero(etAbdominales, "Abdominales", 0, 200);
            double conducta = leerNota(etConducta, "Conducta");

            alumno.setNotasAcademicas(notas);
            alumno.setTiempo1500(carrera);
            alumno.setTiempoNatacion(natacion);
            alumno.setBarras(barras);
            alumno.setAbdominales(abdominales);
            alumno.setConducta(conducta);

            datos.actualizarAlumno(alumno);

            Toast.makeText(this, "Notas guardadas. Antigüedad recalculada.",
                    Toast.LENGTH_LONG).show();
            finish();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private double leerNota(EditText campo, String nombre) {
        String texto = campo.getText().toString().trim().replace(',', '.');
        if (texto.isEmpty()) {
            throw new IllegalArgumentException(nombre + ": ingresa una nota.");
        }
        try {
            double valor = Double.parseDouble(texto);
            if (valor < 0 || valor > 20) {
                throw new IllegalArgumentException(nombre + ": la nota debe estar entre 0 y 20.");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + ": nota inválida.");
        }
    }

    private int leerEntero(EditText campo, String nombre, int minimo, int maximo) {
        String texto = campo.getText().toString().trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException(nombre + ": ingresa un valor.");
        }
        try {
            int valor = Integer.parseInt(texto);
            if (valor < minimo || valor > maximo) {
                throw new IllegalArgumentException(nombre + ": valor entre "
                        + minimo + " y " + maximo + ".");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + ": valor inválido.");
        }
    }

    private double leerTiempo(EditText campo, String nombre) {
        String texto = campo.getText().toString().trim().replace(',', '.');
        if (texto.isEmpty()) {
            throw new IllegalArgumentException(nombre + ": ingresa un tiempo.");
        }

        double segundos;
        try {
            if (texto.contains(":")) {
                String[] partes = texto.split(":");
                if (partes.length != 2) {
                    throw new IllegalArgumentException(nombre + ": usa el formato m:ss.");
                }
                int minutos = Integer.parseInt(partes[0].trim());
                double resto = Double.parseDouble(partes[1].trim());
                if (minutos < 0 || resto < 0 || resto >= 60) {
                    throw new IllegalArgumentException(nombre + ": usa el formato m:ss.");
                }
                segundos = minutos * 60 + resto;
            } else {
                segundos = Double.parseDouble(texto);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + ": tiempo inválido. Usa mm:ss.");
        }

        if (segundos < 30 || segundos > 1800) {
            throw new IllegalArgumentException(nombre + ": tiempo fuera de rango (0:30 a 30:00).");
        }
        return segundos;
    }

    private String formato1(double valor) {
        return String.format(Locale.US, "%.1f", valor);
    }
}
