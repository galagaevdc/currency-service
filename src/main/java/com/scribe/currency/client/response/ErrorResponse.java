package com.scribe.currency.client.response;

import lombok.Data;

@Data
public class ErrorResponse {
  private int code;
  private String type;
  private String info;
}
