package com.moutamid.beam.models;

import android.net.Uri;

public class DocumentModel {
    public Uri uri;
    public boolean isDoc;

    public DocumentModel(Uri uri, boolean isDOc) {
        this.uri = uri;
        this.isDoc = isDOc;
    }
}
