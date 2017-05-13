#!/bin/sh
USER=$(expr $(cat main-study-participants.txt | wc -l) + 1)
echo "USER: $USER"

read -e -p "NAME: " NAME
echo "$USER $NAME" >> main-study-participants.txt

java -cp "bin:lib/*" ntu.csie.keydial.Prompter \
  -Participant $USER \
  -PhraseCount 20 \
  -StudyPlan main-study-plan.tsv \
  -CharacterTrainingSet main-study-training-set.txt \
  -PhraseSet main-study-phrase-set.txt \
  -Record main-study-record.tsv
