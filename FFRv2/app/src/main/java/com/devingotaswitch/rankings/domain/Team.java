package com.devingotaswitch.rankings.domain;

public class Team {

    private String name;
    private String oLineRanks;
    private String draftClass;
    private int qbSos;
    private int rbSos;
    private int wrSos;
    private int teSos;
    private int dstSos;
    private int kSos;
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

    public int getQbSos() {
        return qbSos;
    }

    public void setQbSos(int qbSos) {
        this.qbSos = qbSos;
    }

    public int getRbSos() {
        return rbSos;
    }

    public void setRbSos(int rbSos) {
        this.rbSos = rbSos;
    }

    public int getWrSos() {
        return wrSos;
    }

    public void setWrSos(int wrSos) {
        this.wrSos = wrSos;
    }

    public int getTeSos() {
        return teSos;
    }

    public void setTeSos(int teSos) {
        this.teSos = teSos;
    }

    public int getDstSos() {
        return dstSos;
    }

    public void setDstSos(int dstSos) {
        this.dstSos = dstSos;
    }

    public int getkSos() {
        return kSos;
    }

    public void setkSos(int kSos) {
        this.kSos = kSos;
    }

    public void setDraftClass(String draftClass) {
        this.draftClass = draftClass;
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