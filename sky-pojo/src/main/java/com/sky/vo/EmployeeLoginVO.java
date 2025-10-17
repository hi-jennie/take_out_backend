package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "employee login view object")
public class EmployeeLoginVO implements Serializable {

    @ApiModelProperty("employee id")
    private Long id;

    @ApiModelProperty("uername")
    private String userName;

    @ApiModelProperty("employee name")
    private String name;

    @ApiModelProperty("jwt token")
    private String token;

}
