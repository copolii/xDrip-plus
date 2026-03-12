# Handling Non-Numeric CGM Glucose Status Values

## Problem

When a CGM sensor reads glucose above its reportable range (e.g., > 400 mg/dL) or below it (e.g., < 40 mg/dL), the companion app displays a **localized status word** ("HIGH", "LOW", "HOCH", "ÉLEVÉ", "ALTO", "BAS", etc.) instead of a numeric value in its notification.

If your collector extracts text from these notifications and attempts numeric parsing, non-numeric strings will fail, and the reading is **silently dropped**.

## Why String Matching Doesn't Work

The companion app localizes "HIGH" and "LOW" to the device locale. Maintaining a lookup table of every translation is fragile and incomplete. New locales or wording changes will break it.

## Solution: Detection by Elimination

After filtering out trend arrows, units, and special characters, a valid glucose notification is either:

- **Numeric** — a parseable glucose value
- **Non-numeric** — a short, digit-free string (the localized HIGH/LOW status)

No other short, purely non-numeric text appears in a glucose notification after filtering, so this heuristic is reliable without needing to know the language.

---

### Step 1: Detect Non-Numeric Glucose Status

```
function isNonNumericGlucoseStatus(filteredText):
    if filteredText is null or empty → return false
    if length > 10                  → return false
    if any character is a digit     → return false
    return true
```

**Criteria:**

| Check | Rationale |
|---|---|
| Non-empty | Reject blank input |
| Short (≤ 10 chars) | Reject longer notification text like "Calibrate now" |
| Contains zero digits | Distinguish from actual glucose numbers like "105" |

This catches "HIGH", "LOW", "HOCH", "BAS", "ÉLEVÉ", "ALTO", etc. without knowing the language.

### Step 2: Infer Direction From Last Reading

Since you can't tell HIGH from LOW by the string alone, use the most recent stored glucose value to infer direction:

| Last Reading | Inference | Action |
|---|---|---|
| ≥ high threshold (e.g., 300 mg/dL) | HIGH | Store a value just above the sensor maximum |
| ≤ low threshold (e.g., 80 mg/dL) | LOW | Store a value at or just above the sensor minimum |
| Mid-range | Ambiguous | Drop the reading (log a warning) |
| No history | Unknown | Drop the reading (log a warning) |

**Choose thresholds conservatively.** The last numeric reading should be clearly trending toward an extreme before you classify.

### Step 3: Apply in All Parsing Paths

If your collector has multiple extraction strategies (e.g., RemoteViews parsing and notification extras fallback), apply the non-numeric check in **every path** that attempts to parse a glucose value. The logic should be:

```
value = tryNumericParse(filteredText)
if value is valid:
    use value as normal
else if isNonNumericGlucoseStatus(filteredText):
    handle as HIGH/LOW using last-reading inference
else:
    skip (not a glucose notification)
```

---

## Edge Cases

| Scenario | Behavior |
|---|---|
| Numeric match found | Non-numeric detection is skipped entirely; no behavior change |
| Mid-range last reading | Non-numeric status is ignored with a warning log to avoid misclassification |
| No previous reading | Safely skipped; cannot infer direction |
| Long non-glucose text | Filtered out by the ≤ 10 character length check |
| Text with embedded digits (e.g., "G7") | Filtered out by the zero-digits check |

## Key Design Decisions

1. **Language-agnostic** — No translation tables to maintain. Works for any locale automatically.
2. **Conservative inference** — When direction is ambiguous, the reading is dropped rather than misclassified.
3. **Short-string heuristic** — The ≤ 10 character limit is generous enough for any known translation of "HIGH" or "LOW" but short enough to reject unrelated notification content.
4. **Stored values at the boundary** — Storing a value like `MAX + 1` or `MIN + 1` preserves the out-of-range signal for downstream consumers (graphs, alerts, trend calculations).
