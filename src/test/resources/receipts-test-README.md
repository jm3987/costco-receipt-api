# Receipt integration test fixture

`receipts-test.csv` is the standard happy-path dataset shared by the Quarkus
resource tests. It is deliberately small enough to audit manually while still
covering receipt browsing, analytics, and item purchase history.

## Global expectations

| Measurement | Expected value |
|---|---:|
| CSV item rows | 13 |
| Distinct receipts | 8 |
| Earliest receipt | 2024-01-10 |
| Latest receipt | 2026-07-13 |
| Total spending | 166.56 |

The CSV is intentionally not chronological. Application code must sort results
instead of relying on source row order.

## Receipt scenarios

| Receipt | Date | Type | Final total | Purpose |
|---|---|---|---:|---|
| ORDER-100 | 2024-01-10 | warehouse | 8.24 | Earliest receipt and detailed field parsing |
| ORDER-110 | 2024-03-15 | gas_station | 40.00 | First gas purchase |
| ORDER-200 | 2025-01-05 | online | 15.00 | First online receipt and shipping |
| ORDER-210 | 2025-06-15 | warehouse | 16.00 | Repeated milk purchase and same-date ordering |
| ORDER-220 | 2025-06-15 | warehouse | 10.00 | Same-date secondary order |
| ORDER-300 | 2026-02-20 | online | 22.00 | Repeated milk and coffee purchases |
| ORDER-310 | 2026-04-01 | gas_station | 45.00 | Repeated gas purchase |
| ORDER-400 | 2026-07-13 | warehouse | 10.32 | Latest receipt and latest milk price |

Receipts sharing a date use ascending order number as their deterministic
secondary order. Therefore, ORDER-210 precedes ORDER-220.

## Receipt type counts

| Type | Receipt count |
|---|---:|
| warehouse | 4 |
| gas_station | 2 |
| online | 2 |

## Monthly spending expectations

| Month | Receipt count | Total spending |
|---|---:|---:|
| 2024-01 | 1 | 8.24 |
| 2024-03 | 1 | 40.00 |
| 2025-01 | 1 | 15.00 |
| 2025-06 | 2 | 26.00 |
| 2026-02 | 1 | 22.00 |
| 2026-04 | 1 | 45.00 |
| 2026-07 | 1 | 10.32 |

## Item scenarios

| SKU | Item | Receipt count | Purpose |
|---|---|---:|---|
| SKU-001 | Milk | 4 | Price statistics, descriptions, and purchase frequency |
| SKU-002 | Bread | 2 | Repeated item with savings on one purchase |
| SKU-003 | Rice | 1 | Nullable FSA value |
| SKU-004 | Coffee | 2 | Repeated online item |
| SKU-005 | Eggs | 1 | Single-purchase frequency case |
| SKU-006 | Bananas | 1 | Weighted/quantity item |
| GAS-001 | Gasoline | 2 | Repeated gas purchase |

### SKU-001 price expectations

| Receipt | Date | Description | Unit price |
|---|---|---|---:|
| ORDER-100 | 2024-01-10 | WHOLE MILK / 1 GALLON | 4.99 |
| ORDER-210 | 2025-06-15 | VITAMIN D MILK / GALLON | 5.49 |
| ORDER-300 | 2026-02-20 | WHOLE MILK / 1 GAL | 4.79 |
| ORDER-400 | 2026-07-13 | FRESH WHOLE MILK / GALLON | 5.99 |

Expected future statistics for SKU-001:

- Receipt count: 4
- First purchase: 2024-01-10
- Last purchase: 2026-07-13
- Lowest unit price: 4.79
- Highest unit price: 5.99
- Latest unit price: 5.99
- Arithmetic mean before rounding: 5.315
- Mean rounded to two decimal places with HALF_UP: 5.32

## Ordering expectations

Newest first:

1. ORDER-400
2. ORDER-310
3. ORDER-300
4. ORDER-210
5. ORDER-220
6. ORDER-200
7. ORDER-110
8. ORDER-100

Oldest first:

1. ORDER-100
2. ORDER-110
3. ORDER-200
4. ORDER-210
5. ORDER-220
6. ORDER-300
7. ORDER-310
8. ORDER-400

Malformed CSV and import-error scenarios should use separate, narrowly scoped
fixtures rather than adding invalid rows to this standard dataset.
