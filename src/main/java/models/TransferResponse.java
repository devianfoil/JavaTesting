package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse extends BaseModel {
    private String message;
    private double amount;
    private int receiverAccountId;
    private int senderAccountId;


}
