package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateProfileResponse extends BaseModel {
    private String message;
    private ProfileResponse customer;
}
