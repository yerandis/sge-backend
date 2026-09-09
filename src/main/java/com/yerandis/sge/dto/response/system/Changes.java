package com.yerandis.sge.dto.response.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Changes {

    String field;
    String oldValue;
    String newValue;
}
