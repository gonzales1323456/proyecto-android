package com.esofa.notas;

import java.util.Locale;

public class Alumno {

    public static final String[] MATERIAS = {
            "Programación",
            "Redes y Comunicaciones",
            "Base de Datos",
            "Desarrollo Web",
            "Inglés Técnico"
    };

    private final int id;
    private final String nombres;
    private final String apellidos;
    private final double[] notasAcademicas;
    private double tiempo1500;
    private double tiempoNatacion;
    private int barras;
    private int abdominales;
    private double conducta;
    private int antiguedad;
    private int antiguedadBase;

    public Alumno(int id, String nombres, String apellidos, double[] notasAcademicas,
                  double tiempo1500, double tiempoNatacion, int barras, int abdominales,
                  double conducta) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.notasAcademicas = new double[MATERIAS.length];
        if (notasAcademicas != null) {
            System.arraycopy(notasAcademicas, 0, this.notasAcademicas, 0,
                    Math.min(notasAcademicas.length, this.notasAcademicas.length));
        }
        this.tiempo1500 = tiempo1500;
        this.tiempoNatacion = tiempoNatacion;
        this.barras = barras;
        this.abdominales = abdominales;
        this.conducta = conducta;
    }

    public int getId() {
        return id;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombreCompleto() {
        return apellidos + ", " + nombres;
    }

    public String getCodigo() {
        return String.format(Locale.US, "%03d", id + 1);
    }

    public double getNotaAcademica(int indice) {
        return notasAcademicas[indice];
    }

    public void setNotasAcademicas(double[] notas) {
        for (int i = 0; i < notasAcademicas.length && i < notas.length; i++) {
            notasAcademicas[i] = notas[i];
        }
    }

    public double getPromedioAcademico() {
        double suma = 0;
        for (double nota : notasAcademicas) {
            suma += nota;
        }
        return suma / notasAcademicas.length;
    }

    public double getTiempo1500() {
        return tiempo1500;
    }

    public void setTiempo1500(double tiempo1500) {
        this.tiempo1500 = tiempo1500;
    }

    public double getTiempoNatacion() {
        return tiempoNatacion;
    }

    public void setTiempoNatacion(double tiempoNatacion) {
        this.tiempoNatacion = tiempoNatacion;
    }

    public int getBarras() {
        return barras;
    }

    public void setBarras(int barras) {
        this.barras = barras;
    }

    public int getAbdominales() {
        return abdominales;
    }

    public void setAbdominales(int abdominales) {
        this.abdominales = abdominales;
    }

    public double getConducta() {
        return conducta;
    }

    public void setConducta(double conducta) {
        this.conducta = conducta;
    }

    public int getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(int antiguedad) {
        this.antiguedad = antiguedad;
    }

    public int getAntiguedadBase() {
        return antiguedadBase;
    }

    public void setAntiguedadBase(int antiguedadBase) {
        this.antiguedadBase = antiguedadBase;
    }

    public double getPuntajeCarrera() {
        return puntajePorTiempo(tiempo1500, 360, 720);
    }

    public double getPuntajeNatacion() {
        return puntajePorTiempo(tiempoNatacion, 60, 180);
    }

    public double getPuntajeBarras() {
        return limitar(barras, 0, 20);
    }

    public double getPuntajeAbdominales() {
        return limitar(abdominales / 2.0, 0, 20);
    }

    public double getPromedioPsicofisico() {
        return (getPuntajeCarrera() + getPuntajeNatacion()
                + getPuntajeBarras() + getPuntajeAbdominales()) / 4.0;
    }

    public double getPromedioMilitar() {
        return conducta;
    }

    public double getPromedioGeneral() {
        return (getPromedioAcademico() + getPromedioPsicofisico() + getPromedioMilitar()) / 3.0;
    }

    public static String formatearTiempo(double segundos) {
        int total = (int) Math.round(segundos);
        return String.format(Locale.US, "%d:%02d", total / 60, total % 60);
    }

    private static double puntajePorTiempo(double tiempo, double tiempoIdeal, double tiempoMaximo) {
        if (tiempo <= tiempoIdeal) {
            return 20.0;
        }
        if (tiempo >= tiempoMaximo) {
            return 0.0;
        }
        return 20.0 * (tiempoMaximo - tiempo) / (tiempoMaximo - tiempoIdeal);
    }

    private static double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
