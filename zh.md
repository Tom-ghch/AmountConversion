# FinancialAmountConverter 类说明

`FinancialAmountConverter` 类是一个用于将英文金额单词转换为数字金额的工具类。该类能够解析复杂的金额表达式，并将其转换为标准的两位小数格式的字符串表示。

## 主要功能

- **金额解析**：将英文金额单词转换为数字金额。
- **单位处理**：支持处理百万、千万、百万、千和百等单位。
- **小数处理**：支持处理小数部分，包括点和分。
- **异常处理**：对于无效的输入，抛出异常。

## 注意事项

1. 输入字符串必须符合特定的格式，否则会抛出 `AmountFormatException`。
2. 该类不支持处理负数和货币符号。
3. 该类假设输入字符串中的单位和数字是正确的，并且按正确的顺序排列。
4. 具体转换类型可见: https://www.iamwawa.cn/yingwendaxie.html
<img width="1442" alt="image" src="https://github.com/user-attachments/assets/78b11f24-cbf1-4f7b-a5bb-c5f00386e69c" />

