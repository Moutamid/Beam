package com.moutamid.beam.models;

public class DocumentLinkModel {
    public String link, name;
    public boolean isDoc;

    public DocumentLinkModel() {
    }

    public DocumentLinkModel(String link, String name, boolean isDoc) {
        this.link = link;
        this.name = name;
        this.isDoc = isDoc;
    }
}
