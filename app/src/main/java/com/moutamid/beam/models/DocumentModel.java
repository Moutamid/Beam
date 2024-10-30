package com.moutamid.beam.models;

import android.net.Uri;

public class DocumentModel {
    public String uri;
    public boolean isDoc;

    public DocumentModel() {
    }

    public DocumentModel(String uri, boolean isDOc) {
        this.uri = uri;
        this.isDoc = isDOc;
    }
}
