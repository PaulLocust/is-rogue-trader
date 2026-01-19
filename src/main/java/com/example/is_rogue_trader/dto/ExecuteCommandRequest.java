package com.example.is_rogue_trader.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCommandRequest {
    private String result;          // Результат выполнения
    private String notes;           // Примечания
    private Boolean success = true; // Успешно ли выполнено
}