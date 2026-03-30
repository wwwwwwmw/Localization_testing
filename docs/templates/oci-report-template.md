# OCI Report Template

## 1. Run Metadata
- Run ID: {{RUN_ID}}
- Date Time: {{RUN_DATETIME}}
- Branch: {{BRANCH}}
- Build Version: {{BUILD_VERSION}}
- Environment: {{ENV}}
- Triggered By: {{TRIGGER}}

## 2. Input Metrics
- localeTarget: {{localeTarget}}
- localeCovered: {{localeCovered}}
- featureTarget: {{featureTarget}}
- featureCovered: {{featureCovered}}
- methodTarget: {{methodTarget}}
- methodCovered: {{methodCovered}}
- riskWeightTotal: {{riskWeightTotal}}
- riskWeightCovered: {{riskWeightCovered}}
- boundaryTarget: {{boundaryTarget}}
- boundaryCovered: {{boundaryCovered}}
- autoTarget: {{autoTarget}}
- autoCovered: {{autoCovered}}

## 3. Component Scores
- L: {{L}}
- F: {{F}}
- M: {{M}}
- R: {{R}}
- B: {{B}}
- A: {{A}}

## 4. OCI Summary
- OCI: {{OCI_PERCENT}}%
- Rating: {{OCI_RATING}}

## 5. Quality Gate
- Gate Result: {{GATE_RESULT}}
- Blocking Findings:
  - {{BLOCKER_1}}
  - {{BLOCKER_2}}

## 6. Domain Breakdown
| Domain | Covered | Target | Coverage |
|---|---:|---:|---:|
| Translation | {{TR_COVERED}} | {{TR_TARGET}} | {{TR_PERCENT}}% |
| Date/Time | {{DT_COVERED}} | {{DT_TARGET}} | {{DT_PERCENT}}% |
| Currency | {{CUR_COVERED}} | {{CUR_TARGET}} | {{CUR_PERCENT}}% |
| Overflow | {{OVF_COVERED}} | {{OVF_TARGET}} | {{OVF_PERCENT}}% |
| RTL | {{RTL_COVERED}} | {{RTL_TARGET}} | {{RTL_PERCENT}}% |
| Encoding | {{ENC_COVERED}} | {{ENC_TARGET}} | {{ENC_PERCENT}}% |

## 7. Top Failed Tests
1. {{FAIL_TEST_1}} - {{FAIL_REASON_1}}
2. {{FAIL_TEST_2}} - {{FAIL_REASON_2}}
3. {{FAIL_TEST_3}} - {{FAIL_REASON_3}}

## 8. Action Plan
1. {{ACTION_1}}
2. {{ACTION_2}}
3. {{ACTION_3}}
