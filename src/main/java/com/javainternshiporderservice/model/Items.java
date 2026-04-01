package com.javainternshiporderservice.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "items")
@Data
public class Items extends BaseAuditingEntity{


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;


}
