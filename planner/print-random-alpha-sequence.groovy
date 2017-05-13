#!/usr/bin/env groovy

def chars = ('A'..'Z') * 3

3.times{ Collections.shuffle(chars) }

chars.each{ println it }
