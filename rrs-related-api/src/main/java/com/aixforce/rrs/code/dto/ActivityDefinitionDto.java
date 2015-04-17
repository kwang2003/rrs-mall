package com.aixforce.rrs.code.dto;

import com.aixforce.rrs.code.model.ActivityDefinition;
import lombok.Data;

import java.util.List;

/**
 * @author wanggen on 14-7-5.
 * @Desc:
 */
@Data
public class ActivityDefinitionDto {

    private ActivityDefinition activityDefinition;

    private List<Long> itemIds;

    private Integer itemType;

    private List<String> codes;

}
