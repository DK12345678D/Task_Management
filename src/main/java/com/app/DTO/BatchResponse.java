package com.app.DTO;

import java.util.List;
import java.util.Map;

public class BatchResponse {
    public static class Processed {
        public String client_id;
        public String server_id;
        public String status;
        public Map<String, Object> resolved_data;
    }

    private List<Processed> processed_items;

    public List<Processed> getProcessed_items() { return processed_items; }
    public void setProcessed_items(List<Processed> processed_items) { this.processed_items = processed_items; }
}
