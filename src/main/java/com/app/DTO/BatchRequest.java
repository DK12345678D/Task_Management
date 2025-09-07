package com.app.DTO;

import java.util.List;
import java.util.Map;

public class BatchRequest {
    public static class Item {
        public String id; // queue-item client id
        public String task_id; // may be client task uuid
        public String operation; // create/update/delete
        public Map<String, Object> data;
        public String created_at;
        public int retry_count;
    }
    private List<Item> items;
    private String client_timestamp;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public String getClient_timestamp() { return client_timestamp; }
    public void setClient_timestamp(String client_timestamp) { this.client_timestamp = client_timestamp; }
}
