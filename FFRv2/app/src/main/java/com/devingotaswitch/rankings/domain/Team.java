package com.devingotaswitch.rankings.domain;

import com.amazonaws.util.StringUtils;
import com.devingotaswitch.utils.Constants;

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
    private String incomingFA;
    private String outgoingFA;
    private String schedule;

    private static final String FA_DELIMITER = "~~~";

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

    public String getOutgoingFA() {
        return outgoingFA;
    }

    public void setOutgoingFA(String outgoingFA) {
        this.outgoingFA = outgoingFA;
    }

    public String getIncomingFA() {
        return incomingFA;
    }

    public void setIncomingFA(String incomingFA) {
        this.incomingFA = incomingFA;
    }

    public String getFaClass() {
        if (!StringUtils.isBlank(incomingFA) && !StringUtils.isBlank(outgoingFA)) {
            return new StringBuilder(incomingFA)
                    .append(FA_DELIMITER)
                    .append(outgoingFA)
                    .toString();
        }
        return "";
    }

    public void setFaClass(String faClass) {
        if (faClass != null) {
            String[] faArr = faClass.split(FA_DELIMITER);
            this.incomingFA = faArr[0];
            this.outgoingFA = faArr[1];
        }
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public int getSosForPosition(String position) {
        switch (position) {
            case Constants.QB:
                return getQbSos();
            case Constants.RB:
                return getRbSos();
            case Constants.WR:
                return getWrSos();
            case Constants.TE:
                return getTeSos();
            case Constants.DST:
                return getDstSos();
            case Constants.K:
                return getkSos();
        }
        return -1;
    }
}
