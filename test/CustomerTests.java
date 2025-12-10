import com.bank.user.*;
import com.bank.accounts.*;
import org.junit.Test;
import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerTests {

    @Test
    public void testCreateCustomer() {
        Customer c = new Customer("C1", "Zahraa", "1234", User.Role.Customer);

        assertEquals("C1", c.getId());
        assertEquals("Zahraa", c.getName());
        assertEquals(User.Role.Customer, c.getRole());
        assertEquals(0, c.getFailedAttempts());
        assertEquals(0, c.getLockTime());
        assertTrue(c.isFirstLogin());
        assertTrue(c.getAccounts().isEmpty());
    }

    @Test
    public void testAddAccount() {
        Customer c = new Customer("C1", "Ali", "pass", User.Role.Customer);

        Account acc = new CheckingAccount("ACC-1", "C1", 0.0,
                new CardType(CardType.DebitCardType.Standard_Mastercard));

        c.addAccountObject(acc);

        assertEquals(1, c.getAccounts().size());
        assertEquals("ACC-1", c.getAccounts().get(0).getAccountId());
    }



    @Test
    public void testSaveCustomerCreatesFile() {
        Customer c = new Customer("C5","Test","444", Customer.Role.Customer);
        c.saveCustomer(c);


        File folder = new File("src/data/users");
        File[] files = folder.listFiles((dir, name) -> name.contains("C5"));
        assertNotNull(files);
        assertTrue(files.length > 0);
    }



    @Test
    public void testGetSavingAccount() {
        Customer c = new Customer("C3","Sara","222", Customer.Role.Customer);
        Account a = new SavingAccount("ACC-S-2", "C3", 100.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
        c.addAccountObject(a);


        assertNotNull(c.getSavingAccount());
        assertEquals("ACC-S-2", c.getSavingAccount().getAccountId());
    }

    @Test
    public void testGetCheckingAccount() {
        Customer c = new Customer("C3","Sara","222", Customer.Role.Customer);
        Account a = new CheckingAccount("ACC-C-2", "C3", 200.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
        c.addAccountObject(a);


        assertNotNull(c.getCheckingAccount());
        assertEquals("ACC-C-2", c.getCheckingAccount().getAccountId());
    }


    @Test
    public void testUpgradeCardType() {
        Customer c = new Customer("C7","Nora","666", Customer.Role.Customer);
        Account a = new SavingAccount("ACC-S-4","C7", 0.0, new CardType(CardType.DebitCardType.Standard_Mastercard));
        c.addAccountObject(a);


        a.getCard().setCardType(CardType.DebitCardType.Platinum_Mastercard);
        c.saveCustomer(c);

        Customer loaded = Customer.loadCustomer("C7");
        assertEquals(CardType.DebitCardType.Platinum_Mastercard, loaded.getSavingAccount().getCard().getCardType());
    }


    @Test
    public void testSafeNameRemovesSpaces() throws Exception {
        Customer c = new Customer("C13","Test User","123", Customer.Role.Customer);
        c.saveCustomer(c);


        File folder = new File("src/data/users");
        File[] files = folder.listFiles((dir,name)-> name.contains("Test_User"));
        assertNotNull(files);
        assertTrue(files.length > 0);
    }


}


