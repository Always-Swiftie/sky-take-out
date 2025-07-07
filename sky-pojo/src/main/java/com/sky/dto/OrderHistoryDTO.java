package com.sky.dto;


import lombok.Data;

@Data
public class OrderHistoryDTO {

    private int page;

    private int pageSize;

    private Integer status;

    private Long userId;
}
