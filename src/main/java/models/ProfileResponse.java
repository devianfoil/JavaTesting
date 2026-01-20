package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileResponse extends BaseModel {
    private int id;
    private String username;
    private String name;
    private String role;
}
