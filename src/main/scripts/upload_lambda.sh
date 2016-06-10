#!/bin/bash

aws lambda update-function-code \
--function-name alexaHeadlines \
--zip-file fileb://target/alexa-headlines-0.1-jar-with-dependencies.jar \
--profile adminuser \
