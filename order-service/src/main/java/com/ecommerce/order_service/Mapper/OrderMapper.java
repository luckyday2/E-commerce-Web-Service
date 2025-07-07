package com.ecommerce.order_service.Mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ecommerce.order_service.DTO.OrderItemRequestDTO;
import com.ecommerce.order_service.DTO.OrderItemResponseDTO;
import com.ecommerce.order_service.DTO.OrderRequestDTO;
import com.ecommerce.order_service.DTO.OrderResponseDTO;
import com.ecommerce.order_service.Model.Order;
import com.ecommerce.order_service.Model.OrderItem;

public class OrderMapper {

        // Digunakan saat mengambil Order dari DB untuk dikembalikan ke client
        public static OrderResponseDTO toDTO(Order order) {
                List<OrderItemResponseDTO> itemDtos = order.getItems() != null
                                ? order.getItems().stream()
                                                .map(OrderMapper::mapToItemDTO)
                                                .collect(Collectors.toList())
                                : List.of();

                return new OrderResponseDTO(
                                order.getOrderId(),
                                order.getUserId(),
                                order.getOrderDate(),
                                order.getStatus(),
                                order.getTotalAmount(),
                                itemDtos);
        }

        // Digunakan hanya untuk update order (bukan saat create)
        public static Order toEntity(OrderRequestDTO dto) {
                List<OrderItem> items = dto.getItems() != null
                                ? dto.getItems().stream()
                                                .map(OrderMapper::mapToOrderItemForUpdate)
                                                .collect(Collectors.toList())
                                : List.of();

                double totalAmount = items.stream()
                                .mapToDouble(OrderItem::getSubtotal)
                                .sum();

                Order order = new Order();
                order.setOrderId(UUID.randomUUID().toString()); // Backend-generated ID
                order.setUserId(dto.getUserId());
                order.setOrderDate(dto.getOrderDate());
                order.setStatus(dto.getStatus());
                order.setItems(items);
                order.setTotalAmount(totalAmount);

                return order;
        }

        // Digunakan hanya saat update (karena create pakai data dari
        // ProductClientResponse)
        private static OrderItem mapToOrderItemForUpdate(OrderItemRequestDTO dto) {
                OrderItem item = new OrderItem();
                item.setOrderItemId(UUID.randomUUID().toString());
                item.setProductId(dto.getId());
                item.setProductName(dto.getProductName());
                item.setProductPrice(dto.getProductPrice());
                item.setQuantity(dto.getQuantity());
                item.setSubtotal(dto.getProductPrice() * dto.getQuantity());
                return item;
        }

        private static OrderItemResponseDTO mapToItemDTO(OrderItem item) {
                return new OrderItemResponseDTO(
                                item.getProductName(),
                                item.getProductPrice(),
                                item.getQuantity(),
                                item.getSubtotal());
        }
}
