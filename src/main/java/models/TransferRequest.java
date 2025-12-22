package models;

import com.sun.net.httpserver.Request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest extends BaseModel {
    int senderAccountId;
    int receiverAccountId;
    double amount;
}
