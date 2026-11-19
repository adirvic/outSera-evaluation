package com.outsera.razzies.studio.application.port.in;

import com.outsera.razzies.shared.application.PageQuery;
import com.outsera.razzies.shared.application.PageResult;
import com.outsera.razzies.studio.domain.Studio;

public interface FindStudiosUseCase {

    /** @param name optional case-insensitive "contains" filter */
    PageResult<Studio> list(String name, PageQuery page);

    /** @throws com.outsera.razzies.shared.domain.ResourceNotFoundException if it does not exist */
    Studio get(long id);
}
