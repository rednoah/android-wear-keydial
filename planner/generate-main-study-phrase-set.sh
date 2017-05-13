#!/bin/sh

# 3 Main Study Keyboards * 20 Phrases per Keyboard
PHRASE_COUNT="60"

groovy print-random-phrases.groovy 'MacKenzie Phrase Set.txt' | head -n $PHRASE_COUNT | tee '../prompter/main-study-phrase-set.txt'
