# FinancialAmountConverter Class Documentation

The `FinancialAmountConverter` class is a utility for converting English words representing amounts into numeric monetary values. This class can parse complex amount expressions and convert them into a standard two-decimal-place formatted string.

## Main Functions

- **Amount Parsing**: Converts English amount words into numeric amounts.
- **Unit Handling**: Supports units such as million, billion, thousand, and hundred.
- **Decimal Handling**: Handles decimal parts, including point and cents.
- **Exception Handling**: Throws an exception for invalid input.

## Notes

1. The input string must follow a specific format; otherwise, an `AmountFormatException` will be thrown.
2. This class does not support negative numbers and currency symbols.
3. The class assumes that the input string contains correct units and numbers in the correct order.
4. For specific conversion types, see: [English Words to Amount](https://www.iamwawa.cn/yingwendaxie.html)

![Example Image](https://github.com/user-attachments/assets/78b11f24-cbf1-4f7b-a5bb-c5f00386e69c)
