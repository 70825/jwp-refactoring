package kitchenpos.tablegroup.application;

import java.util.List;
import java.util.stream.Collectors;
import kitchenpos.order.dto.OrderTableInTableGroupDto;
import kitchenpos.order.dto.UpdateGroupOrderTableEvent;
import kitchenpos.order.dto.UpdateUngroupOrderTableEvent;
import kitchenpos.order.dto.ValidateAppendOrderTableInTableGroupEvent;
import kitchenpos.order.dto.ValidateOrderIsNotCompletionInOrderTableEvent;
import kitchenpos.order.dto.ValidateSameSizeOrderTableEvent;
import kitchenpos.tablegroup.domain.TableGroup;
import kitchenpos.tablegroup.dto.TableGroupCreateRequest;
import kitchenpos.tablegroup.dto.TableGroupResponse;
import kitchenpos.tablegroup.repository.TableGroupRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TableGroupService {

    private final TableGroupRepository tableGroupRepository;
    private final ApplicationEventPublisher publisher;

    public TableGroupService(
            final TableGroupRepository tableGroupRepository,
            final ApplicationEventPublisher publisher
    ) {
        this.tableGroupRepository = tableGroupRepository;
        this.publisher = publisher;
    }

    public TableGroupResponse create(final TableGroupCreateRequest request) {
        final List<Long> orderTableIds = request.getOrderTables().stream()
                .map(OrderTableInTableGroupDto::getId)
                .collect(Collectors.toList());

        publisher.publishEvent(new ValidateSameSizeOrderTableEvent(orderTableIds));

        final TableGroup tableGroup = TableGroup.create();
        tableGroupRepository.save(tableGroup);

        publisher.publishEvent(new ValidateAppendOrderTableInTableGroupEvent(orderTableIds));
        publisher.publishEvent(new UpdateGroupOrderTableEvent(tableGroup.getId(), orderTableIds));

        return TableGroupResponse.from(tableGroup);
    }

    public void ungroup(final Long tableGroupId) {
        publisher.publishEvent(new ValidateOrderIsNotCompletionInOrderTableEvent(tableGroupId));
        publisher.publishEvent(new UpdateUngroupOrderTableEvent(tableGroupId));
    }
}
