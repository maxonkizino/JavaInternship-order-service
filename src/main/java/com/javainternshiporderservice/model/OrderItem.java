package com.javainternshiporderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem extends BaseAuditingEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "quantity")
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

}
