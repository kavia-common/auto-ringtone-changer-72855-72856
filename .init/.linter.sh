#!/bin/bash
cd /home/kavia/workspace/code-generation/auto-ringtone-changer-72855-72856/android_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

