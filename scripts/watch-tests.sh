#!/bin/sh

node out/tests.js;
while inotifywait -e close_write out/tests.js;
do
    node out/tests.js;
done
