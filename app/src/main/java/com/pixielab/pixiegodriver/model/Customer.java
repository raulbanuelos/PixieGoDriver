package com.pixielab.pixiegodriver.model;

/**
 * Created by raulb on 23/09/2017.
 */

public class Customer {
    private String nombre;
    private String apellidos;
    private String contrasena;
    private String direccion;
    private String email;
    private Boolean estatus;
    private String idUsuarioAplicacion;
    private int tipoAutenticacion;


    public Customer(){

    }

    public Customer(String nombre, String apellidos, String contrasena, String direccion, String email, Boolean estatus, String idUsuarioAplicacion, int tipoAutenticacion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.contrasena = contrasena;
        this.direccion = direccion;
        this.email = email;
        this.estatus = estatus;
        this.idUsuarioAplicacion = idUsuarioAplicacion;
        this.tipoAutenticacion = tipoAutenticacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEstatus() {
        return estatus;
    }

    public void setEstatus(Boolean estatus) {
        this.estatus = estatus;
    }

    public String getIdUsuarioAplicacion() {
        return idUsuarioAplicacion;
    }

    public void setIdUsuarioAplicacion(String idUsuarioAplicacion) {
        this.idUsuarioAplicacion = idUsuarioAplicacion;
    }

    public int getTipoAutenticacion() {
        return tipoAutenticacion;
    }

    public void setTipoAutenticacion(int tipoAutenticacion) {
        this.tipoAutenticacion = tipoAutenticacion;
    }
}
