package org.intelehealth.ekalarogya.models.statewise_location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Setup_DistrictModel {
    @SerializedName("sanchs")
    @Expose
    private List<Setup_SanchModel> sanchs;
    @SerializedName("name")
    @Expose
    private String name;

    public List<Setup_SanchModel> getSanchs() {
        return sanchs;
    }

    public void setSanchs(List<Setup_SanchModel> sanchs) {
        this.sanchs = sanchs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
