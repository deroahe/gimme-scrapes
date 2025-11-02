package com.deroahe.gimmescrapes.commons.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailJobMessage implements Serializable {

    private Long jobId;
    private String recipientEmail;
    private String emailType;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
}
