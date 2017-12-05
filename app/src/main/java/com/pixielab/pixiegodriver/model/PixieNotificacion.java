package com.pixielab.pixiegodriver.model;

import java.lang.ref.SoftReference;

/**
 * Created by raulb on 25/11/2017.
 */

public class PixieNotificacion {
    private String title;
    private String description;
    private String id;
    private String descount;

    public PixieNotificacion(String title, String description, String id, String descount) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.descount = descount;
    }

    public PixieNotificacion() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescount() {
        return descount;
    }

    public void setDescount(String descount) {
        this.descount = descount;
    }
}
