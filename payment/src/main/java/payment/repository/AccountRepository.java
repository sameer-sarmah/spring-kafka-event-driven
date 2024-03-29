package payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import payment.entities.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Account findByName(String name);
}
