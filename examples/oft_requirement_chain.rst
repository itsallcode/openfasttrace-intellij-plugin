OFT Requirement Chain Example (RST)
===================================

This example is a manual test bed for OFT item declarations and references.

- Specification item declarations are valid both without backticks (``req~name~1``) and with backticks (``req~name~1``).
- ``Covers:`` references are also valid in both forms.

Feature
-------
feat~example_manual_trace_chain~1

Defines a small end-to-end traceability chain from feature to implementation.

Needs: req

Requirement (plain declaration)
-------------------------------
req~example_manual_trace_chain_plain~1

Represents the plain (non-backticked) declaration form.

Covers:
- feat~example_manual_trace_chain~1

Needs: dsn

Requirement (backticked declaration)
------------------------------------
`req~example_manual_trace_chain_backticked~1`

Represents the backticked declaration form.

Covers:
- `feat~example_manual_trace_chain~1`

Needs: dsn

Design (plain declaration)
--------------------------
dsn~example_manual_trace_chain_plain~1

Design item for the plain requirement branch.

Covers:
- req~example_manual_trace_chain_plain~1

Needs: impl

Design (backticked declaration)
-------------------------------
`dsn~example_manual_trace_chain_backticked~1`

Design item for the backticked requirement branch.

Covers:
- `req~example_manual_trace_chain_backticked~1`

Needs: impl

Implementation
--------------
impl~example_manual_trace_chain_plain~1

Implementation placeholder covered by ``examples/implementation_example.sh``.

Covers:
- dsn~example_manual_trace_chain_plain~1

- `dsn~example_manual_trace_chain_backticked~1`
