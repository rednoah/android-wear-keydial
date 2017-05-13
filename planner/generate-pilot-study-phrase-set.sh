#!/bin/sh

# 5 Pilot Study Keyboards * 10 Phrases per Keyboard
PHRASE_COUNT="50"

groovy print-random-phrases.groovy 'MacKenzie Phrase Set.txt' | head -n $PHRASE_COUNT | tee '../prompter/pilot-study-phrase-set.txt'
