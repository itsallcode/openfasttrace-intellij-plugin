#!/usr/bin/env bash

# Pretend implementation for the OpenFastTrace IntelliJ plugin live demo.
#
# [impl->dsn~tell-late-work-excuse~1]

excuses=(
  "My alarm clock had needed an unplanned maintenance window for battery replacement."
  "The bus driver took a scenic route."
  "My laptop insisted on one more update."
  "My cat ate the spec."
  "I met a GitHub unicorn."
)

printf '%s\n' "${excuses[$((RANDOM % ${#excuses[@]}))]}"
