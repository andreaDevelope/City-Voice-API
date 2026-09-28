# Districts

Stories are tied to a district. The district is the unit the neighborhood score counts distinct zones on, so it is a closed set.

## Why 354 units

The source is Rome's official toponymy as updated by the July 2026 reform: 332 *quartieri* and 22 *rioni*, 354 entries in total.

The alternative was the 155 *zone urbanistiche*, dropped because they use administrative names instead of the ones the districts are actually called by.

## How they are exposed

`GET /api/cityvoice/public/districts` is public, because the dropdown is needed before registration. It returns the districts grouped by *municipio*: the groups in roman-numeral order, the districts alphabetically inside each group.

The table is seeded manually, via SQL, in a single `INSERT`.