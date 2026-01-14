package models;

import lombok.Data;

@Data
public class ProfileResponse extends BaseModel {
    private int id;
    private String username;
    private String name;
    private String role;
}
