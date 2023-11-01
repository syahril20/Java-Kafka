package org.acme.models;


public class KafkaModel {
    public int id;
    public String name;

    public KafkaModel(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public KafkaModel() {

    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
