import com.bank.accounts.Account;
import com.bank.accounts.CardType;
import com.bank.accounts.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTransactionTests {

@Test
public void testActivateDeactivate() {
    CheckingAccount acc = new CheckingAccount("ACC-C-6", "C1", 100.0,
            new CardType(CardType.DebitCardType.Standard_Mastercard));

    assertTrue(acc.isActive());
    acc.deactivate();
    assertFalse(acc.isActive());
    acc.activate();
    assertTrue(acc.isActive());
}


@Test
public void testOverdraftCountIncrement() throws Exception {
    CheckingAccount acc = new CheckingAccount("ACC-C-7", "C1", 10.0,
            new CardType(CardType.DebitCardType.Standard_Mastercard));

    acc.withdraw(50.0, "OD");
    assertEquals(1, acc.getOverdraftCount());
}

@Test
public void testCardDailyLimit() {
    CardType card = new CardType(CardType.DebitCardType.Standard_Mastercard);
    assertTrue(card.dailyWithdrawLimit() > 0);
}


Account account;

@BeforeEach
    void setUp() {
        account = new CheckingAccount("ACC-C-100", "C100", 1000.0,
                new CardType(CardType.DebitCardType.Standard_Mastercard));
    }


    @Test
    void testCanWithdrawWithinDailyLimit() {
        assertTrue(account.canWithdrawAmount(50));
    }


    @Test
    void testCannotWithdrawExceedingDailyLimit() throws Exception {
        account.deposit(0, "Initial");
        account.getTransactionHistory().add(new Transaction("ACC-C-100", Transaction.TransactionType.Withdraw, account.getCard().dailyWithdrawLimit(), 0, 0, LocalDateTime.now(), "Test"));

        assertFalse(account.canWithdrawAmount(1));
    }


    @Test
    void testCanDepositWithinDailyLimit() {
        assertTrue(account.canDepositAmount(100, true));
    }


    @Test
    void testCannotDepositExceedingDailyLimit() throws Exception {
        double limit = account.getCard().dailyDepositLimit();
        account.deposit(limit, "Deposit to reach limit");
        assertFalse(account.canDepositAmount(1, true));
    }


    @Test
    void testCanTransferWithinDailyLimit() throws Exception {
        assertTrue(account.canTransferAmount(50, true));
    }

}





