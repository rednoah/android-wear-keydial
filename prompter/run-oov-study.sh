#!/bin/sh
USER=$(expr $(cat oov-study-participants.txt | wc -l) + 1)
echo "USER: $USER"

read -e -p "NAME: " NAME
echo "$USER $NAME" >> oov-study-participants.txt

java -cp "bin:lib/*" ntu.csie.keydial.Prompter \
  -Participant $USER \
  -PhraseCount 5 \
  -StudyPlan main-study-plan.tsv \
  -CharacterTrainingSet oov-study-training-set.txt \
  -PhraseSet oov-study-phrase-set.txt \
  -Record oov-study-record.tsv
