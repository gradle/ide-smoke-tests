#!/usr/bin/env bash
for sample in $(ls | grep -e '^[0-9][0-9].*'); do cd $sample; ./gradlew wrapper --gradle-version nightly; cd -; done
