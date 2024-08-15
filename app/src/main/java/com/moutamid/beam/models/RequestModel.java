package com.moutamid.beam.models;

import java.util.ArrayList;

public class RequestModel {
    public String ID, userID, title, description, category;
    public long timestamp, deadline;
    public ArrayList<String> mandatory;
    public ArrayList<DocumentLinkModel> documents;

    public RequestModel() {
    }

}
