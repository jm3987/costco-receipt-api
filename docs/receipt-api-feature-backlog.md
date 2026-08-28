# Receipt API Feature Backlog

> Working planning document. This backlog is expected to change as the data,
> API, and product goals become clearer. It is not a binding specification.

## Contents

1. [Project direction](#project-direction)
2. [Current architecture](#current-architecture)
3. [Roadmap](#roadmap)
4. [Feature 3: Browse and filter purchases](#feature-3-browse-and-filter-purchases)
5. [Feature 4: View item purchase statistics](#feature-4-view-item-purchase-statistics)
6. [Future feature outlines](#future-feature-outlines)
7. [Shared Definition of Done](#shared-definition-of-done)
8. [Lightweight workflow](#lightweight-workflow)
9. [Current board](#current-board)

## Project direction

The application begins with imported receipt data and grows toward three broad
goals:

1. Explore past purchases by receipt or individual item.
2. Analyze spending, prices, and purchase patterns.
3. Use those patterns to support budgets and grocery planning through a future
   user interface.

The application is organized first by business capability and then, where a
capability has enough classes, by technical role.

```text
dev.shelfspace.receipts/
├── purchase/
│   ├── model/
│   ├── repository/
│   ├── resource/
│   └── service/
└── analytics/
    ├── model/
    ├── resource/
    └── service/
```

Future capabilities may include `catalog`, `budget`, and `grocery`. Each should
contain only the role packages it actually needs.

## Current architecture

### Purchase

Owns factual historical data and the ways it can be retrieved:

- Receipts
- Receipt line items
- Receipt summaries
- Individual-item purchase history
- CSV-backed receipt access

### Analytics

Calculates information derived from purchase data:

- Monthly net spending
- Future date-range and purchase-type breakdowns
- Future price and purchase-frequency trends

### Repository ownership

`ReceiptRepository` remains under `purchase.repository` because it represents
access to purchase records. Other capabilities may use it, but purchase owns
its meaning and contract.

As the application grows, other capabilities may own separate repositories:

```text
catalog.repository.ProductRepository
budget.repository.BudgetRepository
grocery.repository.GroceryListRepository
```

## Roadmap

| Step | Feature | Status |
|---:|---|---|
| 1 | Move receipt and item-purchase code into `purchase` | Done |
| 2 | Make test packages mirror production packages | Done |
| 3 | Add receipt listing, filtering, ordering, and pagination | Ready |
| 4 | Add item statistics derived from purchase history | Backlog |
| 5 | Add date-range and purchase-type analytics | Backlog |
| 6 | Add CSV validation and import reporting | Backlog |
| 7 | Introduce database persistence | Backlog |
| 8 | Build the canonical product catalog | Backlog |
| 9 | Add category analytics and budgets | Backlog |
| 10 | Add grocery lists, suggestions, and eventually the UI | Backlog |

---

# Feature 3: Browse and filter purchases

## Feature statement

As a user, I want to browse my receipts in manageable pages and narrow them by
date and purchase type so that I can find a transaction without knowing its
order number.

## Feature-level decisions

- Endpoint: `GET /api/receipts`
- Existing detail endpoint remains: `GET /api/receipts/{orderNumber}`
- Results are newest first by default.
- Date boundaries are inclusive.
- Pagination uses zero-based page numbers.
- Filtering occurs before sorting and pagination.
- List results contain receipt summaries, not complete item lists.

## Out of scope

- Searching individual products
- Category filtering
- Monthly analytics
- Database persistence
- UI implementation

## PUR-3.1: List receipt summaries

### User story

As a user, I want to see a page of receipt summaries so that I can identify a
receipt before opening its full details.

### Proposed request

```http
GET /api/receipts?page=0&size=20
```

### Proposed response

```json
{
  "items": [
    {
      "orderNumber": "ORDER-300",
      "transactionDate": "2026-02-20",
      "itemCount": 2,
      "finalTotal": 16.24
    }
  ],
  "page": 0,
  "size": 20,
  "totalItems": 3,
  "totalPages": 1
}
```

### Acceptance criteria

- Requesting `/api/receipts` returns status `200`.
- Each result contains enough information to identify a receipt.
- A receipt appears once even when it contains several items.
- Results are ordered newest first.
- The default page is `0`.
- The default size is `20`.
- An empty repository returns an empty `items` list rather than `404`.
- The existing receipt-detail endpoint continues to work.

### Likely code changes

```text
purchase/model/ReceiptListItem.java
purchase/model/ReceiptPage.java
purchase/repository/ReceiptRepository.java
purchase/service/ReceiptService.java
purchase/resource/ReceiptResource.java
purchase/resource/ReceiptResourceTest.java
```

### Design question

Which fields from `ReceiptDetail` help a user select a receipt without exposing
the complete item list?

## PUR-3.2: Filter receipts by date range

### User story

As a user, I want to restrict receipts to a date range so that I can examine
purchases from a particular period.

### Proposed requests

```http
GET /api/receipts?from=2025-01-01
GET /api/receipts?to=2025-12-31
GET /api/receipts?from=2025-01-01&to=2025-12-31
```

### Acceptance criteria

- `from` and `to` are optional.
- `from` includes receipts purchased on that date.
- `to` includes receipts purchased on that date.
- Supplying only one boundary is valid.
- A range with no matches returns an empty page.
- A `from` date later than `to` returns `400`.
- An invalid date returns `400`.
- Date filtering occurs before pagination.
- Default ordering remains newest first.

### Likely code changes

```text
Extend ReceiptService
Extend ReceiptResource
Extend ReceiptResourceTest
Possibly add purchase/model/ReceiptFilter.java
```

### Business rule to document in code

Date boundaries are inclusive because users expect a receipt purchased on the
selected beginning or ending date to be included.

## PUR-3.3: Filter receipts by purchase type

### User story

As a user, I want to filter receipts by purchase type so that I can separately
review warehouse, gas, or online transactions.

### Proposed request

```http
GET /api/receipts?type=warehouse
```

### Acceptance criteria

- The filter is optional.
- Supported values reflect the actual values in the imported data.
- Matching is case-insensitive.
- Only receipts of the selected type are returned.
- An unsupported value returns `400` with a useful error response.
- The type filter can be combined with date filters.
- All filters are applied before pagination.

### Likely code changes

```text
Possibly add purchase/model/PurchaseType.java
Possibly extend ReceiptListItem
Extend ReceiptService
Extend ReceiptResource
Extend ReceiptResourceTest
```

### Open question

Inspect the CSV before creating an enum. The model should reflect the values
actually present in the source data.

## PUR-3.4: Select receipt ordering

### User story

As a user, I want to choose ascending or descending receipt order so that I can
review transactions in the sequence useful to me.

### Proposed requests

```http
GET /api/receipts?sort=date,desc
GET /api/receipts?sort=date,asc
```

### Acceptance criteria

- Default ordering is `date,desc`.
- `date,asc` returns oldest receipts first.
- `date,desc` returns newest receipts first.
- An unsupported sort field or direction returns `400`.
- Sorting occurs after filtering and before pagination.
- Results with the same date use a deterministic secondary order, such as order
  number.

### Likely code changes

```text
Extend ReceiptService
Extend ReceiptResource
Extend ReceiptResourceTest
```

### Business rule to document in code

The secondary order prevents receipts with the same date from moving
unpredictably between pages.

## PUR-3.5: Validate pagination

### User story

As a user, I want predictable pagination limits so that receipt browsing remains
stable and responsive.

### Acceptance criteria

- `page` cannot be negative.
- `size` must be at least `1`.
- `size` cannot exceed `100`.
- Invalid pagination returns `400`.
- A page beyond the available results returns an empty `items` list with correct
  metadata.
- `totalItems` reports the number of receipts after filtering.
- `totalPages` is calculated from the filtered count and page size.

### Likely code changes

```text
Extend ReceiptService
Extend ReceiptResource
Extend ReceiptResourceTest
Possibly add a common API error model later
```

---

# Feature 4: View item purchase statistics

## Feature statement

As a user, I want summary statistics for a product I have purchased so that I
can understand its price range, latest price, and purchase frequency.

## Temporary identity decision

Until the canonical catalog exists, use the receipt SKU or item number as the
product identifier.

```text
Current identity: receipt SKU
Future identity: catalog Product
```

A free-text query such as `milk` can match several products and is therefore not
precise enough for statistics.

## PUR-4.1: Retrieve purchase history for an exact SKU

### User story

As a user, I want to select one exact receipt item so that statistics are not
accidentally calculated across several similarly named products.

### Proposed request

```http
GET /api/items/123456/purchases
```

### Acceptance criteria

- The path identifies an exact SKU.
- Only records with that SKU are returned.
- Results are newest first.
- Different receipt descriptions do not prevent records with the same SKU from
  matching.
- An unknown SKU returns `404`.
- The existing free-text search remains available for discovering the SKU.

### Likely code changes

```text
Extend ItemService
Extend ItemResource
Extend ItemResourceTest
```

### API decision

- Free-text search with no matches returns `200` and an empty list.
- An exact SKU request for a missing item returns `404`.

The first is an empty search result; the second requests a specific resource
that does not exist.

## PUR-4.2: Calculate basic item statistics

### User story

As a user, I want to see price and purchase statistics for one SKU so that I can
understand what I usually pay for it.

### Proposed request

```http
GET /api/items/123456/statistics
```

### Proposed response

```json
{
  "sku": "123456",
  "displayName": "COCONUT MILK",
  "receiptCount": 4,
  "firstPurchaseDate": "2024-03-10",
  "lastPurchaseDate": "2026-07-13",
  "lowestPrice": 8.49,
  "highestPrice": 10.99,
  "averagePrice": 9.62,
  "latestPrice": 10.49
}
```

### Acceptance criteria

- Statistics are calculated for one exact SKU.
- `receiptCount` counts distinct receipts containing the SKU.
- First and last dates are correct even when source data is unsorted.
- Lowest and highest prices use the price field exposed by purchase history.
- Latest price comes from the most recent purchase.
- Average price uses `BigDecimal`.
- Average price is rounded to two decimal places using an explicitly selected
  rounding rule.
- An unknown SKU returns `404`.
- The calculation uses the established purchase data source rather than reading
  the CSV independently.
- Price statistics use positive purchase prices; return and refund behavior can
  be modeled separately later.

### Likely code changes

```text
purchase/model/ItemStatistics.java
purchase/service/ItemService.java
purchase/resource/ItemResource.java
purchase/resource/ItemResourceTest.java
```

### Business rules to document in code

- Why distinct receipts, rather than raw CSV rows, determine `receiptCount`.
- Which rounding rule is used for average currency.
- Why return or refund prices are excluded from purchase-price statistics.

## PUR-4.3: Calculate purchase frequency

### User story

As a user, I want to know how often I purchase an item so that I can eventually
use that information for budgeting and grocery-list suggestions.

### Possible response additions

```json
{
  "averageDaysBetweenPurchases": 42,
  "purchaseIntervalCount": 3
}
```

### Acceptance criteria

- Purchase events are sorted chronologically before calculating intervals.
- The calculation uses distinct receipt purchases rather than duplicate CSV
  rows.
- The number of intervals is one less than the number of purchase events.
- An item purchased only once has no meaningful average interval.
- An unavailable average is represented as `null`, not `0`.
- Tests use fixed dates so the results remain deterministic.

### Likely code changes

```text
Extend ItemStatistics
Extend ItemService
Extend ItemResourceTest
```

### Future connection

```text
Last purchased 40 days ago
Typical interval 35 days
→ possibly needed again
```

---

# Future feature outlines

These features remain intentionally less detailed. Their stories should be
written closer to implementation, after earlier work reveals more about the
data and desired behavior.

## Feature 5: Date-range and purchase-type analytics

Possible stories:

- Filter monthly spending by inclusive date range.
- Break down spending by warehouse, gas, and online purchase type.
- Compare two periods without changing the underlying purchase records.
- Return results in deterministic chart-friendly order.

## Feature 6: CSV validation and import reporting

Possible stories:

- Detect missing required headers before import.
- Report malformed dates, currency values, and booleans with row numbers.
- Continue or stop according to a documented error policy.
- Produce a sanitized import summary without exposing private receipt data.

## Feature 7: Database persistence

Possible stories:

- Select and configure a database for development and testing.
- Import validated receipt data without creating duplicate receipts.
- Add a database-backed implementation of `ReceiptRepository`.
- Preserve API behavior while changing the storage implementation.
- Keep the CSV as an import source rather than the live query source.

## Feature 8: Canonical product catalog

Possible stories:

- Create a canonical product from one or more receipt SKUs.
- Associate alternate receipt descriptions with the same product.
- Assign and edit product categories.
- Preserve historical receipt text while adding normalized catalog data.

## Feature 9: Category analytics and budgets

Possible stories:

- Calculate actual spending by category and month.
- Create a monthly overall budget.
- Create optional category-specific limits.
- Compare actual spending with budgeted spending.
- Show remaining budget or amount exceeded.

## Feature 10: Grocery lists, suggestions, and UI

Possible stories:

- Create and rename a grocery list.
- Add, update, check, and remove grocery-list items.
- Suggest products based on purchase frequency and last purchase date.
- Display receipt browsing, item statistics, and analytics in a UI.
- Display budget progress and grocery-list status.

---

# Shared Definition of Done

A story moves to `Done` only when:

- Its acceptance criteria are met.
- The sanitized fixture covers the new behavior.
- Both successful and invalid inputs are tested where applicable.
- Existing tests still pass.
- Business-rule comments explain intent rather than Java syntax.
- No personal receipt data is committed.
- `./mvnw test` passes.
- `git diff --check` passes.
- The change is reviewed before committing.

# Lightweight workflow

Use four statuses:

```text
Backlog → Ready → In Progress → Done
```

Keep one story in progress. Before implementing it:

1. Read the user story and acceptance criteria.
2. Confirm the request and response contract.
3. Identify the model changes.
4. Determine whether the repository already provides the required data.
5. Put business rules in the service.
6. Keep HTTP concerns in the resource.
7. Add or update controlled test data.
8. Verify the public behavior with tests.

# Current board

## Ready

- PUR-3.1: List receipt summaries

## Backlog

- PUR-3.2: Filter receipts by date range
- PUR-3.3: Filter receipts by purchase type
- PUR-3.4: Select receipt ordering
- PUR-3.5: Validate pagination
- PUR-4.1: Retrieve purchase history for an exact SKU
- PUR-4.2: Calculate basic item statistics
- PUR-4.3: Calculate purchase frequency
- Features 5–10

## In Progress

- None

## Done

- CSV-backed receipt summary
- Receipt detail
- Item purchase search
- Monthly spending analytics
- Feature package organization
