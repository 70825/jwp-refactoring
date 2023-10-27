package kitchenpos.order.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.repository.MenuRepository;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.domain.OrderTable;
import kitchenpos.order.dto.OrderChangeStatusRequest;
import kitchenpos.order.dto.OrderCreateRequest;
import kitchenpos.order.dto.OrderLineItemInOrderDto;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.repository.OrderRepository;
import kitchenpos.order.repository.OrderTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;

    public OrderService(
            final MenuRepository menuRepository,
            final OrderRepository orderRepository,
            final OrderTableRepository orderTableRepository
    ) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
    }

    public OrderResponse create(final OrderCreateRequest request) {
        final List<OrderLineItemInOrderDto> orderLineItemInOrderDtos = request.getOrderLineItems();
        final List<OrderLineItem> orderLineItems = orderLineItemInOrderDtos.stream()
                .map(this::convertOrderLineItem)
                .collect(Collectors.toList());

        final OrderTable orderTable = findOrderTable(request.getOrderTableId());

        final Order order = new Order(orderTable, OrderStatus.COOKING, orderLineItems);
        orderRepository.save(order);

        return OrderResponse.from(order);
    }

    private OrderTable findOrderTable(final Long orderTableId) {
        return orderTableRepository.findById(orderTableId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 주문 테이블입니다."));
    }

    private OrderLineItem convertOrderLineItem(final OrderLineItemInOrderDto request) {
        final Menu menu = findMenu(request.getMenuId());
        return new OrderLineItem(menu, request.getQuantity());
    }

    private Menu findMenu(final Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 메뉴입니다."));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        final List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderResponse changeOrderStatus(final Long orderId, final OrderChangeStatusRequest request) {
        final Order findOrder = findOrder(orderId);
        findOrder.changeOrderStatus(OrderStatus.valueOf(request.getOrderStatus()));
        return OrderResponse.from(findOrder);
    }

    private Order findOrder(final Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 주문입니다."));
    }
}