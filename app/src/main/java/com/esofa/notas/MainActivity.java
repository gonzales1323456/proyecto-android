package com.esofa.notas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String ESTADO_INDICE = "indice_seleccionado";

    private DatosAlumnos datos;
    private List<Alumno> alumnos;
    private int indice = 0;

    private Spinner spinnerAlumnos;
    private TextView tvNombre;
    private TextView tvCodigo;
    private TextView tvAntiguedad;
    private TextView tvMovimiento;
    private TextView tvPromedioGeneral;
    private TextView tvAcademico;
    private TextView tvPsicofisico;
    private TextView tvMilitar;
    private TextView tvMensaje;
    private LinearLayout contenedorAcademico;
    private LinearLayout contenedorPsicofisico;
    private LinearLayout contenedorMilitar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datos = DatosAlumnos.getInstance(this);
        alumnos = new ArrayList<>(datos.getAlumnos());
        alumnos.sort(Comparator.comparing(Alumno::getNombreCompleto, String.CASE_INSENSITIVE_ORDER));

        if (savedInstanceState != null) {
            indice = savedInstanceState.getInt(ESTADO_INDICE, 0);
        }
        if (indice < 0 || indice >= alumnos.size()) {
            indice = 0;
        }

        spinnerAlumnos = findViewById(R.id.spinnerAlumnos);
        tvNombre = findViewById(R.id.tvNombre);
        tvCodigo = findViewById(R.id.tvCodigo);
        tvAntiguedad = findViewById(R.id.tvAntiguedad);
        tvMovimiento = findViewById(R.id.tvMovimiento);
        tvPromedioGeneral = findViewById(R.id.tvPromedioGeneral);
        tvAcademico = findViewById(R.id.tvAcademico);
        tvPsicofisico = findViewById(R.id.tvPsicofisico);
        tvMilitar = findViewById(R.id.tvMilitar);
        tvMensaje = findViewById(R.id.tvMensaje);
        contenedorAcademico = findViewById(R.id.contenedorAcademico);
        contenedorPsicofisico = findViewById(R.id.contenedorPsicofisico);
        contenedorMilitar = findViewById(R.id.contenedorMilitar);

        String[] etiquetas = new String[alumnos.size()];
        for (int i = 0; i < alumnos.size(); i++) {
            Alumno alumno = alumnos.get(i);
            etiquetas[i] = alumno.getCodigo() + " · " + alumno.getNombreCompleto();
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, etiquetas);
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlumnos.setAdapter(adaptador);

        spinnerAlumnos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indice = position;
                mostrarAlumno(alumnos.get(indice));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button btnEditar = findViewById(R.id.btnEditar);
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Alumno alumno = alumnos.get(indice);
                Intent intent = new Intent(MainActivity.this, EditarNotasActivity.class);
                intent.putExtra(EditarNotasActivity.EXTRA_ALUMNO_ID, alumno.getId());
                startActivity(intent);
            }
        });

        spinnerAlumnos.setSelection(indice, false);
        mostrarAlumno(alumnos.get(indice));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alumnos != null && !alumnos.isEmpty()) {
            mostrarAlumno(alumnos.get(indice));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_INDICE, indice);
    }

    private void mostrarAlumno(Alumno alumno) {
        tvNombre.setText(alumno.getNombreCompleto());
        tvCodigo.setText("Código " + alumno.getCodigo() + " · 9no curso · Informática");
        tvAntiguedad.setText("#" + alumno.getAntiguedad() + " de " + alumnos.size());

        int diferencia = alumno.getAntiguedadBase() - alumno.getAntiguedad();
        if (diferencia > 0) {
            tvMovimiento.setText("▲ Subió " + diferencia
                    + (diferencia == 1 ? " puesto" : " puestos"));
            tvMovimiento.setTextColor(getColor(R.color.green));
        } else if (diferencia < 0) {
            int bajados = -diferencia;
            tvMovimiento.setText("▼ Bajó " + bajados
                    + (bajados == 1 ? " puesto" : " puestos"));
            tvMovimiento.setTextColor(getColor(R.color.red));
        } else {
            tvMovimiento.setText("= Se mantiene");
            tvMovimiento.setTextColor(getColor(R.color.neutral));
        }

        tvMensaje.setText("Antigüedad inicial: #" + alumno.getAntiguedadBase()
                + " de " + alumnos.size()
                + ". El puesto se recalcula con el promedio general del curso.");

        tvPromedioGeneral.setText(formato2(alumno.getPromedioGeneral()));
        tvAcademico.setText(formato2(alumno.getPromedioAcademico()));
        tvPsicofisico.setText(formato2(alumno.getPromedioPsicofisico()));
        tvMilitar.setText(formato2(alumno.getPromedioMilitar()));

        contenedorAcademico.removeAllViews();
        for (int i = 0; i < Alumno.MATERIAS.length; i++) {
            agregarFila(contenedorAcademico, Alumno.MATERIAS[i],
                    formato1(alumno.getNotaAcademica(i)), false);
        }
        agregarSeparador(contenedorAcademico);
        agregarFila(contenedorAcademico, "Promedio académico",
                formato2(alumno.getPromedioAcademico()), true);

        contenedorPsicofisico.removeAllViews();
        agregarFila(contenedorPsicofisico, "Carrera 1500 m",
                Alumno.formatearTiempo(alumno.getTiempo1500())
                        + "  →  " + formato1(alumno.getPuntajeCarrera()) + " pts", false);
        agregarFila(contenedorPsicofisico, "Natación 100 m",
                Alumno.formatearTiempo(alumno.getTiempoNatacion())
                        + "  →  " + formato1(alumno.getPuntajeNatacion()) + " pts", false);
        agregarFila(contenedorPsicofisico, "Barras",
                alumno.getBarras() + " rep  →  " + formato1(alumno.getPuntajeBarras()) + " pts", false);
        agregarFila(contenedorPsicofisico, "Abdominales",
                alumno.getAbdominales() + " rep  →  " + formato1(alumno.getPuntajeAbdominales()) + " pts", false);
        agregarSeparador(contenedorPsicofisico);
        agregarFila(contenedorPsicofisico, "Promedio psicofísico",
                formato2(alumno.getPromedioPsicofisico()), true);

        contenedorMilitar.removeAllViews();
        agregarFila(contenedorMilitar, "Nota de conducta",
                formato1(alumno.getConducta()) + " / 20", false);
        agregarSeparador(contenedorMilitar);
        agregarFila(contenedorMilitar, "Promedio militar",
                formato2(alumno.getPromedioMilitar()), true);
    }

    private void agregarFila(LinearLayout contenedor, String etiqueta, String valor,
                             boolean destacada) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(0, dp(7), 0, dp(7));

        TextView tvEtiqueta = new TextView(this);
        tvEtiqueta.setText(etiqueta);
        tvEtiqueta.setTextSize(14);
        tvEtiqueta.setTextColor(getColor(R.color.text_secondary));
        tvEtiqueta.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextSize(14);
        tvValor.setTextColor(getColor(R.color.text_primary));
        tvValor.setGravity(Gravity.END);
        if (destacada) {
            tvValor.setTypeface(Typeface.DEFAULT_BOLD);
        }

        fila.addView(tvEtiqueta);
        fila.addView(tvValor, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        contenedor.addView(fila);
    }

    private void agregarSeparador(LinearLayout contenedor) {
        View separador = new View(this);
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        parametros.topMargin = dp(4);
        parametros.bottomMargin = dp(4);
        separador.setLayoutParams(parametros);
        separador.setBackgroundColor(getColor(R.color.stroke));
        contenedor.addView(separador);
    }

    private String formato1(double valor) {
        return String.format(Locale.getDefault(), "%.1f", valor);
    }

    private String formato2(double valor) {
        return String.format(Locale.getDefault(), "%.2f", valor);
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
}
