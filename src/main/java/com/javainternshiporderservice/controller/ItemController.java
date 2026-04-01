package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;



@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<ItemResponse>> getItemsWithFilter(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal price,
            Pageable pageable) {
        Page<ItemResponse> items = itemService.getItemsWithFilter(active, name, price, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable UUID id) {
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<ItemResponse>> getAllItems(Pageable pageable) {
        Page<ItemResponse> items = itemService.getAllItems(pageable);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody CreateItemRequest createItemRequest) {
        ItemResponse createdItem = itemService.createItem(createItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ItemResponse> updateItem(
            @Valid @RequestBody UpdateItemRequest updateItemRequest) {
        ItemResponse updatedItem = itemService.updateItem(updateItemRequest);
        return ResponseEntity.ok(updatedItem);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> activateItem(@PathVariable UUID id) {
        itemService.activateItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateItem(@PathVariable UUID id) {
        itemService.deactivateItem(id);
        return ResponseEntity.noContent().build();
    }

}
