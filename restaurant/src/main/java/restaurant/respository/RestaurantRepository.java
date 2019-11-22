package restaurant.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import restaurant.entities.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
}
