#!/usr/bin/env groovy


// see MacKenzie Phrase Set at http://www.yorku.ca/mack/chi03b.html
def f = args[0] as File

def phrases = f.readLines()*.trim()

10.times{ Collections.shuffle(phrases) }

phrases.each{ println it }
