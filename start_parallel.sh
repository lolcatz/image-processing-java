#!/bin/bash

java \
  -Dinfile=/home/fs/kerola/rio_testdata/7976x4480.ppm \
  -Doutfile=data/7976x4480_parallel.ppm \
  -Doperations=AAABBB \
  -Dthreads=-1 \
  -jar dist/image_processing.jar

