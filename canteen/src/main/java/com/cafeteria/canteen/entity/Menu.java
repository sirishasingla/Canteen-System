package com.cafeteria.canteen.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "menu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Meals this menu item is served during. Empty set = always available (any time).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "menu_meal",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "meal_id")
    )
    private Set<Meal> meals = new HashSet<>();

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Double price;

    /**
     * Optional per-audience prices. When any is set, the item is audience-restricted:
     *   • STAFF employees see the item only when staffPrice is set (at staffPrice).
     *   • WORKER employees see it only when workerPrice is set (at workerPrice).
     *   • OUTSIDER / GUEST see it only when outsiderPrice is set (at outsiderPrice).
     * When all three are null, the item is universal at base price.
     */
    @Column(name = "staff_price")
    private Double staffPrice;

    @Column(name = "worker_price")
    private Double workerPrice;

    @Column(name = "outsider_price")
    private Double outsiderPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** Admin-controlled sort order (ascending). Nulls sort last. */
    @Column(name = "display_order")
    private Integer displayOrder;
}