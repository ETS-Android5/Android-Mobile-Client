package org.intelehealth.msfarogyabharat.models;

public class FollowUpModel {

    String uuid;
    String patientuuid;
    String openmrs_id;
    String first_name;
    String last_name;
    String date_of_birth;
    String phone_number;
    String visit_speciality;
    String followup_date;
    String sync;
    String visitStartDate;

    public FollowUpModel() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    String comment;

    public String getVisitStartDate() {
        return visitStartDate;
    }

    public void setVisitStartDate(String visitStartDate) {
        this.visitStartDate = visitStartDate;
    }

    public FollowUpModel(String uuid, String patientuuid, String openmrs_id,
                         String first_name, String last_name, String date_of_birth,
                         String phone_number, String visit_speciality,
                         String followup_date, String sync, String comments, String visitStartDate) {
        this.uuid = uuid;
        this.patientuuid = patientuuid;
        this.openmrs_id = openmrs_id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
        this.visit_speciality = visit_speciality;
        this.followup_date = followup_date;
        this.sync = sync;
        this.comment = comments;
        this.visitStartDate = visitStartDate;

    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOpenmrs_id() {
        return openmrs_id;
    }

    public void setOpenmrs_id(String openmrs_id) {
        this.openmrs_id = openmrs_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getVisit_speciality() {
        return visit_speciality;
    }

    public void setVisit_speciality(String visit_speciality) {
        this.visit_speciality = visit_speciality;
    }

    public String getFollowup_date() {
        return followup_date;
    }

    public void setFollowup_date(String followup_date) {
        this.followup_date = followup_date;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public int getSeverity() {
        int severity = 0;
        if (comment == null)
            return severity;
        else if (comment.contains("Severe"))
            severity = 4;
        else if (comment.contains("Moderate"))
            severity = 3;
        else if (comment.contains("Mild"))
            severity = 2;
        else if (comment.contains("Asymptomatic"))
            severity = 1;
        return severity;
    }
}
