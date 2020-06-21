package com.service.sohan.nfcreadwritetest.Model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class StateResponse {

    @SerializedName("status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("Data")
    @Expose
    private List<Data> data = null;

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "StateResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public class Data {

        @SerializedName("state")
        @Expose
        private String state;
        @SerializedName("STATENAME")
        @Expose
        private String sTATENAME;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getSTATENAME() {
            return sTATENAME;
        }

        public void setSTATENAME(String sTATENAME) {
            this.sTATENAME = sTATENAME;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "state='" + state + '\'' +
                    ", sTATENAME='" + sTATENAME + '\'' +
                    '}';
        }
    }


}
