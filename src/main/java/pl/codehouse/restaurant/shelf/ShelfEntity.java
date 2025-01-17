package pl.codehouse.restaurant.shelf;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("shelf")
record ShelfEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE) int id,
        String menuItemName,
        int menuItemId,
        int quantity,
        long version,
        LocalDateTime updatedAt
) {
}
