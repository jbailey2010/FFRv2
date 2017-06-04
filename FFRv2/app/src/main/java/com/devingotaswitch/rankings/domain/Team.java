package com.devingotaswitch.rankings.domain;

import java.util.Map;

public class Team {
    private String name;
    private String oLineRanks;
    private String draftClass;
    private Map<String, Integer> sos;
    private String bye;
    private String faClass;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getoLineRanks() {
        return oLineRanks;
    }

    public void setoLineRanks(String oLineRanks) {
        this.oLineRanks = oLineRanks;
    }

    public String getDraftClass() {
        return draftClass;
    }

    public void setDraftClass(String draftClass) {
        this.draftClass = draftClass;
    }

    public Map<String, Integer> getSos() {
        return sos;
    }

    public void setSos(Map<String, Integer> sos) {
        this.sos = sos;
    }

    public String getBye() {
        return bye;
    }

    public void setBye(String bye) {
        this.bye = bye;
    }

    public String getFaClass() {
        return faClass;
    }

    public void setFaClass(String faClass) {
        this.faClass = faClass;
    }
}
