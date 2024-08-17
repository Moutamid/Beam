package com.moutamid.beam.models;

import java.util.ArrayList;

public class RequestModel {
    public String ID, userID, title, description, category;
    public long timestamp, deadline;
    public ArrayList<String> mandatory;
    public ArrayList<DocumentLinkModel> documents;

    public RequestModel() {
    }

    @Override
    public String toString() {
        return "RequestModel{" +
                "ID='" + ID + '\'' +
                ", userID='" + userID + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", timestamp=" + timestamp +
                ", deadline=" + deadline +
                ", mandatory=" + mandatory +
                ", documents=" + documents +
                '}';
    }
}
