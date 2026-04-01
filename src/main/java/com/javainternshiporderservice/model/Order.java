package com.javainternshiporderservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order extends BaseAuditingEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "status")
    private String status;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

}
