#!/bin/bash

java \
  -Dinfile=/home/fs/kerola/rio_testdata/7976x4480.ppm \
  -Doutfile=data/7976x4480_serial.ppm \
  -Dphases=3 \
  -Dthreads=1 \
  -jar dist/image_processing.jar

