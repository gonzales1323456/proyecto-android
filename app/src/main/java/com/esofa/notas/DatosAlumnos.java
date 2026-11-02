package com.esofa.notas;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DatosAlumnos {

    private static final String PREFERENCIAS = "esofa_notas";
    private static final String CLAVE_DATOS = "alumnos_v1";
    private static final int TOTAL_ALUMNOS = 164;

    private static final String[] NOMBRES = {
            "Juan", "José", "Carlos", "Luis", "Miguel", "Jorge", "Diego", "Andrés",
            "Fernando", "Ricardo", "Raúl", "Pedro", "Sergio", "Óscar", "Marco", "Javier",
            "Daniel", "Pablo", "Iván", "Hugo", "Eduardo", "Alonso", "Bruno", "César",
            "Rodrigo", "Alejandro", "Martín", "Gabriel", "Renzo", "Álvaro", "Emilio",
            "Fabián", "Gonzalo", "Héctor", "Ignacio", "Joaquín", "Kevin", "Leonardo",
            "Mauricio", "Nicolás"
    };

    private static final String[] APELLIDOS = {
            "Gonzales", "Pérez", "Rodríguez", "Fernández", "López", "García", "Martínez",
            "Sánchez", "Ramírez", "Torres", "Flores", "Vásquez", "Rojas", "Mendoza",
            "Chávez", "Castillo", "Reyes", "Gutiérrez", "Ortiz", "Silva", "Cruz", "Ramos",
            "Herrera", "Medina", "Aguilar", "Vargas", "Castro", "Romero", "Suárez",
            "Navarro", "Campos", "Ríos", "Paredes", "Salazar", "Espinoza", "Cabrera",
            "Valdivia", "Quispe", "Huamán", "Zapata"
    };

    private static DatosAlumnos instancia;

    private final SharedPreferences preferencias;
    private final List<Alumno> alumnos = new ArrayList<>();

    public static synchronized DatosAlumnos getInstance(Context contexto) {
        if (instancia == null) {
            instancia = new DatosAlumnos(contexto.getApplicationContext());
        }
        return instancia;
    }

    private DatosAlumnos(Context contexto) {
        preferencias = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        boolean cargado = cargar();
        if (!cargado) {
            generarAlumnos();
        }
        recalcularAntiguedades();

        boolean faltanBases = false;
        for (Alumno alumno : alumnos) {
            if (alumno.getAntiguedadBase() <= 0) {
                faltanBases = true;
                break;
            }
        }
        if (faltanBases) {
            for (Alumno alumno : alumnos) {
                alumno.setAntiguedadBase(alumno.getAntiguedad());
            }
        }

        if (!cargado || faltanBases) {
            guardar();
        }
    }

    public List<Alumno> getAlumnos() {
        return Collections.unmodifiableList(alumnos);
    }

    public Alumno getAlumnoPorId(int id) {
        for (Alumno alumno : alumnos) {
            if (alumno.getId() == id) {
                return alumno;
            }
        }
        return null;
    }

    public void actualizarAlumno(Alumno alumno) {
        recalcularAntiguedades();
        guardar();
    }

    public void recalcularAntiguedades() {
        List<Alumno> ordenados = new ArrayList<>(alumnos);
        Collections.sort(ordenados, new Comparator<Alumno>() {
            @Override
            public int compare(Alumno a, Alumno b) {
                int comparacion = Double.compare(b.getPromedioGeneral(), a.getPromedioGeneral());
                if (comparacion != 0) {
                    return comparacion;
                }
                comparacion = Double.compare(b.getPromedioAcademico(), a.getPromedioAcademico());
                if (comparacion != 0) {
                    return comparacion;
                }
                return a.getNombreCompleto().compareToIgnoreCase(b.getNombreCompleto());
            }
        });

        for (int i = 0; i < ordenados.size(); i++) {
            ordenados.get(i).setAntiguedad(i + 1);
        }
    }

    private void generarAlumnos() {
        alumnos.clear();
        for (int i = 0; i < TOTAL_ALUMNOS; i++) {
            Random random = new Random(202600L + i);

            String nombre = NOMBRES[i % NOMBRES.length];
            String primerApellido = APELLIDOS[(i / NOMBRES.length) % APELLIDOS.length];
            String segundoApellido = APELLIDOS[(i * 7 + 3) % APELLIDOS.length];

            double[] notas = new double[Alumno.MATERIAS.length];
            for (int j = 0; j < notas.length; j++) {
                notas[j] = (80 + random.nextInt(121)) / 10.0;
            }

            double tiempo1500 = 355 + random.nextInt(320);
            double tiempoNatacion = 55 + random.nextInt(125);
            int barras = random.nextInt(20);
            int abdominales = 10 + random.nextInt(41);
            double conducta = (120 + random.nextInt(81)) / 10.0;

            alumnos.add(new Alumno(i, nombre, primerApellido + " " + segundoApellido,
                    notas, tiempo1500, tiempoNatacion, barras, abdominales, conducta));
        }
    }

    private boolean cargar() {
        String json = preferencias.getString(CLAVE_DATOS, null);
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            JSONObject raiz = new JSONObject(json);
            JSONArray arreglo = raiz.optJSONArray("alumnos");
            if (arreglo == null || arreglo.length() == 0) {
                return false;
            }

            List<Alumno> cargados = new ArrayList<>();
            for (int i = 0; i < arreglo.length(); i++) {
                JSONObject objeto = arreglo.getJSONObject(i);
                JSONArray notasJson = objeto.getJSONArray("notas");
                double[] notas = new double[Alumno.MATERIAS.length];
                for (int j = 0; j < notas.length && j < notasJson.length(); j++) {
                    notas[j] = notasJson.getDouble(j);
                }
                Alumno alumno = new Alumno(
                        objeto.getInt("id"),
                        objeto.getString("nombres"),
                        objeto.getString("apellidos"),
                        notas,
                        objeto.getDouble("tiempo1500"),
                        objeto.getDouble("tiempoNatacion"),
                        objeto.getInt("barras"),
                        objeto.getInt("abdominales"),
                        objeto.getDouble("conducta"));
                alumno.setAntiguedadBase(objeto.optInt("antiguedadBase", 0));
                cargados.add(alumno);
            }

            alumnos.clear();
            alumnos.addAll(cargados);
            return true;
        } catch (JSONException e) {
            alumnos.clear();
            return false;
        }
    }

    private void guardar() {
        JSONArray arreglo = new JSONArray();
        for (Alumno alumno : alumnos) {
            JSONObject objeto = new JSONObject();
            try {
                objeto.put("id", alumno.getId());
                objeto.put("nombres", alumno.getNombres());
                objeto.put("apellidos", alumno.getApellidos());

                JSONArray notas = new JSONArray();
                for (int i = 0; i < Alumno.MATERIAS.length; i++) {
                    notas.put(alumno.getNotaAcademica(i));
                }
                objeto.put("notas", notas);
                objeto.put("tiempo1500", alumno.getTiempo1500());
                objeto.put("tiempoNatacion", alumno.getTiempoNatacion());
                objeto.put("barras", alumno.getBarras());
                objeto.put("abdominales", alumno.getAbdominales());
                objeto.put("conducta", alumno.getConducta());
                objeto.put("antiguedadBase", alumno.getAntiguedadBase());
                arreglo.put(objeto);
            } catch (JSONException e) {
                // Los valores usados siempre son válidos para JSON.
            }
        }

        JSONObject raiz = new JSONObject();
        try {
            raiz.put("alumnos", arreglo);
        } catch (JSONException e) {
            // No ocurre con una clave y un arreglo válidos.
        }
        preferencias.edit().putString(CLAVE_DATOS, raiz.toString()).apply();
    }
}
