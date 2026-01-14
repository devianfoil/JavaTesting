package Generators;

import models.DepositRequest;
import models.TransferRequest;
import models.UpdateProfileRequest;

public class RandomDataGenerator {
    private RandomDataGenerator() {}

    // ====== Update profile ======
    public static UpdateProfileRequest validProfileUpdate() {
        // name сгенерится по regex в модели
        return RandomModelGenerator.generate(UpdateProfileRequest.class);
    }
    public static DepositRequest deposit(int accountId, double amount) {
        return DepositRequest.builder()
                .accountId(accountId)
                .balance(amount)
                .build();
    }


    public static DepositRequest validDeposit(int accountId) {
        return deposit(accountId, RandomNumbers.money(0.01, 4999.99));
    }

    public static DepositRequest invalidDepositZero(int accountId) {
        return deposit(accountId, 0.00);
    }

    public static DepositRequest invalidDepositNegative(int accountId) {
        // В твоем коде был баг (минус отсутствовал), здесь исправлено:
        return deposit(accountId, -RandomNumbers.money(0.01, 500.00));
    }

    public static DepositRequest invalidDepositTooBig(int accountId) {
        return deposit(accountId, RandomNumbers.money(5000.01, 10000.00));
    }




    // ====== Transfer ======
    public static TransferRequest validTransfer(int senderId, int receiverId) {
        return TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(RandomNumbers.money(0.01, 4999.99))
                .build();
    }

    public static TransferRequest invalidTransferZero(int senderId, int receiverId) {
        return TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(0.00)
                .build();
    }

    public static TransferRequest invalidTransferNegative(int senderId, int receiverId) {
        return TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(-RandomNumbers.money(0.01, 500.00))
                .build();
    }

    public static TransferRequest invalidTransferTooBig(int senderId, int receiverId) {
        return TransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(RandomNumbers.money(5000.00, 10000.00))
                .build();
    }
}
