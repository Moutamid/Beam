package com.moutamid.beam.models;

import android.net.Uri;

public class DocumentModel {
    public Uri uri;
    public boolean isDOc;

    public DocumentModel() {
    }

    public DocumentModel(Uri uri, boolean isDOc) {
        this.uri = uri;
        this.isDOc = isDOc;
    }
}
