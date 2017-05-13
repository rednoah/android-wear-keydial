#!/bin/sh
groovy print-random-phrases.groovy 'MacKenzie Phrase Set.txt' | head -n 10 | tee ../prompter/random-phrase-set.txt
