package com.license.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse <T> {
    private String status;
    private String message;
    private T payload;
    private int statusCode;
}
