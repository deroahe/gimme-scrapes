package com.deroahe.gimmescrapes.commons.dto;

import com.deroahe.gimmescrapes.commons.enums.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapeJobMessage implements Serializable {

    private Long jobId;
    private Long sourceId;
    private String sourceName;
    private TriggerType triggeredBy;
    private LocalDateTime timestamp;
}
