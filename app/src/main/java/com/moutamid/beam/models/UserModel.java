package com.moutamid.beam.models;

import java.util.ArrayList;

public class UserModel {
    public String id, name, phoneNumber, image, category;
    public LocationModel location;
    public ArrayList<Float> rating;
    public boolean status, isAnonymous;
    public UserModel() {
    }
}
