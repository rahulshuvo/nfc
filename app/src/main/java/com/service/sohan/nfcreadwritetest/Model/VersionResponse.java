package com.service.sohan.nfcreadwritetest.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionResponse {
    @SerializedName("version")
    @Expose
    private List<Version> version = null;

    public List<Version> getVersion() {
        return version;
    }

    public void setVersion(List<Version> version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VersionResponse{" +
                "version=" + version +
                '}';
    }

    public class Version {

        @SerializedName("andVerCode")
        @Expose
        private String andVerCode;
        @SerializedName("andVerLabel")
        @Expose
        private String andVerLabel;
        @SerializedName("andMustUpdate")
        @Expose
        private Boolean andMustUpdate;
        @SerializedName("andVerNote")
        @Expose
        private String andVerNote;
        @SerializedName("andLink")
        @Expose
        private String andLink;
        @SerializedName("iOsVerCode")
        @Expose
        private String iOsVerCode;
        @SerializedName("iOsVerLabel")
        @Expose
        private String iOsVerLabel;
        @SerializedName("iOsMustUpdate")
        @Expose
        private Boolean iOsMustUpdate;
        @SerializedName("iOsLink")
        @Expose
        private String iOsLink;
        @SerializedName("iOsVerNote")
        @Expose
        private String iOsVerNote;

        public String getAndVerCode() {
            return andVerCode;
        }

        public void setAndVerCode(String andVerCode) {
            this.andVerCode = andVerCode;
        }

        public String getAndVerLabel() {
            return andVerLabel;
        }

        public void setAndVerLabel(String andVerLabel) {
            this.andVerLabel = andVerLabel;
        }

        public Boolean getAndMustUpdate() {
            return andMustUpdate;
        }

        public void setAndMustUpdate(Boolean andMustUpdate) {
            this.andMustUpdate = andMustUpdate;
        }

        public String getAndVerNote() {
            return andVerNote;
        }

        public void setAndVerNote(String andVerNote) {
            this.andVerNote = andVerNote;
        }

        public String getAndLink() {
            return andLink;
        }

        public void setAndLink(String andLink) {
            this.andLink = andLink;
        }

        public String getIOsVerCode() {
            return iOsVerCode;
        }

        public void setIOsVerCode(String iOsVerCode) {
            this.iOsVerCode = iOsVerCode;
        }

        public String getIOsVerLabel() {
            return iOsVerLabel;
        }

        public void setIOsVerLabel(String iOsVerLabel) {
            this.iOsVerLabel = iOsVerLabel;
        }

        public Boolean getIOsMustUpdate() {
            return iOsMustUpdate;
        }

        public void setIOsMustUpdate(Boolean iOsMustUpdate) {
            this.iOsMustUpdate = iOsMustUpdate;
        }

        public String getIOsLink() {
            return iOsLink;
        }

        public void setIOsLink(String iOsLink) {
            this.iOsLink = iOsLink;
        }

        public String getIOsVerNote() {
            return iOsVerNote;
        }

        public void setIOsVerNote(String iOsVerNote) {
            this.iOsVerNote = iOsVerNote;
        }

        @Override
        public String toString() {
            return "Version{" +
                    "andVerCode='" + andVerCode + '\'' +
                    ", andVerLabel='" + andVerLabel + '\'' +
                    ", andMustUpdate=" + andMustUpdate +
                    ", andVerNote='" + andVerNote + '\'' +
                    ", andLink='" + andLink + '\'' +
                    ", iOsVerCode='" + iOsVerCode + '\'' +
                    ", iOsVerLabel='" + iOsVerLabel + '\'' +
                    ", iOsMustUpdate=" + iOsMustUpdate +
                    ", iOsLink='" + iOsLink + '\'' +
                    ", iOsVerNote='" + iOsVerNote + '\'' +
                    '}';
        }
    }

}
