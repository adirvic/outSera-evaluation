package com.outsera.razzies.producer.application.port.in;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.producer.domain.Producer;

public interface FindProducersUseCase {

    /** @param name optional case-insensitive "contains" filter */
    PageResult<Producer> list(String name, PageQuery page);

    /** @throws com.outsera.razzies.shared.domain.ResourceNotFoundException if it does not exist */
    Producer get(long id);
}
