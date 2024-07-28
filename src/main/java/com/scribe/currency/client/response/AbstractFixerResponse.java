package com.scribe.currency.client.response;

import lombok.Data;

@Data
public abstract class AbstractFixerResponse {
    private boolean success;
    private ErrorResponse error;
}
