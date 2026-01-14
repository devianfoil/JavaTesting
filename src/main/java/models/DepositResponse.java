package models;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class DepositResponse extends BaseModel {
    private int id;
    private double amount;
    private String type;
    private String timestamp;
    private int relatedAccountId;
}
